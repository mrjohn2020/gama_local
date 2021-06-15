/*********************************************************************************************
 *
 * 'PlayPauseSimulationHandler.java, in plugin ummisco.gama.ui.experiment, is part of the source code of the GAMA
 * modeling and simulation platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.ui.commands;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import msi.gama.runtime.GAMA;
import ummisco.gama.ui.bindings.GamaKeyBindings;

public class PlayPauseSimulationHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		GAMA.startPauseFrontmostExperiment();
		return this;
	}

	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		element.setTooltip("Runs or pauses the current experiment (" + GamaKeyBindings.PLAY_STRING + ")");
		if (GAMA.isPaused())
			element.setText("Run Experiment (" + GamaKeyBindings.PLAY_STRING + ")");
		else
			element.setText("Pause Experiment (" + GamaKeyBindings.PLAY_STRING + ")");

	}
}
