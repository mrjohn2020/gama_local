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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.outputs.LayeredDisplayOutput;
import ummisco.gama.java2d.swing.SwingControl;
import ummisco.gama.ui.views.WorkaroundForIssue1353;
import ummisco.gama.ui.views.displays.LayeredDisplayView;



public class AWTDisplayView extends LayeredDisplayView {

	@Override
	protected Composite createSurfaceComposite(final Composite parent) {

		if (getOutput() == null) { return null; }

		surfaceComposite = new SwingControl(parent, SWT.NO_FOCUS) {

			@Override
			protected Java2DDisplaySurface createSwingComponent() {
				return (Java2DDisplaySurface) getDisplaySurface();
			}

		};
		surfaceComposite.setEnabled(false);
		WorkaroundForIssue1594.installOn(this, parent, surfaceComposite, (Java2DDisplaySurface) getDisplaySurface());
		WorkaroundForIssue2745.installOn(this);
		WorkaroundForIssue1353.install();
		return surfaceComposite;
	}
	
	
	protected Composite createNewSurface(final Composite parent) {
		if (getOutput() == null) { return null; }
		
		surfaceComposite2 = new SwingControl(parent, SWT.NO_FOCUS) {

			@Override
			protected Java2DDisplaySurface createSwingComponent() {
				// TODO Auto-generated method stub
//				IDisplaySurface testSurface = getOutput().getNewSurface();
				LayeredDisplayOutput a = getOutput();
				IDisplaySurface testSurface = a.getNewSurface();
				testSurface.getManager().disable(testSurface.getManager().getItems().get(1));
//				testSurface.getManager().removeItem(testSurface.getManager().getItems().get(0));// xoa mot cai surface di
				
				return (Java2DDisplaySurface) testSurface;
			}
			
		};
		return surfaceComposite2;
	}
	
	protected Composite createNewSurfaceFromList(final Composite parent, int i) {
		if (getOutput() == null) { return null; }
		
		final Composite s;
		s = new SwingControl(parent, SWT.NO_FOCUS) {
			
			@Override
			protected Java2DDisplaySurface createSwingComponent() {
				LayeredDisplayOutput a = getOutput();
				IDisplaySurface surface = a.getListSurface().get(i);
				for(int j = 0; j < surface.getManager().getItems().size(); j++) {
					if(j != i)
						surface.getManager().disable(surface.getManager().getItems().get(j));
				}
				
				return (Java2DDisplaySurface) surface;
				
			}
		};
		surfaceComposite_list.add(s);
		return surfaceComposite_list.get(i);
		
	}
	
	
}