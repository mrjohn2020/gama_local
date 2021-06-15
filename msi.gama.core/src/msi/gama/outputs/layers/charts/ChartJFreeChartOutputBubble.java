package msi.gama.outputs.layers.charts;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYZDataset;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.runtime.IScope;
import msi.gaml.expressions.IExpression;

public class ChartJFreeChartOutputBubble extends ChartJFreeChartOutput{
	
	
	public ChartJFreeChartOutputBubble(final IScope scope, String name, IExpression typeexp) {
		super(scope, name, typeexp);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		
		jfreedataset.add(0, createDataset());
				
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		if (reverse_axes) {
			orientation = PlotOrientation.HORIZONTAL;
		}
		
		
		chart = ChartFactory.createBubbleChart(getName(), "test1", "test2", 
				(XYZDataset) jfreedataset.get(0), orientation, true, false, false);
	}
	
	public static XYZDataset createDataset() {
	      DefaultXYZDataset defaultxyzdataset = new DefaultXYZDataset(); 
	      double ad[ ] = { 30 , 40 , 50 , 60 , 70 , 80 };                 
	      double ad1[ ] = { 10 , 20 , 30 , 40 , 50 , 60 };                 
	      double ad2[ ] = { 4 , 5 , 10 , 8 , 9 , 6 };                 
	      double ad3[][] = { ad , ad1 , ad2 };                 
	      defaultxyzdataset.addSeries( "Series 1" , ad3 );
	                   
	      return defaultxyzdataset; 
	   }

	@Override
	public void initdataset() {
		super.initdataset();
		if (getType() == 8) {
			chartdataset.setCommonXSeries(true);
			chartdataset.setByCategory(true);
		}
	}
	
}
