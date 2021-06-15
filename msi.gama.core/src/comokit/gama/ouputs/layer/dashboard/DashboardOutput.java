package comokit.gama.ouputs.layer.dashboard;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.Plot;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.ImageUtils;
import msi.gama.outputs.layers.charts.ChartJFreeChartOutput;
import msi.gama.outputs.layers.charts.ChartJFreeChartOutputBubble;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaColor;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;

public abstract class DashboardOutput {
	
	
	static final String STRATEGIC = "strategic";
	static final String OPERATIONAL = "operational";
	static final String TATICAL = "tatical";
	static final String ANALYTICAL = "analytical";
	
	String dname = "TEST JOHN";
	String type = STRATEGIC;
	boolean title_visible = true;
	Graphics2D g2D;
	
	
	
	
//	Color backgroundColor = GamaColor.BLUE;
//	Color axesColor = null;
//	Color textColor = null;
	
	
//	private Plot plot;
//	public ChartRenderingInfo info;
	
//	Rectangle2D r = new Rectangle2D.Double();
//	BufferedImage cache;
	
	private volatile boolean ready = true;
	
	
	public abstract BufferedImage getImage(final int sizeX, final int sizeY, final boolean antiAlias);
	
	public abstract void draw(IGraphics iGraphics, JFrame rect, boolean antialias);
	
	public DashboardOutput(final IScope scope, final String name, final IExpression typeexp) {
		final String t = typeexp == null ? IKeyword.SERIES : Cast.asString(scope, typeexp.value(scope));
		type = "strategic".equals(t) ? STRATEGIC
				: "operational".equals(t) ? OPERATIONAL
						: "tatical".equals(t) ? TATICAL
								: ANALYTICAL;
	}
	
	//new
	public Graphics2D getGraphic() {
		return g2D;
	}
	
	public void setGraphic() {
		g2D.create();
		g2D.setBackground(Color.cyan);
	}
	
	
	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}
	
	public void initdataset() {

	}
	
	public void createDash(final IScope scope) {}
	
	
	
	public Dashboard getDash() {
		return null;
	}
	
	public void setTitleVisible(final IScope scope, final Boolean asBool) {

		title_visible = asBool;
	}
	
	public boolean getTitleVisible(final IScope scope) {

		return title_visible;
		// return false;
	}
	
	
	public void initDash(final IScope scope, final String dashname) {
		dname = dashname;
	}
	
	public String getName() {
		return dname;
	}
	
	public void dispose(final IScope scope) {}

	public void draw(Graphics2D currentRenderer, JFrame rect, boolean antialias) {
		// TODO Auto-generated method stub
	}

	
//	public Plot getPlot() {
//        return this.plot;
//    }
//	
}
