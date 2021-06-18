/*********************************************************************************************
 *
 * 'FileEditor.java, in plugin ummisco.gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.ui.parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;

import msi.gama.kernel.experiment.IParameter;
import msi.gama.kernel.experiment.InputParameter;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.file.IGamaFile;
import msi.gaml.operators.Files;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ummisco.gama.ui.controls.FlatButton;
import ummisco.gama.ui.interfaces.EditorListener;
import ummisco.gama.ui.resources.IGamaColors;
import ummisco.gama.ui.utils.WorkbenchHelper;

@SuppressWarnings ({ "rawtypes", "unchecked" })
public class FileEditor extends AbstractEditor<IGamaFile> {

	private FlatButton textBox;

	FileEditor(final IScope scope, final IAgent agent, final IParameter param, final EditorListener l) {
		super(scope, agent, param, l);
	}

	FileEditor(final IScope scope, final Composite parent, final String title, final String value,
			final EditorListener<IGamaFile> whenModified) {
		// Convenience method
		super(scope, new InputParameter(title, value), whenModified);
		this.createComposite(parent);
	}

	@Override
	public Control createCustomParameterControl(final Composite comp) {
		textBox = FlatButton.menu(comp, IGamaColors.NEUTRAL, "").light().small();
		textBox.setText("No file");
		textBox.addSelectionListener(this);
		return textBox;
	}

	@Override
	protected GridData getParameterGridData() {
		final GridData d = new GridData(SWT.FILL, SWT.TOP, true, false);
		d.minimumWidth = 50;
		return d;
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		final FileDialog dialog = new FileDialog(WorkbenchHelper.getDisplay().getActiveShell(), SWT.NULL);
		IGamaFile file = currentValue;
		dialog.setFileName(file.getPath(getScope()));
		dialog.setText("Choose a file for parameter '" + param.getTitle() + "'");
		final String path = dialog.open();
		if (path != null) {
			file = Files.from(getScope(), path);
			modifyAndDisplayValue(file);
		}
	}

	@Override
	protected void displayParameterValue() {
		internalModification = true;
		if (currentValue == null) {
			textBox.setText("No file");
		} else {
			final IGamaFile file = currentValue;
			String path;
			try {
				path = file.getPath(getScope());
			} catch (final GamaRuntimeException e) {
				path = file.getOriginalPath();
			}

			textBox.setToolTipText(path);
			textBox.setText(path);
		}
		internalModification = false;
	}

	@Override
	public Control getEditorControl() {
		return textBox;
	}

	@Override
	public IType getExpectedType() {
		return Types.FILE;
	}

	@Override
	protected void applyEdit() {
		widgetSelected(null);
	}

	@Override
	protected int[] getToolItems() {
		return new int[] { EDIT, REVERT };
	}

}
