/*********************************************************************************************
 *
 * 'StringEditor.java, in plugin ummisco.gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.ui.parameters;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import msi.gama.kernel.experiment.IParameter;
import msi.gama.kernel.experiment.InputParameter;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ummisco.gama.ui.interfaces.EditorListener;

public class StringEditor extends ExpressionBasedEditor<String> {

	StringEditor(final IScope scope, final IAgent agent, final IParameter param, final EditorListener<String> l) {
		super(scope, agent, param, l);
	}

	StringEditor(final IScope scope, final Composite parent, final String title, final Object value,
			final EditorListener<String> whenModified) {
		super(scope, new InputParameter(title, value), whenModified);
		this.createComposite(parent);
	}

	StringEditor(final IScope scope, final Composite parent, final String title, final String value,
			final List<String> among, final EditorListener<String> whenModified, final boolean asLabel) {
		super(scope, new InputParameter(title, value, Types.STRING, among), whenModified);
		this.createComposite(parent);
	}

	@Override
	public IType<String> getExpectedType() {
		return Types.STRING;
	}

	@Override
	protected int[] getToolItems() {
		return new int[] { REVERT };
	}

	@Override
	public boolean evaluateExpression() {
		return true;
	}

}
