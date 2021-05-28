package com.secres.secrescam_lib;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatProgressBarUI;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.secres.secresos.DockableFrame;
import com.secres.secresos.MainView;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;

public class CamFrame extends DockableFrame {

    private static final long serialVersionUID = -7595071957174913723L;
    private final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private final HaarCascadeDetector DETECTOR = new HaarCascadeDetector();
    private final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f }, 0.0f);

    private Webcam webcam = null;
    private WebcamPanel.Painter painter = null;
    private List<DetectedFace> faces = null;
    private int delay = 0;
    private int progressBarIndex = delay;

    public CamFrame(String title, JToolBar jtb) {
        super(title, jtb);

        webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open(true);
        
        this.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(InternalFrameEvent e) {
                webcam.close();
            }
        });
        
        WebcamPanel panel = new WebcamPanel(webcam, false);
        panel.setPreferredSize(WebcamResolution.VGA.getSize());
        panel.setPainter(camPanelPainter);
        //panel.setMirrored(true);
        panel.start();

        painter = panel.getDefaultPainter();

        JPanel controlsPanel = new JPanel();

        JButton picButton = new JButton(new FlatSVGIcon("camera.svg"));
        picButton.setToolTipText("Take photo");

        JInternalFrame progressWindow = new JInternalFrame("", false, false, false, false);
        progressWindow.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        progressWindow.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        JProgressBar progressBar = new JProgressBar(0, delay) {
            private static final long serialVersionUID = -3208432173214833026L;

            @Override
            public void updateUI() {
                super.updateUI();
                setUI(new ProgressCircleUI());
            }
        };
        progressBarIndex = delay;
        progressBar.setValue(progressBarIndex);
        progressWindow.add(progressBar);

        progressWindow.setLocation(MainView.getDesktop().getWidth() / 2 - progressWindow.getWidth() / 2, MainView.getDesktop().getHeight() / 2 - progressWindow.getHeight() / 2);

        progressWindow.pack();
        MainView.getDesktop().add(progressWindow);

        Timer progressTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressBarIndex--;
                progressBar.setValue(progressBarIndex);
            }
        });

        picButton.addActionListener(e -> {
            progressBarIndex = delay;
            progressBar.setValue(progressBarIndex);
            progressBar.setMaximum(delay);
            progressWindow.setVisible(true);
            try {
                progressWindow.setSelected(true);
            } catch (PropertyVetoException e3) {
                e3.printStackTrace();
            }
            picButton.setEnabled(false);
            progressTimer.start();

            Timer timer = new Timer(delay * 1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    progressTimer.stop();
                    progressWindow.setVisible(false);

                    BufferedImage webcamImage = webcam.getImage();

                    JOptionPane op = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE);
                    op.setVisible(false);

                    JInternalFrame fileChooserFrame = op.createInternalFrame(CamFrame.this, title);

                    JFileChooser fileChooser = new JFileChooser();
                    if(fileChooser.getActionMap().get("viewTypeDetails") != null) {
                        Action details = fileChooser.getActionMap().get("viewTypeDetails");
                        details.actionPerformed(null);
                    }
                    fileChooser.addActionListener(e1 -> {
                        if(JFileChooser.APPROVE_SELECTION.equals(e1.getActionCommand())) {
                            try {
                                ImageIO.write(webcamImage, "PNG", new File(fileChooser.getSelectedFile().getAbsolutePath() + ".png"));
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        fileChooserFrame.dispose();
                    });

                    fileChooserFrame.add(fileChooser);
                    fileChooserFrame.pack();
                    fileChooserFrame.setVisible(true);

                    picButton.setEnabled(true);
                }
            });
            timer.setRepeats(false);
            timer.start();
        });

        JSpinner delaySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        delaySpinner.setToolTipText("Timer (in seconds)");
        delaySpinner.setValue(0);
        delaySpinner.addChangeListener(e -> {
            delay = (int) delaySpinner.getValue();
        });

        controlsPanel.add(picButton);
        controlsPanel.add(delaySpinner);

        add(panel);
        add(controlsPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);

        EXECUTOR.execute(() -> {
            while(true) {
                if(!webcam.isOpen()) {
                    return;
                }
                faces = DETECTOR.detectFaces(ImageUtilities.createFImage(webcam.getImage()));
            }
        });
    }

    WebcamPanel.Painter camPanelPainter = new WebcamPanel.Painter() {
        @Override
        public void paintPanel(WebcamPanel panel, Graphics2D g2) {
            if(painter != null) {
                painter.paintPanel(panel, g2);
            }
        }

        @Override
        public void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2) {
            if(painter != null) {
                painter.paintImage(panel, image, g2);
            }

            if(faces == null) {
                return;
            }

            Iterator<DetectedFace> dfi = faces.iterator();
            while(dfi.hasNext()) {

                DetectedFace face = dfi.next();
                Rectangle bounds = face.getBounds();

                int dx = (int) (0.1 * bounds.width);
                int dy = (int) (0.2 * bounds.height);
                int x = (int) bounds.x - dx;
                int y = (int) bounds.y - dy;
                int w = (int) bounds.width + 2 * dx;
                int h = (int) bounds.height + dy;

                g2.setStroke(STROKE);
                g2.setColor(Color.RED);
                g2.drawRect(x, y, w, h);
            }
        }
    };

    class ProgressCircleUI extends FlatProgressBarUI {
        @Override
        public Dimension getPreferredSize(JComponent c) {
            Dimension d = super.getPreferredSize(c);
            int v = Math.max(d.width, d.height);
            d.setSize(v, v);
            return d;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            // Insets b = progressBar.getInsets(); // area for border
            // int barRectWidth = progressBar.getWidth() - b.right - b.left;
            // int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
            // if (barRectWidth <= 0 || barRectHeight <= 0) {
            //   return;
            // }
            java.awt.Rectangle rect = SwingUtilities.calculateInnerArea(progressBar, null);
            if(rect.isEmpty()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double start = 90d;
            double degree = 360d * progressBar.getPercentComplete();
            double sz = Math.min(rect.width, rect.height);
            double cx = rect.getCenterX();
            double cy = rect.getCenterY();
            double or = sz * .5;
            double ir = or * .5; // .8;
            Shape inner = new Ellipse2D.Double(cx - ir, cy - ir, ir * 2d, ir * 2d);
            Shape outer = new Ellipse2D.Double(cx - or, cy - or, sz, sz);
            Shape sector = new Arc2D.Double(cx - or, cy - or, sz, sz, start, degree, Arc2D.PIE);

            Area foreground = new Area(sector);
            Area background = new Area(outer);
            Area hole = new Area(inner);

            foreground.subtract(hole);
            background.subtract(hole);

            // Draw the track
            g2.setPaint(new Color(0xDD_DD_DD));
            g2.fill(background);

            // Draw the circular sector
            // AffineTransform at = AffineTransform.getScaleInstance(-1.0, 1.0);
            // at.translate(-(barRectWidth + b.left * 2), 0);
            // AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(degree), cx, cy);
            // g2.fill(at.createTransformedShape(area));
            g2.setPaint(progressBar.getForeground());
            g2.fill(foreground);
            g2.dispose();

            // Deal with possible text painting
            if(progressBar.isStringPainted()) {
                Insets ins = progressBar.getInsets();
                paintString(g, rect.x, rect.y, rect.width, rect.height, 0, ins);
            }
        }
    }

}
