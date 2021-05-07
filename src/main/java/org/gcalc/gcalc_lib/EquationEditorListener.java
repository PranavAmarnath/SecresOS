package org.gcalc.gcalc_lib;

/**
 * Used to receive raw events from an equation editor. Usually, using an `EquationListener` is preferable, as it is a higher-level interface.
 */
public interface EquationEditorListener {
    /**
     * Signals to a listener that an EquationEditor's equation field has been modified.
     *
     * @param id       The id of the EquationEditor
     * @param equation The string contents of the EquationEditor's JTextField
     */
    void equationEdited(int id, Equation equation);

    /**
     * Signals to a listener that an EquationEditor's delete button has been pushed, or the EquationEditor's delete() method has been called.
     *
     * @param id The id of the removed EquationEditor
     */
    void equationRemoved(int id);
}
