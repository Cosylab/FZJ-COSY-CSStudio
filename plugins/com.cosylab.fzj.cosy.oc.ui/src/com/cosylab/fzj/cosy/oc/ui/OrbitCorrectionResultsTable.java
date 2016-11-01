package com.cosylab.fzj.cosy.oc.ui;

import java.util.Arrays;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * <code>OrbitCorrectionResultsTable</code> is an table which contains information about orbit correction results.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class OrbitCorrectionResultsTable extends TableView<OrbitCorrectionResultsEntry> {

    private static class Column extends TableColumn<OrbitCorrectionResultsEntry,String> {

        Column(String title, String property) {
            super(title);
            setCellValueFactory(new PropertyValueFactory<>(property));
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
        prefHeightProperty().bind(Bindings.size(getItems()).multiply(getFixedCellSize()).add(26));
    }

    /**
     * Creates orbit correction results table columns.
     */
    private void createTable() {
        getColumns().addAll(Arrays.asList(new Column("","name"),new Column("Min","min"),new Column("Max","max"),
                new Column("Average","avg"),new Column("RMS","rms"),new Column("STD","std")));
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
