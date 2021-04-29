package com.secres.secresos;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import com.secres.filebro.FileBrowserFrame;
import com.secres.secresbrowser_lib.Browser;
import com.secres.secrescsv_lib.CSVFrame;

public class MainView {

    private static JFrame frame;
    private JToolBar docker;

    public MainView() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("SecresOS") {
            private static final long serialVersionUID = 1562453637101862279L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 600);
            }
        };
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JDesktopPane desktopPane = new JDesktopPane();

        JMenuBar menuBar = new JMenuBar();

        JMenu appsMenu = new JMenu("Applications");
        JMenu secresMenu = new JMenu("Secres");
        JMenuItem csvItem = new JMenuItem("SecresCSV");
        JMenuItem browserItem = new JMenuItem("SecresBrowser");
        secresMenu.add(csvItem);
        secresMenu.add(browserItem);
        JMenuItem fileItem = new JMenuItem("FileBro");
        appsMenu.add(secresMenu);
        appsMenu.add(fileItem);

        menuBar.add(appsMenu);

        csvItem.addActionListener(e -> {
            JInternalFrame csvFrame = new CSVFrame("SecresCSV", docker);
            csvFrame.setClosable(true);
            csvFrame.setMaximizable(true);
            csvFrame.setResizable(true);
            csvFrame.setIconifiable(true);
            desktopPane.add(csvFrame);
        });

        fileItem.addActionListener(e -> {
            JInternalFrame fileFrame = new FileBrowserFrame("FileBro", docker);
            fileFrame.setClosable(true);
            fileFrame.setMaximizable(true);
            fileFrame.setResizable(true);
            fileFrame.setIconifiable(true);
            desktopPane.add(fileFrame);
        });
        
        browserItem.addActionListener(e -> {
            JInternalFrame browserFrame = new Browser("SecresBrowser", docker);
            browserFrame.setClosable(true);
            browserFrame.setMaximizable(true);
            browserFrame.setResizable(true);
            browserFrame.setIconifiable(true);
            desktopPane.add(browserFrame);
        });

        frame.add(desktopPane);
        frame.setJMenuBar(menuBar);

        docker = new JToolBar();
        frame.add(docker, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    public static JFrame getFrame() {
        return frame;
    }

}
