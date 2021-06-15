/*********************************************************************************************
 *
 * 'FreeFlyCamera.java, in plugin ummisco.gama.opengl, is part of the source code of the GAMA modeling and simulation
 * platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.opengl.camera;

import org.eclipse.swt.SWT;

import msi.gama.common.geometry.Envelope3D;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.outputs.LayeredDisplayData;
import msi.gaml.operators.Maths;
import ummisco.gama.opengl.renderer.IOpenGLRenderer;
import ummisco.gama.ui.bindings.GamaKeyBindings;

public class FreeFlyCamera extends AbstractCamera {

	private static final GamaPoint up = new GamaPoint(0.0f, 0.0f, 1.0f);
	private final GamaPoint forward = new GamaPoint(0, 0, 0);
	private final GamaPoint left = new GamaPoint(0, 0, 0);
	private final double speed = getRenderer().getMaxEnvDim() * 0.0001;

	public FreeFlyCamera(final IOpenGLRenderer renderer) {
		super(renderer);
		initialize();
	}

	@Override
	public void updateCartesianCoordinatesFromAngles() {
		if (phi > 89) {
			this.phi = 89;
		} else if (phi < -89) {
			this.phi = -89;
		}
		final double factorP = phi * Maths.toRad;
		final double factorT = theta * Maths.toRad;
		final double r_temp = Math.cos(factorP);
		forward.setLocation(r_temp * Math.cos(factorT), r_temp * Math.sin(factorT), Math.sin(factorP));
		left.setLocation(new GamaPoint(up.y * forward.z - up.z * forward.y, up.z * forward.x - up.x * forward.z,
				up.x * forward.y - up.y * forward.x).normalized());
		setTarget(forward.plus(position));
	}

	@Override
	public double getDistance() {
		return position.minus(target).norm();
	}

	@Override
	public void animate() {
		super.animate();
		if (isForward()) {
			if (shiftPressed) {
				this.phi = phi - -getKeyboardSensivity() * getSensivity();
				updateCartesianCoordinatesFromAngles();
			} else {
				setPosition(position.plus(forward.times(speed * 200))); // go
																		// forward
			}
		}
		if (isBackward()) {
			if (shiftPressed) {
				this.phi = phi - getKeyboardSensivity() * getSensivity();
				updateCartesianCoordinatesFromAngles();
			} else {
				setPosition(position.minus(forward.times(speed * 200))); // go
																			// backward
			}
		}
		if (isStrafeLeft()) {
			if (shiftPressed) {
				this.theta = theta - -getKeyboardSensivity() * getSensivity();
				updateCartesianCoordinatesFromAngles();
			} else {
				setPosition(position.plus(left.times(speed * 200))); // move
																		// on
																		// the
																		// right
			}
		}
		if (isStrafeRight()) {
			if (shiftPressed) {
				this.theta = theta - getKeyboardSensivity() * getSensivity();
				updateCartesianCoordinatesFromAngles();
			} else {
				setPosition(position.minus(left.times(speed * 200))); // move
																		// on
																		// the
																		// left
			}
		}

		setTarget(position.plus(forward));
	}

	@Override
	public void setUpVector(final double xPos, final double yPos, final double zPos) {
		// Not allowed for this camera
	}

	@Override
	public void initialize() {
		upVector.setLocation(up);
		final LayeredDisplayData data = getRenderer().getData();
		final double envWidth = data.getEnvWidth();
		final double envHeight = data.getEnvHeight();
		setPosition(envWidth / 2, -envHeight * 1.75, getRenderer().getMaxEnvDim());
		setTarget(envWidth / 2, -envHeight * 0.5, 0);
		this.phi = -45;
		this.theta = 90;
		updateCartesianCoordinatesFromAngles();
	}

	@Override
	public Double zoomLevel() {
		return getRenderer().getMaxEnvDim() * getInitialZFactor() / position.getZ();
	}

	@Override
	public void zoom(final double level) {
		setPosition(position.x, position.y, getRenderer().getMaxEnvDim() * getInitialZFactor() / level);
		updateCartesianCoordinatesFromAngles();
	}

	@Override
	public void zoom(final boolean in) {
		final float step = Math.abs(getPosition().getZ() != 0 ? (float) position.getZ() / 10 : 0.1f);
		final GamaPoint vector = forward.times(speed * 800 + step);
		setPosition(getPosition().plus(in ? vector : vector.negated()));
		setTarget(forward.plus(getPosition()));
		getRenderer().getData().setZoomLevel(zoomLevel(), true, false);
	}

	@Override
	public void setDistance(final double distance) {
		// ??
	}

	@Override
	public void zoomFocus(final Envelope3D env) {
		final double extent = env.maxExtent();
		final double z;
		if (extent == 0) {
			z = env.getMaxZ() + getRenderer().getMaxEnvDim() / 100d;
		} else {
			z = extent * 1.5;
		}
		setPosition(env.centre().x, env.centre().y, z);
		getRenderer().getData().setZoomLevel(zoomLevel(), true, false);
	}

	@Override
	public void internalMouseMove(final org.eclipse.swt.events.MouseEvent e) {
		super.internalMouseMove(e);
		if ((e.stateMask & SWT.BUTTON_MASK) == 0) { return; }
		if (GamaKeyBindings.shift(e) /** || alt(e)) */
				&& isViewInXYPlan()) {
			getMousePosition().x = e.x;
			getMousePosition().y = e.y;
			getRenderer().getOpenGLHelper().defineROI(
					new GamaPoint(firstMousePressedPosition.x, firstMousePressedPosition.y),
					new GamaPoint(getMousePosition().x, getMousePosition().y));
		} else {
			final int horizMovement = e.x - (int) getLastMousePressedPosition().x;
			final int vertMovement = e.y - (int) getLastMousePressedPosition().y;
			lastMousePressedPosition.setLocation(e.x, e.y, 0);
			this.theta = theta - horizMovement * getSensivity();
			this.phi = phi - vertMovement * getSensivity();
			updateCartesianCoordinatesFromAngles();
		}
	}

	@Override
	protected boolean canSelectOnRelease(final org.eclipse.swt.events.MouseEvent arg0) {
		return GamaKeyBindings.shift(arg0) /* || alt(arg0) */;
	}

	@Override
	public boolean isViewInXYPlan() {
		return phi >= -89 && phi < -85;

	}

	@Override
	protected void drawRotationHelper() {}

}
