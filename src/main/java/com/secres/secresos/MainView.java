package com.secres.secresos;

import java.awt.BorderLayout;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import com.secres.filebro.FileBrowserFrame;
import com.secres.secrescsv_lib.CSVFrame;

public class MainView {

    private static JFrame frame;
    private JToolBar docker;

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
        JMenuItem fileItem = new JMenuItem("FileBro");
        appsMenu.add(csvItem);
        appsMenu.add(fileItem);
        menuBar.add(appsMenu);

        JDesktopPane desktopPane = new JDesktopPane();

        csvItem.addActionListener(e -> {
            JInternalFrame csvFrame = new CSVFrame("SecresCSV", docker);
            csvFrame.setClosable(true);
            csvFrame.setMaximizable(true);
            csvFrame.setResizable(true);
            csvFrame.setIconifiable(true);
            csvFrame.toFront();
            desktopPane.add(csvFrame);
        });

        fileItem.addActionListener(e -> {
            JInternalFrame fileFrame = new FileBrowserFrame("FileBro", docker);
            fileFrame.setClosable(true);
            fileFrame.setMaximizable(true);
            fileFrame.setResizable(true);
            fileFrame.setIconifiable(true);
            fileFrame.toFront();
            desktopPane.add(fileFrame);
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
