/*********************************************************************************************
 *
 * 'AgentInspectView.java, in plugin ummisco.gama.ui.experiment, is part of the source code of the GAMA modeling and
 * simulation platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.ui.views.inspectors;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

import msi.gama.common.interfaces.IGui;
import msi.gama.kernel.experiment.IParameter;
import msi.gama.kernel.experiment.ITopLevelAgent;
import msi.gama.kernel.experiment.ParameterAdapter;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.outputs.IDisplayOutput;
import msi.gama.outputs.IOutput;
import msi.gama.outputs.InspectDisplayOutput;
import msi.gama.runtime.IScope;
import msi.gaml.types.Types;
import ummisco.gama.ui.controls.FlatButton;
import ummisco.gama.ui.controls.ParameterExpandBar;
import ummisco.gama.ui.controls.ParameterExpandItem;
import ummisco.gama.ui.experiment.parameters.AgentAttributesEditorsList;
import ummisco.gama.ui.menus.AgentsMenu;
import ummisco.gama.ui.parameters.AbstractEditor;
import ummisco.gama.ui.resources.GamaColors.GamaUIColor;
import ummisco.gama.ui.resources.GamaFonts;
import ummisco.gama.ui.resources.IGamaColors;
import ummisco.gama.ui.views.toolbar.IToolbarDecoratedView;

public class AgentInspectView extends AttributesEditorsView<IAgent>
		implements IToolbarDecoratedView.Pausable /* implements GamaSelectionListener */ {

	public static final String ID = IGui.AGENT_VIEW_ID;
	public String firstPartName = null;

	@Override
	public void addOutput(final IDisplayOutput output) {

		if (output == null) {
			reset();
			return;
		}
		if (!(output instanceof InspectDisplayOutput)) { return; }
		final InspectDisplayOutput out = (InspectDisplayOutput) output;
		final IAgent[] agents = out.getLastValue();
		if (agents == null || agents.length == 0) {
			reset();
			return;
		}

		final IAgent agent = agents[0];
		if (getParentComposite() == null) {
			super.addOutput(out);
		} else if (editors == null || !editors.getCategories().containsKey(agent)) {
			super.addOutput(out);
			addItem(agent);
		}
	}

	@Override
	public void ownCreatePartControl(final Composite parent) {
		// DEBUG.LOG("Inspector creating its own part control");
		parent.setBackground(parent.getBackground());
		if (!outputs.isEmpty()) {
			final IAgent[] init = getOutput().getLastValue();
			if (init != null && init.length > 0) {
				for (final IAgent a : init) {
					addItem(a);
				}
			}
		}
	}

	@Override
	public InspectDisplayOutput getOutput() {
		return (InspectDisplayOutput) super.getOutput();
	}

	@Override
	public boolean areItemsClosable() {
		return true;
	}

	@Override
	protected Composite createItemContentsFor(final IAgent agent) {
		final Composite attributes = super.createItemContentsFor(agent);
		final Label l = AbstractEditor.createLeftLabel(attributes, "Actions", false);
		l.setFont(GamaFonts.getExpandfont());
		final Composite composite = new Composite(attributes, SWT.NONE);
		composite.setBackground(attributes.getBackground());
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.minimumWidth = 150;
		composite.setLayoutData(data);

		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 5;

		composite.setLayout(layout);
		final FlatButton b = FlatButton.menu(composite, IGamaColors.BLUE, "Select...");
		b.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Menu m = new Menu(b);
				AgentsMenu.createMenuForAgent(m, agent, agent instanceof ITopLevelAgent, false);
				m.setVisible(true);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				widgetSelected(e);
			}
		});

		return attributes;
	}

	@Override
	public boolean addItem(final IAgent agent) {
		if (editors == null) {
			editors = new AgentAttributesEditorsList();
		}
		updatePartName();
		if (!editors.getCategories().containsKey(agent)) {
			editors.add(getParametersToInspect(agent), agent);
			// DEBUG.LOG("Asking to create the item " + agent.getName()
			// + " in inspector");
			final ParameterExpandItem item = createItem(getParentComposite(), agent, true, null);
			if (item == null) { return false; }
			return true;
		}
		return false;
	}

	@Override
	protected ParameterExpandItem buildConcreteItem(final ParameterExpandBar bar, final IAgent data,
			final GamaUIColor color) {
		return new ParameterExpandItem(bar, data, SWT.None, 0, color);
	}

	private List<IParameter> getParametersToInspect(final IAgent agent) {
		final Map<String, String> names = getOutput().getAttributes();
		if (names == null) { return new ArrayList<IParameter>(agent.getSpecies().getVars()); }
		final List<IParameter> params = new ArrayList<>();
		for (final String s : names.keySet()) {
			if (agent.getSpecies().getVar(s) != null)
				params.add(agent.getSpecies().getVar(s));
			else
				params.add(buildAttribute(agent, s, names.get(s)));
		}
		return params;
	}

	private IParameter buildAttribute(final IAgent agent, final String att, final String t) {
		final IParameter result = new ParameterAdapter(att, Types.get(t).id()) {

			@Override
			public void setValue(final IScope scope, final Object value) {
				agent.setAttribute(att, value);
			}

			@Override
			public boolean isEditable() {
				return true;
			}

			@Override
			public boolean isDefined() {
				return true;
			}

			@Override
			public Object value() {
				return agent.getAttribute(att);
			}

		};
		return result;
	}

	@Override
	public void removeItem(final IAgent a) {
		InspectDisplayOutput found = null;
		for (final IDisplayOutput out : outputs) {
			final InspectDisplayOutput output = (InspectDisplayOutput) out;
			final IAgent[] agents = output.getLastValue();
			if (agents != null && agents.length > 0 && agents[0] == a) {
				found = output;
				break;
			}
		}
		if (found != null) {
			found.close();
			removeOutput(found);
		}
		updatePartName();
		super.removeItem(a);
	}

	public void updatePartName() {
		if (firstPartName == null) {
			final InspectDisplayOutput out = getOutput();
			firstPartName = out == null ? "Inspect: " : out.getName();
		}
		final Set<String> names = new LinkedHashSet<>();
		for (final IOutput o : outputs) {
			final InspectDisplayOutput out = (InspectDisplayOutput) o;
			final IAgent a = out.getLastValue()[0];
			if (a != null) {
				names.add(a.getName());
			}
		}
		this.setPartName(firstPartName + " " + (names.isEmpty() ? "" : names.toString()));
	}

	/**
	 * Method pauseChanged()
	 * 
	 * @see ummisco.gama.ui.views.toolbar.IToolbarDecoratedView.Pausable#pauseChanged()
	 */
	@Override
	public void pauseChanged() {}

	/**
	 * Method synchronizeChanged()
	 * 
	 * @see ummisco.gama.ui.views.toolbar.IToolbarDecoratedView.Pausable#synchronizeChanged()
	 */
	@Override
	public void synchronizeChanged() {}

	/**
	 * Method handleMenu()
	 * 
	 * @see msi.gama.common.interfaces.ItemList#handleMenu(java.lang.Object, int, int)
	 */
	@Override
	public Map<String, Runnable> handleMenu(final IAgent data, final int x, final int y) {
		return null;
	}

}
