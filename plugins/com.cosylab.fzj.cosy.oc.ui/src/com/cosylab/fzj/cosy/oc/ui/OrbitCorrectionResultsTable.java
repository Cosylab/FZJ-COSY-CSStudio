/*
 * Copyright (c) 2017 Cosylab d.d.
 *
 * Contact Information:
 *   Cosylab d.d., Ljubljana, Slovenia
 *   http://www.cosylab.com
 */
package com.cosylab.fzj.cosy.oc.ui;

import java.util.Arrays;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;

/**
 * <code>OrbitCorrectionResultsTable</code> is an table which contains information about orbit correction results.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class OrbitCorrectionResultsTable extends TableView<OrbitCorrectionResultsEntry> {

    private static class Column extends TableColumn<OrbitCorrectionResultsEntry,String> {

        Column(String property) {
            setCellValueFactory(new PropertyValueFactory<>(property));
        }

        Column(String title, String property) {
            this(property);
            Label label = new Label(title);
            Tooltip tooltip = new Tooltip();
            tooltip.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/" + property + ".png"))));
            label.setTooltip(tooltip);
            label.setTextAlignment(TextAlignment.CENTER);
            setGraphic(label);
        }
    }

    /**
     * Constructs new orbit correction results table.
     */
    public OrbitCorrectionResultsTable() {
        setEditable(false);
        setMaxWidth(Double.MAX_VALUE);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createTable();
        setFixedCellSize(23);
        String s = System.getProperty("os.name","nix").toLowerCase();
        final int c = s.contains("win") ? 26 : 29;
        prefHeightProperty().bind(Bindings.size(getItems()).multiply(getFixedCellSize()).add(c));
    }

    /**
     * Creates orbit correction results table columns.
     */
    private void createTable() {
        getColumns().addAll(Arrays.asList(new Column("name"),new Column("Min [mm]","min"),new Column("Max [mm]","max"),
                new Column("Average [mm]","avg"),new Column("RMS [mm]","rms"),new Column("STD [mm]","std")));
    }

    /**
     * Updates table values.
     *
     * @param entries updated entries
     */
    public void updateTable(List<OrbitCorrectionResultsEntry> entries) {
        getItems().setAll(entries);
    }
}
