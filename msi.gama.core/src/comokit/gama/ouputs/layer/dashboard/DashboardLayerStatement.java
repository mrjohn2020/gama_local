package comokit.gama.ouputs.layer.dashboard;

import static msi.gama.common.interfaces.IKeyword.DASHBOARD;

import java.util.LinkedHashMap;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.outputs.LayoutStatement;
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
		kind = ISymbolKind.OUTPUT,
		with_sequence = false,
		unique_in_context = true,
		concept = { IConcept.DISPLAY })
@inside (
		symbols = { IKeyword.OUTPUT })
@facets (
		value = {
				@facet (
						name = IKeyword.VALUE,
						type = IType.NONE,
						optional = true,
						doc = @doc ("Either #none, to indicate that no layout will be imposed, or one of the four possible predefined layouts: #stack, #split, #horizontal or #vertical. This layout will be applied to both experiment and simulation display views. In addition, it is possible to define a custom layout using the horizontal() and vertical() operators")),
						@facet (
								name = "editors",
								type = IType.BOOL,
								optional = true,
								doc = @doc ("Whether the editors should initially be visible or not")),
						@facet (
								name = "toolbars",
								type = IType.BOOL,
								optional = true,
								doc = @doc ("Whether the displays should show their toolbar or not")),
						@facet (
								name = "controls",
								type = IType.BOOL,
								optional = true,
								doc = @doc ("Whether the experiment should show its control toolbar on top or not")),
						@facet (
								name = "parameters",
								type = IType.BOOL,
								optional = true,
								doc = @doc ("Whether the parameters view is visible or not (true by default)")),
						@facet (
								name = "navigator",
								type = IType.BOOL,
								optional = true,
								doc = @doc ("Whether the navigator view is visible or not (true by default)")),
						@facet (
								name = "consoles",
								type = IType.BOOL,
								optional = true,
								doc = @doc ("Whether the consoles are visible or not (true by default)")),
						@facet (
								name = "tray",
								type = IType.BOOL,
								optional = true,
								doc = @doc ("Whether the bottom tray is visible or not (true by default)")),
						@facet (
								name = "tabs",
								type = IType.BOOL,
								optional = true,
								doc = @doc ("Whether the displays should show their tab or not")), 
						//new facet for dashboard
						@facet (
								name = "metric_position",
								type = IType.STRING,
								optional = true,
								doc = @doc ("Set position for metric group")),
						@facet (
								name = "proportion_position",
								type = IType.STRING,
								optional = true,
								doc = @doc ("Set position for proportion group")),
						@facet (
								name = "comparision_position",
								type = IType.STRING,
								optional = true,
								doc = @doc ("Set position for comparision group")),
						@facet (
								name = "diagram_position",
								type = IType.STRING,
								optional = true,
								doc = @doc ("Set position for diagram group")),
						@facet (
								name = "map_position",
								type = IType.STRING,
								optional = true,
								doc = @doc ("Set position for map group")),
						@facet (
								name = "table_position",
								type = IType.STRING,
								optional = true,
								doc = @doc ("Set position for table group"))},
		omissible = IKeyword.VALUE)

@doc (
		value = "Represents the layout of the dashboard views of simulations and experiments",
		usages = { @usage (
				value = "For instance, this layout statement will allow to split the screen occupied by displays in four equal parts, with no tabs. Pairs of display::weight represent the number of the display in their order of definition and their respective weight within a horizontal and vertical section",
				examples = { @example (
						value = "dashboard horizontal([vertical([0::5000,1::5000])::5000,vertical([2::5000,3::5000])::5000]) tabs: false;",
						isExecutable = false) }) })


public class DashboardLayerStatement extends LayoutStatement {
	
	
	
	
	public DashboardLayerStatement(final IDescription desc) {
		super(desc);
		// TODO Auto-generated constructor stub
	}
	
	
	public IExpression getMetricPosition(final IScope scope) {
		IExpression string1 = getFacet("metric_position");
		return string1;
	}
	
	
		

}
