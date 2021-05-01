package com.secres.secresmail_lib;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

import com.secres.secresos.MainView;

/**
 * HTMLDocumentEditor.java
 * 
 * @author Charles Bell
 * @author Pranav Amarnath
 * @version April 30, 2021
 * 
 */
public class HTMLDocumentEditor extends JInternalFrame implements ActionListener {

	private static final long serialVersionUID = -5807173437950418191L;

	private HTMLDocument document;
	private JTextPane textPane = new JTextPane();
	private boolean debug = false;
	private File currentFile;

	/** Listener for the edits on the current document. */
	protected UndoableEditListener undoHandler = new UndoHandler();

	/** UndoManager that we add edits to. */
	protected UndoManager undo = new UndoManager();

	private UndoAction undoAction = new UndoAction();
	private RedoAction redoAction = new RedoAction();

	private Action cutAction = new DefaultEditorKit.CutAction();
	private Action copyAction = new DefaultEditorKit.CopyAction();
	private Action pasteAction = new DefaultEditorKit.PasteAction();

	private Action boldAction = new StyledEditorKit.BoldAction();
	private Action underlineAction = new StyledEditorKit.UnderlineAction();
	private Action italicAction = new StyledEditorKit.ItalicAction();

	private Action insertBreakAction = new DefaultEditorKit.InsertBreakAction();
	private HTMLEditorKit.InsertHTMLTextAction unorderedListAction = new HTMLEditorKit.InsertHTMLTextAction("Bullets", "<ul><li> </li></ul>", HTML.Tag.P, HTML.Tag.UL);
	private HTMLEditorKit.InsertHTMLTextAction bulletAction = new HTMLEditorKit.InsertHTMLTextAction("Bullets", "<li> </li>", HTML.Tag.UL, HTML.Tag.LI);

	private JInternalFrame dialog;

	public HTMLDocumentEditor(Properties props) {
		super("HTMLDocumentEditor");
		HTMLEditorKit editorKit = new HTMLEditorKit();
		document = (HTMLDocument) editorKit.createDefaultDocument();
		init(props);
	}

