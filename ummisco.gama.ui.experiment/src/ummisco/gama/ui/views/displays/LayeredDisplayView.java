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
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
//import org.eclipse.e4.ui.workbench.UIEvents.ToolItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
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
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGamaView;
import msi.gama.common.interfaces.ILayerManager;
import msi.gama.kernel.experiment.ITopLevelAgent;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.IShape;
import msi.gama.outputs.IDisplayOutput;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.layers.charts.ChartDataSet;
import msi.gama.outputs.layers.charts.ChartOutput;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListArrayWrapper;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IMap;
import msi.gaml.variables.IVariable;
import ummisco.gama.ui.resources.GamaColors;
import ummisco.gama.ui.resources.GamaIcons;
import ummisco.gama.ui.resources.IGamaColors;
import ummisco.gama.ui.utils.WorkbenchHelper;
import ummisco.gama.ui.views.GamaViewPart;
import ummisco.gama.ui.views.toolbar.GamaToolbar2;
import ummisco.gama.ui.views.toolbar.IToolbarDecoratedView;

import org.eclipse.swt.widgets.ToolItem;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;




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
    
    // List of surface composite
    public List<Composite> surfaceComposite_list = new ArrayList<Composite>();
    public Hashtable<Composite, String> paneManager;
    public List<String> listNamePane;
       
    public IAgent agentSimulate;
    public Integer temp;
    
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
    
    public void createView_old(final Composite c) {
    	if (getOutput() == null) { return; }
		c.setLayout(emptyLayout());

		// First create the sashform
		form = new SashForm(c, SWT.HORIZONTAL);
		form.setLayoutData(fullData());
		form.setBackground(IGamaColors.WHITE.color());
		form.setSashWidth(8);
		decorator.createSidePanel(form);
		final Composite centralPanel = new Composite(form, CORE_DISPLAY_BORDER.getValue() ? SWT.BORDER : SWT.NONE);

		centralPanel.setLayout(emptyLayout());
		setParentComposite(new Composite(centralPanel, SWT.NONE) {

			@Override
			public boolean setFocus() {
				// decorator.keyAndMouseListener.focusGained(null);
				return forceFocus();
			}

		});

		getParentComposite().setLayoutData(fullData());
		getParentComposite().setLayout(emptyLayout());
		createSurfaceComposite(getParentComposite());
		surfaceComposite.setLayoutData(fullData());
		getOutput().setSynchronized(getOutput().isSynchronized() || CORE_SYNC.getValue());
		form.setMaximizedControl(centralPanel);
		decorator.createDecorations(form);
		c.layout();
    }
   
    @Override
    public void ownCreatePartControl(final Composite c) {
    	
    	String view = getOutput().getViewType();
    	
    	if (view.equals("sashform")) {
    		createView_old(c);
    	} else 
    	if (view.equals("dashboard")){
    	
		// Set layout
    	c.setLayout(new RowLayout(SWT.HORIZONTAL));
    	
    	// Number surface created
    	int nb_surface_init = 0;
    	nb_surface_init = getOutput().getListSurface().size();
    	
    	// List Composite created
    	paneManager = new Hashtable<Composite, String>();
    	listNamePane = new ArrayList<>();
    	   	   	
    	// Create toolbar
    	createToolbarForParent(c);
		
    	// Create main simulation
    	final Composite simulate = new Composite(c, SWT.Resize | SWT.BORDER);
    	simulate.setLayout(new FillLayout());
    	simulate.setLayoutData(new RowData(400, 300));
    	createView_old(simulate);
    	paneManager.put(simulate, "main");
    	    	
    	for (int i = 0; i< nb_surface_init; i++) {
    		
    		final Composite p = new Composite(c, SWT.Resize | SWT.BORDER);
    		p.setSize(400, 300);
    		p.setLayout(new FillLayout(SWT.VERTICAL));
    		p.setLayoutData(new RowData(400, 300));
		
//    		createToolbarForPane(p);
    		
    		final Composite surface_comp = new Composite(p, SWT.RESIZE | SWT.NONE);
    		surface_comp.setLayout(new FillLayout());
    		surface_comp.setLayoutData(new RowData(p.getSize().x - 10, p.getSize().y - 50));
    		surface_comp.setVisible(true);

    		createNewSurfaceFromList(surface_comp, i);
    		surfaceComposite_list.get(i).setLayoutData(fullData());
    		paneManager.put(p, listNamePane.get(i));
    		
    	}
    	
    	updateListener(c);
    	
    	getOutput().setSynchronized(getOutput().isSynchronized() || CORE_SYNC.getValue());
    	c.layout();
    	} else {
    		return;
    	}
    }
    
    public void createToolbarForParent(Composite c) {
    	final Composite ToolBar = new Composite(c, SWT.NONE);
    	ToolBar.setLayout(new FillLayout(SWT.HORIZONTAL));
    	ToolBar.setLayoutData(new RowData(1330,30));
    	// BUTTON ADD PANE
    	Button addButton = new Button(ToolBar, SWT.PUSH);
    	addButton.setImage(getDefaultImage());
    	addButton.setText("Add pane");
    	addButton.addListener(SWT.Selection, new Listener() {
  
			@Override
			public void handleEvent(Event event) {
				createPane(c);
			}
    		
    	});
    	
    	
    	Button layoutButton = new Button(ToolBar, SWT.PUSH);
    	layoutButton.setImage(getDefaultImage());
    	layoutButton.setText("Row Layout");
    	layoutButton.addListener(SWT.Selection, new Listener() {
  
			@Override
			public void handleEvent(Event event) {
				if(layoutButton.getText().equals("Row Layout")) {
					layoutButton.setText("Free Layout");
					c.setLayout(null);
				} else {
					layoutButton.setText("Row Layout");
					c.setLayout(new RowLayout());
				}
				c.requestLayout();
			}
    		
    	});
    	
    	// BUTTON ENABLE PANE
    	Button buttonSetEnable = new Button(ToolBar, SWT.CHECK);
    	buttonSetEnable.setText("Move pane");
    	
    	buttonSetEnable.addSelectionListener(new SelectionAdapter() {
    	
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (buttonSetEnable.getSelection()) {
					paneManager.forEach((k, v) -> {
						k.setEnabled(false);
					});
				} else {
					paneManager.forEach((k, v) -> {
						k.setEnabled(true);
					});
				}
			}
    		
    	});

    }
       
    public void updateListener(Composite c) {
    	
    	int count = paneManager.size();
 
    	// List Point
    	List<Point[]> list_point = new ArrayList<>();
    	for (int i = 0; i < count; i++) {
    		final Point[] offset = new Point[1];
    		list_point.add(offset);
    	}
    	
    	Listener listener = new Listener() {

			@Override
			public void handleEvent(Event event) {
		
				switch (event.type) {
				case SWT.MouseDown:
					int i = 0;
					for(Composite composite : paneManager.keySet()) {
						Rectangle rect = composite.getBounds();
						if(rect.contains(event.x, event.y)) {
							Point pt1 = composite.toDisplay(0, 0);
							Point pt2 = c.toDisplay(event.x, event.y);
							list_point.get(i)[0] =  new Point(pt2.x - pt1.x, pt2.y - pt1.y);
						}
						i++;
			    	}
					
					break;
				case SWT.MouseMove:
					int j = 0;
					for(Composite composite : paneManager.keySet()) {
						if (list_point.get(j)[0] != null) {
							Point pt = list_point.get(j)[0];
							composite.setLocation(event.x - pt.x,  event.y - pt.y);
						}
						j++;
					}
					break;
				case SWT.MouseUp:
					for (int t = 0; t < count; t++) {
						list_point.get(t)[0] = null;
					}
					break;
				}
			}
    	};
    	
    	// MOUSE EVENT
    	c.addListener(SWT.MouseDown, listener);
    	c.addListener(SWT.MouseUp, listener);
    	c.addListener(SWT.MouseMove, listener);
    	
    }
    
    public void createPane(Composite c) {
    	int width = 400;
    	int height = 300;
    	// Pane Composite
    	Composite pane = new Composite(c, SWT.BORDER);
    	pane.setLayout(new FillLayout(SWT.VERTICAL));
    	pane.setSize(width, height);
    	pane.setLayoutData(new RowData(width, height));
    	
    	Button addButton = new Button(pane, SWT.NONE);
    	addButton.setText("Add");
    	addButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				ShowSelectionCombo(pane);
				addButton.dispose();
	            pane.requestLayout();
			}
    		
    	});
    	
    	// Toolbar of Pane
