package com.cosylab.fzj.cosy.oc.ui.util;

import java.util.Optional;

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
        this(title,content,Optional.empty());
    }

    /**
     * Constructs a new bordered titled pane with the given text as the title and the given content. If the title
     * background is provided the content pane becomes transparent and the title has the given background. If the title
     * background is not provided, CSS defines the style entirely.
     *
     * @param title the bordered pane title
     * @param content the bordered pane content
     * @param titleBackground the style to apply to the title
     */
    public BorderedTitledPane(String title, Node content, Optional<String> titleBackground) {
        StringBuilder sb = new StringBuilder(title.length() + 2).append(' ').append(title).append(' ');
        Label titleLabel = new Label(sb.toString());
        titleLabel.getStyleClass().add("bordered-titled-title");
        StackPane contentPane = new StackPane();
        contentPane.getStyleClass().add("bordered-titled-content");
        contentPane.getChildren().add(content);
        StackPane.setAlignment(titleLabel,Pos.TOP_LEFT);
        getStyleClass()
                .add(titleBackground.isPresent() ? "bordered-titled-border-transparent" : "bordered-titled-border");
        getChildren().addAll(contentPane,titleLabel);
        titleBackground.ifPresent(titleLabel::setStyle);
    }
}
