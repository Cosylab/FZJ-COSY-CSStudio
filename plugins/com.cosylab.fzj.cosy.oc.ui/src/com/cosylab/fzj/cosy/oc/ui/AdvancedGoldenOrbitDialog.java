package com.cosylab.fzj.cosy.oc.ui;

import static org.csstudio.ui.fx.util.FXUtilities.setGridConstraints;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.csstudio.ui.fx.util.FXUtilities;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.cosylab.fzj.cosy.oc.LatticeElementType;
import com.cosylab.fzj.cosy.oc.ui.model.BPM;
import com.cosylab.fzj.cosy.oc.ui.model.SeriesType;
import com.cosylab.fzj.cosy.oc.ui.util.SymmetricAxis;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * <code>AdvancedGoldenOrbitDialog</code> provides means to change the golden orbit.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 */
public class AdvancedGoldenOrbitDialog extends Dialog {

    private static class Cell extends TextFieldTableCell<BPM,Double> {

        private static final Background BACKGROUND = new Background(new BackgroundFill(new Color(1,0,0,0.2),null,null));
        private Background defaultBackground = null;

        Cell(final Consumer<Boolean> editingCallback) {
            super(new StringConverter<Double>() {

                @Override
                public Double fromString(String string) {
                    try {
                        return Double.parseDouble(string);
                    } catch (NumberFormatException e) {
                        return Double.NaN;
                    }
                }

                @Override
                public String toString(Double object) {
                    return object == null ? "NaN" : object.toString();
                }
            });
            editingProperty().addListener((o, a, n) -> editingCallback.accept(n));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item,empty);
            TableRow<BPM> currentRow = getTableRow();
            if (currentRow.getItem() != null && currentRow.getItem().goldenDifferentProperty().get()) {
                if (defaultBackground == null) {
                    defaultBackground = currentRow.getBackground();
                }
                currentRow.setBackground(BACKGROUND);
            } else {
                if (defaultBackground != null) {
                    currentRow.setBackground(defaultBackground);
                }
            }
        }
    }

    private static class Table extends TableView<BPM> implements Consumer<Boolean> {

        private boolean editing = false;

        Table() {
            setMaxWidth(Double.MAX_VALUE);
            setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            createTable();
            setFixedCellSize(23);
            setEditable(true);
        }

        @Override
        public void accept(Boolean t) {
            editing = t.booleanValue();
        }

        private void createTable() {
            TableColumn<BPM,String> nameColumn = new TableColumn<>("Device");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setEditable(false);
            TableColumn<BPM,Double> locationColumn = new TableColumn<>("Location [m]");
            locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
            locationColumn.setEditable(false);
            TableColumn<BPM,Double> currentColumn = new TableColumn<>("Current [mm]");
            currentColumn.setCellValueFactory(new PropertyValueFactory<>("goldenPosition"));
            currentColumn.setEditable(false);
            TableColumn<BPM,Double> positionColumn = new TableColumn<>("New [mm]");
            positionColumn.setCellValueFactory(new PropertyValueFactory<>("goldenPositionWish"));
            positionColumn.setEditable(true);
            positionColumn.setCellFactory(l -> new Cell(this));
            getColumns().addAll(Arrays.asList(nameColumn,locationColumn,currentColumn,positionColumn));
        }

        void updateTable(List<BPM> entries) {
            getItems().setAll(entries);
        }
    }

    private final OrbitCorrectionController controller;
    private Table horizontalBPMTable;
    private Table verticalBPMTable;
    private final Consumer<LatticeElementType> updater = this::update;
    private final Consumer<SeriesType> seriesUpdater = e -> {
        if (!horizontalBPMTable.editing) {
            horizontalBPMTable.refresh();
        }
        if (!verticalBPMTable.editing) {
            verticalBPMTable.refresh();
        }
    };
    private LineChart<Number,Number> orbitChart;
    private ZoomableLineChart orbitZoom;
    private final Shell parent;

    /**
     * Constructs a new dialog.
     *
     * @param parent the parent shell, used only for positioning of this dialog
     * @param controller the controller which this dialog works with
     */
    public AdvancedGoldenOrbitDialog(Shell parent, OrbitCorrectionController controller) {
        super((Shell)null);
        this.parent = parent;
        this.controller = controller;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.Window#setShellStyle(int)
     */
    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
        setBlockOnOpen(false);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Golden Orbit");
    }

    private void update(LatticeElementType type) {
        switch (type) {
            case HORIZONTAL_BPM:
                horizontalBPMTable.updateTable(controller.getHorizontalBPMs());
                break;
            case VERTICAL_BPM:
                verticalBPMTable.updateTable(controller.getVerticalBPMs());
                break;
            default:
                break;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent,IDialogConstants.SELECT_ALL_ID,"Apply",true)
                .setToolTipText("Write the new golden orbit to the IOC");
        createButton(parent,IDialogConstants.ABORT_ID,"Restore",true)
                .setToolTipText("Restore the golden orbit from the IOC");
        createButton(parent,IDialogConstants.OK_ID,IDialogConstants.CLOSE_LABEL,true)
                .setToolTipText("Close this dialog");
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.SELECT_ALL_ID) {
            controller.updateGoldenOrbit();
        } else if (buttonId == IDialogConstants.ABORT_ID) {
            refresh();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    private void refresh() {
        controller.getHorizontalBPMs().forEach(e -> e.refreshGolden());
        controller.getVerticalBPMs().forEach(e -> e.refreshGolden());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);
        parent.addDisposeListener(e -> {
            controller.removeLaticeUpdateCallback(updater);
            controller.removeGoldernOrbitUpdateCallback(seriesUpdater);
        });
        FXUtilities.createFXBridge(composite,this::createScene);
        applyDialogFont(composite);
        controller.addLaticeUpdateCallback(updater);
        controller.addGoldenOrbitUpdateCallback(seriesUpdater);
        update(LatticeElementType.HORIZONTAL_BPM);
        update(LatticeElementType.VERTICAL_BPM);
        refresh();
        Platform.runLater(() -> {
            //this gets rid of stupid Java FX bug, when the table header is
            //not accommodating for the vertical scrollbar width
            Point p = getShell().getSize();
            getShell().setSize(p.x + 5,p.y);
        });
        return composite;
    }

    private Scene createScene(Composite parent) {
        Scene scene = new Scene(new BorderPane(createFXContents(parent)));
        scene.getStylesheets().add(OrbitCorrectionView.class.getResource("style.css").toExternalForm());
        return scene;
    }

    private Node createFXContents(Composite parent) {
        GridPane pane = new GridPane();
        pane.setStyle(FXUtilities.toBackgroundColorStyle(parent.getBackground()));
        pane.setHgap(20);
        pane.setVgap(5);
        horizontalBPMTable = new Table();
        verticalBPMTable = new Table();
        Label horizontalBPMLabel = new Label("Horizontal Orbit");
        Label verticalBPMLabel = new Label("Vertical Orbit");
        horizontalBPMLabel.setStyle("-fx-font-weight: bold");
        verticalBPMLabel.setStyle("-fx-font-weight: bold");
        pane.add(createOrbitChart(),0,0,2,1);
        pane.add(horizontalBPMLabel,0,1);
        pane.add(verticalBPMLabel,1,1);
        pane.add(horizontalBPMTable,0,2);
        pane.add(verticalBPMTable,1,2);
        setGridConstraints(orbitChart,true,true,HPos.CENTER,VPos.CENTER,Priority.ALWAYS,Priority.ALWAYS);
        setGridConstraints(horizontalBPMLabel,false,false,HPos.CENTER,VPos.CENTER,Priority.NEVER,Priority.NEVER);
        setGridConstraints(verticalBPMLabel,false,false,HPos.CENTER,VPos.CENTER,Priority.NEVER,Priority.NEVER);
        setGridConstraints(horizontalBPMTable,true,true,HPos.LEFT,VPos.TOP,Priority.ALWAYS,Priority.ALWAYS);
        setGridConstraints(verticalBPMTable,true,true,HPos.LEFT,VPos.TOP,Priority.ALWAYS,Priority.ALWAYS);
        return pane;
    }

    private Region createOrbitChart() {
        ValueAxis<Number> xAxis = new NumberAxis(0,185,5);
        xAxis.setTickLabelFormatter(OrbitCorrectionView.TICK_LABEL_FORMATTER);
        ValueAxis<Number> yAxis = new SymmetricAxis();
        yAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);
        yAxis.setTickLabelsVisible(true);
        yAxis.setTickMarkVisible(true);
        yAxis.setMinorTickVisible(false);
        yAxis.autoRangingProperty().set(true);
        orbitChart = new LineChart<>(xAxis,yAxis);
        orbitChart.setLegendVisible(false);
        orbitChart.setAnimated(false);
        orbitChart.getStyleClass().add("orbit-chart");
        orbitChart.getData().add(new Series<>("Empty Series",FXCollections.emptyObservableList()));
        orbitChart.getData().add(new Series<>("Empty Series",FXCollections.emptyObservableList()));
        addSeries(SeriesType.GOLDEN_HORIZONTAL_ORBIT);
        addSeries(SeriesType.GOLDEN_VERTICAL_ORBIT);
        orbitZoom = new ZoomableLineChart(orbitChart,false,true,true);
        OrbitCorrectionView.setMinMax(orbitZoom,orbitChart);
        return orbitZoom;
    }

    private void addSeries(SeriesType type) {
        Series<Number,Number> series = new Series<>(type.getSeriesName(),getData(type));
        if (orbitChart.getData().size() > type.getSeriesIndex()) {
            orbitChart.getData().set(type.getSeriesIndex(),series);
        } else {
            orbitChart.getData().add(series);
        }
    }

    private ObservableList<Data<Number,Number>> getData(SeriesType type) {
        ObservableList<Data<Number,Number>> data = FXCollections.observableArrayList();
        List<BPM> bpms;
        final Function<BPM,DoubleProperty> property;
        if (type == SeriesType.GOLDEN_VERTICAL_ORBIT) {
            property = bpm -> bpm.goldenPositionWishProperty();
            bpms = controller.getVerticalBPMs();
        } else if (type == SeriesType.GOLDEN_HORIZONTAL_ORBIT) {
            property = bpm -> bpm.goldenPositionWishProperty();
            bpms = controller.getHorizontalBPMs();
        } else {
            return data;
        }
        bpms.stream().forEach(bpm -> {
            Data<Number,Number> dataPoint = new Data<>();
            dataPoint.XValueProperty().bind(bpm.locationProperty());
            dataPoint.YValueProperty().bind(property.apply(bpm));
            dataPoint.nodeProperty().addListener((a, o, n) -> {
                if (n != null) {
                    Tooltip.install(n,new Tooltip(bpm.nameProperty().get()));
                }
            });
            data.add(dataPoint);
        });
        return data;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getConstrainedShellBounds(org.eclipse.swt.graphics.Rectangle)
     */
    @Override
    protected Rectangle getConstrainedShellBounds(Rectangle preferredSize) {
        Rectangle r = super.getConstrainedShellBounds(preferredSize);
        r.width = Math.max(r.width,1000);
        Rectangle parentR = parent.getBounds();
        int cX = parentR.x + parentR.width / 2;
        int cY = parentR.y + parentR.height / 2;
        r.x = cX - r.width / 2;
        r.y = cY - r.height / 2;
        r.x = r.x < 0 ? 0 : r.x;
        r.y = r.y < 0 ? 0 : r.y;
        return r;
    }
}
