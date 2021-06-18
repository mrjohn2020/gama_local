package comokit.gama.ouputs.layer.dashboard;

import static msi.gama.common.interfaces.IKeyword.DASHBOARD;

import java.util.LinkedHashMap;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.LayoutStatement;
import msi.gama.outputs.layers.AbstractLayerStatement;
import msi.gama.outputs.layers.charts.ChartOutput;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.tree.GamaNode;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.ISymbolKind;
import msi.gaml.compilation.ISymbol;
import msi.gaml.compilation.Symbol;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.types.IType;


@symbol (
		name = IKeyword.DASHBOARD,
		kind = ISymbolKind.LAYER,
		with_sequence = true,
		concept = { IConcept.DASHBOARD, IConcept.DISPLAY })
@inside (
		symbols = { IKeyword.DISPLAY })
@facets (
		value = {
				@facet (
						name = IKeyword.NAME,
						type = IType.STRING,
						optional = false,
						doc = @doc ("the identifier of the chart layer")),
				@facet(
					name = "type",
					type = IType.STRING,
					optional = true,
					doc = @doc ("TEST"))
		},
		omissible = IKeyword.NAME)

@doc (value = "Represents the layout of the dashboard views of simulations and experiments")

public class DashboardLayerStatement extends AbstractLayerStatement {

	private DashboardOutput dashoutput = null;
	
	
	public DashboardOutput getOutput() {
		return dashoutput;
	}
	
	public DashboardLayerStatement(IDescription desc) throws GamaRuntimeException {
		super(desc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public LayerType getType(LayeredDisplayOutput output) {
		// TODO Auto-generated method stub
		return LayerType.DASHBOARD;
	}

	
	@Override
	protected boolean _init(final IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		IExpression string1 = getFacet(IKeyword.TYPE);
		
		dashoutput = DashboardJFreeOutput.createDashOutput(scope, getName(), string1);
		dashoutput.createDash(scope);
		
		return true;
	}

	@Override
	protected boolean _step(IScope scope) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	
		

}
