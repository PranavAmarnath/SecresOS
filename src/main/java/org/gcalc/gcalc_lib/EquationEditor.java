package org.gcalc.gcalc_lib;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Sidebar equation editor for an individual equation. Signals to listeners (typically the Sidebar) when the expression contained within is updated, or the editor deleted.
 */
public class EquationEditor extends JPanel implements AncestorListener, ActionListener {
    private int id, width;
    private boolean idSet = false;

    private JLabel title;
    private JPanel titleRow, buttonRow;
    private JButton deleteBtn;
    private JTextField editor;

    private Color editorNormalColor;

    private ArrayList<EquationEditorListener> listeners = new ArrayList<>();

    private Equation equation = new Equation("");

    /**
     * Creates a new EquationEditor, which functions as a Swing component. The id should be sequential and unique, as it is what is passed to callback interface methods.
     *
     * @param id The unique equation id
     */
    public EquationEditor(int id) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // Create editor title
        this.titleRow = new JPanel();
        this.titleRow.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.title = new JLabel();
        this.titleRow.add(this.title);
        this.add(this.titleRow);

        // Create expression editor
        this.editor = new JTextField();
        this.editor.setFont(new Font("monospaced", Font.PLAIN, 16));
        this.editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                EquationEditor.this.equationChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                EquationEditor.this.equationChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                EquationEditor.this.equationChanged();
            }
        });
        this.add(this.editor);

        // Move focus to new editor field so the user doesn't have to click it
        // NOTE: this is performed as a callback, otherwise it doesn't work
        this.editor.addAncestorListener(this);

        // Line up buttons on bottom row
        this.buttonRow = new JPanel();
        this.buttonRow.setLayout(new FlowLayout(FlowLayout.RIGHT));

        // Create button to remove the expression
        this.deleteBtn = new JButton("Delete");
        this.deleteBtn.addActionListener(this);
        this.buttonRow.add(this.deleteBtn);

        this.add(this.buttonRow);

        this.setID(id);
    }

    /**
     * Called when the equation editor is first added by Swing internally to its container component. This callback steals the keyboard focus and gives it to the JTextField contained within this equation editor. This means that the user does not have to click on the text field themselves after creating a new equation.
     *
     * @param ancestorEvent The event supplied to us by Swing
     */
    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent) {
        final AncestorListener a = this;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JComponent c = ancestorEvent.getComponent();
                c.requestFocusInWindow();
                c.removeAncestorListener(a);
            }
        });
    }

    @Override
    public void ancestorRemoved(AncestorEvent ancestorEvent) {
    }

    @Override
    public void ancestorMoved(AncestorEvent ancestorEvent) {
    }

    /**
     * Listens for click events on any child components of the equation editor.
     *
     * @param actionEvent The event supplied by Swing, used to determine which component was clicked
     */
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        if(source.equals(this.deleteBtn)) {
            this.delete();
        }
    }

    /**
     * This is usually called when an EquationEditor has been deleted. It updates the editor's colour and title.
     *
     * @param newID The new ID to use
     */
    public void setID(int newID) {
        if(this.idSet) {
            if(this.id % 2 == 1 && newID % 2 == 0) {
                this.lightenAllComponents();
                this.lightenAllComponents();
            }
            else if(this.id % 2 == 0 && newID % 2 == 1) {
                this.darkenAllComponents();
                this.darkenAllComponents();
            }
        }
        else {
            if(newID % 2 == 0) {
                this.lightenAllComponents();
            }
            else {
                this.darkenAllComponents();
            }
        }

        this.editorNormalColor = this.editor.getBackground();

        this.id = newID;
        this.idSet = true;

        this.title.setText("Expression " + Integer.toString(newID + 1));
        this.title.setForeground(Graph.lineColours[newID % Graph.lineColours.length]);

        this.repaint();
    }

    /**
     * Gets the ID of the equation contained within this editor.
     *
     * @return The aforementioned ID
     */
    public int getID() {
        return this.id;
    }

    /**
     * Sets the width of the equation editor. Usually called when the parent is resized.
     *
     * @param width The new maximum width of the editor
     */
    public void setWidth(int width) {
        this.width = width;
        this.setPreferredSize(new Dimension(width, 92));
        this.setMaximumSize(new Dimension(width, 92));
        this.editor.setMaximumSize(new Dimension(width - 10, 30));

        this.revalidate();
        this.repaint();
    }

    /**
     * Adds a new target for edit and removal event callbacks.
     *
     * @param listener The new event listener instance
     */
    public void addEquationEditorListener(EquationEditorListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Called when the equation is initially modified, as we automatically assume that the new equation will be valid. Sets the JTextField's background colour to it's normal value.
     */
    public void setValid() {
        this.editor.setBackground(this.editorNormalColor);
    }

    /**
     * Called when the Graph attempts to evaluate the equation, if it finds the equation to be invalid.
     */
    public void setInvalid() {
        this.editor.setBackground(new Color(228, 48, 0));
    }

    /**
     * Retrieves the EquationEditor's Equation
     *
     * @return The equation representing the EquationEditor's contents
     */
    public Equation getEquation() {
        return this.equation;
    }

    /**
     * Triggers the removal of the equation from the UI, and the callback of the `equationRemoved` callback.
     */
    public void delete() {
        for(EquationEditorListener l : this.listeners) {
            l.equationRemoved(this.id);
        }
    }

    /**
     * Triggers processing of a new equation when the equation field is modified.
     */
    protected void equationChanged() {
        try {
            this.equation = new Equation(this.editor.getText());
        } catch (Exception e) {
            // Something is very wrong with the equation if we can't even get
            // this far. Bail here
            this.setInvalid();
            return;
        }

        this.setValid();

        for(EquationEditorListener l : this.listeners) {
            l.equationEdited(this.id, this.equation);
        }
    }

    /**
     * Convenience function to darken the background colours of all child components.
     */
    private void darkenAllComponents() {
        darkenComponent(this);
        darkenComponent(this.titleRow);
        darkenComponent(this.editor);
        darkenComponent(this.buttonRow);
        darkenComponent(this.deleteBtn);
    }

    /**
     * Convenience function to lighten the background colours of all child components.
     */
    private void lightenAllComponents() {
        lightenComponent(this);
        lightenComponent(this.titleRow);
        lightenComponent(this.editor);
        lightenComponent(this.buttonRow);
        lightenComponent(this.deleteBtn);
    }

    /**
     * Lowers the background RGB values by 5.
     *
     * @param c The component to darken
     */
    private void darkenComponent(Component c) {
        this.hsvDecrease(c, 5);
    }

    /**
     * Lowers the background's RGB values by 5.
     *
     * @param c The component to lighten
     */
    private void lightenComponent(Component c) {
        this.hsvDecrease(c, 5);
    }

    /**
     * Retrieves a component's current colour, then decreases each colour channel by amount.
     *
     * @param c      The component to adjust
     * @param amount The amount to alter the colour brightness by
     */
    private void hsvDecrease(Component c, int amount) {
        Color origColour = c.getBackground();
        //System.out.println(origColour.getRed() + " " + origColour.getGreen() + " " + origColour.getBlue());
        c.setBackground(new Color(origColour.getRed() - amount, origColour.getGreen() - amount, origColour.getBlue() - amount));
    }
}
