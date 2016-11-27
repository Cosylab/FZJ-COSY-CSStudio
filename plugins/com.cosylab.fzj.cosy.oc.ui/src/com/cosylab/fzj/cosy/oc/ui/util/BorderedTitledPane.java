package com.cosylab.fzj.cosy.oc.ui.util;

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
     * Constructs a new bordered titled pane with given text as the title and the given content. The background is not
     * transparent.
     *
     * @param title the title
     * @param content the content to put the title on
     */
    public BorderedTitledPane(String title, Node content) {
        this(title,content,false);
    }
    /**
     * Constructs a new bordered titled pane with the given text as the title and the given content.
     *
     * @param title the bordered pane title
     * @param content the bordered pane content
     * @param transparent true if the background should be transparent or false otherwise
     */
    public BorderedTitledPane(String title, Node content, boolean transparent) {
        StringBuilder sb = new StringBuilder(title.length() + 2).append(' ').append(title).append(' ');
        Label titleLabel = new Label(sb.toString());
        titleLabel.getStyleClass().add(transparent ? "bordered-titled-title-transparent" : "bordered-titled-title");
        StackPane contentPane = new StackPane();
        content.getStyleClass().add("bordered-titled-content");
        contentPane.getChildren().add(content);
        StackPane.setAlignment(titleLabel,Pos.TOP_LEFT);
        getStyleClass().add(transparent ? "bordered-titled-border-transparent": "bordered-titled-border");
        getChildren().addAll(titleLabel,contentPane);
    }
}
