package com.cosylab.fzj.cosy.oc.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * <code>BorderedTitledPane</code> is an extension of the javafx {@link StackPane} which displays titled border around
 * the pane.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class BorderedTitledPane extends StackPane {

    /**
     * Constructs a new bordered titled pane with the given text as a title and the given content.
     *
     * @param title the bordered pane title
     * @param content the bordered pane content
     */
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