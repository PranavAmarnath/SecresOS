package com.secres.secresbrowser_lib;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.SystemInfo;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class SinglePage {

    private WebView webView;
    private WebEngine webEngine;
    private JTextField urlTextField;
    private JPanel vBox;

    public SinglePage() {
        initPage();
    }

    private void initPage() {
        //setUserAgentStylesheet(STYLESHEET_CASPIAN); // change style to caspian

        JFXPanel webPanel = new JFXPanel();
        JProgressBar pBar = new JProgressBar();
        Platform.runLater(() -> {
            webView = new WebView();
            webEngine = webView.getEngine();

            // When the user interacts and the link changes, update the text field with the new link
            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                    SwingUtilities.invokeLater(() -> urlTextField.setText(newValue));
                }
            });

            // update the progress bar's progress while the web page is loading
            webEngine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                    SwingUtilities.invokeLater(() -> {
                        pBar.setVisible(true);
                        pBar.setValue(newValue.intValue());
                        if(pBar.getValue() == 100) {
                            pBar.setVisible(false);
                            pBar.setValue(0);
                        }
                    });
                }
            });
        });

        vBox = new JPanel();
        vBox.setLayout(new BoxLayout(vBox, BoxLayout.Y_AXIS));

        JPanel controlsBox = new JPanel();
        controlsBox.setLayout(new BoxLayout(controlsBox, BoxLayout.LINE_AXIS));
        urlTextField = new JTextField();
        loadURL("http://google.com");
        urlTextField.setText("http://google.com");

        urlTextField.addActionListener(e -> {
            loadURL(urlTextField.getText());
        });

        JButton backButton = new JButton();
        backButton.setIcon(new FlatSVGIcon("back.svg"));
        backButton.setFocusable(false);
        backButton.addActionListener(e -> {
            goBack();
        });

        JButton forwardButton = new JButton();
        forwardButton.setIcon(new FlatSVGIcon("forward.svg"));
        forwardButton.setFocusable(false);
        forwardButton.addActionListener(e -> {
            goForward();
        });

        JButton reloadButton = new JButton();
        reloadButton.setIcon(new FlatSVGIcon("reload.svg"));
        reloadButton.setFocusable(false);
        reloadButton.addActionListener(e -> {
            reload();
        });

        controlsBox.add(backButton);
        controlsBox.add(Box.createHorizontalStrut(3));
        controlsBox.add(forwardButton);
        controlsBox.add(Box.createHorizontalStrut(3));
        controlsBox.add(reloadButton);
        controlsBox.add(Box.createHorizontalStrut(3));
        controlsBox.add(urlTextField);
        controlsBox.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        vBox.add(controlsBox);
        vBox.add(pBar);
        Platform.runLater(() -> {
            Scene root = new Scene(webView);
            if(SystemInfo.isMacOS) root.getStylesheets().add("/style_mac.css");
            else root.getStylesheets().add("/style_win.css");
            webPanel.setScene(root);
        });
        vBox.add(webPanel);
    }

    private void reload() {
        Platform.runLater(() -> webEngine.executeScript("history.go(0)"));
    }

    private void goBack() {
        Platform.runLater(() -> webEngine.executeScript("history.back()"));
    }

    private void goForward() {
        Platform.runLater(() -> webEngine.executeScript("history.forward()"));
    }

    private void loadURL(final String url) {
        String tmp = toURL(url);

        if(tmp == null) {
            tmp = toURL("http://" + url);
        }

        urlTextField.setText(tmp);

        final String finalTmp = tmp;

        Platform.runLater(() -> webEngine.load(finalTmp));
    }

    private String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    public JPanel getBox() {
        return vBox;
    }

}
