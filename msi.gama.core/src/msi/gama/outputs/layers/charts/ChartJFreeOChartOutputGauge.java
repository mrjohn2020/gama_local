package msi.gama.outputs.layers.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.data.xy.XYDataset;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.runtime.IScope;
import msi.gaml.expressions.IExpression;
import org.jfree.data.Range;

public class ChartJFreeOChartOutputGauge extends ChartJFreeChartOutput{

	private DefaultValueDataset dataset;
    private int value = 50000;
	
	public ChartJFreeOChartOutputGauge(IScope scope, String name, IExpression typeexp) {
		super(scope, name, typeexp);
		// TODO Auto-generated constructor stub
	}
	
	private static JFreeChart createChart(ValueDataset valuedataset) {
	    MeterPlot meterplot = new MeterPlot(valuedataset);
	  //set minimum and maximum value
	    meterplot.setRange(new Range(0.0D, 100000D));

	    meterplot.addInterval(new MeterInterval("Battery LOW", new Range(0.0D, 10000D), 
	            Color.red, new BasicStroke(2.0F), new Color(255, 0, 0, 128)));

	    meterplot.addInterval(new MeterInterval("Moderate", new Range(10001D, 90000D), 
	            Color.yellow, new BasicStroke(2.0F), new Color(255, 255, 0, 64)));

	    meterplot.addInterval(new MeterInterval("Battery FULL", new Range(90001D, 100000D),
	            Color.green, new BasicStroke(2.0F), new Color(0, 255, 0, 64)));

	    meterplot.setNeedlePaint(Color.darkGray);
	    meterplot.setDialBackgroundPaint(Color.white);
	    meterplot.setDialOutlinePaint(Color.black);
	    meterplot.setDialShape(DialShape.CHORD);
	    meterplot.setMeterAngle(180);
	    meterplot.setTickLabelsVisible(true);
	    meterplot.setTickLabelFont(new Font("Arial", 1, 12));
	    meterplot.setTickLabelPaint(Color.black);
	    meterplot.setTickSize(5D);
	    meterplot.setTickPaint(Color.gray);
	    meterplot.setValuePaint(Color.black);
	    meterplot.setValueFont(new Font("Arial", 1, 14));
	    JFreeChart jfreechart = new JFreeChart("Battery Level",
	            JFreeChart.DEFAULT_TITLE_FONT, meterplot, true);
	    return jfreechart;
	}
	
	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		dataset = new DefaultValueDataset(value);
		chart = createChart(dataset);
		
	}
	
	protected void resetRenderer(final IScope scope, final String serieid) {
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		// final int myrow = IdPosition.get(serieid);
		if (myserie.getMycolor() != null) {
			((PiePlot) this.getJFChart().getPlot()).setSectionPaint(serieid, myserie.getMycolor());
		}

	}
	
	@Override
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);
	}
	
	@Override
	protected void clearDataSet(final IScope scope) {
		// TODO Auto-generated method stub
		super.clearDataSet(scope);
	}
	
	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
	
	}
	
	@Override
	protected void resetSerie(final IScope scope, final String serieid) {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void initRenderer(final IScope scope) {
		// TODO Auto-generated method stub
	}

	@Override
	public ArrayList<Dataset> getJfreedataset() {
		// TODO Auto-generated method stub
		return null;
	}

}
