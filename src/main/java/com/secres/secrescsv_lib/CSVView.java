package com.secres.secrescsv_lib;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.Taskbar.State;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.JTable.PrintMode;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXHyperlink;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.secres.secresos.MainView;

public class CSVView extends JInternalFrame {

    private static final long serialVersionUID = -101882753101966246L;
    private JPanel mainPanel, emptyPanel, tabsPanel;
	private JButton openButton, saveButton, printButton, selectAllButton, refreshButton;
	private JToolBar toolBar;
	private JTabbedPane tabbedPane;
	private LinkedHashMap<TablePanel, File> newPanels = new LinkedHashMap<>();
	private JMenuBar menuBar;
	private JMenu fileMenu, editMenu, helpMenu;
	private JMenuItem openMenuItem, saveMenuItem, printMenuItem, closeMenuItem, exitMenuItem;
	private JMenuItem selectAllMenuItem, refreshMenuItem;
	private JMenuItem aboutMenuItem;
	private JXBusyLabel busyLabel;
	private Model model;
	private JInternalFrame fileChooserFrame;

	public CSVView() {
		createAndShowGUI();
	}

	private void createAndShowGUI() {
	    setTitle("SecresCSV");
	    
		mainPanel = new JPanel(new CardLayout());
		emptyPanel = new JPanel(new BorderLayout());
		tabsPanel = new JPanel(new BorderLayout());

		tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);

		tabbedPane.setTabLayoutPolicy(1); // scrolling tabs
		tabbedPane.putClientProperty("JTabbedPane.tabClosable", true);
		tabbedPane.putClientProperty("JTabbedPane.showTabSeparators", true);
		tabbedPane.putClientProperty("JTabbedPane.tabCloseCallback", (BiConsumer<JTabbedPane, Integer>) (tabbedPane, tabIndex) -> {
			// close tab here
			closeTab(tabIndex);
		});
		tabbedPane.putClientProperty("JTabbedPane.tabCloseToolTipText", "Close");

		menuBar = new JMenuBar();
		toolBar = new JToolBar();
		toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
		toolBar.setFloatable(false);

		createMenuBar();
		createToolBar();
		
		mainPanel.add(emptyPanel, "Empty");
		mainPanel.add(tabsPanel, "Tabs");

		tabbedPane.addChangeListener(e -> {
			int tabCount = tabbedPane.getTabCount();
			boolean enabled = (tabCount > 0);
			enableItems(enabled);
			// Switch between empty panel and tabs depending on if there are any tabs open
			if(enabled) {
				CardLayout cl = (CardLayout) mainPanel.getLayout();
				cl.show(mainPanel, "Tabs");
			}
			else {
				CardLayout cl = (CardLayout) mainPanel.getLayout();
				cl.show(mainPanel, "Empty");
			}
		});
		enableItems(false);
		tabsPanel.add(tabbedPane);

		add(toolBar, BorderLayout.NORTH);
		setJMenuBar(menuBar);

		JPanel noFilesPanel = new JPanel();
		noFilesPanel.setLayout(new BoxLayout(noFilesPanel, BoxLayout.PAGE_AXIS));
		JLabel emptyLabel = new JLabel("No files are open", JLabel.CENTER);
		emptyLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
		emptyLabel.setForeground(new Color(150, 150, 150));
		JLabel openFileWithMenuLabel = new JLabel("Open a file with menu \"File > Open...\"", JLabel.CENTER);
		openFileWithMenuLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
		openFileWithMenuLabel.setForeground(new Color(150, 150, 150));
		JLabel dndLabel = new JLabel("Drag and drop files from file manager", JLabel.CENTER);
		dndLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
		dndLabel.setForeground(new Color(150, 150, 150));

		noFilesPanel.add(emptyLabel);
		noFilesPanel.add(openFileWithMenuLabel);
		noFilesPanel.add(dndLabel);

		JPanel tempPanel = new JPanel(new GridBagLayout());
		tempPanel.add(noFilesPanel);

		emptyPanel.add(tempPanel);

