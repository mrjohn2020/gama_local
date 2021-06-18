/*********************************************************************************************
 *
 * 'Solver.java, in plugin ummisco.gaml.extensions.maths, is part of the source code of the GAMA modeling and simulation
 * platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gaml.extensions.maths.ode.utils.solver;

import java.util.Map;

import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaPair;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gaml.extensions.maths.ode.statements.SystemOfEquationsStatement;

public abstract class Solver {

	final FirstOrderIntegrator integrator;
	int count;
	final double step;

	Solver(final double step, final FirstOrderIntegrator integrator, final IMap<String, IList<Double>> integrated_val) {
		this.step = step;
		this.integrator = integrator;
		if (integrated_val != null) {
			integrator.addStepHandler(new StepHandler() {

				@Override
				public void init(final double t0, final double[] y0, final double t) {}

				@Override
				public void handleStep(final StepInterpolator interpolator, final boolean isLast) {
					final double time = interpolator.getCurrentTime();
					final double[] y = interpolator.getInterpolatedState();
					count++;
					storeValues(time, y, integrated_val);
				}
			});
		}
	}

	// Call the integrator, which should call computeDerivatives on the system
	// of equations;
	public void solve(final IScope scope, final SystemOfEquationsStatement seq, final double initialTime,
			final double finalTime, final IMap<String, IList<Double>> integrationValues) {

		seq.executeInScope(scope, () -> {
			final Map<Integer, IAgent> equationAgents = seq.getEquationAgents(scope);

			// GamaMap<Integer, GamaPair<IAgent, SingleEquationStatement>> myEQ = seq.getEquations(scope.getAgent());
			final IMap<Integer, GamaPair<IAgent, IExpression>> myVar = seq.getVariableDiff(scope.getAgent());
			/*
			 * prepare initial value of variables 1. loop through variables expression 2. if its equaAgents != null, it
			 * mean variable of external equation, set current scope to this agent scope 3. get value 4. return to
			 * previous scope
			 */

			final double[] y = new double[myVar.size()];
			// final ArrayList<IExpression> equationValues = new
			// ArrayList<IExpression>(eq.variables_diff.values());
			int i = 0;
			final int n = myVar.size();
			for (i = 0; i < n; i++) {
				final IAgent a = equationAgents.get(i);
				final String eqkeyname = a + myVar.get(i).getValue().toString();
				if (integrationValues.get(eqkeyname) == null) {
					integrationValues.put(eqkeyname, GamaListFactory.create(Double.class));
				}
				if (!a.dead()) {
					final boolean pushed = scope.push(a);
					try {
						y[i] = Cast.asFloat(scope, myVar.get(i).getValue().value(scope));

						if (Double.isInfinite(y[i])) {
							GAMA.reportAndThrowIfNeeded(scope,
									GamaRuntimeException.create(new NotANumberException(), scope), true);
						}
					} catch (final Exception ex1) {
						DEBUG.OUT(ex1.getMessage());
					} finally {
						if (pushed) {
							scope.pop(a);
						}
					}
				}

			}
			if (integrationValues.get(scope.getAgent() + seq.variable_time.getName()) == null) {
				integrationValues.put(scope.getAgent() + seq.variable_time.getName(),
						GamaListFactory.create(Double.class));
			}

			if (scope.getClock().getCycle() == 0) {
				storeValues(initialTime, y, integrationValues);
			}
			if (y.length > 0) {
				try {
					integrator.integrate(seq, initialTime, y, finalTime, y);
				} catch (final Exception ex) {
					DEBUG.OUT(ex.toString());
				}
			}

			seq.assignValue(scope, finalTime, y);
			storeValues(finalTime, y, integrationValues);
		});

	}

	void storeValues(final double time, final double[] y, final IMap<String, IList<Double>> integrationValues) {
		if (integrationValues != null) {
			for (int i = 0; i < y.length; i++) {
				integrationValues.valueAt(i).add(y[i]);
			}
			integrationValues.valueAt(y.length).add(time);
		}

	}
}
