package comokit.gama.ouputs.layer.dashboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultTableXYDataset;

import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.ImageUtils;
import msi.gama.outputs.layers.charts.ChartJFreeChartOutput;
import msi.gama.outputs.layers.charts.ChartJFreeChartOutputBubble;
import msi.gama.outputs.layers.charts.ChartOutput;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaColor;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;

public class DashboardJFreeOutput extends DashboardOutput implements ChartProgressListener{
	
	
	public DashboardJFreeOutput(IScope scope, String name, IExpression typeexp) {
		super(scope, name, typeexp);
		// TODO Auto-generated constructor stub
	}


	private volatile boolean ready = true;
	Dashboard dash = null;
	Rectangle2D r = new Rectangle2D.Double();
	BufferedImage cache;
	public ChartRenderingInfo info;
	
	private BufferedImage createCompatibleImage(final int sizeX, final int sizeY) {
		if ((int) r.getWidth() != sizeX || (int) r.getHeight() != sizeY) {
			r.setRect(0, 0, sizeX, sizeY);
			if (cache != null) {
				cache.flush();
			}			
			cache = ImageUtils.createCompatibleImage(sizeX, sizeY, false);
		}
		return cache;
	}
	
	@Override
	public BufferedImage getImage(final int sizeX, final int sizeY, final boolean antiAlias) {
		if (!ready) { return cache; }
		
		g2D = getGraphics(sizeX, sizeY);
//		g2D = getGraphics(500, 300);
//		g2D.setBackground(Color.RED);
		
		try {
//			dash.draw(g2D, r, info);
//			g2D.drawRect(30, 30, 400, 120);
//			g2D.setBackground(Color.BLUE);
		} catch (IndexOutOfBoundsException | IllegalArgumentException | NullPointerException e) {
			// Do nothing. See #1605
			// e.printStackTrace();
		} finally {
			g2D.dispose();
		}
		return cache;
	}
	
	
	public static DashboardJFreeOutput createDashOutput(final IScope scope, final String name,
			final IExpression typeexp) {
		DashboardJFreeOutput newDash;
		newDash = new DashboardTestDemo(scope, name, typeexp);
		return newDash;
	}
	
	
	
	protected Graphics2D getGraphics(final int sizeX, final int sizeY) {
		return createCompatibleImage(sizeX, sizeY).createGraphics();
	}
	

	public Dashboard getDash() {
		return dash;
	}
	
	@Override
	public void chartProgress(ChartProgressEvent event) {
		// TODO Auto-generated method stub
		ready = event.getType() == ChartProgressEvent.DRAWING_FINISHED;
	}


//	@Override
//	public void draw(final Graphics2D g2D, final Rectangle2D area, final boolean antiAlias) {
//		dash.draw(g2D, area, info);
//	}
	
	protected void initRenderer(final IScope scope) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void initDash(final IScope scope, final String dashname) {
		super.initDash(scope, dashname);
		
		initRenderer(scope);
		
	}

	@Override
	public void draw(IGraphics iGraphics, JFrame rect, boolean antialias) {
		// TODO Auto-generated method stub
		
	}
	
	HashMap<String, AbstractRenderer> RendererSet = new HashMap<>();
	
	AbstractRenderer getOrCreateRenderer(final IScope scope, final String serieid) {
		if (RendererSet.containsKey(serieid)) { return RendererSet.get(serieid); }
		final AbstractRenderer newrenderer = createRenderer(scope, serieid);
		RendererSet.put(serieid, newrenderer);
		return newrenderer;

	}
	
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		// TODO Auto-generated method stub
		return new XYErrorRenderer();

	}



	
	

}
