/*********************************************************************************************
 *
 * 'OpenGLDisplayView.java, in plugin ummisco.gama.opengl, is part of the source code of the GAMA modeling and
 * simulation platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.opengl.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.jogamp.opengl.util.GLDrawableUtil;

import msi.gama.runtime.GAMA;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.opengl.renderer.helpers.CameraHelper;
import ummisco.gama.ui.views.displays.SWTDisplayView;

/**
 * Class OpenGLLayeredDisplayView.
 *
 * @author drogoul
 * @since 25 mars 2015
 *
 */
public class OpenGLDisplayView extends SWTDisplayView {

	{
		DEBUG.OFF();
	}

	public static String ID = "msi.gama.application.view.OpenGLDisplayView";

	@Override
	public SWTOpenGLDisplaySurface getDisplaySurface() {
		return (SWTOpenGLDisplaySurface) super.getDisplaySurface();
	}

	@Override
	protected Composite createSurfaceComposite(final Composite parent) {
		final SWTOpenGLDisplaySurface surface =
				(SWTOpenGLDisplaySurface) GAMA.getGui().getDisplaySurfaceFor(getOutput(), parent);
		surfaceComposite = surface.renderer.getCanvas();
		surface.outputReloaded();
		return surfaceComposite;
	}

	@Override
	public boolean forceOverlayVisibility() {
		final SWTOpenGLDisplaySurface surface = getDisplaySurface();
		return surface != null && surface.getROIDimensions() != null;
	}

	@Override
	public List<String> getCameraNames() {
		return new ArrayList<>(CameraHelper.PRESETS.keySet());
	}

	@Override
	public void fullScreenSet() {
		// DEBUG.OUT("Invoking explicit reshape + display");
		getDisplaySurface().renderer.getCanvas().invoke(false,
				new GLDrawableUtil.ReshapeGLEventListener(getDisplaySurface().renderer, true));
	}

	@Override
	protected Composite createNewSurfaceFromList(final Composite parent, int i) {
		// TODO Auto-generated method stub
	
		return null;
	}

}
