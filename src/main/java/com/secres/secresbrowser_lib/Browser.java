package com.secres.secresbrowser_lib;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.secres.secresos.DockableFrame;

public class Browser extends DockableFrame {

    private static final long serialVersionUID = 5367507840538341971L;

    public Browser(String title, JToolBar jtb) {
        super(title, jtb);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(1); // scrolling tabs
        tabbedPane.putClientProperty("JTabbedPane.tabClosable", true);
        tabbedPane.putClientProperty("JTabbedPane.showTabSeparators", true);
        tabbedPane.putClientProperty("JTabbedPane.tabCloseCallback", (BiConsumer<JTabbedPane, Integer>) (tabPane, tabIndex) -> {
            // close tab here
            tabbedPane.removeTabAt(tabIndex);
        });
        tabbedPane.putClientProperty("JTabbedPane.tabCloseToolTipText", "Close");

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane);

        KeyStroke newKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK);
        Action newAction = new AbstractAction() {
            private static final long serialVersionUID = 7584979580376385292L;

            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.addTab("New Tab", new SinglePage().getBox());
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            }
        };
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(newKeyStroke, "NewTab");
        this.getRootPane().getActionMap().put("NewTab", newAction);

        KeyStroke closeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK);
        Action closeAction = new AbstractAction() {
            private static final long serialVersionUID = 5640141805009897072L;

            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
            }
        };
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(closeKeyStroke, "CloseTab");
        this.getRootPane().getActionMap().put("CloseTab", closeAction);

        tabbedPane.addTab("New Tab", new SinglePage().getBox());

        add(mainPanel);

        pack();
        setVisible(true);
    }

}
