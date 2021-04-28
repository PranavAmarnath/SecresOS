package com.secres.secresos;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.formdev.flatlaf.FlatLightLaf;
import com.secres.filebro.FileBrowserFrame;
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
        secresMenu.add(csvItem);
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
