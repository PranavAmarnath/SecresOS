package com.secres.secresbrowser_lib;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Button with Image.
 * 
 * @author Zephyr
 * @see https://stackoverflow.com/a/50658048/13772184
 *
 */
public class ImageButton extends Button {

    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 2, 2, 2, 2;";
    private final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 3 1 1 3;";

    public ImageButton(Image originalImage, double h, double w) {

        ImageView image = new ImageView(originalImage);
        image.setFitHeight(h);
        image.setFitHeight(w);
        image.setPreserveRatio(true);
        setGraphic(image);
        setStyle(STYLE_NORMAL);

        setOnMousePressed(event -> setStyle(STYLE_PRESSED));
        setOnMouseReleased(event -> setStyle(STYLE_NORMAL));
    }

}
