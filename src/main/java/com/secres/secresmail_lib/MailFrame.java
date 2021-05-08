package com.secres.secresmail_lib;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;

import com.formdev.flatlaf.util.SystemInfo;
import com.secres.secresos.DockableFrame;
import com.secres.secresos.MainView;
import com.sun.mail.imap.IMAPFolder;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.FolderClosedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.event.ConnectionAdapter;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.MessageChangedEvent;
import jakarta.mail.event.MessageChangedListener;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.internet.MimeBodyPart;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class MailFrame extends DockableFrame {

    private static final long serialVersionUID = -3216943283450414687L;
    private JXTable mailTable;
    private WebView view;
    private JSplitPane splitPane;
    private JFXPanel contentPanel;
    private JList<Object> attachmentsList;
    private LoginSetup loginSetup;
    private Model model;
    private Properties properties;
    private boolean authenticated = false;

    public MailFrame(String title, JToolBar jtb) {
        super(title, jtb);
        loginSetup = new LoginSetup();
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        mailTable = new JXTable() {
            private static final long serialVersionUID = 7038819780398948914L;

            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                if(convertColumnIndexToModel(columnIndex) == 1) {
                    return;
                }
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };

        // mailTable.setShowGrid(true);
        try {
            mailTable.setAutoCreateRowSorter(true);
        } catch (Exception e) {
            /* Move on (i.e. ignore sorting if exception occurs) */ }
        mailTable.setCellSelectionEnabled(true);

        JScrollPane tableScrollPane = new JScrollPane(mailTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(tableScrollPane);

        contentPanel = createJavaFXPanel();

        mailTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = -2357302025054207092L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(noFocusBorder);
                return this;
            }
        });

        class ForcedListSelectionModel extends DefaultListSelectionModel {

            private static final long serialVersionUID = -8193032676014906509L;

            public ForcedListSelectionModel() {
                setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }

            @Override
            public void clearSelection() {
            }

            @Override
            public void removeSelectionInterval(int index0, int index1) {
            }

        }

        mailTable.setSelectionModel(new ForcedListSelectionModel()); // prevent multiple row selection

        mailTable.getColumnModel().setSelectionModel(new DefaultListSelectionModel() {
            private static final long serialVersionUID = 5039886252977060577L;

            @Override
            public boolean isSelectedIndex(int index) {
                return mailTable.convertColumnIndexToModel(index) != 1;
            }
        });

        mailTable.getSelectionModel().setValueIsAdjusting(true); // fire only one ListSelectionEvent

        mailTable.getSelectionModel().addListSelectionListener(e -> {
            if(e.getValueIsAdjusting()) {
                return;
            }
            new Thread(() -> {
                Message message = null;
                Object content = null;
                String email = null;
                try {
                    if(!model.getFolder().isOpen()) {
                        model.getFolder().open(Folder.READ_WRITE);
                    }
                    message = model.getMessages()[(model.getMessages().length - 1) - mailTable.convertRowIndexToModel(mailTable.getSelectedRow())];
                    content = message.getContent();
                    SwingUtilities.invokeLater(() -> {
                        mailTable.getModel().setValueAt(true, mailTable.convertRowIndexToModel(mailTable.getSelectedRow()), 1);
                        ListModel<Object> model = (ListModel<Object>) attachmentsList.getModel();
                        ((DefaultListModel<Object>) model).removeAllElements();
                    });
                    email = "";
                    if(content instanceof Multipart) {
                        Multipart mp = (Multipart) content;
                        email = getText(mp.getParent());
                    }
                    else {
                        email = message.getContent().toString();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                final String finalEmail = email;

                Platform.runLater(() -> {
                    view.getEngine().loadContent(finalEmail);
                });

                int attachmentCount = getAttachmentCount(message);
                if(attachmentCount > 0) {
                    try {
                        Multipart multipart = (Multipart) message.getContent();

                        for(int i = 0; i < multipart.getCount(); i++) {
                            // System.out.println("Entered " + i + " file.");

                            BodyPart bodyPart = multipart.getBodyPart(i);
                            if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                                continue; // dealing with attachments only
                            }
                            // do not do this in production code -- a malicious email can easily contain
                            // this filename: "../etc/passwd", or any other path: They can overwrite _ANY_
                            // file on the system that this code has write access to!
                            File f = new File(bodyPart.getFileName());

                            SwingUtilities.invokeLater(() -> {
                                ((DefaultListModel<Object>) attachmentsList.getModel()).addElement(f);
                            });
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

                // System.out.println(getAttachmentCount(message)); // prints number of
                // attachments
            }).start();
        });

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, contentPanel);
        mainPanel.add(splitPane);

        attachmentsList = new JList<Object>();
        attachmentsList.setModel(new DefaultListModel<Object>());
        attachmentsList.setSelectionModel(new DefaultListSelectionModel() {
            private static final long serialVersionUID = -1175323994925570200L;

            @Override
            public void setAnchorSelectionIndex(final int anchorIndex) {
            }

            @Override
            public void setLeadAnchorNotificationEnabled(final boolean flag) {
            }

            @Override
            public void setLeadSelectionIndex(final int leadIndex) {
            }

            @Override
            public void setSelectionInterval(final int index0, final int index1) {
            }
        });
        attachmentsList.setCellRenderer(new FileListCellRenderer());

        JScrollPane listScrollPane = new JScrollPane(attachmentsList);

        JPanel attachmentsPanel = new JPanel(new BorderLayout());
        attachmentsPanel.add(listScrollPane);
        attachmentsList.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), new CompoundBorder(new ListCellTitledBorder(attachmentsList, "Attachments"), attachmentsList.getBorder())));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentPanel, attachmentsPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(splitPane);

        this.splitPane.setBottomComponent(bottomPanel);
        setDividerLocation(this.splitPane, 0.5);
        this.splitPane.setResizeWeight(0.5);

        setDividerLocation(splitPane, 0.75);
        splitPane.setResizeWeight(0.75);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem sendMenuItem = new JMenuItem("Send...");
        sendMenuItem.addActionListener(e -> {
            new HTMLDocumentEditor(properties);
        });
        fileMenu.add(sendMenuItem);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
        add(mainPanel);

        pack();
    }

    private void setDividerLocation(final JSplitPane splitter, final double proportion) {
        if(splitter.isShowing()) {
            if((splitter.getWidth() > 0) && (splitter.getHeight() > 0)) {
                splitter.setDividerLocation(proportion);
            }
            else {
                splitter.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent ce) {
                        splitter.removeComponentListener(this);
                        setDividerLocation(splitter, proportion);
                    }
                });
            }
        }
        else {
            splitter.addHierarchyListener(new HierarchyListener() {

                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if(((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) && splitter.isShowing()) {
                        splitter.removeHierarchyListener(this);
                        setDividerLocation(splitter, proportion);
                    }
                }

            });
        }
    }

    private JFXPanel createJavaFXPanel() {
        JFXPanel contentPanel = new JFXPanel();

        Platform.runLater(() -> {
            view = new WebView();

            StackPane root = new StackPane();
            root.getChildren().add(view);
            if(SystemInfo.isMacOS) root.getStylesheets().add("/style_mac.css");
            else root.getStylesheets().add("/style_win.css");

            contentPanel.setScene(new Scene(root));
        });

        return contentPanel;
    }

    private int getAttachmentCount(Message message) {
        int count = 0;
        try {
            Object object = message.getContent();
            if(object instanceof Multipart) {
                Multipart parts = (Multipart) object;
                for(int i = 0; i < parts.getCount(); ++i) {
                    MimeBodyPart part = (MimeBodyPart) parts.getBodyPart(i);
                    if(Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) ++count;
                }
            }
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Return the primary text content of the message.
     * 
     * @param p the <code>Part</code>
     * @return String the primary text content
     */
    private String getText(Part p) throws MessagingException, IOException {
        if(p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            return s;
        }

        if(p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for(int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if(bp.isMimeType("text/plain")) {
                    if(text == null) text = getText(bp);
                    continue;
                }
                else if(bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if(s != null) return s;
                }
                else {
                    return getText(bp);
                }
            }
            return text;
        }
        else if(p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for(int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if(s != null) return s;
            }
        }

        return null;
    }

    class LoginSetup {

        private JPanel loginPane;
        private JTextField userField;
        private JPasswordField passField;
        private JInternalFrame dialog;
        private final int SPACE = 5;

        public LoginSetup() {
            final String host = "imap.googlemail.com"; // change accordingly

            loginPane = new JPanel();

            userField = new JTextField();
            passField = new JPasswordField();
            passField.addActionListener(e -> {
                model = new Model(host, userField.getText(), String.valueOf(passField.getPassword()));
            });

            JLabel userLabel = new JLabel("Username:");
            JLabel passLabel = new JLabel("Password:");

            loginPane.setLayout(new BoxLayout(loginPane, BoxLayout.LINE_AXIS));

            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
            labelPanel.add(userLabel);
            labelPanel.add(Box.createVerticalStrut(SPACE * 2));
            labelPanel.add(passLabel);
            labelPanel.setBorder(BorderFactory.createEmptyBorder(SPACE, SPACE, SPACE, SPACE));

            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.PAGE_AXIS));
            fieldPanel.add(userField);
            fieldPanel.add(Box.createVerticalStrut(SPACE));
            fieldPanel.add(passField);
            fieldPanel.setBorder(BorderFactory.createEmptyBorder(SPACE, SPACE, SPACE, SPACE));

            loginPane.add(labelPanel);
            loginPane.add(fieldPanel);
            loginPane.setBorder(BorderFactory.createEmptyBorder(SPACE, SPACE, SPACE, SPACE));

            createLoginDialog();
        }

        void loginFailed() {
            userField.setText("");
            passField.setText("");
        }

        private void createLoginDialog() {
            dialog = new JInternalFrame("Login");
            dialog.addInternalFrameListener(new InternalFrameAdapter() {
                public void internalFrameClosed(InternalFrameEvent e) {
                    if(authenticated == false) {
                        dispose();
                    }
                }
            });
            dialog.setClosable(true);
            dialog.add(loginPane);
            dialog.setPreferredSize(new Dimension(300, 100));
            dialog.pack();
            dialog.setVisible(true);
            MainView.getDesktop().add(dialog);
        }

        public JInternalFrame getDialog() {
            return dialog;
        }

    }

    class Model {

        private DefaultTableModel model;
        private String[] header;
        private Message[] messages;
        private Folder emailFolder;
        private Session emailSession;
        private Timer timer;
        private NewMessageTimer task;

        private final int DELAY = 0;
        private final int PERIOD = 10000;
        private final int IMAPS_PORT = 993;

        public Model(final String host, final String user, final String password) {
            class Worker extends SwingWorker<Void, String> {
                @Override
                protected Void doInBackground() {
                    header = new String[] { "Subject", "Read", "Correspondents", "Date" };

                    model = new DefaultTableModel(header, 0) {
                        private static final long serialVersionUID = -2116346605141053545L;

                        @Override
                        public Class<?> getColumnClass(int columnIndex) {
                            Class<?> clazz = String.class;
                            switch(columnIndex) {
                            case 1:
                                clazz = Boolean.class;
                                break;
                            case 3:
                                clazz = Date.class;
                            }
                            return clazz;
                        }

                        @Override
                        public boolean isCellEditable(int row, int col) {
                            switch(col) {
                            case 1:
                                return true;
                            default:
                                return false;
                            }
                        }

                        @Override
                        public void setValueAt(Object value, int row, int col) {
                            super.setValueAt(value, row, col);
                            if(!emailFolder.isOpen()) {
                                try {
                                    emailFolder.open(Folder.READ_WRITE);
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(col == 1) {
                                if((Boolean) this.getValueAt(row, col) == true) {
                                    try {
                                        emailFolder.setFlags(new Message[] { messages[(messages.length - 1) - row] }, new Flags(Flags.Flag.SEEN), true);
                                    } catch (MessagingException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else if((Boolean) this.getValueAt(row, col) == false) {
                                    try {
                                        emailFolder.setFlags(new Message[] { messages[(messages.length - 1) - row] }, new Flags(Flags.Flag.SEEN), false);
                                    } catch (MessagingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    };
                    MailFrame.this.mailTable.setModel(model);
                    MailFrame.this.mailTable.getColumnModel().getColumn(1).setMaxWidth(50);

                    TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
                        private static final long serialVersionUID = -7189272880275372668L;

                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            if(value instanceof Date) {
                                value = new SimpleDateFormat().format(value);
                            }
                            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            setBorder(noFocusBorder);
                            return this;
                        }
                    };

                    MailFrame.this.mailTable.getColumnModel().getColumn(3).setCellRenderer(tableCellRenderer); // for correct sorting

                    readMail(host, user, password);
                    return null;
                }
            }
            new Worker().execute();
        }

        private void readMail(String host, String user, String password) {
            try {
                // create properties field
                properties = new Properties();
                properties.setProperty("mail.imaps.partialfetch", "false");
                properties.setProperty("mail.smtp.ssl.enable", "true");
                properties.setProperty("mail.user", user);
                properties.setProperty("mail.password", password);
                properties.setProperty("mail.smtp.host", "smtp.gmail.com");
                properties.setProperty("mail.smtp.port", "465");
                properties.setProperty("mail.smtp.auth", "true");

                emailSession = Session.getDefaultInstance(properties);

                Store store = emailSession.getStore("imaps");

                try {
                    store.connect(host, IMAPS_PORT, user, password);
                } catch (Exception e) {
                    e.printStackTrace();
                    MailFrame.this.loginSetup.loginFailed();
                    return;
                }

                MailFrame.this.setVisible(true);
                authenticated = true;
                MailFrame.this.loginSetup.getDialog().dispose();

                // create the folder object and open it
                emailFolder = store.getFolder("INBOX");
                emailFolder.open(Folder.READ_WRITE);

                // retrieve the messages from the folder in an array and print it
                messages = emailFolder.getMessages();

                emailFolder.addConnectionListener(new ConnectionAdapter() {
                    @Override
                    public void closed(ConnectionEvent e) {
                        try {
                            emailFolder.open(Folder.READ_WRITE);
                        } catch (MessagingException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                // Adding a MessageCountListener to "listen" to new messages
                emailFolder.addMessageCountListener(new MessageCountAdapter() {
                    @Override
                    public void messagesAdded(MessageCountEvent ev) {
                        try {
                            if(!emailFolder.isOpen()) {
                                emailFolder.open(Folder.READ_WRITE);
                            }
                            Message[] msgs = ev.getMessages();
                            for(Message message : msgs) {
                                model.insertRow(0, new Object[] { message.getSubject(), message.isSet(Flags.Flag.SEEN), message.getFrom()[0], new SimpleDateFormat().format(message.getSentDate()) });
                                messages = emailFolder.getMessages(); // update messages array length
                            }
                        } catch (MessagingException ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void messagesRemoved(MessageCountEvent ev) {
                        try {
                            if(!emailFolder.isOpen()) {
                                emailFolder.open(Folder.READ_WRITE);
                            }
                            Message[] msgs = ev.getMessages();
                            for(Message message : msgs) {
                                model.removeRow((messages.length - 1) - Arrays.asList(messages).indexOf(message));
                                emailFolder.expunge();
                                messages = emailFolder.getMessages(); // update messages array length
                            }
                        } catch (MessagingException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                emailFolder.addMessageChangedListener(new MessageChangedListener() {
                    @Override
                    public void messageChanged(MessageChangedEvent e) {
                        if(e.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED) {
                            try {
                                if((Boolean) MailFrame.this.mailTable.getModel().getValueAt((messages.length - 1) - Arrays.asList(messages).indexOf(e.getMessage()), 1) == e.getMessage().isSet(Flags.Flag.SEEN)) {
                                    // If this flag (SEEN) is not the one that has changed i.e. the read values on client and server are same, return
                                    return;
                                }
                                else {
                                    // Set message read value to server's value
                                    MailFrame.this.mailTable.getModel().setValueAt(e.getMessage().isSet(Flags.Flag.SEEN), (messages.length - 1) - Arrays.asList(messages).indexOf(e.getMessage()), 1);
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });

                startTimer(); // start timer for incoming messages

                for(int i = messages.length - 1; i >= 0; i--) {
                    if(!emailFolder.isOpen()) {
                        emailFolder.open(Folder.READ_WRITE);
                    }
                    Message message = messages[i];
                    model.addRow(new Object[] { message.getSubject(), message.isSet(Flags.Flag.SEEN), message.getFrom()[0], message.getSentDate() });
                }

                // close the store and folder objects
                // emailFolder.close(false);
                // store.close();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        class NewMessageTimer extends TimerTask {
            @Override
            public void run() {
                try {
                    if(!emailFolder.isOpen()) {
                        emailFolder.open(Folder.READ_WRITE);
                    }
                    // We do the first reading of emails
                    int start = 1;
                    int end = emailFolder.getMessageCount();
                    while(start <= end) {
                        // new messages that have arrived
                        start = end + 1;
                        end = emailFolder.getMessageCount();
                    }

                    // Waiting for new messages
                    for(;;) {
                        try {
                            ((IMAPFolder) emailFolder).idle();
                        } catch (FolderClosedException e) {
                            e.printStackTrace();
                            if(!emailFolder.isOpen()) {
                                emailFolder.open(Folder.READ_WRITE);
                            }
                        }
                    }
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void startTimer() {
            timer = new Timer();
            task = new NewMessageTimer();
            timer.schedule(task, DELAY, PERIOD); // 0 milliseconds till start, 10 seconds delay between checks
        }

        public Properties getProperties() {
            return properties;
        }

        public Session getSession() {
            return emailSession;
        }

        public Message[] getMessages() {
            return messages;
        }

        public DefaultTableModel getModel() {
            return model;
        }

        public Folder getFolder() {
            return emailFolder;
        }

    }

}