	public void init(Properties props) {

		// addWindowListener(new FrameListener());

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");
		JMenu colorMenu = new JMenu("Color");
		JMenu fontMenu = new JMenu("Font");
		JMenu styleMenu = new JMenu("Style");
		JMenu alignMenu = new JMenu("Align");
		JMenu helpMenu = new JMenu("Help");

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(colorMenu);
		menuBar.add(fontMenu);
		menuBar.add(styleMenu);
		menuBar.add(alignMenu);
		menuBar.add(helpMenu);

		JMenuItem newItem = new JMenuItem("New", new ImageIcon("whatsnew-bang.gif"));
		JMenuItem openItem = new JMenuItem("Open", new ImageIcon("open.gif"));
		JMenuItem exitItem = new JMenuItem("Exit", new ImageIcon("exit.gif"));

		newItem.addActionListener(this);
		openItem.addActionListener(this);
		exitItem.addActionListener(this);

		fileMenu.add(newItem);
		//fileMenu.add(openItem);
		fileMenu.add(exitItem);

		JMenuItem undoItem = new JMenuItem(undoAction);
		JMenuItem redoItem = new JMenuItem(redoAction);
		JMenuItem cutItem = new JMenuItem(cutAction);
		JMenuItem copyItem = new JMenuItem(copyAction);
		JMenuItem pasteItem = new JMenuItem(pasteAction);
		JMenuItem clearItem = new JMenuItem("Clear");
		JMenuItem selectAllItem = new JMenuItem("Select All");
		JMenuItem insertBreaKItem = new JMenuItem(insertBreakAction);
		JMenuItem unorderedListItem = new JMenuItem(unorderedListAction);
		JMenuItem bulletItem = new JMenuItem(bulletAction);

		cutItem.setText("Cut");
		copyItem.setText("Copy");
		pasteItem.setText("Paste");
		insertBreaKItem.setText("Break");
		cutItem.setIcon(new ImageIcon("cut.gif"));
		copyItem.setIcon(new ImageIcon("copy.gif"));
		pasteItem.setIcon(new ImageIcon("paste.gif"));
		insertBreaKItem.setIcon(new ImageIcon("break.gif"));
		unorderedListItem.setIcon(new ImageIcon("bullets.gif"));

		clearItem.addActionListener(this);
		selectAllItem.addActionListener(this);

		editMenu.add(undoItem);
		editMenu.add(redoItem);
		editMenu.add(cutItem);
		editMenu.add(copyItem);
		editMenu.add(pasteItem);
		editMenu.add(clearItem);
		editMenu.add(selectAllItem);
		editMenu.add(insertBreaKItem);
		editMenu.add(unorderedListItem);
		editMenu.add(bulletItem);

		JMenuItem redTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Red", Color.red));
		JMenuItem orangeTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Orange", Color.orange));
		JMenuItem yellowTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Yellow", Color.yellow));
		JMenuItem greenTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Green", Color.green));
		JMenuItem blueTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Blue", Color.blue));
		JMenuItem cyanTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Cyan", Color.cyan));
		JMenuItem magentaTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Magenta", Color.magenta));
		JMenuItem blackTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Black", Color.black));

		redTextItem.setIcon(new ImageIcon("red.gif"));
		orangeTextItem.setIcon(new ImageIcon("orange.gif"));
		yellowTextItem.setIcon(new ImageIcon("yellow.gif"));
		greenTextItem.setIcon(new ImageIcon("green.gif"));
		blueTextItem.setIcon(new ImageIcon("blue.gif"));
		cyanTextItem.setIcon(new ImageIcon("cyan.gif"));
		magentaTextItem.setIcon(new ImageIcon("magenta.gif"));
		blackTextItem.setIcon(new ImageIcon("black.gif"));

		colorMenu.add(redTextItem);
		colorMenu.add(orangeTextItem);
		colorMenu.add(yellowTextItem);
		colorMenu.add(greenTextItem);
		colorMenu.add(blueTextItem);
		colorMenu.add(cyanTextItem);
		colorMenu.add(magentaTextItem);
		colorMenu.add(blackTextItem);

		JMenu fontTypeMenu = new JMenu("Font Type");
		fontMenu.add(fontTypeMenu);

		String[] fontTypes = { "SansSerif", "Serif", "Monospaced", "Dialog", "DialogInput" };
		for(int i = 0; i < fontTypes.length; i++) {
			if(debug) System.out.println(fontTypes[i]);
			JMenuItem nextTypeItem = new JMenuItem(fontTypes[i]);
			nextTypeItem.setAction(new StyledEditorKit.FontFamilyAction(fontTypes[i], fontTypes[i]));
			fontTypeMenu.add(nextTypeItem);
		}

		JMenu fontSizeMenu = new JMenu("Font Size");
		fontMenu.add(fontSizeMenu);

		int[] fontSizes = { 6, 8, 10, 12, 14, 16, 20, 24, 32, 36, 48, 72 };
		for(int i = 0; i < fontSizes.length; i++) {
			if(debug) System.out.println(fontSizes[i]);
			JMenuItem nextSizeItem = new JMenuItem(String.valueOf(fontSizes[i]));
			nextSizeItem.setAction(new StyledEditorKit.FontSizeAction(String.valueOf(fontSizes[i]), fontSizes[i]));
			fontSizeMenu.add(nextSizeItem);
		}

		JMenuItem boldMenuItem = new JMenuItem(boldAction);
		JMenuItem underlineMenuItem = new JMenuItem(underlineAction);
		JMenuItem italicMenuItem = new JMenuItem(italicAction);

		boldMenuItem.setText("Bold");
		underlineMenuItem.setText("Underline");
		italicMenuItem.setText("Italic");

		boldMenuItem.setIcon(new ImageIcon("bold.gif"));
		underlineMenuItem.setIcon(new ImageIcon("underline.gif"));
		italicMenuItem.setIcon(new ImageIcon("italic.gif"));

		styleMenu.add(boldMenuItem);
		styleMenu.add(underlineMenuItem);
		styleMenu.add(italicMenuItem);

		JMenuItem subscriptMenuItem = new JMenuItem(new SubscriptAction());
		JMenuItem superscriptMenuItem = new JMenuItem(new SuperscriptAction());
		JMenuItem strikeThroughMenuItem = new JMenuItem(new StrikeThroughAction());

		subscriptMenuItem.setText("Subscript");
		superscriptMenuItem.setText("Superscript");
		strikeThroughMenuItem.setText("StrikeThrough");

		subscriptMenuItem.setIcon(new ImageIcon("subscript.gif"));
		superscriptMenuItem.setIcon(new ImageIcon("superscript.gif"));
		strikeThroughMenuItem.setIcon(new ImageIcon("strikethough.gif"));

		styleMenu.add(subscriptMenuItem);
		styleMenu.add(superscriptMenuItem);
		styleMenu.add(strikeThroughMenuItem);

		JMenuItem leftAlignMenuItem = new JMenuItem(new StyledEditorKit.AlignmentAction("Left Align", StyleConstants.ALIGN_LEFT));
		JMenuItem centerMenuItem = new JMenuItem(new StyledEditorKit.AlignmentAction("Center", StyleConstants.ALIGN_CENTER));
		JMenuItem rightAlignMenuItem = new JMenuItem(new StyledEditorKit.AlignmentAction("Right Align", StyleConstants.ALIGN_RIGHT));

		leftAlignMenuItem.setText("Left Align");
		centerMenuItem.setText("Center");
		rightAlignMenuItem.setText("Right Align");

		leftAlignMenuItem.setIcon(new ImageIcon("left.gif"));
		centerMenuItem.setIcon(new ImageIcon("center.gif"));
		rightAlignMenuItem.setIcon(new ImageIcon("right.gif"));

		alignMenu.add(leftAlignMenuItem);
		alignMenu.add(centerMenuItem);
		alignMenu.add(rightAlignMenuItem);

		JMenuItem helpItem = new JMenuItem("Help");
		helpItem.addActionListener(this);
		helpMenu.add(helpItem);

		JMenuItem shortcutsItem = new JMenuItem("Keyboard Shortcuts");
		shortcutsItem.addActionListener(this);
		helpMenu.add(shortcutsItem);

		JMenuItem aboutItem = new JMenuItem("About QuantumHyperSpace");
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);

