/*******************************************************************************************************
 *
 * msi.gaml.operators.Displays.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.operators;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.no_test;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gama.util.tree.GamaNode;
import msi.gaml.types.IType;

public class Displays {

	public static final String HORIZONTAL = "horizontal";
	public static final String VERTICAL = "vertical";
	public static final String STACK = "stack";
	
	//new
	public static final String DASHBOARD = "dashboard_type";
	
	
	
	// @operator (
	// value = IKeyword.LAYOUT,
	// can_be_const = false,
	// doc = @doc (""))
	//
	// public static GamaTree<String> layout(final IScope scope, final GamaNode<String> root) {
	// final GamaTree<String> tree = GamaTree.withRoot(IKeyword.LAYOUT);
	// root.attachTo(tree.getRoot());
	// DEBUG.OUT("Tree: " + tree);
	// return tree;
	// }
	//
	// @operator (
	// value = IKeyword.LAYOUT,
	// can_be_const = false,
	// doc = @doc (""))
	//
	// public static GamaTree<String> layout(final IScope scope, final GamaPair<Object, Integer> pair) {
	// if (pair.key instanceof GamaNode && ((GamaNode<?>) pair.key).getData() instanceof String) { return layout(scope,
	// (GamaNode<String>) pair.key); }
	// throw GamaRuntimeException.error("Layout argument is not recognized", scope);
	// }
	//
	// @operator (
	// value = IKeyword.LAYOUT,
	// can_be_const = false,
	// doc = @doc (""))
	//
	// public static GamaTree<String> layout(final IScope scope, final IMap<Object, Integer> map) {
	// if (map != null) {
	// if (map.size() == 1) {
	// final GamaPair<Object, Integer> pair = (GamaPair<Object, Integer>) map.getPairs().firstValue(scope);
	// if (pair.key instanceof GamaNode && ((GamaNode<?>) pair.key)
	// .getData() instanceof String) { return layout(scope, (GamaNode<String>) pair.key); }
	// } else {
	// return layout(scope, horizontal(scope, map));
	// }
	// }
	// throw GamaRuntimeException.error("Layout argument is not recognized", scope);
	// }

	@operator (
			value = HORIZONTAL,
			expected_content_type = IType.FLOAT,
			can_be_const = false)
	@doc ("Creates a horizontal layout node (a sash). Sashes can contain any number (> 1) of other elements: stacks, horizontal or vertical sashes, or display indices. Each element is represented by a pair in the map, where the key is the element and the value its weight within the sash")
	@no_test
	public static GamaNode<String> horizontal(final IScope scope, final LinkedHashMap<Object,Integer> nodes) {
		return buildSashFromMap(scope, HORIZONTAL, nodes);
	}

	@operator (
			value = VERTICAL,
			expected_content_type = IType.FLOAT,
			can_be_const = false)
	@doc ("Creates a vertical layout node (a sash). Sashes can contain any number (> 1) of other elements: stacks, horizontal or vertical sashes, or display indices. Each element is represented by a pair in the map, where the key is the element and the value its weight within the sash")
	@no_test
	public static GamaNode<String> vertical(final IScope scope, final LinkedHashMap<Object,Integer> nodes) {
		return buildSashFromMap(scope, VERTICAL, nodes);
	}

	@operator (
			value = STACK,
			can_be_const = false)
	@doc ("Creates a stack layout node. Stacks can only contain one or several indices of displays (without weight)")
	@no_test
	public static GamaNode<String> stack(final IScope scope, final IList<Integer> nodes) {
		if (nodes == null) { throw GamaRuntimeException.error("Nodes of a stack cannot be nil", scope); }
		if (nodes.isEmpty()) {
			throw GamaRuntimeException.error("At least one display must be defined in the stack", scope);
		}
		final GamaNode<String> node = new GamaNode<>(STACK);
		nodes.forEach(n -> node.addChild(String.valueOf(n)));
		return node;
	}
	
	// new
	@operator (
			value = DASHBOARD,
			expected_content_type = IType.STRING,
			can_be_const = false)
	@doc ("Creates a dashboard layout node")
	@no_test
	public static GamaNode<String> dashboard_type(final IScope scope, final String type) {
		return buildDashboardLayout(scope, type);
	}
	
	
	private static GamaNode<String> buildDashboardLayout(final IScope scope,final String type) {
		
		String[] values = {"strategic","operational","analytical","tatical"};
		boolean contains = Arrays.stream(values).anyMatch(type::equals);
		if(type.isEmpty() || !contains) {
			return null;
		} else {
			switch(type) {
				case("strategic"): return buildDashboardStrategic(scope, type); 
				case("operational"): return buildDashboardOperational(scope, type); 
				case("analytical"): return buildDashboardAnalytical(scope, type); 
				case("tatical"): return buildDashboardTatical(scope, type); 
				default: {}
			}
		}
		return null;
	}
	
	private static GamaNode<String> buildDashboardStrategic(final IScope scope, final String type) {
		int unit_size = 1000;
		
		final LinkedHashMap<Object,Integer> nodes1 = new LinkedHashMap<Object, Integer>();
		nodes1.put(0, 3*unit_size);
		nodes1.put(1, 3*unit_size);
		nodes1.put(2, 3*unit_size);
		
		final LinkedHashMap<Object,Integer> nodes2 = new LinkedHashMap<Object, Integer>();
		nodes2.put(3, 5*unit_size);
		nodes2.put(4, 3*unit_size);
		nodes2.put(5, 2*unit_size);
		
		final GamaNode<String> node = new GamaNode<>(VERTICAL);
		node.addChild(buildSashFromMap(scope, HORIZONTAL, nodes1));
		node.addChild(buildSashFromMap(scope, HORIZONTAL, nodes2));
		
		return node;
	}
	
	private static GamaNode<String> buildDashboardOperational(final IScope scope, final String type) {
		/*
		 * with layout in GAML: horizontal([0::2260,vertical([horizontal([1::5000,2::5000])::5000,vertical([horizontal([3::5000,4::5000])::5000,5::5000])::5000])::7740])
		 */
		int unit_size = 1000;
		
		final GamaNode<String> node5 = new GamaNode<>(HORIZONTAL);
		final LinkedHashMap<Object,Integer> nodes5 = new LinkedHashMap<Object, Integer>();
		nodes5.put(3, 5*unit_size);
		nodes5.put(4, 5*unit_size);
		node5.addChild(buildSashFromMap(scope, HORIZONTAL, nodes5));
		
		final GamaNode<String> node4 = new GamaNode<>(VERTICAL);
		final LinkedHashMap<Object,Integer> nodes4 = new LinkedHashMap<Object, Integer>();
		nodes4.put(node5, 5000);
		nodes4.put(5, 5000);
		node4.addChild(buildSashFromMap(scope, VERTICAL, nodes4));
		
		final GamaNode<String> node3 = new GamaNode<>(HORIZONTAL);
		final LinkedHashMap<Object,Integer> nodes3 = new LinkedHashMap<Object, Integer>();
		nodes3.put(1, 5000);
		nodes3.put(2, 5000);
		node3.addChild(buildSashFromMap(scope, HORIZONTAL, nodes3));
		
		final GamaNode<String> node2 = new GamaNode<>(VERTICAL);
		final LinkedHashMap<Object,Integer> nodes2 = new LinkedHashMap<Object, Integer>();
		nodes2.put(node3, 5000);
		nodes2.put(node4, 5000);
		node2.addChild(buildSashFromMap(scope, VERTICAL, nodes2));

		final GamaNode<String> node = new GamaNode<>(HORIZONTAL);
		final LinkedHashMap<Object,Integer> nodes = new LinkedHashMap<Object, Integer>();
		nodes.put(0, 2260);
		nodes.put(node2, 7740);
		node.addChild(buildSashFromMap(scope, HORIZONTAL, nodes));
		
		return node;
	}
	
	private static GamaNode<String> buildDashboardAnalytical(final IScope scope, final String type) {
		return null;
	}
	
	private static GamaNode<String> buildDashboardTatical(final IScope scope, final String type) {
		return null;
	}
	

	@SuppressWarnings ("unchecked")
	private static GamaNode<String> buildSashFromMap(final IScope scope, final String orientation,
			final LinkedHashMap<Object,Integer> nodes) {
		if (nodes == null) {
			throw GamaRuntimeException.error("Nodes of a " + orientation + " layout cannot be nil", scope);
		}
		if (nodes.size() < 1) {
			throw GamaRuntimeException.error("At least two elements must be defined in this " + orientation + " layout",
					scope);
		}
		
		final GamaNode<String> node = new GamaNode<>(orientation);
		nodes.forEach((key, value) -> {
			if (key instanceof GamaNode) {
				final GamaNode<String> n = (GamaNode<String>) key;
				n.setWeight(Cast.asInt(scope, value));
				n.attachTo(node);
			} else {
				final Integer index = Cast.asInt(scope, key);
				node.addChild(String.valueOf(index), Cast.asInt(scope, value));
			}
		});
		return node;
	}

}
