/*********************************************************************************************
 *
 * 'LayeredDisplayView.java, in plugin ummisco.gama.ui.experiment, is part of the source code of the GAMA modeling and
 * simulation platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.ui.views.displays;

import static msi.gama.common.preferences.GamaPreferences.Displays.CORE_DISPLAY_BORDER;
import static msi.gama.common.preferences.GamaPreferences.Runtime.CORE_SYNC;

import java.awt.Color;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGamaView;
import msi.gama.common.interfaces.ILayerManager;
import msi.gama.kernel.experiment.ITopLevelAgent;
import msi.gama.outputs.IDisplayOutput;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import ummisco.gama.ui.resources.GamaColors;
import ummisco.gama.ui.resources.GamaIcons;
import ummisco.gama.ui.resources.IGamaColors;
import ummisco.gama.ui.utils.WorkbenchHelper;
import ummisco.gama.ui.views.GamaViewPart;
import ummisco.gama.ui.views.toolbar.GamaToolbar2;
import ummisco.gama.ui.views.toolbar.IToolbarDecoratedView;

public abstract class LayeredDisplayView extends GamaViewPart
	implements IToolbarDecoratedView.Pausable, IToolbarDecoratedView.Zoomable, IGamaView.Display {

    protected int realIndex = -1;
    protected SashForm form;
    public Composite surfaceComposite;
    public final LayeredDisplayDecorator decorator;
    protected volatile boolean disposed;
    // protected volatile boolean realized;
    Thread updateThread;
    private volatile boolean lockAcquired = false;

    @Override
    public void setIndex(final int index) {
	realIndex = index;
    }

    @Override
    public int getIndex() {
	return realIndex;
    }

    public LayeredDisplayView() {
	decorator = new LayeredDisplayDecorator(this);
    }

    public Control controlToSetFullScreen() {
	return form;
    }

    public SashForm getSash() {
	return form;
    }

    @Override
    public boolean containsPoint(final int x, final int y) {
	if (super.containsPoint(x, y)) {
	    return true;
	}
	final Point o = getSurfaceComposite().toDisplay(0, 0);
	final Point s = getSurfaceComposite().getSize();
	return new Rectangle(o.x, o.y, s.x, s.y).contains(x, y);
    }

    @Override
    public void init(final IViewSite site) throws PartInitException {
	super.init(site);
	if (getOutput() != null) {
	    setPartName(getOutput().getName());
	}
    }

    @Override
    public void addOutput(final IDisplayOutput out) {
	super.addOutput(out);
	if (out instanceof LayeredDisplayOutput) {
	    ((LayeredDisplayOutput) out).getData().addListener(decorator);
	    final IScope scope = out.getScope();
	    if (scope != null && scope.getSimulation() != null) {
		final ITopLevelAgent root = scope.getRoot();
		final Color color = root.getColor();
		this.setTitleImage(GamaIcons.createTempColorIcon(GamaColors.get(color)));
	    }
	}
    }

    public boolean isOpenGL() {
	if (outputs.isEmpty()) {
	    return false;
	}
	return getOutput().getData().isOpenGL();
    }

    public ILayerManager getDisplayManager() {
	return getDisplaySurface().getManager();
    }

    public Composite getSurfaceComposite() {
	return surfaceComposite;
    }

    @Override
    public void ownCreatePartControl(final Composite c) {
    
    	// SET LAYOUT FOR PARENT COMPOSITE
    	c.setLayout(new RowLayout(SWT.HORIZONTAL));
//    	c.setLayout(new FillLayout(SWT.HORIZONTAL));
    	
    	// BUTTON ADD PANE 300x200
    	Button button = new Button(toolbar, SWT.PUSH);
    	button.setText("Add Pane 300x200");
    	button.addListener(SWT.Selection, new Listener() {
  
			@Override
			public void handleEvent(Event event) {
				createPane(c, 0);
    	    	c.layout();
			}
    		
    	});
    	
    	// BUTTON ADD PANE 200x250
    	Button button2 = new Button(toolbar, SWT.PUSH);
    	button2.setText("Add Pane 200x250");
    	button2.addListener(SWT.Selection, new Listener() {
    		  
			@Override
			public void handleEvent(Event event) {
				createPane(c, 1);
    	    	c.layout();
			}
    		
    	});
    	Button radioBtn = new Button(toolbar, SWT.RADIO);
    	radioBtn.setText("Row Layout");
    	radioBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				c.setLayout(new RowLayout(SWT.HORIZONTAL));
				c.layout();
			}
    		
    	});
    	Button radioBtn2 = new Button(toolbar, SWT.RADIO);
    	radioBtn2.setText("Free Moving Layout");
    	radioBtn2.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				c.setLayout(null);
				c.layout();
			}
    		
    	});
    	
    	// COMPONENT TEST
    	final Composite composite = new Composite(c, SWT.BORDER | SWT.NONE);
    	composite.setEnabled(false);
    	composite.setBounds(0, 50,300, 200);
    	composite.setLayout(new FillLayout());
    	composite.setLayoutData(new RowData(500,300));
    	createSurfaceComposite(composite);
    	surfaceComposite.setLayoutData(fullData());

    	final Composite comp = new Composite(c, SWT.BORDER | SWT.RESIZE);
    	comp.setEnabled(false);
    	comp.setBounds(310,50, 200, 250);
    	comp.setLayout(new FillLayout());
    	comp.setLayoutData(new RowData(200,250));

    	// MOUSE EVENT
    	final Point[] offset = new Point[1];
    	final Point[] offset1 = new Point[1];
    	Listener listener = new Listener() {
    	    public void handleEvent(Event event) {
    		switch (event.type) {
    		case SWT.MouseDown:
    		    Rectangle rect = composite.getBounds();
    		    if (rect.contains(event.x, event.y)) {
    			Point pt1 = composite.toDisplay(0, 0);
    			Point pt2 = c.toDisplay(event.x, event.y);
    			offset[0] = new Point(pt2.x - pt1.x, pt2.y - pt1.y);
    		    }
    		    Rectangle rect1 = comp.getBounds();
    		    if (rect1.contains(event.x, event.y)) {
    			Point pt1 = comp.toDisplay(0, 0);
    			Point pt2 = c.toDisplay(event.x, event.y);
    			offset1[0] = new Point(pt2.x - pt1.x, pt2.y - pt1.y);
    		    }
    		    break;
    		case SWT.MouseMove:
    		    if (offset[0] != null) {
    			Point pt = offset[0];
    			composite.setLocation(event.x - pt.x, event.y - pt.y);
    		    }
    		    if (offset1[0] != null) {
    			Point pt = offset1[0];
    			comp.setLocation(event.x - pt.x, event.y - pt.y);
    		    }
    		    break;
    		case SWT.MouseUp:
    		    offset[0] = null;
    		    offset1[0] = null;
    		    break;
    		}
    	    }
    	};

    	c.addListener(SWT.MouseDown, listener);
    	c.addListener(SWT.MouseUp, listener);
    	c.addListener(SWT.MouseMove, listener);
    	getOutput().setSynchronized(getOutput().isSynchronized() || CORE_SYNC.getValue());
    	// form.setMaximizedControl(centralPanel);
    	// decorator.createDecorations(form);
//    	decorator.createDecorations();
    	c.layout();

    }
    
    public void createPane(Composite c, int type) {
    	// Parent Composite
    	Composite pane = new Composite(c, SWT.BORDER);
    	pane.setLayout(new RowLayout(SWT.VERTICAL));
    	if(type == 1) {
    		if(c.getLayout() == null) {
    			pane.setBounds(100, 100, 200, 250);
    		}else {
        		pane.setLayoutData(new RowData(200,250));	
    		}
    	} else {
    		if(c.getLayout() == null) {
    			pane.setBounds(100, 100, 300, 200);
    		}else {
        		pane.setLayoutData(new RowData(300,200));	
    		}
    	}
    	
    	// Toolbar (sub-composite)
    	Composite tbar = new Composite(pane, SWT.NONE);
    	FillLayout f = new FillLayout();
    	f.spacing = 10;
    	f.marginHeight = 10;
    	f.marginWidth = 10;
    	tbar.setLayout(f);
    	
    	Button btn1 = new Button(tbar, SWT.PUSH);
    	btn1.setText("Add");
    	Button btn2 = new Button(tbar, SWT.PUSH);
    	btn2.setText("Remove");
    	btn2.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				pane.dispose();
				c.requestLayout();
			}
    		
    	});
    	
    	// Content (sub-composite)
    	Composite cont = new Composite(pane, SWT.NONE);
    	cont.setLayout(new FillLayout());
    }

    GridLayout emptyLayout() {
	final GridLayout gl = new GridLayout(1, true);
	gl.horizontalSpacing = 0;
	gl.marginHeight = 0;
	gl.marginWidth = 0;
	gl.verticalSpacing = 0;
	return gl;
    }

    GridData fullData() {
	return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    public void setFocus() {
	if (getParentComposite() != null && !getParentComposite().isDisposed()
		&& !getParentComposite().isFocusControl()) {
	    // decorator.keyAndMouseListener.focusGained(null);
	    getParentComposite().forceFocus();
	}
    }

    protected abstract Composite createSurfaceComposite(Composite parent);

    @Override
    public LayeredDisplayOutput getOutput() {
	return (LayeredDisplayOutput) super.getOutput();
    }

    @Override
    public IDisplaySurface getDisplaySurface() {
	final LayeredDisplayOutput out = getOutput();
	if (out != null) {
	    return out.getSurface();
	}
	return null;
    }

    @Override
    public void widgetDisposed(final DisposeEvent e) {
	if (disposed) {
	    return;
	}
	final LayeredDisplayOutput output = getOutput();
	if (output != null) {
	    output.getData().listeners.clear();
	    final IDisplaySurface s = output.getSurface();
	    if (isOpenGL() && s != null) {
		s.dispose();
		output.setSurface(null);
	    }
	}

	disposed = true;
	if (surfaceComposite != null) {
	    try {
		surfaceComposite.dispose();
	    } catch (final RuntimeException ex) {

	    }
	}
	releaseLock();
	// }
	if (updateThread != null) {
	    updateThread.interrupt();
	}

	if (decorator != null) {
	    decorator.dispose();
	}

	super.widgetDisposed(e);
    }

    @Override
    public void pauseChanged() {
	decorator.updateOverlay();
    }

    public boolean forceOverlayVisibility() {
	return false;
    }

    @Override
    public void synchronizeChanged() {
	decorator.updateOverlay();
    }

    @Override
    public void zoomIn() {
	if (getDisplaySurface() != null) {
	    getDisplaySurface().zoomIn();
	}
    }

    @Override
    public void zoomOut() {
	if (getDisplaySurface() != null) {
	    getDisplaySurface().zoomOut();
	}
    }

    @Override
    public void zoomFit() {
	if (getDisplaySurface() != null) {
	    getDisplaySurface().zoomFit();
	}
    }

    @Override
    public Control[] getZoomableControls() {
	return new Control[] { getParentComposite() };
    }

    @Override
    protected GamaUIJob createUpdateJob() {
	return new GamaUIJob() {

	    @Override
	    protected UpdatePriority jobPriority() {
		return UpdatePriority.HIGHEST;
	    }

	    @Override
	    public IStatus runInUIThread(final IProgressMonitor monitor) {
		if (getDisplaySurface() == null) {
		    return Status.CANCEL_STATUS;
		}
		getDisplaySurface().updateDisplay(false);
		return Status.OK_STATUS;
	    }
	};
    }

    @Override
    public void update(final IDisplayOutput output) {

	// Fix for issue #1693
	final boolean oldSync = output.isSynchronized();
	if (output.isInInitPhase()) {
	    output.setSynchronized(false);
	}
	// end fix
	if (updateThread == null) {
	    updateThread = new Thread(() -> {
		final IDisplaySurface s = getDisplaySurface();
		// if (s != null && !s.isDisposed() && !disposed) {
		// s.updateDisplay(false);
		// }
		while (!disposed) {

		    if (s != null && s.isRealized() && !s.isDisposed() && !disposed) {
			acquireLock();
			s.updateDisplay(false);
			if (s.getData().isAutosave()) {
			    SnapshotMaker.getInstance().doSnapshot(output, s, surfaceComposite);
			}
			// Fix for issue #1693
			if (output.isInInitPhase()) {
			    output.setInInitPhase(false);
			    output.setSynchronized(oldSync);
			    // end fix
			}

		    }

		}
	    });
	    updateThread.start();
	}

	if (output.isSynchronized()) {
	    final IDisplaySurface s = getDisplaySurface();
	    s.updateDisplay(false);
	    if (getOutput().getData().isAutosave() && s.isRealized()) {
		SnapshotMaker.getInstance().doSnapshot(output, s, surfaceComposite);
	    }
	    while (!s.isRendered() && !s.isDisposed() && !disposed) {
		try {
		    Thread.sleep(10);
		} catch (final InterruptedException e) {
		    e.printStackTrace();
		}

	    }
	} else if (updateThread.isAlive()) {
	    releaseLock();
	}

    }

    synchronized void acquireLock() {
	while (lockAcquired) {
	    try {
		wait();
	    } catch (final InterruptedException e) {
	    }
	}
	lockAcquired = true;
    }

    private synchronized void releaseLock() {
	lockAcquired = false;
	notify();
    }

    @Override
    public boolean zoomWhenScrolling() {
	return true;
    }

    @Override
    public void removeOutput(final IDisplayOutput output) {
	if (output == null) {
	    return;
	}
	if (output == getOutput()) {
	    if (isFullScreen()) {
		WorkbenchHelper.run(() -> toggleFullScreen());
	    }
	}
	output.dispose();
	outputs.remove(output);
	if (outputs.isEmpty()) {
	    close(GAMA.getRuntimeScope());
	}
    }

    @Override
    public boolean isFullScreen() {
	return decorator.isFullScreen();
    }

    @Override
    public void toggleSideControls() {
	decorator.toggleSideControls();
    }

    @Override
    public void toggleOverlay() {
	decorator.toggleOverlay();
    }

    @Override
    public void toggleFullScreen() {
	decorator.toggleFullScreen();
    }

    @Override
    public void createToolItems(final GamaToolbar2 tb) {
	super.createToolItems(tb);
	decorator.createToolItems(tb);
    }

    @Override
    public void showToolbar() {
	toolbar.show();
    }

    @Override
    public void hideToolbar() {
	toolbar.hide();
    }

    @Override
    public void showOverlay() {
	decorator.overlay.setVisible(true);
    }

    @Override
    public void hideOverlay() {
	decorator.overlay.setVisible(false);
    }

    /**
     * A call indicating that fullscreen has been set on the display. Views
     * might decide to do something or not. Default is to do nothing.
     */
    public void fullScreenSet() {
    }

}
