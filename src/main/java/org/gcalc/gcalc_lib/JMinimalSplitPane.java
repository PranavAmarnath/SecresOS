// Based off https://www.formdev.com/blog/swing-tip-jsplitpane-with-zero-size-divider/
package org.gcalc.gcalc_lib;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

/**
 * Creates a split pane with the split being only 1px wide, whilst the clickable area is larger, allowing for easy use.
 */
public class JMinimalSplitPane extends JSplitPane {
    private int dividerDragSize = 9;
    private int dividerDragOffset = 4;

    /**
     * Default to creating a horizontal split, as Swing does.
     */
    public JMinimalSplitPane() {
        this(HORIZONTAL_SPLIT);
    }

    /**
     * Use supplied orientation, but no default members.
     *
     * @param orientation Whether to split horizontally, or vertically.
     */
    public JMinimalSplitPane(int orientation) {
        super(orientation, null, null);
    }

    /**
     * Full constructor which takes an orientation as well as the two components to use - one for each side of the split.
     *
     * @param orientation Which way to split the split pane
     * @param component   Component to add to the left / top side
     * @param component1  Component to add to the right / bottom side
     */
    public JMinimalSplitPane(int orientation, Component component, Component component1) {
        super(orientation, component, component1);
        this.setContinuousLayout(true);
        this.setDividerSize(1);
    }

    /**
     * Where we do our hack to reduce the draw size of the split, but increase the clickable area of the split handle to resize the split.
     */
    @Override
    public void doLayout() {
        super.doLayout();

        // Increase divider width or height
        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) this.getUI()).getDivider();
        Rectangle bounds = divider.getBounds();
        if(this.orientation == HORIZONTAL_SPLIT) {
            bounds.x -= this.dividerDragOffset;
            bounds.width = this.dividerDragSize;
        }
        else {
            bounds.y -= this.dividerDragOffset;
            bounds.height = this.dividerDragSize;
        }

        divider.setBounds(bounds);
    }

    /**
     * Take over managing the UI layout.
     */
    @Override
    public void updateUI() {
        this.setUI(new JMinimalSplitPaneUI());
        this.revalidate();
    }

    /**
     * Allow the divider drag size to be larger than the divider itself.
     *
     * @return The invisible divider's drag size
     */
    public int getDividerDragSize() {
        return this.dividerDragSize;
    }

    /**
     * Set the hacked divider to be even larger / smaller.
     *
     * @param dividerDragSize The new size in pixels to use.
     */
    public void setDividerDragSize(int dividerDragSize) {
        this.dividerDragSize = dividerDragSize;
        this.revalidate();
    }

    /**
     * Gets the size offset (i.e. how much of the drag size is on either side of the divider).
     *
     * @return The drag offset
     */
    public int getDividerDragOffset() {
        return this.dividerDragOffset;
    }

    /**
     * Allows the divider's invisible click area to be biased towards one side or the other of the split.
     *
     * @param dividerDragOffset Amount of pixels to offset from being completely on the far side of the split
     */
    public void setDividerDragOffset(int dividerDragOffset) {
        this.dividerDragOffset = dividerDragOffset;
        this.revalidate();
    }

    /**
     * UI which enables our hack.
     */
    private class JMinimalSplitPaneUI extends BasicSplitPaneUI {
        /**
         * Callback which sets our small divider as the divider for the split pane.
         *
         * @return The divider to use
         */
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new ZeroSizeDivider(this);
        }
    }

    /**
     * Divider which always draws at 1px thickness, but handles click events for its entire invisible area.
     */
    private class ZeroSizeDivider extends BasicSplitPaneDivider {
        /**
         * Creates a new divider.
         *
         * @param ui The UI to attach to
         */
        public ZeroSizeDivider(BasicSplitPaneUI ui) {
            super(ui);
            super.setBorder(null);
            this.setBackground(UIManager.getColor("controlShadow"));
        }

        /**
         * What border?
         *
         * @param border We have no border lol
         */
        @Override
        public void setBorder(Border border) {
        }

        /**
         * Draw a 1px thick line using the UI's graphics.
         *
         * @param g The graphics to use to draw with
         */
        @Override
        public void paint(Graphics g) {
            g.setColor(this.getBackground());
            if(this.orientation == HORIZONTAL_SPLIT) {
                g.drawLine(JMinimalSplitPane.this.dividerDragOffset, 0, JMinimalSplitPane.this.dividerDragOffset, this.getHeight() - 1);
            }
            else {
                g.drawLine(0, JMinimalSplitPane.this.dividerDragOffset, this.getHeight() - 1, JMinimalSplitPane.this.dividerDragOffset);
            }
        }

        /**
         * Handle drag events over the entire invisible area.
         *
         * @param location Where the mouse was dragged to
         */
        @Override
        protected void dragDividerTo(int location) {
            super.dragDividerTo(location + JMinimalSplitPane.this.dividerDragOffset);
            System.out.println(((BasicSplitPaneUI) JMinimalSplitPane.this.getUI()).getDivider().getBounds());
        }

        /**
         * Finalise the drag, using the super method, but accounting for the invisible drag area's offset.
         *
         * @param location Where the drag finished
         */
        @Override
        protected void finishDraggingTo(int location) {
            super.finishDraggingTo(location + JMinimalSplitPane.this.dividerDragOffset);
        }
    }

}
