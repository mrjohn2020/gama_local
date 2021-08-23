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
import msi.gama.outputs.IDisplayOutput;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.layers.charts.ChartDataSet;
import msi.gama.outputs.layers.charts.ChartOutput;
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
    
    // List of surface composite
    public List<Composite> surfaceComposite_list = new ArrayList<Composite>();
   
    public List<Composite> list_pane;
    
    public Combo comboToolbarResize;
    public Combo comboToolbarDelete;
    
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
    	
		// set layout for parent composite
    	c.setLayout(new RowLayout(SWT.HORIZONTAL));
    		
    	// number surface created
    	int nb_surface_init = 0;
    	nb_surface_init = getOutput().getListSurface().size();
    	
    	// list Composite created
    	list_pane = new ArrayList<>(Arrays.asList(new Composite[nb_surface_init]));
    	   	
    	// create toolbar
    	createToolbarForParent(c);
		
    	// create main simulation
    	final Composite simulate = new Composite(c, SWT.Resize | SWT.BORDER);
    	simulate.setLayout(emptyLayout());
    	simulate.setLayoutData(new RowData(400, 300));
		
    	form = new SashForm(simulate, SWT.HORIZONTAL);
		form.setLayoutData(fullData());
		form.setBackground(IGamaColors.BLACK.color());
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
    	   	
    	for (int i = 0; i< nb_surface_init; i++) {
    		
    		final Composite p = new Composite(c, SWT.Resize | SWT.BORDER);
    		p.setLayout(new FillLayout(SWT.VERTICAL));
    		p.setLayoutData(new RowData(400, 300));
		
//    		createToolbarForPane(p);
    		
    		final Composite surface_comp = new Composite(p, SWT.RESIZE | SWT.BORDER);
    		surface_comp.setLayout(new FillLayout());
    		surface_comp.setLayoutData(new RowData(350, 250));
    		surface_comp.setVisible(true);

    		createNewSurfaceFromList(surface_comp, i);
    		surfaceComposite_list.get(i).setLayoutData(fullData());
    		list_pane.set(i, p);
    		
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

    	// BUTTON ADD PANE 300x200
    	Button button = new Button(ToolBar, SWT.PUSH);
    	button.setImage(getDefaultImage());
    	button.setText("Add pane");
    	button.addListener(SWT.Selection, new Listener() {
  
			@Override
			public void handleEvent(Event event) {
				createPane(c);
			}
    		
    	});
    	
    	// BUTTON ENABLE PANE
    	Button buttonSetEnable = new Button(ToolBar, SWT.CHECK);
    	buttonSetEnable.setText("Move pane");
    	
    	buttonSetEnable.addSelectionListener(new SelectionAdapter() {
    	
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (buttonSetEnable.getSelection()) {
					for (Composite pane : list_pane) {
			    		pane.setEnabled(false);
			    	}
				} else {
					for (Composite pane : list_pane) {
			    		pane.setEnabled(true);
			    	}
				}
			}
    		
    	});
    	
    	// Combo delete pane
    	comboToolbarDelete = new Combo(ToolBar, SWT.DROP_DOWN);
    	String[] list_delete = new String[list_pane.size()];
    	for (int i = 0; i < list_pane.size(); i++) {
    		list_delete[i] = "Pane " + i;
    	}
    	comboToolbarDelete.setItems(list_delete);
    	comboToolbarDelete.setText("Choos pane to delete");
    	
    	comboToolbarDelete.addSelectionListener(new SelectionAdapter() {
    		
    		@Override
    		public void widgetSelected(SelectionEvent e) {
    			int idx = comboToolbarDelete.getSelectionIndex();
    			list_pane.get(idx).dispose();
    			list_pane.remove(idx);
    			comboToolbarDelete.remove(idx);
    			comboToolbarResize.remove(idx);
    			list_pane.get(idx).requestLayout();
    		}
    	});
    	
    	// Combo resize pane
    	String[] list_resize = new String[list_pane.size()];
    	for (int i = 0; i < list_pane.size(); i++) {
    		list_resize[i] = "Pane " + i;
    	}
    	
    	comboToolbarResize = new Combo(ToolBar, SWT.DROP_DOWN);
    	comboToolbarResize.setItems(list_resize);
    	comboToolbarResize.setText("Choose pane to resize");
		
		// User select a item in the Combo.
    	comboToolbarResize.addSelectionListener(new SelectionAdapter() {
 
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = comboToolbarResize.getSelectionIndex();
                String str = comboToolbarResize.getItem(idx);
                Tracker tracker = new Tracker(list_pane.get(idx).getParent(), SWT.RESIZE);
    	        tracker.setStippled(true);
    	        Rectangle rect = list_pane.get(idx).getBounds();
    	        tracker.setRectangles(new Rectangle[] { rect });
    	        if (tracker.open()) {
    	          Rectangle after = tracker.getRectangles()[0];
    	          list_pane.get(idx).setBounds(after);
    	        }
    	        tracker.dispose();
            }
        });
    }
       
    public void updateListener(Composite c) {
    	
    	int count = list_pane.size();
    	
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
					for (int i = 0; i < count; i++) {
						Rectangle rect = list_pane.get(i).getBounds();
						if(rect.contains(event.x, event.y)) {
							Point pt1 = list_pane.get(i).toDisplay(0, 0);
							Point pt2 = c.toDisplay(event.x, event.y);
							list_point.get(i)[0] =  new Point(pt2.x - pt1.x, pt2.y - pt1.y);
						}
					}
					break;
				case SWT.MouseMove:
					for (int i = 0; i < count; i++) {
						if (list_point.get(i)[0] != null) {
							Point pt = list_point.get(i)[0];
							list_pane.get(i).setLocation(event.x - pt.x,  event.y - pt.y);
						}
					}
					break;
				case SWT.MouseUp:
					for (int i = 0; i < count; i++) {
						list_point.get(i)[0] = null;
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
    	// Pane Composite
    	Composite pane = new Composite(c, SWT.NONE);
    	pane.setLayout(new FillLayout(SWT.VERTICAL));
    	pane.setLayoutData(new RowData(400, 300));
    	
    	Button addBtn = new Button(pane, SWT.PUSH);
    	addBtn.setImage(getDefaultImage());
    	addBtn.addListener(SWT.Selection, new Listener() {
  
			@Override
			public void handleEvent(Event event) {
				ShowSelectionCombo(pane);
				addBtn.dispose();
	            pane.requestLayout();
			}
    		
    	});
    	
    	list_pane.add(pane);
    	comboToolbarResize.add("New Custom pane");
    	comboToolbarDelete.add("New Custom pane");
    	
    	// Toolbar of Pane
//    	createToolbarForPane(pane);
    	
    	// Update Listener
    	updateListener(c);
    	
    	// Apply layout
    	c.requestLayout();
    	
    }
    
    public void createToolbarForPane(Composite pane) {
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
	        	pane.requestLayout();
	        }
	        else if (string.equals("Zoom In"))
	        {
	        	pane.setLayoutData(new RowData(400 * 2,pane.getSize().y - 4));
	        	pane.requestLayout();
	        }
	        else if (string.equals("Zoom Out"))
	        {
	        	pane.setLayoutData(new RowData(400,pane.getSize().y - 4));
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
		
		itemBack.addListener(SWT.Selection, listener_toolbar);
	    itemForward.addListener(SWT.Selection, listener_toolbar);
	    itemStop.addListener(SWT.Selection, listener_toolbar);
    	itemZoomOut.addListener(SWT.Selection, listener_toolbar);	
    	itemResize.addListener(SWT.Selection, listener_toolbar);
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
		combo.setItems(new String[] {"Chart", "Map", "Table", "Text", "MixComposite", "List"});
		
	
		Button btnNewButton = new Button(visual, SWT.NONE);
		btnNewButton.setBounds(23, 70, 80, 30);
		btnNewButton.setText("OK");
	
		visual.requestLayout();
				
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
				int r = 1;
				int c = 1;
				if(visual.getChildren().length >= 4) {

					Text value_of_metric = (Text) visual.getChildren()[4];
					t = value_of_metric.getText();
					
				}
				
				if(visual.getChildren().length == 7) {
					Text nb_row = (Text) visual.getChildren()[4];
					Text nb_col = (Text) visual.getChildren()[6];
					
					r = Integer.parseInt(nb_row.getText());
					c = Integer.parseInt(nb_col.getText());
				}

				switch (event.type) {
		        case SWT.Selection:
		        		        	
		        	pane.requestLayout();
		        	visual.dispose();
		        	
		        	// Xet Type de hien ra
		        	Composite child_sub = new Composite(pane, SWT.BORDER | SWT.EMBEDDED);
		        	if(l.equals("Map")) {
			        	child_sub.setLayout(new FillLayout());
			        	child_sub.setLayoutData(new RowData(400 - 10, 300 - 30));
			        	createSurfaceComposite(child_sub);
			        	surfaceComposite.setLayoutData(fullData());
		        	} else		        	
		        	if(l.equals("Text")) {
		        		pane.setLayoutData(new RowData(400, 130));
			        	child_sub.setLayout(new FillLayout());
//			        	child_sub.setLayoutData(new RowData(400 - 10, 300 - 30));
			        	Label lab = new Label(child_sub, SWT.NONE);
			        	lab.setAlignment(SWT.CENTER);
//			        	lab.setBounds(30, 30, 200 , 200);
			        	lab.setText(t);
			        	
			        	FontDescriptor descriptor = FontDescriptor.createFrom(lab.getFont());
			        	descriptor = descriptor.setStyle(SWT.NORMAL);
			        	descriptor = descriptor.setHeight(15);
			        	lab.setFont(descriptor.createFont(lab.getDisplay()));
			        	
		        	} else
		        	if(l.equals("Table")) {
		        		
		        		child_sub.setLayout(new FillLayout());
		        		child_sub.setLayoutData(new RowData(400 - 10, 300 - 30));

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
						
		        		
		        		for (int loopIndex = 0; loopIndex < 24; loopIndex++) {
	        		      TableItem item = new TableItem(tab, SWT.NULL);
	        		      item.setText("");
	        		      item.setText(0, caphanhchinh[loopIndex]);
	        		      item.setText(1, mucnguyco[loopIndex]);
	        		      item.setText(2, String.valueOf(sovung[loopIndex]));
	        		    }
		        	
		        	} else
		        	if(l.equals("Chart")) {
		        		child_sub.setLayout(new FillLayout());
//			        	child_sub.setLayoutData(new RowData(400 - 10, 300 - 30));
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
		        		
		        	} else 
	        		if (l.equals("MixComposite")) {
	        			
	        			child_sub.setLayout(new FillLayout(SWT.VERTICAL));
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
	        		} else
	        		if (l.equals("List")) {
	        			child_sub.setLayout(new FillLayout());
	        			int res = Integer.parseInt(t);
	        			list_pane.get(res).setParent(child_sub);
	        			list_pane.remove(res);
	        			comboToolbarDelete.remove(res);
	        			comboToolbarResize.remove(res);
	        		}
		        	
		        	
		        	child_sub.requestLayout();

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
	    		visual.requestLayout();
	    		break;
	    	}
    	case "Text":
	    	{
	    		Label lblNewLabel_1 = new Label(visual, SWT.NONE);
	    		lblNewLabel_1.setBounds(23, 40, 36, 23);
	    		lblNewLabel_1.setText("Value");
	    		
	    		Text text = new Text(visual, SWT.BORDER);
	    		text.setBounds(65, 40, 200, 23);
	    		visual.requestLayout();
	    		break;
	    	}
    	case "MixComposite":	
	    	{
	    		Label nb_row = new Label(visual, SWT.NONE);
	    		nb_row.setBounds(23,  40, 36, 23);
	    		nb_row.setText("Row");
	    		
	    		Text text_nb_row = new Text(visual, SWT.BORDER);
	    		text_nb_row.setBounds(65, 40, 50, 23);
	    		
	    		Label nb_col = new Label(visual, SWT.NONE);
	    		nb_col.setBounds(200,  40, 36, 23);
	    		nb_col.setText("Col");
	    	
	    		Text text_nb_col = new Text(visual, SWT.BORDER);
	    		text_nb_col.setBounds(242, 40, 50, 23);
	    		
	    		visual.requestLayout();
	    		break;
	    	}
    	case "List": 
	    	{
	    		Label lblNewLabel_1 = new Label(visual, SWT.NONE);
	    		lblNewLabel_1.setBounds(23, 40, 36, 23);
	    		lblNewLabel_1.setText("Surface");
	    		
	    		Text text = new Text(visual, SWT.BORDER);
	    		text.setBounds(65, 40, 200, 23);
	    		visual.requestLayout();
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
