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
import java.awt.Frame;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.FontDescriptor;
//import org.eclipse.e4.ui.workbench.UIEvents.ToolItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGamaView;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.ILayerManager;
import msi.gama.kernel.experiment.ITopLevelAgent;
import msi.gama.outputs.IDisplayOutput;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.display.AWTDisplayGraphics;
import msi.gama.outputs.layers.AbstractLayerStatement;
import msi.gama.outputs.layers.charts.ChartJFreeChartOutputBubble;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import ummisco.gama.ui.resources.GamaColors;
import ummisco.gama.ui.resources.GamaIcons;
import ummisco.gama.ui.resources.IGamaColors;
import ummisco.gama.ui.utils.WorkbenchHelper;
import ummisco.gama.ui.views.GamaViewPart;
import ummisco.gama.ui.views.toolbar.GamaToolbar2;
import ummisco.gama.ui.views.toolbar.IToolbarDecoratedView;

import org.eclipse.swt.widgets.ToolItem;

import java.util.Arrays;

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
    //new
    public Composite surfaceComposite2;
    public List<Composite> surfaceComposite_list = new ArrayList<Composite>();
 
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
    	
    	// List pane
    	final List<Composite> list = new ArrayList<Composite>();
    	
    	// SET LAYOUT FOR PARENT COMPOSITE
    	c.setLayout(new RowLayout(SWT.HORIZONTAL));
		    
    	// BUTTON ADD PANE 300x200
    	Button button = new Button(toolbar, SWT.PUSH);
    	button.setText("Add Pane 300x200");
    	button.addListener(SWT.Selection, new Listener() {
  
			@Override
			public void handleEvent(Event event) {
				createPane(c, list);
			}
    		
    	});
    	

    	
//    	// COMPONENT TEST
    	
//    	final Composite composite = new Composite(c, SWT.BORDER | SWT.NONE);
//    	composite.setEnabled(false);
//    	composite.setBounds(0, 50,300, 200);
//    	composite.setLayout(new FillLayout());
//    	composite.setLayoutData(new RowData(500,300));
//    	createSurfaceComposite(composite);
//    	surfaceComposite.setLayoutData(fullData());
    
    	
    	
//    	final Composite comp = new Composite(c, SWT.BORDER | SWT.RESIZE);
//    	comp.setEnabled(false);
//    	comp.setBounds(310,50, 200, 250);
//    	comp.setLayout(new FillLayout());
//    	comp.setLayoutData(new RowData(200,250));
//    	comp.setVisible(true);
////    	ToolBar t = new ToolBar(comp, SWT.NONE);
////    	ToolItem itemBack = new ToolItem(t, SWT.PUSH);
////	    itemBack.setText("Add");
////	    ToolItem itemForward = new ToolItem(t, SWT.PUSH);
////	    itemForward.setText("Delete");
////	    ToolItem itemStop = new ToolItem(t, SWT.PUSH);
////	    itemStop.setText("Zoom In");
////	    ToolItem itemZoomOut = new ToolItem(t, SWT.PUSH);
////	    itemZoomOut.setText("Zoom Out");
//    	createNewSurface(comp);
//    	surfaceComposite2.setLayoutData(fullData());
    	
    	
    	//test
    	int count_surface = getOutput().getLayers().size();
    	for (int i = 0; i< count_surface; i++) {
    		final Composite surface_comp = new Composite(c, SWT.BORDER | SWT.RESIZE);
    		surface_comp.setEnabled(false);
    		surface_comp.setBounds(310,50, 200, 250);
    		surface_comp.setLayout(new FillLayout());
    		surface_comp.setLayoutData(new RowData(400,300));
    		surface_comp.setVisible(true);
     
    		createNewSurfaceFromList(surface_comp, i);
     
    		surfaceComposite_list.get(i).setLayoutData(fullData());
    	}
    	
    	
    	
    	
    	// MOUSE EVENT
    	  	
