package com.cosylab.fzj.cosy.oc.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class BorderedTitledPane extends StackPane {

    public BorderedTitledPane(String title, Node content) {
        StringBuilder sb = new StringBuilder().append(' ').append(title).append(' ');
        final Label titleLabel = new Label(sb.toString());
        titleLabel.getStyleClass().add("bordered-titled-title");

        final StackPane contentPane = new StackPane();
        content.getStyleClass().add("bordered-titled-content");
        contentPane.getChildren().add(content);

        StackPane.setAlignment(titleLabel, Pos.TOP_LEFT);
        getStyleClass().add("bordered-titled-border");
        getChildren().addAll(titleLabel, contentPane);
    }
}