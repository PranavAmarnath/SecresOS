package com.secres.secresos;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;

import java.awt.event.ActionEvent;

public class DockableFrame extends JInternalFrame {

    private static final long serialVersionUID = -1918774150281023704L;
    private JButton btnDock;
    private JToolBar jtb;

    public DockableFrame(String title, JToolBar jtb) {
        this.jtb = jtb;

        btnDock = new JButton(title);
        btnDock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                toggleState();
            }
        });

        jtb.add(btnDock);
        this.setTitle(title);
    }

    @Override
    public void dispose() {
        super.dispose();
        jtb.remove(btnDock);
        jtb.repaint();
    }

    private void toggleState() {
        try {
            if(this.isIcon()) {
                this.setIcon(false);
            }
            else {
                if(this.isSelected()) this.setIcon(true);
                else this.setSelected(true);
            }
            this.setSelected(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