		JPanel editorControlPanel = new JPanel();
		// editorControlPanel.setLayout(new GridLayout(3,3));
		editorControlPanel.setLayout(new FlowLayout());

		/* JButtons */
		JButton cutButton = new JButton(cutAction);
		JButton copyButton = new JButton(copyAction);
		JButton pasteButton = new JButton(pasteAction);

		JButton boldButton = new JButton(boldAction);
		JButton underlineButton = new JButton(underlineAction);
		JButton italicButton = new JButton(italicAction);

		// JButton insertButton = new JButton(insertAction);
		// JButton insertBreakButton = new JButton(insertBreakAction);
		// JButton tabButton = new JButton(tabAction);

		cutButton.setText("Cut");
		copyButton.setText("Copy");
		pasteButton.setText("Paste");

		boldButton.setText("Bold");
		underlineButton.setText("Underline");
		italicButton.setText("Italic");

		// insertButton.setText("Insert");
		// insertBreakButton.setText("Insert Break");
		// tabButton.setText("Tab");

		cutButton.setIcon(new ImageIcon("cut.gif"));
		copyButton.setIcon(new ImageIcon("copy.gif"));
		pasteButton.setIcon(new ImageIcon("paste.gif"));

		boldButton.setIcon(new ImageIcon("bold.gif"));
		underlineButton.setIcon(new ImageIcon("underline.gif"));
		italicButton.setIcon(new ImageIcon("italic.gif"));