		new FileDrop(emptyPanel, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				Arrays.sort(files, new FileSizeComparator()); // sort so that largest files are processed last
				int i = 0;
				for(File path : files) {
					if(i == files.length-1) { // check if it's the last file
						addTablePanel(path, true);
					}
					else {
						addTablePanel(path, false);
					}
					i++;
				}
			}
		});

		add(mainPanel);

		//Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		//URL imageResource = getClass().getResource("/icons8-slider-1024.png"); // URL: https://cdn.pixabay.com/photo/2012/05/04/10/57/gear-47203_1280.png
		//Image image = defaultToolkit.getImage(imageResource);
		//setFrameIcon(new ImageIcon(image));
		
		if(Taskbar.getTaskbar().isSupported(Taskbar.Feature.MENU)) {
			PopupMenu popupMenu = new PopupMenu(); // popup menu for macOS dock icon
			MenuItem open = new MenuItem("Open...");
			open.addActionListener(e -> {
				openDialog();
			});
			popupMenu.add(open);
			Taskbar.getTaskbar().setMenu(popupMenu);
		}
		
		pack();
		setVisible(true);
	}
	
	/** Sorter class based on file size so that largest files are processed last */
	public class FileSizeComparator implements Comparator<File> {
		// see: https://stackoverflow.com/a/8107018/13772184
		@Override
	    public int compare(File a, File b) {
	        long aSize = a.length();
	        long bSize = b.length();
	        if (aSize == bSize) {
	            return 0;
	        }
	        else {
	            return Long.compare(aSize, bSize);
	        }
	    }
	}

	private void openDialog() {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files (*.csv), Text files (*.txt)", "csv", "txt");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(true);
		if(fileChooser.getActionMap().get("viewTypeDetails") != null) {
			Action details = fileChooser.getActionMap().get("viewTypeDetails");
			details.actionPerformed(null);
		}

		if(newPanels.get(tabbedPane.getSelectedComponent()) != null) {
			fileChooser.setCurrentDirectory(newPanels.get(tabbedPane.getSelectedComponent()));
		}

		fileChooserFrame = null;
		
		fileChooser.addActionListener(e -> {
            if(JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
                File[] files = fileChooser.getSelectedFiles();
                Arrays.sort(files, new FileSizeComparator()); // sort so that largest files are processed last
                int i = 0;
                for(File path : files) {
                    if(i == files.length-1) { // check if it's the last file
                        addTablePanel(path, true);
                    }
                    else {
                        addTablePanel(path, false);
                    }
                    i++;
                }
            }
            fileChooserFrame.dispose();
        });
		
		JOptionPane op = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE);
		op.setVisible(false);
		
		fileChooserFrame = op.createInternalFrame(this, title);
		fileChooserFrame.add(fileChooser);
		fileChooserFrame.pack();
		fileChooserFrame.setVisible(true);
	}

	private void addTablePanel(File path, boolean isLastFile) {
		TablePanel newPanel = new TablePanel();
		newPanels.put(newPanel, path);
		if(tabbedPane.getTabCount() > 0) {
			for(int i = 0; i < tabbedPane.getTabCount(); i++) {
				if(tabbedPane.getTitleAt(i).equals(path.getName())) {
					tabbedPane.setSelectedIndex(i);
					return;
				}
			}
		}
		tabbedPane.addTab(path.getName(), FileSystemView.getFileSystemView().getSystemIcon(path), (Component) newPanels.keySet().toArray()[newPanels.size()-1]);
		model = new Model(path, ((TablePanel) newPanels.keySet().toArray()[newPanels.size()-1]).getTable(), false, isLastFile);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
	}

	private void closeTab(int tabIndex) {
		saveDialog();
		tabbedPane.removeTabAt(tabIndex);
	}
	
	private void saveDialog() {
		int result = JOptionPane.showInternalConfirmDialog(this, "Do you want to save any changes?", "Save", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.YES_OPTION) {
			save();
		}
	}

	private void save() {
		model.save(newPanels.get(tabbedPane.getSelectedComponent()).getAbsolutePath(), ((TablePanel) tabbedPane.getSelectedComponent()).getTable());
	}

	private void selectAll() {
		((TablePanel) tabbedPane.getSelectedComponent()).getTable().requestFocus();
		((TablePanel) tabbedPane.getSelectedComponent()).getTable().selectAll();
	}

	private void print() {
		try {
			MessageFormat header = new MessageFormat("Page {0,number,integer}");
			((TablePanel) tabbedPane.getSelectedComponent()).getTable().print(PrintMode.FIT_WIDTH, header, null, true, null, true);
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}

	private void refresh() {
		model = new Model(newPanels.get((TablePanel) tabbedPane.getSelectedComponent()), ((TablePanel) tabbedPane.getSelectedComponent()).getTable(), true, true);
	}

	private void enableItems(boolean enabled) {
		saveButton.setEnabled(enabled);
		selectAllButton.setEnabled(enabled);
		refreshButton.setEnabled(enabled);
		editMenu.setEnabled(enabled);
		saveMenuItem.setEnabled(enabled);
		printButton.setEnabled(enabled);
		//printMenuItem.setEnabled(enabled);
		closeMenuItem.setEnabled(enabled);
	}
	
	private void createMenuBar() {
		fileMenu = new JMenu("File");
		openMenuItem = new JMenuItem("Open...");
		openMenuItem.setToolTipText("Open file");
		openMenuItem.addActionListener(e -> {
			openDialog();
		});
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setToolTipText("Save table");
		saveMenuItem.addActionListener(e -> {
			save();
		});
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		printMenuItem = new JMenuItem("Print...");
		printMenuItem.setToolTipText("Print table");
		printMenuItem.addActionListener(e -> {
			print();
		});
		printMenuItem.setEnabled(false); // disable because print dialog pops up in a dialog but can't override behavior
		printMenuItem.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		closeMenuItem = new JMenuItem("Close");
		closeMenuItem.setToolTipText("Close current tab");
		closeMenuItem.addActionListener(e -> {
			closeTab(tabbedPane.getSelectedIndex());
		});
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setToolTipText("Quit SecresCSV");
		exitMenuItem.addActionListener(e -> {
			System.exit(0);
		});
		fileMenu.add(openMenuItem);
		fileMenu.add(closeMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(printMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);

		editMenu = new JMenu("Edit");
		selectAllMenuItem = new JMenuItem("Select All");
		selectAllMenuItem.setToolTipText("Select all cells");
		selectAllMenuItem.addActionListener(e -> {
			selectAll();
		});
		selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		refreshMenuItem = new JMenuItem("Refresh");
		refreshMenuItem.setToolTipText("Refresh table data");
		refreshMenuItem.addActionListener(e -> {
			refresh();
		});
		refreshMenuItem.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		editMenu.add(selectAllMenuItem);
		editMenu.add(refreshMenuItem);

		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About SecresCSV");
		aboutMenuItem.setToolTipText("About the app");
		JPanel aboutPanel = createAboutPanel();
		aboutMenuItem.addActionListener(e -> {
			JOptionPane.showInternalMessageDialog(this, aboutPanel, "About SecresCSV", JOptionPane.PLAIN_MESSAGE);
		});
		helpMenu.add(aboutMenuItem);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
	}

	private void createToolBar() {
		openButton = new JButton(new FlatSVGIcon("open.svg"));
		openButton.setFocusable(false);
		openButton.setToolTipText("Open");
		openButton.addActionListener(e -> {
			openDialog();
		});

		saveButton = new JButton(new FlatSVGIcon("save.svg"));
		saveButton.setFocusable(false);
		saveButton.setToolTipText("Save");
		saveButton.addActionListener(e -> {
			save();
		});

		printButton = new JButton(new FlatSVGIcon("print.svg"));
		printButton.setFocusable(false);
		printButton.setToolTipText("Print");
		printButton.addActionListener(e -> {
			print();
		});

		selectAllButton = new JButton();
		selectAllButton.setIcon(new FlatSVGIcon("select_all.svg"));
		selectAllButton.setFocusable(false);
		selectAllButton.setToolTipText("Select All");
		selectAllButton.addActionListener(e -> {
			selectAll();
		});

		refreshButton = new JButton();
		refreshButton.setIcon(new FlatSVGIcon("refresh.svg"));
		refreshButton.setFocusable(false);
		refreshButton.setToolTipText("Refresh");
		refreshButton.addActionListener(e -> {
			refresh();
		});

		toolBar.add(openButton);
		toolBar.add(saveButton);
		toolBar.add(printButton);
		toolBar.add(selectAllButton);
		toolBar.add(refreshButton);

		toolBar.addSeparator();
		toolBar.add(Box.createHorizontalStrut(8)); // space between separator and busy label
		
		busyLabel = new JXBusyLabel(new Dimension(18, 18)); // dimensions of icons to keep scaled
		busyLabel.setVisible(false);
		toolBar.add(busyLabel);
	}

	static JPanel createAboutPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		URL imageResource = CSVView.class.getResource("/icons8-slider-1024.png"); // URL: https://cdn.pixabay.com/photo/2012/05/04/10/57/gear-47203_1280.png
		BufferedImage img = toBufferedImage(new ImageIcon(imageResource).getImage());
		JLabel icon = new JLabel();
		icon.setIcon(new ImageIcon(img));
		Image dimg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
		icon.setIcon(new ImageIcon(dimg));
		JPanel imgPanel = new JPanel();
		imgPanel.add(icon);
		mainPanel.add(imgPanel);

		JPanel namePanel = new JPanel();
		JXHyperlink nameLink = new JXHyperlink();
		nameLink.setText("SecresCSV");
		nameLink.setToolTipText("<html>SecresCSV<br>https://github.com/PranavAmarnath/SecresCSV</html>");
		nameLink.addActionListener(e -> {
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(new URI("https://github.com/PranavAmarnath/SecresCSV"));
					nameLink.setClicked(true);
					nameLink.setClickedColor(new Color(70, 39, 89)); // purple
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		namePanel.add(nameLink, SwingConstants.CENTER);
		JPanel versionPanel = new JPanel();
		JLabel versionLabel = new JLabel("Version 2.0", SwingConstants.CENTER);
		versionLabel.setForeground(new Color(150, 150, 150));
		versionLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
		versionPanel.add(versionLabel);
		JPanel copyrightPanel = new JPanel();
		JLabel copyrightLabel = new JLabel("<html>Copyright \u00a9 2021 Pranav Amarnath<br><div style='text-align: center;'>All Rights Reserved.</div><br><div style='text-align: center;'>\"Slider Icon\" Provided By Icons8.</div></html>", SwingConstants.CENTER);
		copyrightLabel.setForeground(new Color(150, 150, 150));
		copyrightLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
		copyrightPanel.add(copyrightLabel);

		JPanel productPanel = new JPanel();
		productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.PAGE_AXIS));
		productPanel.add(namePanel);
		productPanel.add(versionPanel);
		productPanel.add(copyrightPanel);
		mainPanel.add(productPanel, BorderLayout.SOUTH);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return mainPanel;
	}

	/**
	 * Converts a given Image into a BufferedImage
	 * 
	 * @param img The Image to be converted
	 * @return The converted <code>BufferedImage</code>
	 */
	private static BufferedImage toBufferedImage(Image img) {
		/** Reference: @see https://stackoverflow.com/a/13605411 */
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
	
	class TablePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3961573416357564849L;
		private JTable table = new JTable();
		private RowNumberTable rowTable;
		private JScrollPane scrollPane;

		public TablePanel() {
			setLayout(new BorderLayout());

			table.setShowGrid(true);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			try {
				table.setAutoCreateRowSorter(true);
			} catch (Exception e) { /* Move on (i.e. ignore sorting if exception occurs) */ }
			table.setCellSelectionEnabled(true);
			
			new TableColumnManager(table);

			scrollPane = new JScrollPane(table);
			rowTable = new RowNumberTable(table);
			rowTable.setShowGrid(true);
			scrollPane.setRowHeaderView(rowTable);
			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());

			add(scrollPane);
		}

		public JTable getTable() {
			return table;
		}
		
		public RowNumberTable getRowTable() {
			return rowTable;
		}
	}

	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}
	
	public JXBusyLabel getBusyLabel() {
		return busyLabel;
	}
	
	/**
	 * The <code>Model</code> class defines all I/O from the CSV files.
	 * <P>
	 * Each instance of <code>Model</code> spawns a new {@link SwingWorker} for adding each new row to the current <code>JTable</code>'s <code>TableModel</code>
	 * <P>
	 * 
	 * The class also manages exporting table data to a CSV file.
	 * 
	 * @author Pranav Amarnath
	 *
	 */
	class Model {

	    /** Table model */
	    private DefaultTableModel model;
	    /** Table header */
	    private Object[] header;
	    //private List<String[]> myEntries = new ArrayList<>();
	    /** OpenCSV parser */
	    private CSVReader reader;
	    /** Current line */
	    private String[] line;
	    
	    /**
	     * Model constructor to load CSV data
	     * @param path  Path to file
	     * @param table  The table
	     * @param refresh  If the user is refreshing (true) or if it's the first load (false)
	     */
	    public Model(File path, JTable table, boolean refresh, boolean isLastFile) {
	        createBusyLabel();
	        class Worker extends SwingWorker<Void, String> {
	            @Override
	            protected Void doInBackground() {
	                try {
	                    reader = new CSVReader(new FileReader(path));
	                } catch (FileNotFoundException e1) {
	                    showError("File Not Found :(", e1);
	                }
	                try {
	                    header = (String[]) reader.readNext();
	                } catch (CsvValidationException e1) {
	                    showError("CSV Not Validated :(", e1);
	                } catch (IOException e1) {
	                    showError("I/O Exception :(", e1);
	                }
	                //SwingUtilities.invokeAndWait(() -> model = new DefaultTableModel(header, 0)); // NOT invokeLater() because model HAS to be initialized immediately on EDT
	                model = new DefaultTableModel(header, 0);
	                table.setModel(model);
	                try {
	                    while((line = reader.readNext()) != null) {
	                        model.addRow(line);
	                    }
	                } catch(Exception e) {
	                    showError("An Exception Occurred :(", e);
	                }
	                return null;
	            }
	            @Override
	            protected void done() {
	                try {
	                    if(isLastFile) {
	                        removeBusyLabel(); // remove the busy label only after the last file (largest file) is over
	                    }
	                    if(refresh == true) {
	                        table.requestFocus();
	                        ((TablePanel) tabbedPane.getSelectedComponent()).getTable().setModel(model);
	                        JOptionPane.showInternalMessageDialog(getThis(), "Refreshed data.");
	                    }
	                    else {
	                        JOptionPane.showInternalMessageDialog(getThis(), "Finished loading " + path.getName());
	                    }
	                    reader.close();
	                } catch (IOException e) {
	                    showError("I/O Exception :(", e);
	                }
	            }
	        };
	        Worker worker = new Worker();
	        worker.execute();
	    }
	    
	    /**
	     * Executes {@link #exportToCSV(String, JTable)} on a SwingWorker after creating the busy label
	     * @param path  The path to export to
	     * @param table  The table to export
	     * @see #save(String, JTable)
	     */
	    void save(String path, JTable table) {
	        createBusyLabel();
	        new SwingWorker<Void, String>() {
	            @Override
	            protected Void doInBackground() {
	                exportToCSV(path, table);
	                return null;
	            }
	            @Override
	            protected void done() {
	                removeBusyLabel();
	                int index = path.lastIndexOf('\\');
	                String fileName = path.substring(index + 1, path.length());
	                JOptionPane.showInternalMessageDialog(getThis(), "Finished saving " + fileName);
	            }
	        }.execute();
	    }
	    
	    /**
	     * Export table data to same path of CSV file.
	     * @param pathToExportTo  The path to export to
	     * @param tableToExport  The table to export
	     */
	    private void exportToCSV(String pathToExportTo, JTable tableToExport) {
	        try {
	            TableModel model = tableToExport.getModel();
	            FileWriter csv = new FileWriter(new File(pathToExportTo));

	            for(int i = 0; i < model.getColumnCount(); i++) {
	                if(i != model.getColumnCount() - 1) {
	                    csv.write(model.getColumnName(i) + ",");
	                }
	                else {
	                    csv.write(model.getColumnName(i));
	                }
	            }

	            csv.write("\n");

	            for(int i = 0; i < model.getRowCount(); i++) {
	                for(int j = 0; j < model.getColumnCount(); j++) {
	                    if(j != model.getColumnCount() - 1) {
	                        csv.write(model.getValueAt(i, j).toString() + ",");
	                    }
	                    else {
	                        csv.write(model.getValueAt(i, j).toString());
	                    }
	                }
	                csv.write("\n");
	            }

	            csv.close();
	        } catch (IOException e) {
	            showError("I/O Exception :(", e);
	        }
	    }
	    
	    /**
	     * A method to show an error in a <code>JOptionPane</code>.
	     * @param title  Title of the dialog
	     * @param e  The Exception
	     */
	    private void showError(String title, Exception e) {
	        JTextPane textPane = new JTextPane();
	        textPane.setText(e.getMessage());
	        JOptionPane.showInternalMessageDialog(getThis(), textPane, title, JOptionPane.ERROR_MESSAGE);
	    }
	    
	    /**
	     * Displays the busy label.
	     */
	    private void createBusyLabel() {
	        if(Taskbar.getTaskbar().isSupported(Taskbar.Feature.PROGRESS_STATE_WINDOW)) {
	            Taskbar.getTaskbar().setWindowProgressState(MainView.getFrame(), State.INDETERMINATE);
	        }
	        busyLabel.setBusy(true);
	        busyLabel.setVisible(true);
	    }
	    
	    /**
	     * Removes the busy label.
	     */
	    private void removeBusyLabel() {
	        if(Taskbar.getTaskbar().isSupported(Taskbar.Feature.PROGRESS_STATE_WINDOW)) {
	            Taskbar.getTaskbar().setWindowProgressState(MainView.getFrame(), State.OFF);
	        }
	        busyLabel.setBusy(false);
	        busyLabel.setVisible(false);
	    }
	    
	    /**
	     * Returns table model
	     * @return <code>DefaultTableModel</code> - table model
	     */
	    public DefaultTableModel getModel() {
	        return model;
	    }
	    
	    /**
	     * Returns table header
	     * @return <code>Object[]</code> - header
	     */
	    public Object[] getHeaders() {
	        return header;
	    }
	    
	}
	
	private JInternalFrame getThis() {
	    return this;
	}

}
