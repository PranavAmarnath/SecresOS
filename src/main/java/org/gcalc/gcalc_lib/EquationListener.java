package org.gcalc.gcalc_lib;

/**
 * Contains event triggers for any equation in a list of equations is modified in any way. Designed as an efficient interface between the Sidebar and a Graph, but in reality, can be used as an interface between any list of equations and a Graph.
 */
public interface EquationListener {
    /**
     * Signals that a new equation has been created. A reference to the new equation is supplied.
     *
     * @param id          The array index of the new equation
     * @param newEquation The newly added equation
     * @param editor      The editor used to modify the equation
     */
    void equationAdded(int id, Equation newEquation, EquationEditor editor);

    /**
     * Called when one or more equations have been removed.
     *
     * @param id The array index of the removed equation
     */
    void equationRemoved(int id);

    /**
     * Called when a pre-existing equation has been modified. Note that the new equation instance may be the same instance as the previous instance.
     *
     * @param id The array index of the modified equation
     * @param e  The equation object to replace the old one with
     */
    void equationChanged(int id, Equation e);
}
