/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.util.matrix;

import java.util.*;
import msi.gama.common.util.RandomUtils;
import msi.gama.metamodel.shape.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gaml.operators.Cast;
import com.google.common.primitives.Doubles;
import com.vividsolutions.jts.index.quadtree.IntervalSize;

public class GamaFloatMatrix extends GamaMatrix<Double> {

	static public GamaFloatMatrix from(final IScope scope, final IMatrix m) {
		if ( m instanceof GamaFloatMatrix ) { return (GamaFloatMatrix) m; }
		if ( m instanceof GamaObjectMatrix ) { return new GamaFloatMatrix(scope, m.getCols(scope), m.getRows(scope),
			((GamaObjectMatrix) m).matrix); }
		if ( m instanceof GamaIntMatrix ) { return new GamaFloatMatrix(scope, m.getCols(scope), m.getRows(scope),
			((GamaIntMatrix) m).matrix); }
		return null;
	}

	static public GamaFloatMatrix from(final IScope scope, final int c, final int r, final IMatrix m) {
		if ( m instanceof GamaFloatMatrix ) { return new GamaFloatMatrix(scope, c, r, ((GamaFloatMatrix) m).matrix); }
		if ( m instanceof GamaObjectMatrix ) { return new GamaFloatMatrix(scope, c, r, ((GamaObjectMatrix) m).matrix); }
		if ( m instanceof GamaIntMatrix ) { return new GamaFloatMatrix(scope, c, r, ((GamaIntMatrix) m).matrix); }
		return null;
	}

	double[] matrix;

	public GamaFloatMatrix(final IScope scope, final double[] mat) {
		super(scope, 1, mat.length);
		matrix = mat;
	}

	public GamaFloatMatrix(final IScope scope, final GamaPoint p) {
		this(scope, (int) p.x, (int) p.y);
	}

	public GamaFloatMatrix(final IScope scope, final int cols, final int rows) {
		super(scope, cols, rows);
		matrix = new double[cols * rows];
	}

	public GamaFloatMatrix(final IScope scope, final int cols, final int rows, final double[] objects) {
		this(scope, cols, rows);
		java.lang.System.arraycopy(objects, 0, matrix, 0, Math.min(objects.length, rows * cols));
	}

	public GamaFloatMatrix(final IScope scope, final int cols, final int rows, final int[] objects) {
		this(scope, cols, rows);
		java.lang.System.arraycopy(objects, 0, matrix, 0, Math.min(objects.length, rows * cols));
	}

	public GamaFloatMatrix(final IScope scope, final int cols, final int rows, final Object[] objects) {
		this(scope, cols, rows);
		for ( int i = 0, n = Math.min(objects.length, rows * cols); i < n; i++ ) {
			matrix[i] = Cast.asFloat(null, objects[i]);
		}
	}

	public GamaFloatMatrix(final IScope scope, final List objects, final boolean flat, final GamaPoint preferredSize)
		throws GamaRuntimeException {
		super(scope, objects, flat, preferredSize);
		matrix = new double[numRows * numCols];
		if ( preferredSize != null ) {
			for ( int i = 0, stop = Math.min(matrix.length, objects.size()); i < stop; i++ ) {
				matrix[i] = Cast.asFloat(scope, objects.get(i));
			}
		} else if ( flat || GamaMatrix.isFlat(objects) ) {
			for ( int i = 0, stop = objects.size(); i < stop; i++ ) {
				matrix[i] = Cast.asFloat(scope, objects.get(i));
			}
		} else {
			for ( int i = 0; i < numRows; i++ ) {
				for ( int j = 0; j < numCols; j++ ) {
					set(scope, j, i, Cast.asFloat(scope, ((List) objects.get(j)).get(i)));
				}
			}
		}
	}

	public GamaFloatMatrix(final IScope scope, final Object[] mat) {
		this(scope, 1, mat.length);
		for ( int i = 0; i < mat.length; i++ ) {
			matrix[i] = Cast.asFloat(null, mat[i]);
		}
	}

	@Override
	protected void _clear() {
		Arrays.fill(matrix, 0d);
	}

	@Override
	public boolean _contains(final IScope scope, final Object o) {
		if ( o instanceof Double ) {
			final Double d = (Double) o;
			for ( int i = 0; i < matrix.length; i++ ) {
				if ( IntervalSize.isZeroWidth(matrix[i], d) ) { return true; }
			}
		}
		return false;
	}

	@Override
	public Double _first(final IScope scope) {
		if ( matrix.length == 0 ) { return 0d; }
		return matrix[0];
	}

	@Override
	public Double _last(final IScope scope) {
		if ( matrix.length == 0 ) { return 0d; }
		return matrix[matrix.length - 1];
	}

	@Override
	public Integer _length(final IScope scope) {
		return matrix.length;
	}