		editorControlPanel.add(cutButton);
		editorControlPanel.add(copyButton);
		editorControlPanel.add(pasteButton);

		editorControlPanel.add(boldButton);
		editorControlPanel.add(underlineButton);
		editorControlPanel.add(italicButton);

		// editorControlPanel.add(insertButton);
		// editorControlPanel.add(insertBreakButton);
		// editorControlPanel.add(tabButton);

		JButton subscriptButton = new JButton(new SubscriptAction());
		JButton superscriptButton = new JButton(new SuperscriptAction());
		JButton strikeThroughButton = new JButton(new StrikeThroughAction());

		subscriptButton.setIcon(new ImageIcon("subscript.gif"));
		superscriptButton.setIcon(new ImageIcon("superscript.gif"));
		strikeThroughButton.setIcon(new ImageIcon("strikethough.gif"));

		JPanel specialPanel = new JPanel();
		specialPanel.setLayout(new FlowLayout());

		specialPanel.add(subscriptButton);
		specialPanel.add(superscriptButton);
		specialPanel.add(strikeThroughButton);

		// JButton leftAlignButton = new JButton(new AlignLeftAction());
		// JButton centerButton = new JButton(new CenterAction());
		// JButton rightAlignButton = new JButton(new AlignRightAction());

		JButton leftAlignButton = new JButton(new StyledEditorKit.AlignmentAction("Left Align", StyleConstants.ALIGN_LEFT));
		JButton centerButton = new JButton(new StyledEditorKit.AlignmentAction("Center", StyleConstants.ALIGN_CENTER));
		JButton rightAlignButton = new JButton(new StyledEditorKit.AlignmentAction("Right Align", StyleConstants.ALIGN_RIGHT));
		JButton colorButton = new JButton(new StyledEditorKit.AlignmentAction("Right Align", StyleConstants.ALIGN_RIGHT));

		leftAlignButton.setIcon(new ImageIcon("left.gif"));
		centerButton.setIcon(new ImageIcon("center.gif"));
		rightAlignButton.setIcon(new ImageIcon("right.gif"));
		colorButton.setIcon(new ImageIcon("color.gif"));

		leftAlignButton.setText("Left Align");
		centerButton.setText("Center");
		rightAlignButton.setText("Right Align");

		JPanel alignPanel = new JPanel();
		alignPanel.setLayout(new FlowLayout());
		alignPanel.add(leftAlignButton);
		alignPanel.add(centerButton);
		alignPanel.add(rightAlignButton);

		document.addUndoableEditListener(undoHandler);
		resetUndoManager();

		textPane = new JTextPane(document);
		textPane.setContentType("text/html");
		JScrollPane scrollPane = new JScrollPane(textPane);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension scrollPaneSize = new Dimension(5 * screenSize.width / 8, 5 * screenSize.height / 8);
		scrollPane.setPreferredSize(scrollPaneSize);

