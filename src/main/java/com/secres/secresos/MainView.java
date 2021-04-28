package com.secres.secresos;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.secres.secrescsv_lib.CSVView;

public class MainView {

    private static JFrame frame;
    private static JInternalFrame csvFrame;
    
    public MainView() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("SecresOS");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu appsMenu = new JMenu("Applications");
        JMenuItem csvItem = new JMenuItem("SecresCSV");
        appsMenu.add(csvItem);
        menuBar.add(appsMenu);
        
        JDesktopPane desktopPane = new JDesktopPane();

        csvItem.addActionListener(e -> {
            csvFrame = new CSVView();
            csvFrame.setClosable(true);
            csvFrame.setMaximizable(true);
            csvFrame.setResizable(true);
            csvFrame.setIconifiable(true);
            csvFrame.toFront();
            desktopPane.add(csvFrame);
        });
        
        frame.add(desktopPane);
        frame.setJMenuBar(menuBar);
        
        frame.pack();
        frame.setVisible(true);
    }

    public static JFrame getFrame() {
        return frame;
    }
    
    public static JInternalFrame getCSVFrame() {
        return csvFrame;
    }

}
