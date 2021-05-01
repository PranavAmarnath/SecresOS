package com.secres.secresos;

import java.awt.Desktop;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

public class Main {

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "SecresOS");
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("apple.awt.antialiasing", "true");
        System.setProperty("apple.awt.textantialiasing", "true");
        System.setProperty("flatlaf.useWindowDecorations", "false");

        if(System.getProperty("os.name").toString().contains("Mac")) {
            try {
                SwingUtilities.invokeLater(() -> {
                    Desktop desktop = Desktop.getDesktop();

                    desktop.setAboutHandler(e -> {
                        JOptionPane.showMessageDialog(MainView.getFrame(), "About", "About SecresOS", JOptionPane.PLAIN_MESSAGE);
                    });
                    desktop.setPreferencesHandler(e -> {
                        JOptionPane.showMessageDialog(MainView.getFrame(), "Preferences", "Preferences", JOptionPane.INFORMATION_MESSAGE);
                    });
                    desktop.setQuitHandler((e, r) -> {
                        System.exit(0);
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FlatLightLaf.install();

        SwingUtilities.invokeLater(() -> {
            new MainView();
        });
    }

}
