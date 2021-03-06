/*********************************************************************************************
 *
 * 'AbstractEditor.java, in plugin ummisco.gama.ui.shared, is part of the source code of the GAMA modeling and
 * simulation platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.ui.parameters;

import static ummisco.gama.ui.utils.PreferencesHelper.CORE_EDITORS_HIGHLIGHT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import msi.gama.application.workbench.ThemeHelper;
import msi.gama.common.util.StringUtils;
import msi.gama.kernel.experiment.ExperimentParameter;
import msi.gama.kernel.experiment.IParameter;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.GamaStringType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import msi.gaml.variables.Variable;
import ummisco.gama.ui.interfaces.EditorListener;
import ummisco.gama.ui.interfaces.IParameterEditor;
import ummisco.gama.ui.resources.GamaColors;
import ummisco.gama.ui.resources.GamaFonts;
import ummisco.gama.ui.resources.GamaIcons;
import ummisco.gama.ui.resources.IGamaColors;
import ummisco.gama.ui.resources.IGamaIcons;
import ummisco.gama.ui.utils.WorkbenchHelper;

public abstract class AbstractEditor<T>
		implements SelectionListener, ModifyListener, Comparable<AbstractEditor<T>>, IParameterEditor<T> {

	private class ItemSelectionListener extends SelectionAdapter {

		private final int code;

		ItemSelectionListener(final int code) {
			this.code = code;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			switch (code) {
				case REVERT:
					modifyAndDisplayValue(applyRevert());
					break;
				case PLUS:
					modifyAndDisplayValue(applyPlus());
					break;
				case MINUS:
					modifyAndDisplayValue(applyMinus());
					break;
				case EDIT:
					applyEdit();
					break;
				case INSPECT:
					applyInspect();
					break;
				case BROWSE:
					applyBrowse();
					break;
				case CHANGE:
					if (e.detail != SWT.ARROW) { return; }
					applyChange();
					break;
				case DEFINE:
					applyDefine();
					break;
			}
		}

	}

	// public static final Color NORMAL_BACKGROUND = IGamaColors.PARAMETERS_BACKGROUND.color();

	public final Color HOVERED_BACKGROUND() {
		return getNormalBackground();
		// return (ThemeHelper.isDark() ? IGamaColors.VERY_DARK_GRAY : IGamaColors.VERY_LIGHT_GRAY).color();
	}

	// public static final Color CHANGED_BACKGROUND = IGamaColors.TOOLTIP.color();
	private static int ORDER;
	private final Integer order = ORDER++;
	private final IAgent agent;
	private final IScope scope;
	protected String name;
	protected Label titleLabel = null;
	protected final IParameter param;
	boolean acceptNull = true;
	private T originalValue = null;
	protected T currentValue = null;
	List<T> possibleValues = null;
	final Boolean isCombo;
	private final Boolean isEditable;
	protected Number minValue;
	protected Number maxValue;
	Combo combo;
	private CLabel fixedValue;
	protected volatile boolean internalModification;
	private final EditorListener<T> listener;
	protected Composite composite;
	protected final Button[] items = new Button[8];
	boolean isSubParameter;
	Composite parent;
	protected Composite toolbar;
	protected Set<Control> controlsThatShowHideToolbars = new HashSet<>();
	protected Text unitItem;
	private final MouseTrackListener hideShowToolbarListener = new MouseTrackListener() {

		@Override
		public void mouseEnter(final MouseEvent e) {
			if (GAMA.getExperiment() == null || !GAMA.getExperiment().isBatch()) {
				showToolbar();
			}
		}

		@Override
		public void mouseExit(final MouseEvent e) {
			if (isCombo && combo != null && combo.getListVisible()) { return; }
			if (GAMA.getExperiment() == null || !GAMA.getExperiment().isBatch()) {
				hideToolbar();
			}
		}

		@Override
		public void mouseHover(final MouseEvent e) {}

	};
	private boolean dontUseScope;

	public AbstractEditor(final IScope scope, final IParameter variable) {
		this(scope, null, variable, null);
	}

	public AbstractEditor(final IScope scope, final IParameter variable, final EditorListener<T> l) {
		this(scope, null, variable, l);
	}

	public AbstractEditor(final IScope scope, final IAgent a, final IParameter variable) {
		this(scope, a, variable, null);
	}

	@Override
	public IScope getScope() {
		if (dontUseScope) { return null; }
		if (scope != null) { return scope; }
		if (agent != null) { return agent.getScope(); }
		return GAMA.getRuntimeScope();
	}

	public AbstractEditor(final IScope scope, final IAgent a, final IParameter variable, final EditorListener<T> l) {
		this.scope = scope;
		param = variable;
		agent = a;
		if (param != null) {
			isCombo = param.getAmongValue(getScope()) != null;
			isEditable = param.isEditable();
			name = param.getTitle();
			minValue = param.getMinValue(getScope());
			maxValue = param.getMaxValue(getScope());
		} else {
			isCombo = false;
			isEditable = true;
			name = "";
		}
		listener = l;
	}

	// public boolean isSubParameter() {
	// return isSubParameter;
	// }

	@Override
	public void isSubParameter(final boolean b) {
		isSubParameter = b;
	}

	protected abstract int[] getToolItems();

	@Override
	public void setActive(final Boolean active) {
		if (titleLabel != null) {
			titleLabel.setForeground(
					active ? (ThemeHelper.isDark() ? IGamaColors.VERY_LIGHT_GRAY.color() : IGamaColors.BLACK.color())
							: GamaColors.system(SWT.COLOR_GRAY));
		}
		if (!active) {
			for (final Button t : items) {
				if (t == null) {
					continue;
				}
				t.setEnabled(false);
			}
		} else {
			checkButtons();
		}

		this.getEditor().setEnabled(active);
	}

	private final void valueModified(final Object newValue) throws GamaRuntimeException {

		var a = agent;

		if (param instanceof ExperimentParameter) {
			if (a == null) {
				final var exp = GAMA.getExperiment();
				if (exp != null) {
					a = exp.getAgent();
				}
			}
			if (a != null && GAMA.getExperiment() != null && GAMA.getExperiment().getAgent() != null) {
				GAMA.getExperiment().getAgent().getScope().setAgentVarValue(a, param.getName(), newValue);
			}
			// Introduced to deal with #2306
			if (agent == null) {
				param.setValue(a == null ? null : a.getScope(), newValue);
			}
		} else {
			// param.setValue(a == null ? null : a.getScope(), newValue);
			if (a == null) {
				param.setValue(null, newValue);
			} else {
				if (param instanceof Variable) {
					((Variable) param).setVal(scope, a, newValue);
				} else {
					// a.setDirectVarValue(scope, param.getName(), newValue);
					param.setValue(a.getScope(), newValue);
				}
			}
		}
	}

	@Override
	public IType<?> getExpectedType() {
		return Types.NO_TYPE;
	}

	// In case the editor allows to edit the expression, should it be evaluated
	// ?
	protected boolean evaluateExpression() {
		return true;
	}

	@Override
	public int compareTo(final AbstractEditor<T> e) {
		return order.compareTo(e.order);
	}

	public Label getLabel() {
		return titleLabel;
	}

	public Control getEditor() {
		return !isEditable ? fixedValue : isCombo ? combo : getEditorControl();
	}

	protected abstract Control getEditorControl();

	protected Control createEditorControl(final Composite comp) {
		Control paramControl;
		try {
			paramControl = !isEditable ? createLabelParameterControl(comp)
					: isCombo ? createComboParameterControl(comp) : createCustomParameterControl(comp);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("The editor for " + name + " could not be created");
			GAMA.reportError(GAMA.getRuntimeScope(), e1, false);
			return null;
		}

		final var data = getParameterGridData();
		paramControl.setLayoutData(data);
		paramControl.setBackground(getNormalBackground());
		addToolbarHiders(paramControl);
		return paramControl;
	}

	protected Color getNormalBackground() {
		return parent.getBackground();
	}

	protected Color getChangedBackground() {
		return ThemeHelper.isDark() ? IGamaColors.DARK_ORANGE.color() : IGamaColors.TOOLTIP.color();
	}

	public static Label createLeftLabel(final Composite parent, final String title, final boolean isSubParameter) {
		final var label = new Label(parent, SWT.WRAP | SWT.RIGHT);
		label.setBackground(parent.getBackground());
		final var d = new GridData(SWT.END, SWT.CENTER, true, true);
		d.minimumWidth = 70;
		if (isSubParameter) {
			d.horizontalIndent = 30;
		}
		label.setLayoutData(d);
		label.setFont(GamaFonts.getLabelfont());
		label.setText(title);
		return label;
	}

	public void resizeLabel(final int width) {
		final var l = getLabel();
		if (l != null) {
			((GridData) l.getLayoutData()).widthHint = width;
		}
	}

	public void createComposite(final Composite comp) {
		this.parent = comp;
		internalModification = true;
		titleLabel = createLeftLabel(comp, name, isSubParameter);
		titleLabel.setForeground(GamaColors.getTextColorForBackground(titleLabel.getBackground()).color());
		// IGamaColors.BLACK.color()); by default, see #2601
		try {
			setOriginalValue(getParameterValue());
		} catch (final GamaRuntimeException e1) {
			e1.addContext("Impossible to obtain the value of " + name);
			GAMA.reportError(GAMA.getRuntimeScope(), e1, false);
		}
		currentValue = getOriginalValue();
		composite = new Composite(comp, SWT.NONE);
		composite.setBackground(getNormalBackground());
		final var data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.minimumWidth = 150;
		composite.setLayoutData(data);

		final var layout = new GridLayout(2, false);
		layout.marginWidth = 5;

		composite.setLayout(layout);
		createEditorControl(composite);
		toolbar = createToolbar2();

		if (isEditable && !isCombo) {
			displayParameterValueAndCheckButtons();
		}
		internalModification = false;
		composite.layout();

		addToolbarHiders(composite, toolbar, titleLabel);
		for (final Button b : items) {
			addToolbarHiders(b);
		}
		for (final Control c : controlsThatShowHideToolbars) {
			c.addMouseTrackListener(hideShowToolbarListener);
			c.addDisposeListener(e -> {
				c.removeMouseTrackListener(hideShowToolbarListener);
				controlsThatShowHideToolbars.remove(c);
			});
		}
		if (GAMA.getExperiment() == null || !GAMA.getExperiment().isBatch()) {
			hideToolbar();
		}
	}

	protected void addToolbarHiders(final Control... c) {
		for (final Control control : c) {
			if (control != null) {
				controlsThatShowHideToolbars.add(control);
			}
		}
	}

	protected void hideToolbar() {
		if (toolbar == null || toolbar.isDisposed()) { return; }
		final var d = (GridData) toolbar.getLayoutData();
		if (d.exclude) { return; }
		d.exclude = true;
		toolbar.setVisible(false);
		composite.setBackground(getNormalBackground());
		composite.layout(true, true);
		composite.update();
	}

	protected void showToolbar() {
		if (toolbar == null || toolbar.isDisposed()) { return; }
		final var d = (GridData) toolbar.getLayoutData();
		if (!d.exclude) { return; }
		d.exclude = false;
		toolbar.setVisible(true);
		composite.setBackground(HOVERED_BACKGROUND());
		composite.layout(true, true);
		composite.update();
	}

	protected String computeUnitLabel() {
		var s = typeToDisplay();
		if (minValue != null) {
			final var min = StringUtils.toGaml(minValue, false);
			if (maxValue != null) {
				s += " [" + min + ".." + StringUtils.toGaml(maxValue, false) + "]";
			} else {
				s += ">= " + min;
			}
		} else {
			if (maxValue != null) {
				s += "<=" + StringUtils.toGaml(maxValue, false);
			}
		}
		final var u = param.getUnitLabel(getScope());
		if (u != null) {
			s += " " + u;
		}
		return s;
	}

	protected String typeToDisplay() {
		if (!this.isEditable) { return ""; }
		return param.getType().serialize(false);
	}

	protected Composite createToolbar2() {
		final var t = new Composite(composite, SWT.NONE);
		final var d = new GridData(SWT.FILL, SWT.TOP, false, false);
		t.setLayoutData(d);
		t.setBackground(HOVERED_BACKGROUND());
		final var id =
				GridLayoutFactory.fillDefaults().equalWidth(false).extendedMargins(0, 0, 0, 0).spacing(0, 0).create();
		final var gd = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).indent(0, -1).create();
		t.setLayout(id);
		final var unitText = computeUnitLabel();
		if (!unitText.isEmpty()) {
			unitItem = new Text(t, SWT.READ_ONLY | SWT.FLAT);
			unitItem.setText(unitText);
			unitItem.setBackground(HOVERED_BACKGROUND());
			unitItem.setEnabled(false);
		}
		if (isEditable) {
			final var codes = this.getToolItems();
			for (final int i : codes) {
				Button item = null;
				switch (i) {
					case REVERT:
						item = createItem(t, "Revert to original value", GamaIcons.create("small.revert").image());
						break;
					case PLUS:
						item = createPlusItem(t);
						break;
					case MINUS:
						item = createItem(t, "Decrement the parameter",
								GamaIcons.create(IGamaIcons.SMALL_MINUS).image());
						break;
					case EDIT:
						item = createItem(t, "Edit the parameter", GamaIcons.create("small.edit").image());
						break;
					case INSPECT:
						item = createItem(t, "Inspect the agent", GamaIcons.create("small.inspect").image());
						break;
					case BROWSE:
						item = createItem(t, "Browse the list of agents", GamaIcons.create("small.browse").image());
						break;
					case CHANGE:
						item = createItem(t, "Choose another agent", GamaIcons.create("small.change").image());
						break;
					case DEFINE:
						item = createItem(t, "Set the parameter to undefined",
								GamaIcons.create("small.undefine").image());
				}
				if (item != null) {
					items[i] = item;
					item.setBackground(HOVERED_BACKGROUND());
					item.setLayoutData(GridDataFactory.copyData(gd));

					item.addSelectionListener(new ItemSelectionListener(i));

				}
			}
		}
		id.numColumns = t.getChildren().length;
		t.layout();
		t.pack();
		return t;

	}

	protected ToolItem createPlusItem(final ToolBar t) {
		final var item = createItem(t, "Increment the parameter", GamaIcons.create(IGamaIcons.SMALL_PLUS).image());
		return item;
	}

	protected Button createPlusItem(final Composite t) {
		final var item = createItem(t, "Increment the parameter", GamaIcons.create(IGamaIcons.SMALL_PLUS).image());
		return item;
	}

	/**
	 * @param string
	 * @param image
	 */
	private ToolItem createItem(final ToolBar t, final String string, final Image image) {
		final var i = new ToolItem(t, SWT.FLAT | SWT.PUSH);
		i.setToolTipText(string);
		i.setImage(image);
		return i;
	}

	private Button createItem(final Composite t, final String string, final Image image) {
		final var i = new Button(t, SWT.FLAT | SWT.TRANSPARENT | SWT.PUSH);
		i.setToolTipText(string);
		i.setImage(image);
		return i;
	}

	@SuppressWarnings ("unchecked")
	protected T getParameterValue() throws GamaRuntimeException {
		Object result;
		if (agent == null || !agent.getSpecies().hasVar(param.getName())) {
			result = param.value(scope);
		} else {
			result = scope.getAgentVarValue(getAgent(), param.getName());
		}
		if (getExpectedType() == Types.STRING) {
			return (T) StringUtils.toJavaString(GamaStringType.staticCast(scope, result, false));
		}
		return (T) getExpectedType().cast(scope, result, null, false);

	}

	protected EditorListener<?> getListener() {
		return listener;
	}

	protected void setParameterValue(final T val) {
		WorkbenchHelper.asyncRun(() -> {
			try {
				if (listener == null) {
					valueModified(val);
				} else {
					listener.valueModified(val);
				}
			} catch (final GamaRuntimeException e) {
				e.printStackTrace();
				e.addContext("Value of " + name + " cannot be modified");
				GAMA.reportError(GAMA.getRuntimeScope(), GamaRuntimeException.create(e, GAMA.getRuntimeScope()), false);
				return;
			}
		});
	}

	protected GridData getParameterGridData() {
		final var d = new GridData(SWT.FILL, SWT.TOP, true, false);
		d.minimumWidth = 100;
		return d;
	}

	protected abstract Control createCustomParameterControl(Composite comp) throws GamaRuntimeException;

	protected Control createLabelParameterControl(final Composite comp) {
		fixedValue = new CLabel(comp, SWT.READ_ONLY | SWT.BORDER_SOLID);
		fixedValue
				.setForeground(ThemeHelper.isDark() ? IGamaColors.VERY_LIGHT_GRAY.color() : IGamaColors.BLACK.color());
		// force text color, see #2601
		fixedValue.setText(getOriginalValue() instanceof String ? (String) getOriginalValue()
				: StringUtils.toGaml(getOriginalValue(), false));
		// addToolbarHiders(fixedValue);
		return fixedValue;
	}

	protected Control createComboParameterControl(final Composite comp) {
		possibleValues = new ArrayList<T>(param.getAmongValue(getScope()));
		final var valuesAsString = new String[possibleValues.size()];
		for (var i = 0; i < possibleValues.size(); i++) {
			// if ( param.isLabel() ) {
			// valuesAsString[i] = possibleValues.get(i).toString();
			// } else {
			if (getExpectedType() == Types.STRING) {
				valuesAsString[i] = StringUtils.toJavaString(StringUtils.toGaml(possibleValues.get(i), false));
			} else {
				valuesAsString[i] = StringUtils.toGaml(possibleValues.get(i), false);
				// }
			}
		}
		combo = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setForeground(ThemeHelper.isDark() ? IGamaColors.VERY_LIGHT_GRAY.color() : IGamaColors.BLACK.color());
		// force text color, see #2601
		combo.setItems(valuesAsString);
		combo.select(possibleValues.indexOf(getOriginalValue()));
		// combo.addModifyListener(new ModifyListener() {
		//
		// @Override
		// public void modifyText(final ModifyEvent me) {
		// modifyValue(possibleValues.get(combo.getSelectionIndex()));
		// }
		// });
		combo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent me) {
				modifyValue(possibleValues.get(combo.getSelectionIndex()));
			}
		});

		final var d = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		d.minimumWidth = 48;
		// d.widthHint = 100; // SWT.DEFAULT
		combo.setLayoutData(d);
		combo.pack();
		return combo;
	}

	protected abstract void displayParameterValue();

	protected void checkButtons() {
		final var revert = items[REVERT];
		if (revert == null || revert.isDisposed()) { return; }
		revert.setEnabled(currentValue == null ? originalValue != null : !currentValue.equals(originalValue));
	}

	@Override
	public boolean isValueModified() {
		return isValueDifferent(getOriginalValue());
	}

	public boolean isValueDifferent(final Object newVal) {
		return newVal == null ? currentValue != null : !newVal.equals(currentValue);
	}

	@Override
	public void revertToDefaultValue() {
		modifyAndDisplayValue(getOriginalValue());
	}

	@Override
	public IParameter getParam() {
		return param;
	}

	@SuppressWarnings ("unchecked")
	// Passes Object on purpose so that Float and Int editors can cast it.
	protected void modifyValue(final Object val) throws GamaRuntimeException {
		if (!isValueDifferent(val)) { return; }
		currentValue = (T) val;
		WorkbenchHelper.asyncRun(() -> {
			if (CORE_EDITORS_HIGHLIGHT.getValue()) {
				if (titleLabel != null && !titleLabel.isDisposed()) {
					titleLabel.setBackground(isValueModified() ? getChangedBackground() : getNormalBackground());
				}
			}
		});

		if (!internalModification) {
			setParameterValue((T) val);
		}
	}

	@Override
	public void updateValue(final boolean force) {
		try {
			final var newVal = getParameterValue();
			if (!force && !isValueDifferent(newVal)) { return; }
			internalModification = true;
			if (titleLabel != null && !titleLabel.isDisposed()) {
				modifyAndDisplayValue(newVal);
			}
			internalModification = false;
		} catch (final GamaRuntimeException e) {
			e.addContext("Unable to obtain the value of " + name);
			GAMA.reportError(GAMA.getRuntimeScope(), e, false);
			return;
		}
	}

	@Override
	public void forceUpdateValueAsynchronously() {
		final var newVal = getParameterValue();
		// if (!isValueDifferent(newVal))
		// return;
		currentValue = newVal;
		WorkbenchHelper.asyncRun(() -> {
			internalModification = true;
			if (titleLabel != null && !titleLabel.isDisposed()) {
				titleLabel.setBackground(isValueModified() ? getChangedBackground() : getNormalBackground());
			}
			if (!parent.isDisposed()) {
				if (!isEditable) {
					fixedValue.setText(newVal instanceof String ? (String) newVal : StringUtils.toGaml(newVal, false));
				} else if (isCombo) {
					combo.select(possibleValues.indexOf(newVal));
				} else {
					displayParameterValue();
					checkButtons();
				}
				composite.update();
				internalModification = false;
			}
		});

	}

	private void displayParameterValueAndCheckButtons() {
		WorkbenchHelper.run(() -> {
			displayParameterValue();
			checkButtons();
		});

	}

	protected final void modifyAndDisplayValue(final T val) {
		modifyValue(val);
		WorkbenchHelper.asyncRun(() -> {
			if (!isEditable) {
				if (!fixedValue.isDisposed()) {
					fixedValue.setText(val instanceof String ? (String) val : StringUtils.toGaml(val, false));
				}
			} else if (isCombo) {
				if (!combo.isDisposed()) {
					combo.select(possibleValues.indexOf(val));
				}
			} else {
				displayParameterValueAndCheckButtons();
			}
			if (!composite.isDisposed()) {
				composite.update();
			}
		});

	}

	protected IAgent getAgent() {
		if (agent != null) { return agent; }
		if (scope == null) { return null; }
		return scope.getSimulation();

	}

	@Override
	public void modifyText(final ModifyEvent e) {}

	@Override
	public void widgetSelected(final SelectionEvent e) {}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {}

	protected T getOriginalValue() {
		return originalValue;
	}

	protected void setOriginalValue(final T originalValue) {
		this.originalValue = originalValue;
	}

	protected T applyPlus() {
		return null;
	}

	protected T applyMinus() {
		return null;
	}

	protected T applyRevert() {
		return getOriginalValue();
	}

	protected void applyBrowse() {}

	protected void applyInspect() {}

	protected void applyEdit() {}

	protected void applyChange() {}

	protected void applyDefine() {}

	public Composite getComposite() {
		return composite;
	}

	@Override
	public T getCurrentValue() {
		return currentValue;
	}

	public void dontUseScope(final boolean dont) {
		this.dontUseScope = dont;

	}

}
