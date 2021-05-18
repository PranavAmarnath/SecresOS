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

import org.gcalc.gcalc_lib.GraphWindow;
import org.pscode.filebro_lib.FileBrowserFrame;
import org.quickterm.quickterm_lib.QuickTerminal;

import com.secres.secresbrowser_lib.Browser;
import com.secres.secrescam_lib.CamFrame;
import com.secres.secrescsv_lib.CSVFrame;
import com.secres.secresmail_lib.MailFrame;

import javafx.application.Platform;

public class MainView {

    private static JFrame frame;
    private static JToolBar docker;
    private static JDesktopPane desktopPane;

    public MainView() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        Platform.setImplicitExit(false); // avoid JavaFX WebView display bug
        
        frame = new JFrame("SecresOS") {
            private static final long serialVersionUID = 1562453637101862279L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 600);
            }
        };
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        desktopPane = new JDesktopPane();

        JMenuBar menuBar = new JMenuBar();

        JMenu appsMenu = new JMenu("Applications");
        JMenu secresMenu = new JMenu("Secres");
        JMenuItem csvItem = new JMenuItem("SecresCSV");
        JMenuItem browserItem = new JMenuItem("SecresBrowser");
        JMenuItem mailItem = new JMenuItem("SecresMail");
        JMenuItem camItem = new JMenuItem("SecresCam");
        secresMenu.add(csvItem);
        secresMenu.add(browserItem);
        secresMenu.add(mailItem);
        secresMenu.add(camItem);
        JMenuItem fileItem = new JMenuItem("FileBro");
        JMenuItem calcItem = new JMenuItem("GCalc");
        JMenuItem termItem = new JMenuItem("QuickTerm");
        appsMenu.add(secresMenu);
        appsMenu.add(fileItem);
        appsMenu.add(calcItem);
        appsMenu.add(termItem);

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

        mailItem.addActionListener(e -> {
            JInternalFrame mailFrame = new MailFrame("SecresMail", docker);
            mailFrame.setClosable(true);
            mailFrame.setMaximizable(true);
            mailFrame.setResizable(true);
            mailFrame.setIconifiable(true);
            desktopPane.add(mailFrame);
        });
        
        camItem.addActionListener(e -> {
            JInternalFrame camFrame = new CamFrame("SecresCam", docker);
            camFrame.setClosable(true);
            camFrame.setMaximizable(true);
            camFrame.setResizable(true);
            camFrame.setIconifiable(true);
            desktopPane.add(camFrame);
        });

        calcItem.addActionListener(e -> {
            JInternalFrame calcFrame = null;
            try {
                calcFrame = new GraphWindow("GCalc", docker);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            calcFrame.setClosable(true);
            calcFrame.setMaximizable(true);
            calcFrame.setResizable(true);
            calcFrame.setIconifiable(true);
            desktopPane.add(calcFrame);
        });
        
        termItem.addActionListener(e -> {
            JInternalFrame termFrame = new QuickTerminal("QuickTerm", docker);
            termFrame.setClosable(true);
            termFrame.setMaximizable(true);
            termFrame.setResizable(true);
            termFrame.setIconifiable(true);
            desktopPane.add(termFrame);
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

    public static JDesktopPane getDesktop() {
        return desktopPane;
    }
    
    public static JDesktopPane getDocker() {
        return desktopPane;
    }

}
