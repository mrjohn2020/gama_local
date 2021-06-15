package comokit.gama.ouputs.layer.dashboard;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.DefaultTableXYDataset;

import msi.gama.runtime.IScope;
import msi.gaml.expressions.IExpression;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


public class DashboardTestDemo extends DashboardJFreeOutput {
	
	public DashboardTestDemo(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
	}
	
    
	@Override
	public void createDash(final IScope scope) {
		super.createDash(scope);
		
		
//		this.r.setRect(5,5,200,200);
//		scope.getGraphics().drawDashboard(this);
		
	}
    
    Dataset createDataset(IScope scope) {
		// TODO Auto-generated method stub
    	return new DefaultCategoryDataset();
	}

	@Override
	public void initDash(final IScope scope, final String dashname) {
		super.initDash(scope, dashname);
//		dash.getPlot();
		

	}
  

//    public String getType() {
//        return type;
//    }

//    public void setType(String type) {
//        this.type = type;
//    }

//    public void setTextColor(Color textColor) {
//        this.textColor = textColor;
//    }

}
