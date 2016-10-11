package com.cosylab.fzj.cosy.oc.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * <code>OrbitCorrectionResultsTable</code> is an table which contains information about orbit correction results.
 *
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public class OrbitCorrectionResultsTable extends TableView<OrbitCorrectionResultsEntry> {

    /**
     * Constructs new orbit correction results table.
     */
    public OrbitCorrectionResultsTable () {
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

        List<TableColumn<OrbitCorrectionResultsEntry, ?>> list = new ArrayList<>(6);

        TableColumn<OrbitCorrectionResultsEntry, String> nameColumn = new TableColumn<>("");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        list.add(nameColumn);

        TableColumn<OrbitCorrectionResultsEntry, Double> minColumn = new TableColumn<>("Min");
        minColumn.setCellValueFactory(new PropertyValueFactory<>("min"));
        list.add(minColumn);

        TableColumn<OrbitCorrectionResultsEntry, Double> maxColumn = new TableColumn<>("Max");
        maxColumn.setCellValueFactory(new PropertyValueFactory<>("max"));
        list.add(maxColumn);

        TableColumn<OrbitCorrectionResultsEntry, Double> avgColumn = new TableColumn<>("Avg");
        avgColumn.setCellValueFactory(new PropertyValueFactory<>("avg"));
        list.add(avgColumn);

        TableColumn<OrbitCorrectionResultsEntry, Double> rmsColumn = new TableColumn<>("RMS");
        rmsColumn.setCellValueFactory(new PropertyValueFactory<>("rms"));
        list.add(rmsColumn);

        TableColumn<OrbitCorrectionResultsEntry, Double> stdColumn = new TableColumn<>("STD");
        stdColumn.setCellValueFactory(new PropertyValueFactory<>("std"));
        list.add(stdColumn);

        getColumns().addAll(list);
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