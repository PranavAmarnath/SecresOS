package org.gcalc.gcalc_lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Draws equations which are supplied through `EquationListener` events.
 *
 * Currently supports: - Multiple equations - Discontinuous functions - Adding / removing equations - Adjusting zoom level
 *
 * TODO: - Translation (i.e. dragging around to move away from the origin) - Fixing inf -> -inf and vice-versa transitions resulting in a vertical line being drawn. - Evaluation of functions with multiple roots (i.e. handling multiple return values)
 */
public class Graph extends JLabel implements ComponentListener, EquationListener {
    /**
     * List of colours which will be used to draw lines. The colour used loops around to the start again if there are more than lineColours.length lines on the graph.
     */
    public static final Color[] lineColours = { new Color(231, 76, 60), new Color(26, 188, 156), new Color(241, 196, 15), new Color(211, 84, 0), new Color(39, 174, 96), new Color(41, 128, 185), new Color(255, 0, 255) };

    /**
     * The pixel distance between integer values on each axis at 100% zoom.
     */
    protected static final int normInterval = 50;

    private int width, height;
    private BufferedImage img;
    private double scale = 1;
    private ArrayList<Equation> equations = new ArrayList<>();
    private ArrayList<EquationEditor> editors = new ArrayList<>();

    /**
     * Creates a new Graph Swing component, which integrates fully with the Swing layout model. Note that the graph starts out with no lines except for the grid.
     *
     * @param width  The initial width of the graph
     * @param height The initial height of the graph
     */
    public Graph(int width, int height) {
        this.width = width;
        this.height = height;
        this.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        this.setIcon(new ImageIcon(this.img));
        this.addComponentListener(this);
    }

    /**
     * Fixes stubbornness with some layout managers.
     *
     * @return The width and height that we'd _actually_ like to be
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.width, this.height);
    }

    /**
     * Resize the render image and redraw when the component is resized.
     *
     * @param e Unused (supplied by Swing)
     */
    public void componentResized(ComponentEvent e) {
        Dimension size = this.getSize();
        this.width = size.width;
        this.height = size.height;

        this.img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        this.setIcon(new ImageIcon(this.img));

        this.redraw();
    }

    // Unused component listeners
    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    /**
     * Signals that a new equation has been created. A reference to the new equation is supplied.
     *
     * @param id          The array index of the new equation
     * @param newEquation The newly added equation
     * @param editor      The editor used to modify the equation
     */
    public void equationAdded(int id, Equation newEquation, EquationEditor editor) {
        this.equations.add(id, newEquation);
        this.editors.add(id, editor);
        this.redraw();
    }

    /**
     * Called when one or more equations have been removed.
     *
     * @param id The array index of the removed equation
     */
    public void equationRemoved(int id) {
        this.equations.remove(id);
        this.editors.remove(id);
        this.redraw();
    }

    /**
     * Called when a pre-existing equation has been modified. Note that the new equation instance may be the same instance as the previous instance.
     * 
     * @param id The array index of the modified equation
     * @param e  The equation object to replace the old one with
     */
    public void equationChanged(int id, Equation e) {
        this.equations.set(id, e);
        this.redraw();
    }

    /**
     * Zooms in the graph a bit.
     */
    public void increaseScale() {
        this.setScale(this.getScale() * 1.5);
    }

    /**
     * Zooms out the graph a bit.
     */
    public void decreaseScale() {
        this.setScale(this.getScale() / 1.5);
    }

    /**
     * Sets the zoom level of the graph. Automatically triggers a redraw of the graph.
     *
     * @param scale Scale multiplier (i.e. 1 = no zoom)
     */
    public void setScale(double scale) {
        this.scale = scale;
        this.redraw();
    }

    /**
     * Returns the current scale multiplier in use by the graph.
     */
    public double getScale() {
        return this.scale;
    }

    /**
     * Called when the BufferedImage contents are stale and need updating, such as when the window has been resized, or an equation has been added / removed.
     */
    protected void redraw() {
        Graphics2D g = this.img.createGraphics();

        g.setBackground(Color.BLACK);
        g.setColor(Color.BLACK);
        g.fill(new Rectangle2D.Double(0, 0, this.img.getWidth(), this.img.getHeight()));

        this.drawGrid(g);

        int id = 0;
        for(Equation e : this.equations) {
            try {
                this.drawEquation(g, id, e);
            } catch (Exception ex) {
                // Equation is invalid, so inform the EquationEditor
                this.editors.get(id).setInvalid();
            }

            id++;
        }

        this.repaint();
    }