	// @Override
	// public Double _max(final IScope scope) {
	// Double max = -Double.MAX_VALUE;
	// for ( int i = 0; i < matrix.length; i++ ) {
	// if ( matrix[i] > max ) {
	// max = Double.valueOf(matrix[i]);
	// }
	// }
	// return max;
	// }
	//
	// @Override
	// public Double _min(final IScope scope) {
	// Double min = Double.MAX_VALUE;
	// for ( int i = 0; i < matrix.length; i++ ) {
	// if ( matrix[i] < min ) {
	// min = Double.valueOf(matrix[i]);
	// }
	// }
	// return min;
	// }
	//
	// @Override
	// public Double _product(final IScope scope) {
	// double result = 1.0;
	// for ( int i = 0, n = matrix.length; i < n; i++ ) {
	// result *= matrix[i];
	// }
	// return result;
	// }
	//
	// @Override
	// public Double _sum(final IScope scope) {
	// double result = 0.0;
	// for ( int i = 0, n = matrix.length; i < n; i++ ) {
	// result += matrix[i];
	// }
	// return result;
	// }
	//
	@Override
	public boolean _isEmpty(final IScope scope) {
		for ( int i = 0; i < matrix.length; i++ ) {
			if ( matrix[i] != 0d ) { return false; }
		}
		return true;
	}

	@Override
	public GamaList _listValue(final IScope scope) {
		return new GamaList(matrix);
	}

	@Override
	protected IMatrix _matrixValue(final IScope scope, final ILocation preferredSize) {
		if ( preferredSize == null ) { return this; }
		final int cols = (int) preferredSize.getX();
		final int rows = (int) preferredSize.getY();
		return new GamaFloatMatrix(scope, cols, rows, matrix);
	}

	@Override
	public IMatrix _reverse(final IScope scope) throws GamaRuntimeException {
		final IMatrix result = new GamaFloatMatrix(scope, numRows, numCols);
		for ( int i = 0; i < numCols; i++ ) {
			for ( int j = 0; j < numRows; j++ ) {
				result.set(scope, j, i, get(scope, i, j));
			}
		}
		return result;
	}

	@Override
	public GamaFloatMatrix copy(final IScope scope) {
		return new GamaFloatMatrix(scope, numCols, numRows, matrix);
	}

	@Override
	public boolean equals(final Object m) {
		if ( this == m ) { return true; }
		if ( !(m instanceof GamaFloatMatrix) ) { return false; }
		final GamaFloatMatrix mat = (GamaFloatMatrix) m;
		return Arrays.equals(this.matrix, mat.matrix);
	}

	@Override
	public int hashCode() {
		return matrix.hashCode();
	}

	@Override
	public void _putAll(final IScope scope, final Object o, final Object param) throws GamaRuntimeException {
		// TODO Exception if o == null
		// TODO Verify the type
		Arrays.fill(matrix, ((Double) o).doubleValue());
	}

	@Override
	public Double get(final IScope scope, final int col, final int row) {
		if ( col >= numCols || col < 0 || row >= numRows || row < 0 ) { return 0d; }
		return matrix[row * numCols + col];
	}

	// public void put(final int col, final int row, final double obj) {
	// if ( !(col >= numCols || col < 0 || row >= numRows || row < 0) ) {
	// matrix[row * numCols + col] = obj;
	// }
	// }

	@Override
	public void set(final IScope scope, final int col, final int row, final Object obj) throws GamaRuntimeException {
		if ( !(col >= numCols || col < 0 || row >= numRows || row < 0) ) {
			matrix[row * numCols + col] = Cast.asFloat(scope, obj);
		}
		// put(col, row, Cast.asFloat(GAMA.getDefaultScope(), obj).doubleValue());
	}

	private boolean remove(final double o) {
		for ( int i = 0; i < matrix.length; i++ ) {
			if ( new Double(matrix[i]).equals(new Double(o)) ) {
				matrix[i] = 0d;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean _removeFirst(final IScope scope, final Double o) throws GamaRuntimeException {
		// Exception if o == null
		return remove(o.doubleValue());
	}

	@Override
	public Double remove(final IScope scope, final int col, final int row) {
		if ( col >= numCols || col < 0 || row >= numRows || row < 0 ) { return 0d; }
		final double o = matrix[row * numCols + col];
		matrix[row * numCols + col] = 0d;
		return o;
	}

	private boolean removeAll(final double o) {
		boolean removed = false;
		for ( int i = 0; i < matrix.length; i++ ) {
			if ( new Double(matrix[i]).equals(new Double(o)) ) {
				matrix[i] = 0d;
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public boolean _removeAll(final IScope scope, final IContainer<?, Double> list) {
		// TODO Exception if o == null
		for ( final Double o : list ) {
			removeAll(o.doubleValue());
		}
		// TODO Make a test to verify the return
		return true;
	}

	@Override
	public void shuffleWith(final RandomUtils randomAgent) {
		matrix = randomAgent.shuffle(matrix);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(numRows * numCols * 5);
		sb.append('[');
		for ( int row = 0; row < numRows; row++ ) {
			for ( int col = 0; col < numCols; col++ ) {
				sb.append(get(null, col, row));
				if ( col < numCols - 1 ) {
					sb.append(',');
				}
			}
			if ( row < numRows - 1 ) {
				sb.append(';');
			}
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public String toGaml() {
		return new GamaList(this.matrix).toGaml() + " as matrix";
	}

	/**
	 * Method iterator()
	 * @see msi.gama.util.matrix.GamaMatrix#iterator()
	 */
	@Override
	public Iterator<Double> iterator() {
		return Doubles.asList(matrix).iterator();
	}

	//
	// @Override
	// public String toJava() {
	// return "GamaMatrixType.from(" + Cast.toJava(new GamaList(this.matrix)) + ", false)";
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see msi.gama.interfaces.IGamaContainer#checkValue(java.lang.Object)
	 */
	// @Override
	// public boolean checkValue(final Object value) {
	// return value instanceof Double;
	// }
}