//    	final Point[] offset = new Point[1];
//    	final Point[] offset1 = new Point[1];
//    	Listener listener = new Listener() {
//    	    public void handleEvent(Event event) {
//    		switch (event.type) {
//    		case SWT.MouseDown:
//    			Rectangle rect = comp.getBounds();
//				if (rect.contains(event.x, event.y)) {
//	    			Point pt1 = comp.toDisplay(0, 0);
//	    			Point pt2 = c.toDisplay(event.x, event.y);
//	    			offset[0] = new Point(pt2.x - pt1.x, pt2.y - pt1.y);
//	    		}
//    		    Rectangle rect1 = composite.getBounds();
//    		    if (rect1.contains(event.x, event.y)) {
//    			Point pt1 = composite.toDisplay(0, 0);
//    			Point pt2 = c.toDisplay(event.x, event.y);
//    			offset1[0] = new Point(pt2.x - pt1.x, pt2.y - pt1.y);
//    		    }
//    		    break;
//    		case SWT.MouseMove:
//    			if (offset[0] != null) {
//	    			Point pt = offset[0];
//	    			comp.setLocation(event.x - pt.x, event.y - pt.y);
//	    		    }
//    		    if (offset1[0] != null) {
//    			Point pt = offset1[0];
//    			composite.setLocation(event.x - pt.x, event.y - pt.y);
//    		    }
//    		    break;
//    		case SWT.MouseUp:
//    			offset[0] = null;
//    		    offset1[0] = null;
//    		    break;
//    		case SWT.MouseWheel:
//    			Tracker tracker = new Tracker(composite.getParent(), SWT.RESIZE);
//    	        tracker.setStippled(true);
//    	        Rectangle rect3 = composite.getBounds();
//    	        tracker.setRectangles(new Rectangle[] { rect3 });
//    	        if (tracker.open()) {
//    	          Rectangle after = tracker.getRectangles()[0];
//    	          composite.setBounds(after);
//    	        }
//    	        tracker.dispose();
//    	        
//    		}
//    	    }
//    	};
//
//    	c.addListener(SWT.MouseDown, listener);
//    	c.addListener(SWT.MouseUp, listener);
//    	c.addListener(SWT.MouseMove, listener);
//    	c.addListener(SWT.MouseWheel, listener);
    	getOutput().setSynchronized(getOutput().isSynchronized() || CORE_SYNC.getValue());
    	// form.setMaximizedControl(centralPanel);
    	// decorator.createDecorations(form);