		JPanel toolPanel = new JPanel();
		toolPanel.setLayout(new BorderLayout());
		toolPanel.add(editorControlPanel, BorderLayout.NORTH);
		toolPanel.add(specialPanel, BorderLayout.CENTER);
		toolPanel.add(alignPanel, BorderLayout.SOUTH);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		JPanel toPanel = new JPanel();
		toPanel.setLayout(new BoxLayout(toPanel, BoxLayout.X_AXIS));
		JTextField toTextField = new JTextField();
		JLabel toLabel = new JLabel("To:          ");
		toLabel.setLabelFor(toTextField);
		toPanel.add(toLabel);
		toPanel.add(toTextField);
		toPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel subjectPanel = new JPanel();
		subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.X_AXIS));
		JTextField subjectTextField = new JTextField();
		JLabel subjectLabel = new JLabel("Subject:  ");
		subjectLabel.setLabelFor(subjectTextField);
		subjectPanel.add(subjectLabel);
		subjectPanel.add(subjectTextField);
		subjectPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(e -> {
			if(toTextField.getText().equals("")) {
				JOptionPane.showInternalMessageDialog(this, "Please enter a To address!", "Error", JOptionPane.ERROR_MESSAGE);
				toTextField.requestFocus();
				return;
			}
			if(subjectTextField.getText().equals("")) {
				JOptionPane.showInternalMessageDialog(this, "Please enter a Subject!", "Error", JOptionPane.ERROR_MESSAGE);
				subjectTextField.requestFocus();
				return;
			}
			if(textPane.getText().equals("")) {
				JOptionPane.showInternalMessageDialog(this, "Please enter a Message!", "Error", JOptionPane.ERROR_MESSAGE);
				textPane.requestFocus();
				return;
			}

			String toAddress = toTextField.getText();
			String subject = subjectTextField.getText();
			String message = textPane.getText();

			File[] attachFiles = null;

			/*
			if(!filePicker.getSelectedFilePath().equals("")) {
				File selectedFile = new File(filePicker.getSelectedFilePath());
				attachFiles = new File[]{selectedFile};
			}
			*/

			new Thread(() -> {
				SwingUtilities.invokeLater(() -> {
					JProgressBar pb = new JProgressBar();
					pb.setIndeterminate(true);
					JOptionPane op = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE);
			        op.setVisible(false);
					dialog = op.createInternalFrame(this, "Sending...");
					dialog.add(pb);
					dialog.pack();
					dialog.setVisible(true);
				});
				Properties smtpProperties = props;
				try {
					EmailUtility.sendEmail(smtpProperties, toAddress, subject, message, attachFiles);
				} catch (Exception ex) {
					JOptionPane.showInternalMessageDialog(this, "Error while sending the e-mail: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					dialog.dispose();
					return;
				}
				SwingUtilities.invokeLater(() -> {
					dialog.dispose();
					JOptionPane.showInternalMessageDialog(this, "The e-mail has been sent successfully!");
					dispose();
				});
			}).start();
		});

		topPanel.add(toPanel);
		topPanel.add(subjectPanel);
		topPanel.add(sendButton);

		topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setJMenuBar(menuBar);
		add(topPanel, BorderLayout.NORTH);
		// add(toolPanel, BorderLayout.CENTER);
		add(scrollPane, BorderLayout.CENTER);

		pack();
		startNewDocument();
		setVisible(true);
		setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setIconifiable(true);
		MainView.getDesktop().add(this);
	}

	public void actionPerformed(ActionEvent ae) {
		String actionCommand = ae.getActionCommand();
		if(debug) {
			int modifier = ae.getModifiers();
			long when = ae.getWhen();
			String parameter = ae.paramString();
			System.out.println("actionCommand: " + actionCommand);
			System.out.println("modifier: " + modifier);
			System.out.println("when: " + when);
			System.out.println("parameter: " + parameter);
		}
		if(actionCommand.compareTo("New") == 0) {
			startNewDocument();
		}
		else if(actionCommand.compareTo("Open") == 0) {
			openDocument();
		}
		else if(actionCommand.compareTo("Exit") == 0) {
			exit();
		}
		else if(actionCommand.compareTo("Clear") == 0) {
			clear();
		}
		else if(actionCommand.compareTo("Select All") == 0) {
			selectAll();
		}
		else if(actionCommand.compareTo("Help") == 0) {
			help();
		}
		else if(actionCommand.compareTo("Keyboard Shortcuts") == 0) {
			showShortcuts();
		}
		else if(actionCommand.compareTo("About QuantumHyperSpace") == 0) {
			aboutQuantumHyperSpace();
		}
	}

	protected void resetUndoManager() {
		undo.discardAllEdits();
		undoAction.update();
		redoAction.update();
	}

	public void startNewDocument() {
		Document oldDoc = textPane.getDocument();
		if(oldDoc != null) oldDoc.removeUndoableEditListener(undoHandler);
		HTMLEditorKit editorKit = new HTMLEditorKit();
		document = (HTMLDocument) editorKit.createDefaultDocument();
		textPane.setDocument(document);
		currentFile = null;
		setTitle("HTMLDocumentEditor");
		textPane.getDocument().addUndoableEditListener(undoHandler);
		resetUndoManager();
	}

	public void openDocument() {
		try {
			File current = new File(".");
			JFileChooser chooser = new JFileChooser(current);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setFileFilter(new HTMLFileFilter());
			int approval = chooser.showSaveDialog(this);
			if(approval == JFileChooser.APPROVE_OPTION) {
				currentFile = chooser.getSelectedFile();
				setTitle(currentFile.getName());
				FileReader fr = new FileReader(currentFile);
				Document oldDoc = textPane.getDocument();
				if(oldDoc != null) oldDoc.removeUndoableEditListener(undoHandler);
				HTMLEditorKit editorKit = new HTMLEditorKit();
				document = (HTMLDocument) editorKit.createDefaultDocument();
				editorKit.read(fr, document, 0);
				document.addUndoableEditListener(undoHandler);
				textPane.setDocument(document);
				resetUndoManager();
			}
		} catch (BadLocationException ble) {
			System.err.println("BadLocationException: " + ble.getMessage());
		} catch (FileNotFoundException fnfe) {
			System.err.println("FileNotFoundException: " + fnfe.getMessage());
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}

	}

	public void exit() {
		String exitMessage = "Are you sure you want to exit?";
		if(JOptionPane.showInternalConfirmDialog(this, exitMessage) == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}

	public void clear() {
		startNewDocument();
	}

	public void selectAll() {
		textPane.selectAll();
	}

	public void help() {
		JOptionPane.showInternalMessageDialog(this, "DocumentEditor.java\n" + "Author: Charles Bell\n" + "Version: May 25, 2002\n" + "http://www.quantumhyperspace.com\n" + "QuantumHyperSpace Programming Services");
	}

	public void showShortcuts() {
		String shortcuts = "Navigate in    |  Tab\n" + "Navigate out   |  Ctrl+Tab\n" + "Navigate out backwards    |  Shift+Ctrl+Tab\n" + "Move up/down a line    |  Up/Down Arrown\n" + "Move left/right a component or char    |  Left/Right Arrow\n" + "Move up/down one vertical block    |  PgUp/PgDn\n" + "Move to start/end of line    |  Home/End\n" + "Move to previous/next word    |  Ctrl+Left/Right Arrow\n" + "Move to start/end of data    |  Ctrl+Home/End\n" + "Move left/right one block    |  Ctrl+PgUp/PgDn\n" + "Select All    |  Ctrl+A\n" + "Extend selection up one line    |  Shift+Up Arrow\n" + "Extend selection down one line    |  Shift+Down Arrow\n" + "Extend selection to beginning of line    |  Shift+Home\n" + "Extend selection to end of line    |  Shift+End\n" + "Extend selection to beginning of data    |  Ctrl+Shift+Home\n" + "Extend selection to end of data    |  Ctrl+Shift+End\n" + "Extend selection left    |  Shift+Right Arrow\n" + "Extend selection right    |  Shift+Right Arrow\n" + "Extend selection up one vertical block    |  Shift+PgUp\n" + "Extend selection down one vertical block    |  Shift+PgDn\n" + "Extend selection left one block    |  Ctrl+Shift+PgUp\n" + "Extend selection right one block    |  Ctrl+Shift+PgDn\n" + "Extend selection left one word    |  Ctrl+Shift+Left Arrow\n" + "Extend selection right one word    |  Ctrl+Shift+Right Arrow\n";
		JOptionPane.showInternalMessageDialog(this, shortcuts);
	}

	public void aboutQuantumHyperSpace() {
		JOptionPane.showInternalMessageDialog(this, "QuantumHyperSpace Programming Services\n" + "http://www.quantumhyperspace.com\n" + "email: support@quantumhyperspace.com\n" + "                     or \n" + "email: charles@quantumhyperspace.com\n", "QuantumHyperSpace", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("quantumhyperspace.gif"));
	}

	class FrameListener extends WindowAdapter {
		public void windowClosing(WindowEvent we) {
			exit();
		}
	}

	class SubscriptAction extends StyledEditorKit.StyledTextAction {

		private static final long serialVersionUID = 4761765164423312291L;

		public SubscriptAction() {
			super(StyleConstants.Subscript.toString());
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			JEditorPane editor = getEditor(ae);
			if(editor != null) {
				StyledEditorKit kit = getStyledEditorKit(editor);
				MutableAttributeSet attr = kit.getInputAttributes();
				boolean subscript = (StyleConstants.isSubscript(attr)) ? false : true;
				SimpleAttributeSet sas = new SimpleAttributeSet();
				StyleConstants.setSubscript(sas, subscript);
				setCharacterAttributes(editor, sas, false);
			}
		}
	}

	class SuperscriptAction extends StyledEditorKit.StyledTextAction {

		private static final long serialVersionUID = 3715597214507358147L;

		public SuperscriptAction() {
			super(StyleConstants.Superscript.toString());
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			JEditorPane editor = getEditor(ae);
			if(editor != null) {
				StyledEditorKit kit = getStyledEditorKit(editor);
				MutableAttributeSet attr = kit.getInputAttributes();
				boolean superscript = (StyleConstants.isSuperscript(attr)) ? false : true;
				SimpleAttributeSet sas = new SimpleAttributeSet();
				StyleConstants.setSuperscript(sas, superscript);
				setCharacterAttributes(editor, sas, false);
			}
		}
	}

	class StrikeThroughAction extends StyledEditorKit.StyledTextAction {

		private static final long serialVersionUID = 4702758367755878865L;

		public StrikeThroughAction() {
			super(StyleConstants.StrikeThrough.toString());
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			JEditorPane editor = getEditor(ae);
			if(editor != null) {
				StyledEditorKit kit = getStyledEditorKit(editor);
				MutableAttributeSet attr = kit.getInputAttributes();
				boolean strikeThrough = (StyleConstants.isStrikeThrough(attr)) ? false : true;
				SimpleAttributeSet sas = new SimpleAttributeSet();
				StyleConstants.setStrikeThrough(sas, strikeThrough);
				setCharacterAttributes(editor, sas, false);
			}
		}
	}

	class HTMLFileFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f) {
			return((f.isDirectory()) || (f.getName().toLowerCase().indexOf(".htm") > 0));
		}

		public String getDescription() {
			return "html";
		}
	}

	class UndoHandler implements UndoableEditListener {

		/**
		 * Messaged when the Document has created an edit, the edit is added to
		 * <code>undo</code>, an instance of UndoManager.
		 */
		public void undoableEditHappened(UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			undoAction.update();
			redoAction.update();
		}
	}

	class UndoAction extends AbstractAction {

		private static final long serialVersionUID = 5686379534139648294L;

		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException ex) {
				System.out.println("Unable to undo: " + ex);
				ex.printStackTrace();
			}
			update();
			redoAction.update();
		}

		protected void update() {
			if(undo.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			}
			else {
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction {

		private static final long serialVersionUID = -5075552203023089228L;

		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException ex) {
				System.err.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			update();
			undoAction.update();
		}

		protected void update() {
			if(undo.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			}
			else {
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}
}