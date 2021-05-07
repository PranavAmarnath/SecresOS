package org.gcalc.gcalc_lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

/**
 * Contains a list of `EquationEditors`. Manages the creation, destruction and forward of events from each equation editor. Ensures that ids are sequential, unique, and updated when editors are created or deleted.
 *
 * `EquationListeners` can attach to this to receive high-level events when any of the contained equations are updated.
 */
public class Sidebar extends JScrollPane implements ComponentListener, EquationEditorListener {
    private ArrayList<EquationListener> listeners = new ArrayList<>();
    private ArrayList<EquationEditor> editors = new ArrayList<>();

    int width, height;

    JPanel container;

    /**
     * Creates a new sidebar component which acts like a Swing component.
     *
     * @param width  The initial width to use
     * @param height The initial height to use
     */
    public Sidebar(int width, int height) {
        super(new JPanel());
        this.setPreferredSize(new Dimension(width, height));
        this.getVerticalScrollBar().setUnitIncrement(16);
        this.width = width;
        this.height = height;

        // Create container to hold equation editors
        this.container = (JPanel) this.getViewport().getView();
        this.container.setLayout(new BoxLayout(this.container, BoxLayout.PAGE_AXIS));
        container.setMaximumSize(new Dimension(width - 10, Integer.MAX_VALUE));

        // Create initial equation editor
        this.newEquation();

        this.container.addComponentListener(this);
        this.addComponentListener(this);
    }

    /**
     * Since some layouts are more stubborn, we have to be explicit.
     *
     * @return The size we actually want to be
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.width, this.height);
    }

    /**
     * Resize all equation editors when we are resized.
     *
     * @param componentEvent The event passed by Swing; unused
     */
    public void componentResized(ComponentEvent componentEvent) {
        Dimension size = this.getViewport().getSize();
        this.container.setMaximumSize(new Dimension(size.width, Integer.MAX_VALUE));
        for(EquationEditor e : this.editors) {
            e.setWidth(size.width);
        }
    }

    // Unused component listeners
    public void componentMoved(ComponentEvent componentEvent) {
    }

    public void componentShown(ComponentEvent componentEvent) {
    }

    public void componentHidden(ComponentEvent componentEvent) {
    }

    /**
     * Signals to a listener that an EquationEditor's equation field has been modified.
     *
     * @param id       The id of the EquationEditor
     * @param equation The EquationEditors Equation instance
     */
    public void equationEdited(int id, Equation equation) {
        // Notify listeners that an existing equation has been updated
        for(EquationListener l : this.listeners) {
            l.equationChanged(id, equation);
        }
    }

    /**
     * Signals to a listener that an EquationEditor's delete button has been pushed, or the EquationEditor's delete() method has been called.
     *
     * @param id The id of the removed EquationEditor
     */
    public void equationRemoved(int id) {
        // Remove equation editor fron UI
        this.container.remove(this.editors.get(id));
        this.editors.remove(id);

        // Update IDs of all equation editors
        int newID = 0;
        for(EquationEditor e : this.editors) {
            e.setID(newID);
            newID++;
        }

        // Notify listeners that the equation has been removed
        for(EquationListener l : this.listeners) {
            l.equationRemoved(id);
        }

        // Update width of all equation editors
        int width = this.getViewport().getSize().width;
        for(EquationEditor f : this.editors) {
            f.setWidth(width);
        }

        // Redraw sidebar
        this.revalidate();
        this.repaint();
    }

    /**
     * Triggers the creation of a new, blank equation, and the callback of all EquationListeners once the equation has been created
     */
    public void newEquation() {
        int id = this.editors.size();
        EquationEditor e = new EquationEditor(id);
        e.addEquationEditorListener(this);
        this.container.add(e);
        this.editors.add(e);

        // Update width of all equation editors
        int width = this.getViewport().getSize().width;
        for(EquationEditor f : this.editors) {
            f.setWidth(width);
        }

        // Redraw sidebar
        this.revalidate();
        this.repaint();

        // Notify listeners that a new equation has been added
        for(EquationListener l : this.listeners) {
            l.equationAdded(id, new Equation(""), e);
        }
    }

    /**
     * Removes each equation in sequence.
     */
    public void deleteAllEquations() {
        for(int i = this.editors.size() - 1; i >= 0; i--) {
            this.editors.get(i).delete();
        }
    }

    /**
     * Adds a new listener which will receive events when equations are created or destroyed.
     *
     * @param listener The listener to additionally forward events to
     */
    public void addEquationListener(EquationListener listener) {
        this.listeners.add(listener);

        // Send current list of equations to new listener
        for(EquationEditor e : this.editors) {
            listener.equationAdded(e.getID(), e.getEquation(), e);
        }
    }
}