//    	createToolbarForPane(pane);
    	    	
    	// Update Listener
    	updateListener(c);
    	
    	// Apply layout
    	c.requestLayout();
    	
    }
    
    public void createToolbarForPane(Composite pane) {
    	// Toolbar of Pane
    	Composite tb = new Composite(pane, SWT.NONE);
    	tb.setLayout(new FillLayout());
    	tb.setLayoutData(new RowData(390,25));
    	ToolBar t = new ToolBar(tb, SWT.RIGHT);
    	ToolItem itemAdd = new ToolItem(t, SWT.PUSH);
    	itemAdd.setText("Add");
	    ToolItem itemDelete = new ToolItem(t, SWT.PUSH);
	    itemDelete.setText("Delete");
	    ToolItem itemZoomIn = new ToolItem(t, SWT.PUSH);
	    itemZoomIn.setText("Zoom In");
	    ToolItem itemZoomOut = new ToolItem(t, SWT.PUSH);
	    itemZoomOut.setText("Zoom Out");
	    ToolItem itemResize = new ToolItem(t, SWT.PUSH);
	    itemResize.setText("Resize");
	    
	    
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
	        	paneManager.remove(pane);
	        }
	        else if (string.equals("Zoom In"))
	        {
	        	pane.setLayoutData(new RowData(800,300));
	        	pane.requestLayout();
	        }
	        else if (string.equals("Zoom Out"))
	        {
	        	pane.setLayoutData(new RowData(400,300));
	        	pane.requestLayout();
	        }
	        else if (string.equals("Resize"))
	        {	        	
	        	Tracker tracker = new Tracker(pane.getParent(), SWT.RESIZE);
    	        tracker.setStippled(true);
    	        Rectangle rect = pane.getBounds();
    	        tracker.setRectangles(new Rectangle[] { rect });
    	        if (tracker.open()) {
    	          Rectangle after = tracker.getRectangles()[0];
    	          pane.setBounds(after);
    	        }
    	        tracker.dispose();
	        }
	      }
	    
		};
		itemAdd.addListener(SWT.Selection, listener_toolbar);
		itemDelete.addListener(SWT.Selection, listener_toolbar);
		itemZoomIn.addListener(SWT.Selection, listener_toolbar);
    	itemZoomOut.addListener(SWT.Selection, listener_toolbar);	
    	itemResize.addListener(SWT.Selection, listener_toolbar);
    }
    
       
    // Event Add of Pane
    public void ShowSelectionCombo(Composite pane) {
    	Shell visual = new Shell(SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.ON_TOP );
    	visual.pack();
    	visual.setSize(400,100);
    	visual.setLayout(new GridLayout(2, false));
    	visual.setLayoutData(new RowData(400, 100));
    	visual.open();
    	
    	Label lblType = new Label(visual, SWT.NONE);
    	lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    	lblType.setText("Type");
    	
    	Combo combo = new Combo(visual, SWT.NONE);
    	combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo.setItems(new String[] {"Chart", "Map", "Table", "Text", "MixComposite", "List Layer"});
		
		visual.requestLayout();
				
		// User select a item in the Combo.
        combo.addSelectionListener(new SelectionAdapter() {
 
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = combo.getSelectionIndex();
                String str = combo.getItem(idx);
                showComboType(visual, str, pane, combo);
            }
        });
    	
        
    }
    

    public void showComboType(Shell visual, String str, Composite pane, Combo combo) {

    	switch(str) {
    	case "Chart":
	    	{	
	    		visual.setSize(400,150);
	    		
	    		Label title = new Label(visual, SWT.NONE);
	    		title.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		title.setText("Title");
	    		
	    		Text text = new Text(visual, SWT.BORDER);
	    		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		visual.requestLayout();
	    		break;
	    	}
    	case "Map":
	    	{
	    		visual.setSize(400,150);
	    		
	    		Label title = new Label(visual, SWT.NONE);
	    		title.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		title.setText("Title");
	    		
	    		Text text = new Text(visual, SWT.BORDER);
	    		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		visual.requestLayout();
	    		
	    		break;
	    	}
    	case "Table":
	    	{
	    		visual.setSize(400,150);
	    		
	    		Label title = new Label(visual, SWT.NONE);
	    		title.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		title.setText("Title");
	    		
	    		Text text = new Text(visual, SWT.BORDER);
	    		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		
	    		Button button = new Button(visual, SWT.PUSH);
	    		button.setText("Open new Shell");
	    		button.addListener(SWT.Selection, new Listener() {
	    			
					@Override
					public void handleEvent(Event event) {
						// TODO Auto-generated method stub
						JFileChooser j = new JFileChooser("d:", FileSystemView.getFileSystemView());
						 
						// Open the save dialog
						j.showSaveDialog(null);
					} // error when open dialog
	    			
	    		});
	    		
	    		visual.requestLayout();
	    		
	    		
	    		
	    		
	    		break;
	    	}
    	case "Text":
	    	{
	    		visual.setSize(400,200);
	    		
	    		Label title = new Label(visual, SWT.NONE);
	    		title.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		title.setText("Title");
	    		
	    		Text text_title = new Text(visual, SWT.BORDER);
	    		text_title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		
	    		Label lblValue = new Label(visual, SWT.NONE);
	    		lblValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		lblValue.setText("Value");
	    		
//	    		Text text_value = new Text(visual, SWT.BORDER);
//	    		text_value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		Combo cb_value = new Combo(visual, SWT.NONE);
	    		cb_value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	        	IMap<?, ?> attr = getOutput().getAgent().getOrCreateAttributes();
	        	int len = getOutput().getAgent().getOrCreateAttributes().size();
	        	String[] listValue = new String[len];
	        	attr.getKeys();
	        	for(int i = 0; i < len; i++ ) {
	        		listValue[i] = (String) attr.getKeys().get(i);
	        	}
	        	cb_value.setItems(listValue);
	    		
	    		Label lblSize = new Label(visual, SWT.NONE);
	    		lblSize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		lblSize.setText("Size");
	    		
	    		Button btnSize = new Button(visual, SWT.RADIO);
	    		btnSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		btnSize.setText("Big");
	    		
	    		Label lblUnit = new Label(visual, SWT.NONE);
	    		lblUnit.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		lblUnit.setText("Unit");
	    		
	    		Text text_unit = new Text(visual, SWT.BORDER);
	    		text_unit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		visual.requestLayout();
	    		break;
	    	}
    	case "MixComposite":	
	    	{
	    		visual.setSize(400,250);
	    		
	    		Label title = new Label(visual, SWT.NONE);
	    		title.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		title.setText("Title");
	    		
	    		Text text_title = new Text(visual, SWT.BORDER);
	    		text_title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		
	    		Label nb_row = new Label(visual, SWT.NONE);
	    		nb_row.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		nb_row.setText("Row");
	    		
	    		Text text_nb_row = new Text(visual, SWT.BORDER);
	    		text_nb_row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		
	    		Label nb_col = new Label(visual, SWT.NONE);
	    		nb_col.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		nb_col.setText("Col");
	    	
	    		Text text_nb_col = new Text(visual, SWT.BORDER);
	    		text_nb_col.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		
	    		visual.requestLayout();
	    		break;
	    	}
    	case "List Layer": 
	    	{
	    		visual.setSize(400,150);
	    		
	    		Label title = new Label(visual, SWT.NONE);
	    		title.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    		title.setText("Title");
	    		
	    		Text text_title = new Text(visual, SWT.BORDER);
	    		text_title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    		
	    		visual.requestLayout();
	    		break;
	    	}
	    	
	    	
    	}
    	
    	new Label(visual, SWT.None);
    	Button btnAccept = new Button(visual, SWT.NONE);
    	btnAccept.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    	btnAccept.setText("OK");
    	btnAccept.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				int idx = combo.getSelectionIndex();
				String l = combo.getItem(idx);
				String t = "";
				int r = 1;
				int c = 1;
				
				if(l.equals("Chart")){
					
				}
				
				if(l.equals("Text")) {	
					
				}
				
				if(l.equals("MixComposite")) {
					Text nb_row = (Text) visual.getChildren()[5];
					Text nb_col = (Text) visual.getChildren()[7];
					
					r = Integer.parseInt(nb_row.getText());
					c = Integer.parseInt(nb_col.getText());
				}

				switch (event.type) {
		        case SWT.Selection:
		        		        	
//		        	pane.requestLayout();
//		        	visual.dispose();
		        	
		        	// Display
		        	Composite child_sub = new Composite(pane, SWT.NONE | SWT.EMBEDDED);
		        	if(l.equals("Map")) {        		
			        	child_sub.setLayout(new FillLayout());
			        	child_sub.setLayoutData(new RowData(pane.getSize().x - 10, pane.getSize().y - 50));
			        	//
		        	} else		        	
		        	if(l.equals("Text")) {
		    		        		
		        		Text title = (Text) visual.getChildren()[3];
		        		Combo value_of_metric = (Combo) visual.getChildren()[5];
		        		String valueAttr = value_of_metric.getItem(value_of_metric.getSelectionIndex());
		        		
		        		agentSimulate = getOutput().getAgent();
		        		temp = (Integer) agentSimulate.getAttribute(valueAttr);
		        		
		        		Text unit_of_metric = (Text) visual.getChildren()[9];
		        		paneManager.put(pane, l + " : " + title.getText());
		        		
		        		child_sub.setLayout(new GridLayout(2, false));
		        		child_sub.setLayoutData(new RowData(pane.getSize().x - 10, pane.getSize().y - 50));
		        		
		        		// Display value
			        	Label value = new Label(child_sub, SWT.NONE);
			        	value.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			        	value.setText(temp.toString());
			        	
			        	FontDescriptor descriptor_value = FontDescriptor.createFrom(value.getFont());
			        	descriptor_value = descriptor_value.setStyle(SWT.NORMAL);
			        	descriptor_value = descriptor_value.setHeight(30);
			        	value.setFont(descriptor_value.createFont(value.getDisplay()));
			        	
			        	// display unit
			        	Label unit = new Label(child_sub, SWT.NONE);
			        	unit.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			        	unit.setText(unit_of_metric.getText());
			        	
			        	FontDescriptor descriptor_unit = FontDescriptor.createFrom(unit.getFont());
			        	descriptor_unit = descriptor_unit.setStyle(SWT.NORMAL);
			        	descriptor_unit = descriptor_unit.setHeight(15);
			        	unit.setFont(descriptor_unit.createFont(unit.getDisplay()));
			        	
			        	updateListener(pane.getParent());
			        	
		        	} else
		        	if(l.equals("Table")) {
		        		Text title = (Text) visual.getChildren()[3];
		        		paneManager.put(pane, l + " : " + title.getText());
		        		
		        		child_sub.setLayout(new FillLayout());
		        		child_sub.setLayoutData(new RowData(pane.getSize().x - 10, pane.getSize().y - 50));

		        		// table
		        		Table tab = new Table(child_sub, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		        		tab.setHeaderVisible(true);
		        		tab.setLinesVisible(true);
		        		tab.getItemHeight();
		        		// column
		        		TableColumn tab_c1 = new TableColumn(tab, SWT.NONE);
		        		tab_c1.setMoveable(true);
		        		tab_c1.setWidth(100);
		        		tab_c1.setText("Cap Hanh Chinh");
		        		tab_c1.setWidth(pane.getBounds().width / 3);
		        		
		        		TableColumn tab_c2 = new TableColumn(tab, SWT.NONE);
		        		tab_c2.setMoveable(true);
		        		tab_c2.setWidth(100);
		        		tab_c2.setText("Muc Nguy Co");
		        		tab_c2.setWidth(pane.getBounds().width / 3);
		        		
		        		TableColumn tab_c3 = new TableColumn(tab, SWT.NONE);
		        		tab_c3.setMoveable(true);
		        		tab_c3.setWidth(100);
		        		tab_c3.setText("So Vung");
		        		tab_c3.setWidth(pane.getBounds().width / 3);
		        		
		        		String[] caphanhchinh = {"Tinh/Thanh", "Tinh/Thanh", "Tinh/Thanh", "Quan/Huyen", "Quan/Huyen", "Quan/Huyen", "Xa/Phuong", "Xa/Phuong", "Xa/Phuong"  };
		        		String[] mucnguyco = {"Nguy co rat cao", "Nguy co cao", "Nguy co", "Nguy co rat cao", "Nguy co cao", "Nguy co", "Nguy co rat cao", "Nguy co cao", "Nguy co" };
		        		int[] sovung = {24, 13, 11, 173, 68, 166, 791, 1735, 61 };
						
		        		for (int loopIndex = 0; loopIndex < 9; loopIndex++) {
	        		      TableItem item = new TableItem(tab, SWT.NULL);
	        		      item.setText("");
	        		      item.setText(0, caphanhchinh[loopIndex]);
	        		      item.setText(1, mucnguyco[loopIndex]);
	        		      item.setText(2, String.valueOf(sovung[loopIndex]));
	        		    }
		        		
		        		updateListener(pane.getParent());
		        	
		        	} else
		        	if(l.equals("Chart")) {
		        		Text title = (Text) visual.getChildren()[3];
						paneManager.put(pane, l + " : " + title.getText());
						
						child_sub.setLayout(new FillLayout());
		        		child_sub.setLayoutData(new RowData(pane.getSize().x - 10, pane.getSize().y - 50));
		        		
		        		Frame frame = SWT_AWT.new_Frame(child_sub);
		        		DefaultPieDataset dataSet = new DefaultPieDataset();
		        	
		        		ChartDataSet a = getOutput().getLayers().get(2).getDataSet();//new
		        		ChartOutput b = a.getOutput();
	        	        dataSet.setValue("Chrome", 29);
	        	        dataSet.setValue("InternetExplorer", 36);
	        	        dataSet.setValue("Firefox", 35);
		        		JFreeChart chart = ChartFactory.createRingChart("test", (DefaultPieDataset) b.getJfreedataset().get(0), true, true, false);
		        		ChartPanel chartPanel = new ChartPanel(chart);
		        		frame.add(chartPanel);
		        		
		        		updateListener(pane.getParent());
		        		
		        	} else 
	        		if (l.equals("MixComposite")) {
	        			
	        			Text title = (Text) visual.getChildren()[3];
						paneManager.put(pane, l + " : " + title.getText()); // loop 
						
	        			child_sub.setLayout(new FillLayout(SWT.VERTICAL));
		        		child_sub.setLayoutData(new RowData(pane.getSize().x - 10, pane.getSize().y - 50));
	        			for (int i = 0; i < r; i++) {
	        				Composite CompositeRow = new Composite(child_sub, SWT.BORDER);
	        				CompositeRow.setLayout(new FillLayout(SWT.HORIZONTAL));
	        				for (int j = 0; j < c; j++) {
	        					Composite CompositeColumn = new Composite(CompositeRow, SWT.BORDER);
	        					CompositeColumn.setLayout(new FillLayout());
	        					Button addComponentBtn = new Button(CompositeColumn, SWT.PUSH);
	        					addComponentBtn.setText("Add");
	        					
	        					addComponentBtn.addListener(SWT.Selection, new Listener() {
	        						  
	        						@Override
	        						public void handleEvent(Event event) {
	        							ShowSelectionCombo(CompositeColumn);
	        							addComponentBtn.dispose();
	        							CompositeColumn.requestLayout();
	        						}
	        			    		
	        			    	});
	        					
	        				}
	        			}
	        			updateListener(pane.getParent());
	        		} else
	        		if (l.equals("List Layer")) {
	        			
	        			Text title = (Text) visual.getChildren()[3];
						paneManager.put(pane, l + " : " + title.getText());
						
						pane.setLayoutData(new RowData(600, 600));
						
	        			child_sub.setLayout(new FillLayout(SWT.VERTICAL));
	        			child_sub.setSize(400,400);
	        			child_sub.setLayoutData(new RowData(400,400));
	        			int x = child_sub.getSize().x;
	        			int y = child_sub.getSize().y;
	        			
	        			Text value;
	        			
	        			DateTime a = new DateTime(child_sub, SWT.CALENDAR);
	        			
	        			Scale b = new Scale(pane, SWT.None);
	        			int k = child_sub.getSize().y;
	        			b.setMinimum(100);
	        			b.setMaximum(400 );
	        			b.setIncrement(1);
//	        			b.setPageIncrement(5);
	        			
	        			value = new Text(pane, SWT.BORDER | SWT.SINGLE);
	        			
	        			b.addListener(SWT.Selection, new Listener() {
	        			      public void handleEvent(Event event) {
	        			        int perspectiveValue = b.getMaximum() - b.getSelection() + b.getMinimum();
	        			        value.setText("Vol: " + perspectiveValue);
	        			        
	        			        child_sub.setLayoutData(new RowData(perspectiveValue, perspectiveValue));
	        			        child_sub.requestLayout();
	        			      }
	        			    });
	        				        			
	        			updateListener(pane.getParent());
	        		}
		        	

		        	child_sub.requestLayout();
		        	pane.requestLayout();
		        	visual.dispose();
		        	System.out.print(paneManager);
		        	break;
		        }
				
			}
        	
        });
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
	
	
	// end fix
	if (updateThread == null) {
	    updateThread = new Thread(() -> {
	    	final IDisplaySurface s = getDisplaySurface();
	    	final List<IDisplaySurface> listSur = getOutput().getListSurface();
	    	
//	    	paneManager.entrySet();
	    	
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
		final List<IDisplaySurface> listSur = getOutput().getListSurface();
		
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
