package comokit.gama.outputs.layer.table;

import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gaml.descriptions.IDescription;
import msi.gaml.types.IType;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.layers.AbstractLayerStatement;
import msi.gama.precompiler.IConcept;

import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;


/**
 * 
 * @author Dat
 *..
 */
@symbol (
		name = "tabledata",
		kind = ISymbolKind.LAYER,
		with_sequence = true,
		concept = { IConcept.DISPLAY, "tabledata"})
@inside (
		symbols = {IKeyword.DISPLAY})
@facets (
		value = {
			@facet (
				name = IKeyword.NAME,
				type = IType.STRING,
				optional = true,
				doc = @doc ("the identifier of the table layer"))},
		omissible = IKeyword.NAME)

public class TableLayerStatment extends AbstractLayerStatement{

	public TableLayerStatment(IDescription desc) throws GamaRuntimeException {
		super(desc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public LayerType getType(LayeredDisplayOutput output) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean _init(IScope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean _step(IScope scope) {
		// TODO Auto-generated method stub
		return false;
	}

}
