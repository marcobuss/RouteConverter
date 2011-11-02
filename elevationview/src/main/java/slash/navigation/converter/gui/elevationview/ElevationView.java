/*
    This file is part of RouteConverter.

    RouteConverter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RouteConverter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RouteConverter; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/

package slash.navigation.converter.gui.elevationview;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import slash.navigation.converter.gui.models.PositionsModel;
import slash.navigation.converter.gui.models.PositionsSelectionModel;
import slash.navigation.gui.Application;
import slash.navigation.util.Unit;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static java.text.MessageFormat.format;
import static java.text.NumberFormat.getIntegerInstance;
import static org.jfree.chart.axis.NumberAxis.createStandardTickUnits;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import static org.jfree.ui.Layer.FOREGROUND;

/**
 * Displays the elevations of a {@link PositionsModel}.
 *
 * @author Christian Pesch
 */

public class ElevationView {
    protected static final Preferences preferences = Preferences.userNodeForPackage(ElevationView.class);
    private static final String X_GRID_PREFERENCE = "xGrid";
    private static final String Y_GRID_PREFERENCE = "yGrid";

    private ChartPanel chartPanel;
    private XYPlot plot;
    private PositionsModel positionsModel;
    private ElevationModel elevationModel;

    public ElevationView(PositionsModel positionsModel, final PositionsSelectionModel positionsSelectionModel) {
        this.positionsModel = positionsModel;

        PatchedXYSeries series = new PatchedXYSeries("Elevation");
        this.elevationModel = new ElevationModel(positionsModel, series);
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = createChart(dataset);
        plot = createPlot(chart);
        chartPanel = new ChartPanel(chart, false, true, true, true, true);
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            public void chartMouseClicked(ChartMouseEvent e) {
                ChartEntity entity = e.getEntity();
                if (!(entity instanceof XYItemEntity))
                    return;
                int row = ((XYItemEntity) entity).getItem();
                positionsSelectionModel.setSelectedPositions(new int[]{row}, true);
            }

            public void chartMouseMoved(ChartMouseEvent e) {
            }
        });
    }

    private static ResourceBundle getBundle() {
        return Application.getInstance().getContext().getBundle();
    }

    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYAreaChart(null, null, null, dataset, VERTICAL, false, true, false);
        chart.setBackgroundPaint(new JPanel().getBackground());
        return chart;
    }

    private XYPlot createPlot(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setForegroundAlpha(0.65F);
        plot.setDomainGridlinesVisible(preferences.getBoolean(X_GRID_PREFERENCE, true));
        plot.setRangeGridlinesVisible(preferences.getBoolean(Y_GRID_PREFERENCE, true));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(createStandardTickUnits());
        Font font = new JLabel().getFont();
        rangeAxis.setLabelFont(font);

        NumberAxis valueAxis = (NumberAxis) plot.getDomainAxis();
        valueAxis.setStandardTickUnits(createStandardTickUnits());
        valueAxis.setLowerMargin(0.0);
        valueAxis.setUpperMargin(0.0);
        valueAxis.setLabelFont(font);
        return plot;
    }

    public Component getComponent() {
        return chartPanel;
    }

    public void setUnit(Unit unit) {
        elevationModel.setUnit(unit);
        plot.getDomainAxis().setLabel(format(getBundle().getString("distance-axis"), unit.getDistanceName()));
        plot.getRangeAxis().setLabel(format(getBundle().getString("elevation-axis"), unit.getElevationName()));
        plot.getRenderer().setBaseToolTipGenerator(new StandardXYToolTipGenerator(String.
                format("{2} %s @ {1} %s", unit.getElevationName(), unit.getDistanceName()),
                getIntegerInstance(), getIntegerInstance()));
    }

    public void setSelectedPositions(int[] selectPositions, boolean replaceSelection) {
        if (replaceSelection)
            plot.clearDomainMarkers();

        double[] distances = positionsModel.getRoute().getDistancesFromStart(selectPositions);
        for (double distance : distances) {
            plot.addDomainMarker(0, new ValueMarker(elevationModel.formatDistance(distance)), FOREGROUND, false);
        }
        // make sure the protected fireChangeEvent() is called without any side effects
        plot.setWeight(plot.getWeight());
    }

    public void print() {
        chartPanel.createChartPrintJob();
    }
}