    /**
     * Draws the x and y axes, and the grid lines for each integer along each axis. Draws numbers for each perpendicular dashed line.
     *
     * @param g The graphics object to draw with
     */
    protected void drawGrid(Graphics2D g) {
        float[] dashPattern = new float[] { 10 * (float) this.scale, 5 * (float) this.scale };

        g.setColor(new Color(48, 48, 48));

        // Axis lines
        g.setStroke(new BasicStroke(2));
        g.draw(new Line2D.Double(0, this.img.getHeight() / 2, this.img.getWidth(), this.img.getHeight() / 2));
        g.draw(new Line2D.Double(this.img.getWidth() / 2, 0, this.img.getWidth() / 2, this.img.getHeight()));

        int imgWidth = this.img.getWidth(), imgHeight = this.img.getHeight();

        // X axis vertical lines & numbers
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, (29 - (imgHeight % 30)) / 2.0f + 20));

        // Janky code to scale interval between perpendicular lines
        int xScaledInt = (int) Math.round(normInterval * this.scale);
        int xMultiplier = Math.max(Math.round((float) normInterval / (float) xScaledInt), 1);

        int xInterval = xMultiplier * xScaledInt;
        int xNumInterval = xMultiplier, xCurrent = 0;
        for(int x = imgWidth / 2 - xInterval; x >= 0; x -= xInterval) {
            g.draw(new Line2D.Double(x, imgHeight, x, 0));
            g.draw(new Line2D.Double(imgWidth - x, imgHeight, imgWidth - x, 0));

            xCurrent += xNumInterval;
            g.drawString("-" + Integer.toString(xCurrent), x + 2, imgHeight / 2 + 14);
            g.drawString(Integer.toString(xCurrent), imgWidth - x + 2, imgHeight / 2 + 14);
        }

        // Y axis horizontal lines & numbers
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, (29 - (imgWidth % 30)) / 2.0f + 20));

        int yInterval = xInterval;
        int yNumInterval = xNumInterval, yCurrent = 0;
        for(int y = imgHeight / 2 - yInterval; y >= 0; y -= yInterval) {
            g.draw(new Line2D.Double(imgWidth, y, 0, y));
            g.draw(new Line2D.Double(imgWidth, imgHeight - y, 0, imgHeight - y));

            yCurrent += yNumInterval;
            g.drawString(Integer.toString(yCurrent), imgWidth / 2 + 2, y + 14);
            g.drawString("-" + Integer.toString(yCurrent), imgWidth / 2 + 2, imgHeight - y + 14);
        }
    }

    /**
     * Takes an equation and plugs each screen-visible x axis value into it, then draws the output value(s) onto the graph.
     *
     * @param g  The graphics object to draw with
     * @param id The equation number (decides which colour to use to draw with)
     * @param e  A prepared equation to plot
     */
    protected void drawEquation(Graphics2D g, int id, Equation e) {
        // TODO handle multiple roots
        // TODO handle invalid ranges (i.e. NaN return values)

        // Use equation colour to draw equation line(s)
        g.setColor(lineColours[id % lineColours.length]);

        // Determine the range of X values that are visible
        double bounds = (this.img.getWidth() / 2.0) / (normInterval * this.scale);

        // Values used inside loop
        boolean lastValSet = false;
        double lastVal = 0;
        int drawX = 0;

        for(double x = -bounds; x < bounds; x += (2 * bounds) / this.img.getWidth()) {
            if(!lastValSet) {
                lastVal = e.evaluate(x)[0];
                lastValSet = true;
                continue;
            }

            double currentVal = e.evaluate(x)[0];

            // Draw line between last and current value
            g.setStroke(new BasicStroke(2));
            g.draw(new Line2D.Double(drawX - 1, this.img.getHeight() / 2.0 - lastVal * (normInterval * this.scale), drawX, this.img.getHeight() / 2.0 - currentVal * (normInterval * this.scale)));

            lastVal = currentVal;
            drawX++;
        }
    }
}
