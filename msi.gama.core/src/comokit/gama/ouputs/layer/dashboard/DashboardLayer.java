package comokit.gama.ouputs.layer.dashboard;

import java.awt.geom.Rectangle2D;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.metamodel.shape.IShape;
import msi.gama.outputs.layers.AbstractLayer;
import msi.gama.outputs.layers.ILayerStatement;
import msi.gama.outputs.layers.charts.ChartJFreeChartOutput;
import msi.gama.outputs.layers.charts.ChartLayerStatement;
import msi.gama.outputs.layers.charts.ChartOutput;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

public class DashboardLayer extends AbstractLayer {

	public DashboardLayer(final ILayerStatement model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

//	private DashboardOutput getDashboard() {
//		return ((DashboardLayerStatement) definition).getOutput();
//	}
//	
	@Override
	public String getType() {
		return "Dashboard layer";
	}

	@Override
	public void privateDraw(final IScope scope, final IGraphics dg) {
//		dg.drawDashboard(getDashboard());
	}

}
