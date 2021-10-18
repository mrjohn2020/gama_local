/*********************************************************************************************
 *
 * 'AWTDisplayView.java, in plugin ummisco.gama.java2d, is part of the source code of the GAMA modeling and simulation
 * platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.java2d;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.ILayer;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.layers.ILayerStatement;
import msi.gaml.descriptions.IDescription;
import ummisco.gama.java2d.swing.SwingControl;
import ummisco.gama.ui.views.WorkaroundForIssue1353;
import ummisco.gama.ui.views.displays.LayeredDisplayView;



public class AWTDisplayView extends LayeredDisplayView {

	@Override
	protected Composite createSurfaceComposite(final Composite parent) {

		if (getOutput() == null) { return null; }
		
		IDisplaySurface surface_without_chart = getDisplaySurface();
		if (getOutput().getViewType().equals("dashboard")) {
			int count_layer = surface_without_chart.getManager().getItems().size();
			for (int i = count_layer - 1; i >= 0; i--){
				if (surface_without_chart.getManager().getItems().get(i).getDefinition().getKeyword().equals("chart")) {
					surface_without_chart.getManager().disable(surface_without_chart.getManager().getItems().get(i));
				}
			}
		}
		surfaceComposite = new SwingControl(parent, SWT.NO_FOCUS) {

			@Override
			protected Java2DDisplaySurface createSwingComponent() {
				return (Java2DDisplaySurface) surface_without_chart;
			}

		};
		surfaceComposite.setEnabled(false);
		WorkaroundForIssue1594.installOn(this, parent, surfaceComposite, (Java2DDisplaySurface) surface_without_chart);
		WorkaroundForIssue2745.installOn(this);
		WorkaroundForIssue1353.install();
		return surfaceComposite;
	}
	
	@Override
	protected Composite createNewSurfaceFromList(final Composite parent, int i) {
		if (getOutput() == null) { return null; }
		
		final Composite s;
		LayeredDisplayOutput o = getOutput();
		IDisplaySurface surface = o.getListSurface().get(i); 
		
		int nb_layer = surface.getManager().getItems().size();
		for(int j = nb_layer - 1; j >= 0; j--) {
			if(j != i ) {
				surface.getManager().disable(surface.getManager().getItems().get(j));
				surface.getManager().setNullOverLayer();
			}
		}
		String t = (surface.getManager().getItems().get(i).getDefinition().getKeyword() + " : " + surface.getManager().getItems().get(i).getDefinition().getName());
		listNamePane.add(t);
		
		s = new SwingControl(parent, SWT.NO_FOCUS) {
			
			@Override
			protected Java2DDisplaySurface createSwingComponent() {
				return (Java2DDisplaySurface) surface;
			}
		};
		s.setEnabled(false);
		surfaceComposite_list.add(s);
		return surfaceComposite_list.get(i);
		
	}
	
	
}