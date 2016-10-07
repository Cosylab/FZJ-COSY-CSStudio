package com.cosylab.fzj.cosy.oc.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.csstudio.ui.fx.util.FXUtilities;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * <code>MultiLineButton</code> is an extension of the javafx {@link Button} which displays the text in multiple lines.
 * The text is split by spaces (individual words are never split) and is shown in as little lines as possible. If the
 * width of the button is smaller than the width of individual line that line is split into two lines and the second
 * line is concatenated with the next one. The process is repeated until all text is displayed.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class MultiLineButton extends Button implements ChangeListener<Number> {

    private final VBox box;

    /**
     * Constructs a new multi line button showing the given text.
     *
     * @param text the text of to display on the button
     */
    public MultiLineButton(String text) {
        super(text);
        box = new VBox();
        box.getChildren().addAll(new Label(text));
        box.setAlignment(Pos.CENTER);
        setGraphic(box);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        widthProperty().addListener(this);
    }

    /* (non-Javadoc)
     * @see javafx.beans.value.ChangeListener#changed(javafx.beans.value.ObservableValue, java.lang.Object, java.lang.Object)
     */
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        final Insets padding = getPadding();
        //new width is the width available for the text, which is the total button width - padding and subtract 1,
        //to get rid of potential discrepancy (renderer might already decide to paint the ... instead of the full
        //text, while there might still be enough room for that)
        final int newWidth = (int) (newValue.doubleValue() - padding.getLeft() - padding.getRight()) - 1;
        String text = getText();
        Font font = getFont();
        int width = FXUtilities.measureStringWidth(text, font);
        int height = measureStringHeight(text, font);
        int maxHeight = (int)(getHeight());
        int maxLines = height == 0 ? Integer.MAX_VALUE : maxHeight/height;
        if (width > newWidth) {
            String[] parts = text.split("\\s+");
            if (parts.length < 2) {
                if (box.getChildren().size() != 1) {
                    box.getChildren().setAll(new Label(text));
                }
            } else {
                int count = parts.length;
                List<String> labels = new ArrayList<>(count);
                StringBuilder newText = new StringBuilder(text.length());
                newText.append(parts[0]);
                int start = 0;
                while (start != count) {
                    String currentText = newText.toString();
                    width = FXUtilities.measureStringWidth(currentText, font);
                    int i = start + 1;
                    if (labels.size() == maxLines - 1) {
                        for (; i < count; i++) {
                            newText.append(' ').append(parts[i]);
                        }
                        currentText = newText.toString();
                        start = count;
                    } else {
                        if (width < newWidth) {
                            for (; i < count; i++) {
                                newText.append(' ').append(parts[i]);
                                width = FXUtilities.measureStringWidth(newText.toString(), font);
                                if (width > newWidth) {
                                    break;
                                }
                                currentText = newText.toString();
                            }
                            start = i;
                        } else {
                            start++;
                        }
                    }
                    labels.add(currentText);
                    if (start != count) {
                        newText = new StringBuilder(text.length());
                        newText.append(parts[start]);
                    }
                }
                if (box.getChildren().size() != labels.size()) {
                    box.getChildren().setAll(labels.stream().map(s -> new Label(s)).collect(Collectors.toList()));
                }
            }
        } else if (box.getChildren().size() != 1) {
            box.getChildren().setAll(new Label(text));
        }
    }

    /**
     * Measures the height of the string when displayed with the given font.
     *
     * @param text the text to measure the width of
     * @param font the font to use for measurement
     * @return the width of the text in pixels
     */
    public static int measureStringHeight(String text, Font font) {
        Text mText = new Text(text);
        if (font != null) {
            mText.setFont(font);
        }
        return (int) mText.getLayoutBounds().getHeight();
    }

}