//    	decorator.createDecorations();
    	c.layout();

    }
    
    public void createPane(Composite c, List<Composite> l) {
    	// Pane Composite
    	Composite pane = new Composite(c, SWT.BORDER);
    	pane.setLayout(new RowLayout(SWT.VERTICAL));
    	pane.setLayoutData(new RowData(300, 50));
    	l.add(pane);
    	
    	// Toolbar of Pane
    	ToolBar t = new ToolBar(pane, SWT.NONE);
    	ToolItem itemBack = new ToolItem(t, SWT.PUSH);
	    itemBack.setText("Add");
	    ToolItem itemForward = new ToolItem(t, SWT.PUSH);
	    itemForward.setText("Delete");
	    ToolItem itemStop = new ToolItem(t, SWT.PUSH);
	    itemStop.setText("Zoom In");
	    ToolItem itemZoomOut = new ToolItem(t, SWT.PUSH);
	    itemZoomOut.setText("Zoom Out");
	    
	    
	    // Event for toolbar
	    Listener listener_toolbar = new Listener() {
	      public void handleEvent(Event event) {
	        ToolItem item = (ToolItem) event.widget;
	        String string = item.getText();
	        if (string.equals("Add"))
	        {	
	        	ShowSelectionCombo(pane);
	            pane.requestLayout();
	                    	
	        }
	        else if (string.equals("Delete"))
	        {
	        	pane.dispose();
	        	c.requestLayout();
	        }
	        else if (string.equals("Zoom In"))
	        {
	        	pane.setLayoutData(new RowData(300 * 2,pane.getSize().y - 4));
	        	pane.requestLayout();
	        }
	        else if (string.equals("Zoom Out"))
	        {
	        	pane.setLayoutData(new RowData(300,pane.getSize().y - 4));
	        	pane.requestLayout();
	        }
	      }

		};
		
		itemBack.addListener(SWT.Selection, listener_toolbar);
	    itemForward.addListener(SWT.Selection, listener_toolbar);
	    itemStop.addListener(SWT.Selection, listener_toolbar);
    	itemZoomOut.addListener(SWT.Selection, listener_toolbar);	
    	
    	// Apply layout
    	c.layout();
    	
    }
    
    
    // Event Add of Pane
    public void ShowSelectionCombo(Composite pane) {
    	Shell visual = new Shell(SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.ON_TOP );
    	visual.pack();
    	visual.setSize(500,150);
    	visual.setLayout(null);
    	visual.open();
    	
    	Label lblNewLabel = new Label(visual, SWT.NONE);
		lblNewLabel.setBounds(23, 10, 36, 23);
		lblNewLabel.setText("Type");
    	
    	Combo combo = new Combo(visual, SWT.NONE);
    	combo.setBounds(65, 7, 372, -89);
		combo.setItems(new String[] {"Chart", "Map", "Table", "Text"});
		
	
		Button btnNewButton = new Button(visual, SWT.NONE);
		btnNewButton.setBounds(23, 70, 80, 30);
		btnNewButton.setText("OK");
	
		visual.layout();
		
		
		// User select a item in the Combo.
        combo.addSelectionListener(new SelectionAdapter() {
 
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = combo.getSelectionIndex();
                String str = combo.getItem(idx);
                showComboType(visual, str);
            }
        });
    	
        btnNewButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				int idx = combo.getSelectionIndex();
				String l = combo.getItem(idx);
				String t = "";
				if(visual.getChildren().length >= 4) {

					Text value_of_metric = (Text) visual.getChildren()[4];
					t = value_of_metric.getText();
					
				}
				
				
				switch (event.type) {
		        case SWT.Selection:
		        	
		        	switch(pane.getSize().y) {
		        	case 54: 
		        	{
		        		pane.setLayoutData(new RowData(300, 200));
			        	pane.requestLayout();
			        	visual.dispose();
			        	break;
		        	}
		        	case 204: 
		        	{
		        		pane.setLayoutData(new RowData(300, 400));
			        	pane.requestLayout();
			        	visual.dispose();
			        	break;
		        	}
		        	case 404: 
		        	{
		        		pane.setLayoutData(new RowData(300, 600));
			        	pane.requestLayout();
			        	visual.dispose();
			        	break;
		        	}
		        	}
		        	
		        	// Xet Type de hien ra
		        	Composite child_sub = new Composite(pane, SWT.NONE);
		        	if(l.equals("Map")) {
			        	child_sub.setLayout(new FillLayout());
			        	child_sub.setLayoutData(new RowData(300 - 10, 200 - 30));
//			        	createNewSurface(child_sub);
//			        	surfaceComposite2.setLayoutData(fullData());
			        	surfaceComposite2.setParent(child_sub);
			        	surfaceComposite2.setLayoutData(fullData());
		        	} else		        	
		        	if(l.equals("Text")) {
			        	child_sub.setLayout(null);
			        	child_sub.setLayoutData(new RowData(300 - 10, 200 - 30));
			        	Label lab = new Label(child_sub, SWT.NONE);
			        	lab.setBounds(30, 30, 200 , 200);
			        	lab.setText(t);
			        	FontDescriptor descriptor = FontDescriptor.createFrom(lab.getFont());
			        	descriptor = descriptor.setStyle(SWT.BOLD);
			        	descriptor = descriptor.setHeight(50);
			        	lab.setFont(descriptor.createFont(lab.getDisplay()));
			        	
		        	} else
		        	if(l.equals("Table")) {
		        		child_sub.setLayout(new FillLayout());
		        		child_sub.setLayoutData(new RowData(300 - 10, 200 - 30));
		        		
		        		// table
		        		Table tab = new Table(child_sub, SWT.FULL_SELECTION);
		        		tab.setHeaderVisible(true);
		        		tab.setLinesVisible(true);
		        		
		        		// column
		        		TableColumn tab_c1 = new TableColumn(tab, SWT.NONE);
		        		tab_c1.setMoveable(true);
		        		tab_c1.setWidth(100);
		        		tab_c1.setText("Title 1");
		        		
		        		TableColumn tab_c2 = new TableColumn(tab, SWT.NONE);
		        		tab_c2.setMoveable(true);
		        		tab_c2.setWidth(100);
		        		tab_c2.setText("Title 2");
		        		
		        		TableColumn tab_c3 = new TableColumn(tab, SWT.NONE);
		        		tab_c3.setMoveable(true);
		        		tab_c3.setWidth(100);
		        		tab_c3.setText("Title 3");
		        		
		        	} else
		        	if(l.equals("Chart")) {
		        		child_sub.setLayout(null);
			        	child_sub.setLayoutData(new RowData(300 - 10, 200 - 30));
		        		Frame frame = SWT_AWT.new_Frame(child_sub);
		        		DefaultPieDataset dataSet = new DefaultPieDataset();
	        	        dataSet.setValue("Chrome", 29);
	        	        dataSet.setValue("InternetExplorer", 36);
	        	        dataSet.setValue("Firefox", 35);
		        		JFreeChart chart = ChartFactory.createPieChart3D("test", dataSet, true, true, false);
		        		ChartPanel chartPanel = new ChartPanel(chart);
		        		frame.add(chartPanel);
		        		//not render
		        	}
		        	
		        	
		        	child_sub.layout();
		        	
		        	
		        	break;
		        }
				
				
				
			}
        	
        });
    }
    

    public void showComboType(Shell visual, String str) {
    	
    	switch(str) {
    	case "Chart":
    	{
    		Label lblNewLabel_1 = new Label(visual, SWT.NONE);
    		lblNewLabel_1.setBounds(23, 40, 36, 23);
    		lblNewLabel_1.setText("Type");
    		
    		Text text = new Text(visual, SWT.BORDER);
    		text.setBounds(65, 40, 200, 23);
    		visual.layout();
    		break;
    	}
    	case "Text":
    	{
    		Label lblNewLabel_1 = new Label(visual, SWT.NONE);
    		lblNewLabel_1.setBounds(23, 40, 36, 23);
    		lblNewLabel_1.setText("Value");
    		
    		Text text = new Text(visual, SWT.BORDER);
    		text.setBounds(65, 40, 200, 23);
    		visual.layout();
    		break;
    	}
    	}
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
    protected abstract Composite createNewSurface(Composite parent);
    protected abstract Composite createNewSurfaceFromList(Composite parent, int i);
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
	
	final List<IDisplaySurface> listSur = getOutput().getListSurface();
	
	// end fix
	if (updateThread == null) {
	    updateThread = new Thread(() -> {
//		final IDisplaySurface s = getDisplaySurface();
//	    LayeredDisplayOutput a = getOutput(); // new
//	    LayeredDisplayOutput b = getOutput();
		
//		final IDisplaySurface s1 = listSur.get(0);
//		final IDisplaySurface s2 = listSur.get(1); //new
//		final IDisplaySurface s3 = listSur.get(2); //new
//		final IDisplaySurface s4 = listSur.get(3); //new
		
		
		
		// if (s != null && !s.isDisposed() && !disposed) {
		// s.updateDisplay(false);
		// }
		while (!disposed) {

			for(int i = 0; i < listSur.size(); i++) {
				if (listSur.get(i) != null && listSur.get(i).isRealized() && !listSur.get(i).isDisposed() && !disposed) {
					acquireLock();
					listSur.get(i).updateDisplay(false);
					if(listSur.get(i).getData().isAutosave()) {
						SnapshotMaker.getInstance().doSnapshot(output, listSur.get(i), surfaceComposite_list.get(i));
					}
					
					//Fix for issue #1693
					if (output.isInInitPhase()) {
						output.setInInitPhase(false);
						output.setSynchronized(oldSync);
						//end fix
					}
				}
			}
			
//		    if (s1 != null && s1.isRealized() && !s1.isDisposed() && !disposed) {
//			acquireLock();
//			s1.updateDisplay(false);
//			if (s1.getData().isAutosave()) {
//			    SnapshotMaker.getInstance().doSnapshot(output, s1, surfaceComposite_list.get(0)); //ban dau la surfaceComposite
//			}
//			// Fix for issue #1693
//			if (output.isInInitPhase()) {
//			    output.setInInitPhase(false);
//			    output.setSynchronized(oldSync);
//			    // end fix
//			}
//
//		    }
//		    
//		    // them
//		    if (s2 != null && s2.isRealized() && !s2.isDisposed() && !disposed) {
//				acquireLock();
//				s2.updateDisplay(false);
//				if (s2.getData().isAutosave()) {
//				    SnapshotMaker.getInstance().doSnapshot(output, s2, surfaceComposite_list.get(1)); //ban dau la surfaceComposite
//				}
//				// Fix for issue #1693
//				if (output.isInInitPhase()) {
//				    output.setInInitPhase(false);
//				    output.setSynchronized(oldSync);
//				    // end fix
//				}
//
//			    }
//		 // them
//		    if (s3 != null && s3.isRealized() && !s3.isDisposed() && !disposed) {
//				acquireLock();
//				s3.updateDisplay(false);
//				if (s3.getData().isAutosave()) {
//				    SnapshotMaker.getInstance().doSnapshot(output, s3, surfaceComposite_list.get(2)); //ban dau la surfaceComposite
//				}
//				// Fix for issue #1693
//				if (output.isInInitPhase()) {
//				    output.setInInitPhase(false);
//				    output.setSynchronized(oldSync);
//				    // end fix
//				}
//
//			    }
//		    
//		 // them
//		    if (s4 != null && s4.isRealized() && !s4.isDisposed() && !disposed) {
//				acquireLock();
//				s4.updateDisplay(false);
//				if (s4.getData().isAutosave()) {
//				    SnapshotMaker.getInstance().doSnapshot(output, s4, surfaceComposite_list.get(3)); //ban dau la surfaceComposite
//				}
//				// Fix for issue #1693
//				if (output.isInInitPhase()) {
//				    output.setInInitPhase(false);
//				    output.setSynchronized(oldSync);
//				    // end fix
//				}
//
//			    }
		    
		}
	    });
	    updateThread.start();
	}

	if (output.isSynchronized()) {
//	    final IDisplaySurface s = getDisplaySurface();
		//new
//		LayeredDisplayOutput a = getOutput(); // new
		
//		final IDisplaySurface s1 = listSur.get(0);
//		final IDisplaySurface s2 = listSur.get(1); //new
//		final IDisplaySurface s3 = listSur.get(2);
//		final IDisplaySurface s4 = listSur.get(3); //new
		
		for (int i = 0; i < listSur.size(); i++) {
			listSur.get(i).updateDisplay(false);
			
			if(getOutput().getData().isAutosave() && listSur.get(i).isRealized()) {
				SnapshotMaker.getInstance().doSnapshot(output, listSur.get(i), surfaceComposite_list.get(i));
			}
			
			while (!listSur.get(i).isRendered() && !listSur.get(i).isDisposed() && !disposed) {
				try {
					Thread.sleep(10);
				} catch (final InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}
		
		
//	    s1.updateDisplay(false);
//	    s2.updateDisplay(false);
//	    s3.updateDisplay(false);
//	    s4.updateDisplay(false);
//	    
//	    if (getOutput().getData().isAutosave() && s1.isRealized()) {
//		SnapshotMaker.getInstance().doSnapshot(output, s1, surfaceComposite_list.get(0));
//	    }
//	    
//	    if (getOutput().getData().isAutosave() && s2.isRealized()) {
//			SnapshotMaker.getInstance().doSnapshot(output, s2, surfaceComposite_list.get(1));
//		    }
//	    
//	    if (getOutput().getData().isAutosave() && s3.isRealized()) {
//			SnapshotMaker.getInstance().doSnapshot(output, s3, surfaceComposite_list.get(2));
//		    }
////	    
//	    if (getOutput().getData().isAutosave() && s4.isRealized()) {
//			SnapshotMaker.getInstance().doSnapshot(output, s4, surfaceComposite_list.get(3));
//		    }
//	    
//	    while (!s1.isRendered() && !s1.isDisposed() && !disposed) {
//		try {
//		    Thread.sleep(10);
//		} catch (final InterruptedException e) {
//		    e.printStackTrace();
//		}
//
//	    }
	    
	    
	    
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
