package com.secres.secresos;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

public class PreferencesFrame extends DockableFrame {

    private static final long serialVersionUID = 6857310626223453955L;

    public PreferencesFrame(String title, JToolBar jtb) {
        super(title, jtb);
        createAndShowGUI();
    }
    
    private void createAndShowGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel imagePanel = new JPanel(new GridLayout(4, 4));
        int buttonIndex = 0;
        
        ButtonGroup buttonGroup = new ButtonGroup();
        if(MainView.imageButtons == null) {
            MainView.imageButtons = new JToggleButton[10];
            MainView.imageButtons[0] = new JToggleButton(createImage("/bg/default_bg.PNG"));
            MainView.imageButtons[0].setActionCommand("/bg/default_bg.PNG");
            MainView.imageButtons[1] = new JToggleButton(createImage("/bg/gradient.jpg"));
            MainView.imageButtons[1].setActionCommand("/bg/gradient.jpg");
            MainView.imageButtons[2] = new JToggleButton(createImage("/bg/10-9--thumb.jpg"));
            MainView.imageButtons[2].setActionCommand("/bg/10-9--thumb.jpg");
            MainView.imageButtons[3] = new JToggleButton(createImage("/bg/10-10--thumb.jpg"));
            MainView.imageButtons[3].setActionCommand("/bg/10-10--thumb.jpg");
            MainView.imageButtons[4] = new JToggleButton(createImage("/bg/10-11--thumb.jpg"));
            MainView.imageButtons[4].setActionCommand("/bg/10-11--thumb.jpg");
            MainView.imageButtons[5] = new JToggleButton(createImage("/bg/10-12--thumb.jpg"));
            MainView.imageButtons[5].setActionCommand("/bg/10-12--thumb.jpg");
            MainView.imageButtons[6] = new JToggleButton(createImage("/bg/10-13--thumb.jpg"));
            MainView.imageButtons[6].setActionCommand("/bg/10-13--thumb.jpg");
            MainView.imageButtons[7] = new JToggleButton(createImage("/bg/10-14-Day-Thumb.jpg"));
            MainView.imageButtons[7].setActionCommand("/bg/10-14-Day-Thumb.jpg");
            MainView.imageButtons[8] = new JToggleButton(createImage("/bg/10-14-Night-Thumb.jpg"));
            MainView.imageButtons[8].setActionCommand("/bg/10-14-Night-Thumb.jpg");
            MainView.imageButtons[9] = new JToggleButton(createImage("/bg/recreate-macos-big-sur.jpeg"));
            MainView.imageButtons[9].setActionCommand("/bg/recreate-macos-big-sur.jpeg");
        }
        
        for(JToggleButton btn : MainView.imageButtons) {
            buttonGroup.add(btn);
            imagePanel.add(btn);
        }
        
        if(MainView.selectedButton == null) {
            MainView.selectedButton = MainView.imageButtons[0];
        }
        else {
            ArrayList<JToggleButton> imageButtonsList = new ArrayList<JToggleButton>(Arrays.asList(MainView.imageButtons));
            buttonIndex = imageButtonsList.indexOf(MainView.selectedButton);
            MainView.imageButtons[buttonIndex].setSelected(true);
        }
        
        mainPanel.add(imagePanel);
        JPanel applyPanel = new JPanel();
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            for(Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
                AbstractButton button = buttons.nextElement();

                if(button.isSelected()) {
                    MainView.currentBG = new ImageIcon(getClass().getResource(button.getActionCommand()));
                    MainView.selectedButton = (JToggleButton) button;
                    MainView.getDesktop().getGraphics().drawImage(new ImageIcon(getClass().getResource(button.getActionCommand())).getImage(), 0, 0, MainView.getDesktop().getWidth(), MainView.getDesktop().getHeight(), MainView.getDesktop());
                }
            }
            try {
                setIcon(true);
                setIcon(false);
            } catch (PropertyVetoException e1) {
                e1.printStackTrace();
            }
        });
        applyPanel.add(applyButton);
        mainPanel.add(applyPanel, BorderLayout.SOUTH);
        add(mainPanel);
        pack();
        setVisible(true);

        final int i = buttonIndex;
        SwingUtilities.invokeLater(() -> {
            MainView.imageButtons[i].requestFocusInWindow();
        });
    }

    private Icon createImage(String PATH) {
        URL imageResource = getClass().getResource(PATH);
        BufferedImage img = toBufferedImage(new ImageIcon(imageResource).getImage());
        Image dimg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        return new ImageIcon(dimg);
    }
    
    /**
     * Converts a given Image into a BufferedImage
     * 
     * @param img The Image to be converted
     * @return The converted <code>BufferedImage</code>
     */
    private BufferedImage toBufferedImage(Image img) {
        /** Reference: @see https://stackoverflow.com/a/13605411 */
        if(img instanceof BufferedImage) {
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
    
}
