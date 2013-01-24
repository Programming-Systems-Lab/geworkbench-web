package org.geworkbenchweb.layout;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.APSerializable;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorTableResultSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.events.PluginEvent.PluginEventListener;
import org.geworkbenchweb.plugins.DataTypeUI;
import org.geworkbenchweb.plugins.anova.AnovaAnalysis;
import org.geworkbenchweb.plugins.anova.AnovaUI;
import org.geworkbenchweb.plugins.aracne.AracneAnalysisWeb;
import org.geworkbenchweb.plugins.cnkb.CNKBInteractions;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringWrapper;
import org.geworkbenchweb.plugins.marina.MarinaAnalysis;
import org.geworkbenchweb.plugins.tools.Tools;
import org.geworkbenchweb.plugins.ttest.TTestAnalysisWeb;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.Comment;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.CurrentContext;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.alump.fancylayouts.FancyCssLayout;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * UMainLayout sets up the basic layout and style of the application.
 * @author Nikhil Reddy
 */
@SuppressWarnings("deprecation")
public class UMainLayout extends VerticalLayout {
	
	private static Log log = LogFactory.getLog(UMainLayout.class);

	private static final long serialVersionUID = 6214334663802788473L;

	private final SplitPanel mainSplit;

	private ComboBox search;

	private final Tree navigationTree;

	VisualPluginView pluginView = new VisualPluginView();

	private HorizontalLayout dataNavigation;

	private Long dataSetId;

	User user = SessionHandler.get();

	private Tree markerTree;

	private Tree arrayTree;

	private Tree markerSetTree;

	private Tree arraySetTree;

	private CssLayout leftMainLayout;

	private ICEPush pusher;
	
	private MenuBar annotationBar;
	
	final MenuBar toolBar = new MenuBar();
 
	private FancyCssLayout annotationLayout;
	
	private Button annotButton; 		
	
	private Button removeButton;	
	
	private Button removeSetButton;
	
	private ComboBox contextSelector;
	
	private VerticalLayout contextpane;
	
	private Long selectedSubSetId;
	
	static private ThemeResource hcIcon	 		=	new ThemeResource("../custom/icons/dendrogram16x16.gif");
	static private ThemeResource pendingIcon 	=	new ThemeResource("../custom/icons/pending.gif");
	static private ThemeResource networkIcon 	=	new ThemeResource("../custom/icons/network16x16.gif");
	static private ThemeResource markusIcon		=	new ThemeResource("../custom/icons/icon_world.gif");
	static private ThemeResource anovaIcon		=	new ThemeResource("../custom/icons/significance16x16.gif");
	static private ThemeResource marinaIcon		=	new ThemeResource("../custom/icons/generic16x16.gif");
	static private ThemeResource annotIcon 		= 	new ThemeResource("../custom/icons/icon_info.gif");
	static private ThemeResource CancelIcon 	= 	new ThemeResource("../runo/icons/16/cancel.png");

	public UMainLayout() {

		/* Add listeners here */
		NodeAddListener addNodeListener = new NodeAddListener();
		GeworkbenchRoot.getBlackboard().addListener(addNodeListener);

		PluginListener pluginListener = new PluginListener();
		GeworkbenchRoot.getBlackboard().addListener(pluginListener);

		AnalysisListener analysisListener = new AnalysisListener();
		GeworkbenchRoot.getBlackboard().addListener(analysisListener);

		setSizeFull();
		setImmediate(true);
		
		pusher = GeworkbenchRoot.getPusher();
		addComponent(pusher);

		HorizontalLayout topBar 		= 	new HorizontalLayout();
		final UMainToolBar mainToolBar 	= 	new UMainToolBar();
		
		addComponent(topBar);
		topBar.setHeight("44px");
		topBar.setWidth("100%");
		topBar.setStyleName("topbar");
		topBar.setSpacing(true);

		dataNavigation = new HorizontalLayout();
		dataNavigation.setHeight("24px");
		dataNavigation.setWidth("100%");
		dataNavigation.setStyleName("menubar");
		dataNavigation.setSpacing(false);
		dataNavigation.setMargin(false);
		dataNavigation.setImmediate(true);

		annotButton 	= 	new Button();
		removeButton	=	new Button();
		removeSetButton =	new Button();
		
		annotButton.setDescription("Show Annotation");
		annotButton.setStyleName(BaseTheme.BUTTON_LINK);
		annotButton.setIcon(annotIcon);
		
		removeButton.setDescription("Delete selected data");
		removeButton.setStyleName(BaseTheme.BUTTON_LINK);
		removeButton.setIcon(CancelIcon);
		
		removeSetButton.setDescription("Delete selected subset");
		removeSetButton.setStyleName(BaseTheme.BUTTON_LINK);
		removeSetButton.setIcon(CancelIcon);
		
		annotButton.setEnabled(false);
		removeButton.setEnabled(false);
		removeSetButton.setVisible(false);
		
		annotButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				annotationLayout.removeAllComponents();
				annotationLayout.setMargin(true);
				annotationLayout.setHeight("250px");
				annotationLayout.setWidth("100%");
				for(int i=0; i<annotationBar.getItems().size(); i++) {
					if(annotationBar.getItems().get(i).getDescription().equalsIgnoreCase("Close Annotation")) {
						annotationBar.getItems().get(i).setVisible(true);	
					} else {
						annotationBar.getItems().get(i).setVisible(false);	
					}
				}
				annotationLayout.addComponent(buildAnnotationTabSheet());	
				addComponent(annotationLayout);
			}
		});
		
		toolBar.setEnabled(false);
		toolBar.setImmediate(true);
		toolBar.setStyleName("transparent");
		final MenuItem set = toolBar.addItem("Set View", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				selectedItem.setEnabled(false);
				removeButton.setVisible(false);
				annotButton.setVisible(false);
				navigationTree.setVisible(false);
				
				removeSetButton.setVisible(true);

				markerTree	 	= 	new Tree();
				arrayTree 		=	new Tree();
				markerSetTree 	= 	new Tree();
				arraySetTree 	= 	new Tree();
				 
				markerTree.addActionHandler(new MarkerTreeActionHandler(dataSetId, markerSetTree));
				markerTree.setImmediate(true);
				markerTree.setSelectable(true);
				markerTree.setMultiSelect(true);
				markerTree.setDescription("Markers");

				HierarchicalContainer markerData 	= 	new HierarchicalContainer();
				List<?> sets 						= 	SubSetOperations.getMarkerSets(dataSetId);
				Item mainItem 						= 	markerData.addItem("MarkerSets");
				
				markerData.addContainerProperty("setName", String.class, null);
				mainItem.getItemProperty("setName").setValue("Marker Sets");
				for (int i=0; i<sets.size(); i++) {
					List<String> markers = ((SubSet) sets.get(i)).getPositions();
					markerData.addItem(((SubSet) sets.get(i)).getId());
					markerData.getContainerProperty(((SubSet) sets.get(i)).getId(), "setName").setValue(((SubSet) sets.get(i)).getName() + " [" + markers.size() + "]");
					markerData.setParent(((SubSet) sets.get(i)).getId(), "MarkerSets");
					markerData.setChildrenAllowed(((SubSet) sets.get(i)).getId(), true);
					for(int j=0; j<markers.size(); j++) {
						markerData.addItem(markers.get(j)+j);
						markerData.getContainerProperty(markers.get(j)+j, "setName").setValue(markers.get(j));
						markerData.setParent(markers.get(j)+j, ((SubSet) sets.get(i)).getId());
						markerData.setChildrenAllowed(markers.get(j)+j, false);
					}
				}
				markerSetTree.setImmediate(true);
				markerSetTree.setSelectable(true);
				markerSetTree.setMultiSelect(false);
				markerSetTree.setContainerDataSource(markerData);
				markerSetTree.addListener(new ItemClickEvent.ItemClickListener() {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void itemClick(ItemClickEvent event) {
						try {
							arraySetTree.select(null);
							selectedSubSetId = (Long) event.getItemId();
						}catch (Exception e) {							
						}
					}
				});
				
				HierarchicalContainer arrayData = new HierarchicalContainer();
				List<?> aSets = SubSetOperations.getArraySets(dataSetId);
				arrayData.addContainerProperty("setName", String.class, null);
				Item mainItem1 = arrayData.addItem("arraySets");
				mainItem1.getItemProperty("setName").setValue("Phenotype Sets");
				
				for (int i=0; i<aSets.size(); i++) {
					List<String> arrays = ((SubSet) aSets.get(i)).getPositions();
					arrayData.addItem(((SubSet) aSets.get(i)).getId());
					arrayData.getContainerProperty(((SubSet) aSets.get(i)).getId(), "setName").setValue(((SubSet) aSets.get(i)).getName() + " [" + arrays.size() + "]");
					arrayData.setParent(((SubSet) aSets.get(i)).getId(), "arraySets");
					arrayData.setChildrenAllowed(((SubSet) aSets.get(i)).getId(), true);
					for(int j=0; j<arrays.size(); j++) {
						arrayData.addItem(arrays.get(j)+j);
						arrayData.getContainerProperty(arrays.get(j)+j, "setName").setValue(arrays.get(j));
						arrayData.setParent(arrays.get(j)+j, ((SubSet) aSets.get(i)).getId());
						arrayData.setChildrenAllowed(arrays.get(j)+j, false);
					}
				}
				arraySetTree.setImmediate(true);
				arraySetTree.setMultiSelect(false);
				arraySetTree.setSelectable(true);
				arraySetTree.setContainerDataSource(arrayData);
				arraySetTree.addListener(new ItemClickEvent.ItemClickListener() {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void itemClick(ItemClickEvent event) {
						try {
							markerSetTree.select(null);
							selectedSubSetId = (Long) event.getItemId();
						}catch (Exception e) {							
						}
					}
				});

			    
				//arrayTree.addActionHandler(arrayTreeActionHandler);
				//arrayTree.addActionHandler(new ArrayTreeActionHandler(dataSetId, arraySetTree, contextSelector));
				arrayTree.setImmediate(true);
				arrayTree.setMultiSelect(true);
				arrayTree.setSelectable(true);
				arrayTree.setDescription("Phenotypes");

				DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(UserDirUtils.getDataSet(dataSetId));

				markerTree.setContainerDataSource(markerTableView(maSet));
				arrayTree.setContainerDataSource(arrayTableView(maSet));

				markerTree.setItemCaptionPropertyId("Labels");
				markerTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

				arrayTree.setItemCaptionPropertyId("Labels");
				arrayTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

				markerSetTree.setItemCaptionPropertyId("setName");
				markerSetTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

				arraySetTree.setItemCaptionPropertyId("setName");
				arraySetTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

				contextSelector = new ComboBox();
				contextSelector.setWidth("160px");
				contextSelector.setImmediate(true);
				contextSelector.setNullSelectionAllowed(false);
				contextSelector.addListener(new Property.ValueChangeListener() {
					private static final long serialVersionUID = 5667499645414167736L;
					public void valueChange(ValueChangeEvent event) {
						Object val = contextSelector.getValue();
						if (val != null){
							Context context = (Context)val;
							SubSetOperations.setCurrentContext(dataSetId, context);

							HierarchicalContainer arrayData = new HierarchicalContainer();
							List<SubSet> arraysets = SubSetOperations.getArraySetsForContext(context);
							arrayData.addContainerProperty("setName", String.class, null);
							Item mainItem1 = arrayData.addItem("arraySets");
							mainItem1.getItemProperty("setName").setValue("Phenotype Sets");
							arraySetTree.setContainerDataSource(arrayData);

							for (SubSet arrayset : arraysets){
								List<String> arrays = arrayset.getPositions();
								Long id = arrayset.getId();
								arrayData.addItem(id);
								arrayData.getContainerProperty(id, "setName").setValue(arrayset.getName() + " [" + arrays.size() + "]");
								arrayData.setParent(id, "arraySets");
								arrayData.setChildrenAllowed(id, true);
								for(int j=0; j<arrays.size(); j++) {
									arrayData.addItem(arrays.get(j)+j);
									arrayData.getContainerProperty(arrays.get(j)+j, "setName").setValue(arrays.get(j));
									arrayData.setParent(arrays.get(j)+j, id);
									arrayData.setChildrenAllowed(arrays.get(j)+j, false);
								}
							}
						}
					}
				});

				arrayTree.addActionHandler(new ArrayTreeActionHandler(dataSetId, arraySetTree, contextSelector));
				
				List<Context> contexts = SubSetOperations.getAllContexts(dataSetId);
				Context current = SubSetOperations.getCurrentContext(dataSetId);
				for (Context c : contexts){
					contextSelector.addItem(c);
					if (current!=null && c.getId()==current.getId()) contextSelector.setValue(c);
				}

				Button newContextButton = new Button("New");
				newContextButton.addListener(new Button.ClickListener() {
					private static final long serialVersionUID = -5508188056515818970L;
					public void buttonClick(ClickEvent event) {
						final Window nameWindow = new Window();
						nameWindow.setModal(true);
						nameWindow.setClosable(true);
						nameWindow.setWidth("300px");
						nameWindow.setHeight("150px");
						nameWindow.setResizable(false);
						nameWindow.setCaption("Add New ArraySet Context");
						nameWindow.setImmediate(true);

						final TextField contextName = new TextField();
						contextName.setInputPrompt("Please enter arrayset context name");
						contextName.setImmediate(true);
						nameWindow.addComponent(contextName);
						nameWindow.addComponent(new Button("Ok", new Button.ClickListener() {
							private static final long serialVersionUID = 634733324392150366L;
							public void buttonClick(ClickEvent event) {
				                String name = (String)contextName.getValue();
				                for (Context context : SubSetOperations.getAllContexts(dataSetId)){
				                	if (context.getName().equals(name)){
				                		MessageBox mb = new MessageBox(getWindow(), 
												"Warning", MessageBox.Icon.WARN, "Name already exists",  
												new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
										mb.show();
				                		return;
				                	}
				                }
				                Context context = new Context(name, dataSetId);
				                FacadeFactory.getFacade().store(context);
				    			getApplication().getMainWindow().removeWindow(nameWindow);
				                contextSelector.addItem(context);
				                contextSelector.setValue(context);
				            }}));
						getApplication().getMainWindow().addWindow(nameWindow);
					}
				});
				
				contextpane = new VerticalLayout();
				//contextpane.setSpacing(true);
				Label label = new Label("Context for Phenotype Sets");
				contextpane.addComponent(label);
				HorizontalLayout hlayout = new HorizontalLayout();
				hlayout.addComponent(contextSelector);
				hlayout.addComponent(newContextButton);
				contextpane.addComponent(hlayout);
				
				leftMainLayout.removeAllComponents();
				leftMainLayout.addComponent(navigationTree);
				leftMainLayout.addComponent(markerTree);
				leftMainLayout.addComponent(markerSetTree);
				leftMainLayout.addComponent(contextpane);
				leftMainLayout.addComponent(arrayTree);
				leftMainLayout.addComponent(arraySetTree);

				for(int i=0; i<toolBar.getItems().size(); i++) {
					if(toolBar.getItems().get(i).getText().equalsIgnoreCase("PROJECT VIEW")) {
						toolBar.getItems().get(i).setEnabled(true);	
					}
				}
				pluginView.setEnabled(false);
				mainToolBar.setEnabled(false);
			}	
		});
		set.setEnabled(false);
		final MenuItem project = toolBar.addItem("Project View", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				removeButton.setVisible(true);
				annotButton.setVisible(true);
				removeSetButton.setVisible(false);
				navigationTree.setVisible(true);
				markerTree.setVisible(false);
				contextpane.setVisible(false);
				arrayTree.setVisible(false);
				markerSetTree.setVisible(false);
				arraySetTree.setVisible(false);
				selectedItem.setEnabled(false);
				set.setEnabled(true);
				pluginView.setEnabled(true);
				mainToolBar.setEnabled(true);
			}
		});

		project.setEnabled(false);
		
		/* Deletes the data set and its dependencies from DB */
		removeButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				
				MessageBox mbMain = new MessageBox(getWindow(), 
						"Information", 
						MessageBox.Icon.INFO, 
						"This action will delete the selected data.", 
						new MessageBox.ButtonConfig(ButtonType.CANCEL, "Cancel"),
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				
				mbMain.show(new MessageBox.EventListener() {
					
					private static final long serialVersionUID = 1L;
					
					@Override
					public void buttonClicked(ButtonType buttonType) {    	
						if(buttonType == ButtonType.OK) {
							Long dataId = dataSetId;

							DataSet data =  FacadeFactory.getFacade().find(DataSet.class, dataId);
							if(data != null) {
								
								Map<String, Object> params 		= 	new HashMap<String, Object>();
								params.put("parent", dataId);

								List<?> SubSets =  FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent =:parent", params);
								if(SubSets.size() != 0){
									for(int i=0;i<SubSets.size();i++) {
										FacadeFactory.getFacade().delete((SubSet) SubSets.get(i));
									}
								}
								
								Map<String, Object> param 		= 	new HashMap<String, Object>();
								param.put("parent", dataId);

								List<?> resultSets =  FacadeFactory.getFacade().list("Select p from ResultSet as p where p.parent =:parent", param);
								if(resultSets.size() != 0){
									for(int i=0;i<resultSets.size();i++) {
										Map<String, Object> cParam 		= 	new HashMap<String, Object>();
										cParam.put("parent", ((ResultSet) resultSets.get(i)).getId());

										List<?> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", cParam);
										if(comments.size() != 0){
											for(int j=0;j<comments.size();j++) {
												FacadeFactory.getFacade().delete((Comment) comments.get(j));
											}
										}
										boolean success =  UserDirUtils.deleteResultSet(((ResultSet) resultSets.get(i)).getId());
										if(!success) {
											MessageBox mb = new MessageBox(getWindow(), 
													"Error", 
													MessageBox.Icon.ERROR, 
													"Unable to delete the selected data. Please contact administrator.", 
													new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
											mb.show();
										}
										FacadeFactory.getFacade().delete((ResultSet) resultSets.get(i));
										navigationTree.removeItem(((ResultSet) resultSets.get(i)).getId());
									}
								}
								Map<String, Object> cParam 		= 	new HashMap<String, Object>();
								cParam.put("parent", dataId);

								List<?> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", cParam);
								if(comments.size() != 0){
									for(int j=0;j<comments.size();j++) {
										FacadeFactory.getFacade().delete((Comment) comments.get(j));
									}
								}

								cParam.clear();
								cParam.put("datasetid", dataId);
								List<DataSetAnnotation> dsannot = FacadeFactory.getFacade().list("Select p from DataSetAnnotation as p where p.datasetid=:datasetid", cParam);
								if (dsannot.size() > 0){
									Long annotId = dsannot.get(0).getAnnotationId();
									FacadeFactory.getFacade().delete(dsannot.get(0));

									Annotation annot = FacadeFactory.getFacade().find(Annotation.class, annotId);
									if (annot!=null && annot.getOwner()!=null){
										cParam.clear();
										cParam.put("annotationid", annotId);
										List<Annotation> annots = FacadeFactory.getFacade().list("select p from DataSetAnnotation as p where p.annotationid=:annotationid", cParam);
										if (annots.size()==0) FacadeFactory.getFacade().delete(annot);
									}
								}

								List<Context> contexts = SubSetOperations.getAllContexts(dataId);
								for (Context c : contexts) {
									cParam.clear();
									cParam.put("contextid", c.getId());	
									List<SubSetContext> subcontexts = FacadeFactory.getFacade().list("Select a from SubSetContext a where a.contextid=:contextid", cParam);
									FacadeFactory.getFacade().deleteAll(subcontexts);
									FacadeFactory.getFacade().delete(c);
								}

								cParam.clear();
								cParam.put("datasetid", dataId);
								List<CurrentContext> cc =  FacadeFactory.getFacade().list("Select p from CurrentContext as p where p.datasetid=:datasetid", cParam);
								if (cc.size()>0) FacadeFactory.getFacade().delete(cc.get(0));

								boolean success = UserDirUtils.deleteDataSet(data.getId());
								if(!success) {
									MessageBox mb = new MessageBox(getWindow(), 
											"Error", 
											MessageBox.Icon.ERROR, 
											"Unable to delete the selected data. Please contact administrator.", 
											new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
									mb.show();
								}
								FacadeFactory.getFacade().delete(data);
							}else {
								Map<String, Object> cParam 		= 	new HashMap<String, Object>();
								cParam.put("parent", dataId);

								List<?> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", cParam);
								if(comments.size() != 0){
									for(int j=0;j<comments.size();j++) {
										FacadeFactory.getFacade().delete((Comment) comments.get(j));
									}
								}
								ResultSet result =  FacadeFactory.getFacade().find(ResultSet.class, dataId);
								boolean success = UserDirUtils.deleteResultSet(result.getId());
								if(!success) {
									MessageBox mb = new MessageBox(getWindow(), 
											"Error", 
											MessageBox.Icon.ERROR, 
											"Unable to delete the selected data. Please contact administrator.", 
											new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
									mb.show();
								}
								FacadeFactory.getFacade().delete(result);
							} 
							navigationTree.removeItem(dataId);
						}
					}
				});	
				
				annotButton.setEnabled(false);
				removeButton.setEnabled(false);
				set.setEnabled(false);
				VisualPlugin tools = new Tools(null);
				setVisualPlugin(tools);
			}
		});

		/* Deletes seletected subset from the datatree. */
		removeSetButton.addListener(new Button.ClickListener() {
		
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {	
				try {
					if(selectedSubSetId != null) {
						
						/* Deleteing all contexts and then subset */
						Map<String, Object> cParam 		= 	new HashMap<String, Object>();
						cParam.put("subsetid", selectedSubSetId);	
						List<SubSetContext> subcontexts = FacadeFactory.getFacade().list("Select a from SubSetContext a where a.subsetid=:subsetid", cParam);
						if(!subcontexts.isEmpty()) {
							FacadeFactory.getFacade().deleteAll(subcontexts);
						}
						
						Map<String, Object> Param 	= 	new HashMap<String, Object>();
						Param.put("id", selectedSubSetId);	
						List<SubSet> subSets = FacadeFactory.getFacade().list("Select a from SubSet a where a.id=:id", Param);
						
						if(subSets.get(0).getType().equalsIgnoreCase("microarray")) {
							if(arraySetTree.hasChildren(selectedSubSetId)) {
								Collection<?> children = arraySetTree.getChildren(selectedSubSetId);
								LinkedList<String> children2 = new LinkedList<String>();
								for (Iterator<?> i = children.iterator(); i.hasNext();)
									 children2.add((String) i.next());
								 
								// Remove the children of the collapsing node
						        for (Iterator<String> i = children2.iterator(); i.hasNext();) {
						            String child = i.next();
						            arraySetTree.removeItem(child);
						        }
							} 
							arraySetTree.removeItem(selectedSubSetId);
						}else {
							if(markerSetTree.hasChildren(selectedSubSetId)) {
								Collection<?> children = markerSetTree.getChildren(selectedSubSetId);
						        LinkedList<String> children2 = new LinkedList<String>();
								for (Iterator<?> i = children.iterator(); i.hasNext();)
									 children2.add((String) i.next());
								
								 // Remove the children of the collapsing node
						        for (Iterator<String> i = children2.iterator(); i.hasNext();) {
						            String child = i.next();
						            markerSetTree.removeItem(child);
						        }
							}
							markerSetTree.removeItem(selectedSubSetId);
						}
						FacadeFactory.getFacade().deleteAll(subSets);
					}
				} catch(Exception e) {
					e.printStackTrace();
					MessageBox mb = new MessageBox(getWindow(), 
							"Warning", 
							MessageBox.Icon.INFO, 
							"Please select SubSet to delete", 
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();	
				}
			}
		});
		dataNavigation.addComponent(toolBar);
		dataNavigation.addComponent(annotButton);
		dataNavigation.setComponentAlignment(annotButton, Alignment.MIDDLE_LEFT);
		dataNavigation.addComponent(removeButton);
		dataNavigation.setComponentAlignment(removeButton, Alignment.MIDDLE_LEFT);
		dataNavigation.addComponent(removeSetButton);
		dataNavigation.setComponentAlignment(removeSetButton, Alignment.MIDDLE_LEFT);
		dataNavigation.setComponentAlignment(toolBar, Alignment.TOP_LEFT);
		dataNavigation.addComponent(mainToolBar);
		dataNavigation.setExpandRatio(mainToolBar, 1);
		dataNavigation.setComponentAlignment(mainToolBar, Alignment.TOP_RIGHT);

		addComponent(dataNavigation);

		Component logo = createLogo();
		topBar.addComponent(logo);
		topBar.setComponentAlignment(logo, Alignment.MIDDLE_LEFT);

		mainSplit = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);
		mainSplit.setSizeFull();
		mainSplit.setStyleName("main-split");

		addComponent(mainSplit);
		setExpandRatio(mainSplit, 1);

		leftMainLayout = new CssLayout();
		leftMainLayout.setImmediate(true);
		leftMainLayout.setSizeFull();
		leftMainLayout.addStyleName("mystyle");

		navigationTree = createMenuTree();
		navigationTree.setImmediate(true);
		
		navigationTree.setItemCaptionPropertyId("Name");
		navigationTree.setItemIconPropertyId("Icon");
		navigationTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		navigationTree.setStyleName(Reindeer.TREE_CONNECTORS);
		 
	
		leftMainLayout.addComponent(navigationTree);
		mainSplit.setFirstComponent(leftMainLayout);
		mainSplit.setSplitPosition(275, SplitPanel.UNITS_PIXELS);
		mainSplit.setSecondComponent(pluginView);

		HorizontalLayout quicknav = new HorizontalLayout();
		topBar.addComponent(quicknav);
		topBar.setComponentAlignment(quicknav, Alignment.MIDDLE_RIGHT);
		quicknav.setStyleName("segment");

		Component searchComponent = createSearch();
		quicknav.addComponent(searchComponent);

		Component treeSwitch = createTreeSwitch();
		quicknav.addComponent(treeSwitch);
		
		annotationLayout = new FancyCssLayout();
		annotationLayout.setSlideEnabled(true);
		VisualPlugin tools = new Tools(null);
		setVisualPlugin(tools);
		
		annotationBar = new MenuBar();
		annotationBar.setWidth("100%");
		MenuBar.MenuItem up = annotationBar.addItem("", new ThemeResource(
				"../runo/icons/16/arrow-up.png"), new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				annotationLayout.removeAllComponents();
				annotationLayout.setMargin(true);
				annotationLayout.setHeight("250px");
				annotationLayout.setWidth("100%");
				annotationLayout.setImmediate(true);
				annotationLayout.addComponent(buildAnnotationTabSheet());	
				addComponent(annotationLayout);	

				for(int i=0; i<annotationBar.getItems().size(); i++) {
					if(annotationBar.getItems().get(i).getDescription().equalsIgnoreCase("Close Annotation")) {
						annotationBar.getItems().get(i).setVisible(true);	
					} else {
						annotationBar.getItems().get(i).setVisible(false);	
					}
				}
			}
		});
		up.setDescription("View Annotation");
		MenuBar.MenuItem down = annotationBar.addItem("", new ThemeResource(
				"../runo/icons/16/arrow-down.png"), new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				removeComponent(annotationLayout);
				for(int i=0; i<annotationBar.getItems().size(); i++) {
					if(annotationBar.getItems().get(i).getDescription().equalsIgnoreCase("Close Annotation")) {
						annotationBar.getItems().get(i).setVisible(false);	
					} else {
						annotationBar.getItems().get(i).setVisible(true);	
					}
				}
			}
		});
		down.setDescription("Close Annotation");
		down.setVisible(false);
		annotationBar.setVisible(false);
		addComponent(annotationBar);
		
		pluginView.setVisualPlugin(new Tools(0L));
		
		
		
	}

	/**
	 * Creates the data tree for the project panel
	 * @return Tree
	 */
	private Tree createMenuTree() {
		final Tree tree = new Tree();
		tree.setImmediate(true);
		tree.setStyleName("menu");
		tree.setContainerDataSource(getDataContainer());
		tree.setSelectable(true);
		tree.setMultiSelect(false);
		tree.addListener(new Tree.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {

				Item selectedItem = tree.getItem(event.getProperty().getValue());
				VisualPlugin f;
				try {				
					if( event.getProperty().getValue()!=null ) {

						annotButton.setEnabled(true);
						removeButton.setEnabled(true);
						String className = (String) selectedItem.getItemProperty("Type").getValue();
						annotationBar.setVisible(true);
						for(int i=0; i<annotationBar.getItems().size(); i++) {
							if(annotationBar.getItems().get(i).getDescription().equalsIgnoreCase("Close Annotation")) {
								annotationBar.getItems().get(i).setVisible(false);	
							} else {
								annotationBar.getItems().get(i).setVisible(true);	
							}
						}

						if (className.contains("Results") && selectedItem.getItemProperty("Name").toString().contains("Pending")){
							pluginView.removeAllComponents();
							return;
						}
						
						dataSetId = (Long) event.getProperty().getValue();    
						
						toolBar.setEnabled(false);
						// TODO special things to do for microarray set should be considered as part of the overall design
						// not as a special case patched as aftermath fix
						if (className.equals("org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet")){
							// (1)
							DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(UserDirUtils.getDataSet(dataSetId));
							Map<String, Object> parameters = new HashMap<String, Object>();	
							parameters.put("datasetid", dataSetId);	
							List<Annotation> annots = FacadeFactory.getFacade().list(
									"Select a from Annotation a, DataSetAnnotation da where a.id=da.annotationid and da.datasetid=:datasetid", parameters);
							if (!annots.isEmpty()){
								APSerializable aps = (APSerializable) ObjectConversion.toObject(UserDirUtils.getAnnotation(annots.get(0).getId()));
								AnnotationParser.setFromSerializable(aps);
							}else {
								AnnotationParser.setCurrentDataSet(maSet);
							}
							((CSMicroarraySet)maSet).getMarkers().correctMaps();

							// (2)
							toolBar.setEnabled(true);
							for(int i=0; i<toolBar.getItems().size(); i++) {
								if(toolBar.getItems().get(i).getText().equalsIgnoreCase("SET VIEW")) {
									toolBar.getItems().get(i).setEnabled(true);	
								}
							}
						}
						
						ClassLoader classLoader = this.getClass().getClassLoader();
						String packageName = className.toLowerCase();
						if(className.contains("Results")) {
							packageName = className.substring(0, className.length() - 7).toLowerCase()+".results";

						} else {
							// TODO try the input data type first. the same design will be extended to result data later
							// input for now is only either microarray set or protein structure
							Class<?> aClass = classLoader.loadClass(className);
							removeComponent(annotationLayout);
							Class<? extends DataTypeUI> uiComponentClass = GeworkbenchRoot.getPluginRegistry().getDataUI(aClass);
							DataTypeUI dataUI = uiComponentClass.getDeclaredConstructor(Long.class).newInstance(dataSetId);
							pluginView.setDataUI(dataUI); // TODO to be implemented
							return;
						}
						// FIXME this part can be reached by result only. 
						// in other words, we only expect CSMcrioarraySet and CSProteinStructure (not VisualPlugin) if it is not result
						String loadClass = "org.geworkbenchweb.plugins." + packageName
								+ "." + className;
						Class<?> aClass = classLoader.loadClass(loadClass);
						f = (VisualPlugin) aClass.getDeclaredConstructor(Long.class).newInstance(dataSetId); 
						removeComponent(annotationLayout);
						log.warn("set visual plugin f="+f.getClass()+" "+f.getName());
						setVisualPlugin(f);
					}
				}catch (Exception e) { // FIXME what kind of exception is expected here? why?
					e.printStackTrace();
					VisualPlugin tools = new Tools(null);
					setVisualPlugin(tools);
					removeComponent(annotationLayout);
					annotationBar.setVisible(false);
				}
			}
		});
		
		tree.setItemDescriptionGenerator(new ItemDescriptionGenerator() {
            
			private static final long serialVersionUID = -3576690826530527342L;

			@SuppressWarnings("unchecked")
			@Override
            public String generateDescription(Component source, Object itemId,
                    Object propertyId) {
               if (itemId != null)
               {
            	    
            		   long id = Long.parseLong(itemId.toString());
            		   Item item = tree.getItem(itemId);            		   
            		   String className = (String) item.getItemProperty("Type").getValue();
            		   if(!className.contains("Results")) 
            		   {
            			    		    
            		       DSDataSet<? extends DSBioObject> df = (DSDataSet<? extends DSBioObject>) ObjectConversion.toObject(UserDirUtils.getDataSet(id));
            		       String description = null;
            		       try {
            		    	   description = df.getDescription();
            		       } catch (NullPointerException e) { // CSProteinStrcuture has null pointer exception not handled
            		    	   log.error(e.getMessage());
            		       }
            	           return description;
            		   }            	  
            	    
               }                
                
                return null;
            }
		});
		
		return tree;
	}

	/**
	 * Sets the VisualPlugin 
	 * @return
	 */
	/* FIXME this is fact a map from a 'VisualPlugin', say, Abc, to its corresponding AbcUI
	 * Because of many problems in the basic design of VisualPlugin, I am changing things away from it.
	 * For example, Analysis now is no long a VisualPuglin.
	 * Many things would be simpler if they don't go through this mapping, e.g.
	 * DataSet.getType() not "PDB File" -> tree node type set to be "Microarry" -> class Microarray -> MicroarrayUI would become
	 * DataSet.getType() -> MicroarrayUI
	 * */
	public void setVisualPlugin(VisualPlugin f) {
		// comment out the code that does not apply after some cleaning-up. to be removed. TODO
//		if(f instanceof Microarray) {
//			toolBar.setEnabled(true);
//			for(int i=0; i<toolBar.getItems().size(); i++) {
//				if(toolBar.getItems().get(i).getText().equalsIgnoreCase("SET VIEW")) {
//					toolBar.getItems().get(i).setEnabled(true);	
//				}
//			}
//		} else {
			toolBar.setEnabled(false);
			for(int i=0; i<toolBar.getItems().size(); i++) {
				toolBar.getItems().get(i).setEnabled(false);	
			}
//		} 
		pluginView.setVisualPlugin(f);
	}

	/**
	 * Creates the logo for the Application		
	 * @return
	 */
	private Component createLogo() {
		Button logo = new NativeButton("", new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {

			}
		});
		logo.setDescription("geWorkbench Home");
		logo.setStyleName(Button.STYLE_LINK);
		logo.addStyleName("logo");
		return logo;
	}

	public void removeSubwindows() {
		Collection<Window> wins = getApplication().getMainWindow().getChildWindows();
		if (null != wins) {
			for (Window w : wins) {
				getApplication().getMainWindow().removeWindow(w);
			}
		}
	}

	/**
	 * Search
	 */
	private Component createSearch() {
		search = new ComboBox();
		search.setWidth("160px");
		search.setNewItemsAllowed(false);
		search.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		search.setNullSelectionAllowed(true);
		search.setImmediate(true);
		search.setInputPrompt("Search samples...");
		/*
		 * PopupView pv = new PopupView("", search) { public void
		 * changeVariables(Object source, Map variables) {
		 * super.changeVariables(source, variables); if (isPopupVisible()) {
		 * search.focus(); } } };
		 */
		final PopupView pv = new PopupView("<span></span>", search);
		pv.addListener(new PopupView.PopupVisibilityListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if (event.isPopupVisible()) {
					search.focus();
				}
			}
		});
		pv.setStyleName("quickjump");
		pv.setDescription("Quick jump");
		return pv;
	}
	
	/**
	 * Creates the tree switch for the mainsplit 
	 * @return
	 */
	private Component createTreeSwitch() {
		final Button b = new NativeButton();
		b.setStyleName("tree-switch");
		b.addStyleName("down");
		b.setDescription("Toggle sample tree visibility");
		b.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if (b.getStyleName().contains("down")) {
					b.removeStyleName("down");
					mainSplit.setSplitPosition(0);
					navigationTree.setVisible(false);
					mainSplit.setLocked(true);
				} else {
					b.addStyleName("down");
					mainSplit.setSplitPosition(250, SplitPanel.UNITS_PIXELS);
					mainSplit.setLocked(false);
					navigationTree.setVisible(true);
				}
			}
		});
		mainSplit.setSplitPosition(250, SplitPanel.UNITS_PIXELS);
		return b;
	}


	/**
	 * Supplies the container for the dataset and result tree to display. 
	 * @return dataset and resultset container 
	 */
	public HierarchicalContainer getDataContainer() {

		HierarchicalContainer dataSets 		= 	new HierarchicalContainer();
		dataSets.addContainerProperty("Name", String.class, null);
		dataSets.addContainerProperty("Type", String.class, null);
		dataSets.addContainerProperty("Icon", Resource.class, null);
		
		Map<String, Object> param 		= 	new HashMap<String, Object>();
		param.put("owner", user.getId());
		param.put("workspace", WorkspaceUtils.getActiveWorkSpace());

		List<?> data =  FacadeFactory.getFacade().list("Select p from DataSet as p where p.owner=:owner and p.workspace =:workspace", param);

		for(int i=0; i<data.size(); i++) {

			String id 		=	((DataSet) data.get(i)).getName();
			Long dataId		=	((DataSet) data.get(i)).getId();	

			Item subItem = dataSets.addItem(dataId);
			subItem.getItemProperty("Name").setValue(id);
			String className = ((DataSet) data.get(i)).getType();
			subItem.getItemProperty("Type").setValue(className);
			try {
				ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getIcon(Class.forName(className));
				subItem.getItemProperty("Icon").setValue(icon);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				//subItem.getItemProperty("Icon").setValue(icon);
			}

			Map<String, Object> params 	= 	new HashMap<String, Object>();
			params.put("owner", user.getId());
			params.put("parent", dataId);
			List<?> results = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.owner=:owner and p.parent=:parent ORDER by p.date", params);

			if(results.size() == 0) {
				dataSets.setChildrenAllowed(dataId, false);
			}

			for(int j=0; j<results.size(); j++) {
				String subId 		=	((ResultSet) results.get(j)).getName();
				Long subSetId		=	((ResultSet) results.get(j)).getId();	
				String type			=	((ResultSet) results.get(j)).getType();

				Item res = dataSets.addItem(subSetId);
				res.getItemProperty("Name").setValue(subId);
				res.getItemProperty("Type").setValue(type);
				if(subId.contains("Pending")) {
					res.getItemProperty("Icon").setValue(pendingIcon);
				} else {
					if(type.equalsIgnoreCase("HierarchicalClusteringResults")) {
						res.getItemProperty("Icon").setValue(hcIcon);
					} else if(type.equalsIgnoreCase("CNKBResults")) {
						res.getItemProperty("Icon").setValue(networkIcon);
					} else if(type.equalsIgnoreCase("CytoscapeResults")) {
						res.getItemProperty("Icon").setValue(networkIcon);
					} else if(type.equalsIgnoreCase("MarkusResults")) {
						res.getItemProperty("Icon").setValue(markusIcon);
					} else if(type.equalsIgnoreCase("AnovaResults")) {
						res.getItemProperty("Icon").setValue(anovaIcon);
					} else if (type.equalsIgnoreCase("AracneResults")) {
						res.getItemProperty("Icon").setValue(networkIcon);
					} else if(type.equalsIgnoreCase("MarinaResults")) {
						res.getItemProperty("Icon").setValue(marinaIcon);
					} else if(type.equalsIgnoreCase("TTestResults")) {
						res.getItemProperty("Icon").setValue(anovaIcon);
					} 
				}
				dataSets.setChildrenAllowed(subSetId, false);
				dataSets.setParent(subSetId, dataId);
			}
		}
		return dataSets;
	}

	/**
	 * Method is used to populate Phenotype Panel
	 * @param maSet
	 * @return - Indexed container with array labels
	 */
	private HierarchicalContainer arrayTableView(DSMicroarraySet maSet) {

		HierarchicalContainer tableData 		= 	new HierarchicalContainer();

		tableData.addContainerProperty("Labels", String.class, null);
		Item mainItem 					= 	tableData.addItem("Phenotypes");
		mainItem.getItemProperty("Labels").setValue("Phenotypes" + " ["+ maSet.size() + "]");

		for(int k=0;k<maSet.size();k++) {
			Item item 					= 	tableData.addItem(k);
			tableData.setChildrenAllowed(k, false);
			item.getItemProperty("Labels").setValue(maSet.get(k).getLabel());
			tableData.setParent(k, "Phenotypes");
		}
		return tableData;
	}

	/**
	 * Method is used to populate Marker Panel
	 * @param maSet
	 * @return - Indexed container with marker labels
	 */
	private HierarchicalContainer markerTableView(DSMicroarraySet maSet) {

		HierarchicalContainer tableData 		= 	new HierarchicalContainer();
		tableData.addContainerProperty("Labels", String.class, null);

		Item mainItem =  tableData.addItem("Markers");
		mainItem.getItemProperty("Labels").setValue("Markers" + " [" + maSet.getMarkers().size()+ "]");

		for(int j=0; j<maSet.getMarkers().size();j++) {

			Item item 					= 	tableData.addItem(j);
			tableData.setChildrenAllowed(j, false);

			for(int k=0;k<=maSet.size();k++) {
				if(k == 0) {
					item.getItemProperty("Labels").setValue(maSet.getMarkers().get(j).getLabel() 
							+ " (" 
							+ maSet.getMarkers().get(j).getGeneName()
							+ ")");
					tableData.setParent(j, "Markers");
				} 
			}
		}
		return tableData;
	}

	/**
	 * Adds the node to the dataTree  
	 */
	public class NodeAddListener implements NodeAddEventListener {
		@Override
		public void addNode(NodeAddEvent event) {	
			if(event.getData() instanceof ResultSet ) {
				ResultSet  res = (ResultSet) event.getData();
				navigationTree.setChildrenAllowed(res.getParent(), true);
				navigationTree.addItem(res.getId());
				navigationTree.getContainerProperty(res.getId(), "Name").setValue(res.getName());
				navigationTree.getContainerProperty(res.getId(), "Type").setValue(res.getType());
				if(res.getName().contains("Pending")) {
					navigationTree.getContainerProperty(res.getId(), "Icon").setValue(pendingIcon);
				} else {
					if(res.getType().equalsIgnoreCase("HierarchicalClusteringResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(hcIcon);
					} else if(res.getType().equalsIgnoreCase("CNKBResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(networkIcon);
					} else if(res.getType().equalsIgnoreCase("CytoscapeResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(networkIcon);
					} else if (res.getType().equalsIgnoreCase("MarkusResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(markusIcon);
					} else if (res.getType().equalsIgnoreCase("AnovaResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(anovaIcon);
					} else if (res.getType().equalsIgnoreCase("AracneResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(networkIcon);
					} else if (res.getType().equalsIgnoreCase("MarinaResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(marinaIcon);
					} else if (res.getType().equalsIgnoreCase("TTestResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(anovaIcon);
					} 
				}
				navigationTree.setChildrenAllowed(res.getId(), false);
				navigationTree.setParent(res.getId(), res.getParent());
				if (res.getType().equals("MarkusResults")) navigationTree.select(res.getId());
			} else if(event.getData() instanceof DataSet) {
				DataSet dS = (DataSet) event.getData();
				String className = dS.getType();
				navigationTree.addItem(dS.getId());
				navigationTree.setChildrenAllowed(dS.getId(), false);
				try {
					ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getIcon(Class.forName(className));
					navigationTree.getContainerProperty(dS.getId(), "Icon").setValue(icon);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				navigationTree.getContainerProperty(dS.getId(), "Name").setValue(dS.getName());
				navigationTree.getContainerProperty(dS.getId(), "Type").setValue(className);
				navigationTree.select(dS.getId());
			}
		}
	}

	/**
	 * PluginListener class listenes to the PluginEvent and sets the desired VisualPlugin
	 * @author np2417
	 */
	public class PluginListener implements PluginEventListener {

		@SuppressWarnings("unchecked")
		@Override
		public void pluginSet(PluginEvent event) {

			navigationTree.unselect(event.getDataId());
			String pluginName = event.getPluginName();
			Long dataSetId = event.getDataId();

			ClassLoader classLoader = this.getClass().getClassLoader();
			String loadClass = "org.geworkbenchweb.plugins." + 
					pluginName.toLowerCase() +
					"."+
					pluginName;

			@SuppressWarnings("rawtypes")
			Class aClass = null;
			try {
				aClass = classLoader.loadClass(loadClass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			VisualPlugin f = null;
			try {
				f = (VisualPlugin) aClass.getDeclaredConstructor(Long.class).newInstance(dataSetId);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} 
			setVisualPlugin(f);
		}
	}

	/**
	 * Used to submit the analysis in geWorkbench and updates the data tree with result nodes once the 
	 * analysis is complete in the background.
	 * @author Nikhil
	 */
	public class AnalysisListener implements AnalysisSubmissionEventListener {

		@Override
		public void SubmitAnalysis(final AnalysisSubmissionEvent event) {

			Thread analysisThread = new Thread() {
				@Override
				public void run() {
					final ResultSet resultSet = event.getResultSet();
					HashMap<Serializable, Serializable> params = event.getParameters();

					DSMicroarraySet dataSet = null;
					try {
						dataSet = (DSMicroarraySet) event.getDataSet();
					} catch (Exception e) {
						// FIXME catching all clause is evil; catching all and doing nothing is the evil of evils
						e.printStackTrace();
					}
					// FIXME this particular if/else structure destroys any value of OO paradigm
					if(resultSet.getType().contains("HierarchicalClusteringResults")) {
						HierarchicalClusteringWrapper analysis = new HierarchicalClusteringWrapper(
								dataSet, params);
						resultSet.setName("Hierarchical Clustering");
						UserDirUtils.saveResultSet(resultSet.getId(), ObjectConversion.convertToByte(analysis.execute()));
					} else if(resultSet.getType().contains("CNKBResults")) {
						CNKBInteractions cnkb = new CNKBInteractions();
						Vector<CellularNetWorkElementInformation> hits = cnkb.CNKB(dataSet, params);
						resultSet.setName("CNKB");
						UserDirUtils.saveResultSet(resultSet.getId(), ObjectConversion.convertToByte(hits));
					} else if(resultSet.getType().contains("CytoscapeResults")) {
						CNKBInteractions cnkb = new CNKBInteractions();
						UserDirUtils.saveResultSet(resultSet.getId(), ObjectConversion.convertToByte(cnkb.CreateNetwork(params)));	
						resultSet.setName("Cytoscape");
						 
					} else if(resultSet.getType().contains("AnovaResults")) {
						AnovaAnalysis analysis = new AnovaAnalysis(dataSet, (AnovaUI) params.get("form"));
						UserDirUtils.saveResultSet(resultSet.getId(), ObjectConversion.convertToByte(analysis.execute()));
						resultSet.setName("Anova");
					} else if(resultSet.getType().contains("AracneResults")) {
						AracneAnalysisWeb analyze = new AracneAnalysisWeb(dataSet, params);
						UserDirUtils.saveResultSet(resultSet.getId(), ObjectConversion.convertToByte(analyze.execute()));
						resultSet.setName("Aracne");
					} else if(resultSet.getType().contains("TTestResults")) {
							TTestAnalysisWeb analyze = new TTestAnalysisWeb(dataSet, params);
							UserDirUtils.saveResultSet(resultSet.getId(), ObjectConversion.convertToByte(analyze.execute()));
							resultSet.setName("TTest");
					}else if(resultSet.getType().contains("MarinaResults")) {
						MarinaAnalysis analyze = new MarinaAnalysis(dataSet, params);
						try{
							CSMasterRegulatorTableResultSet mraRes = analyze.execute();
							UserDirUtils.saveResultSet(resultSet.getId(), ObjectConversion.convertToByte(mraRes));
							resultSet.setName("Marina - " + mraRes.getLabel());
						}catch(RemoteException e){
							String msg = e.getMessage().replaceAll("\n", "<br>");
							MessageBox mb = new MessageBox(getWindow(), 
									"Analysis Problem", MessageBox.Icon.ERROR, msg,  
									new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
							mb.show();	
							FacadeFactory.getFacade().delete(resultSet);
							navigationTree.removeItem(resultSet.getId());
							pusher.push();
							return;	
						}
					}
					FacadeFactory.getFacade().store(resultSet);	
					synchronized(getApplication()) {
						MessageBox mb = new MessageBox(getWindow(), 
								"Analysis Completed", 
								MessageBox.Icon.INFO, 
								"Analysis you submitted is now completed. " +
										"Click on the node to see the results",  
										new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
						mb.show(new MessageBox.EventListener() {
							private static final long serialVersionUID = 1L;
							@Override
							public void buttonClicked(ButtonType buttonType) {    	
								if(buttonType == ButtonType.OK) {
									NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
									GeworkbenchRoot.getBlackboard().fire(resultEvent);
								}
							}
						});	
					}
					pusher.push();
				}
			};
			analysisThread.start();
		}
	}

	 
	private TabSheet buildAnnotationTabSheet() {
		HorizontalSplitPanel dLayout 	=  	new HorizontalSplitPanel();
		dLayout.setSplitPosition(60);
		dLayout.setSizeFull();
		dLayout.setImmediate(true);
		dLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		dLayout.setLocked(true);
		
		final VerticalLayout commentsLayout = new VerticalLayout();
		commentsLayout.setImmediate(true);
		commentsLayout.setMargin(true);
		commentsLayout.setSpacing(true);
		commentsLayout.setSizeUndefined();
		
		Label cHeading 		=	new Label("User Comments:");
		cHeading.setStyleName(Reindeer.LABEL_H2);
		cHeading.setContentMode(Label.CONTENT_PREFORMATTED);
		commentsLayout.addComponent(cHeading);
		
		Map<String, Object> params 		= 	new HashMap<String, Object>();
		params.put("parent", dataSetId);

		List<?> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", params);
		if(comments.size() != 0){
			for(int i=0;i<comments.size();i++) {
				java.sql.Date date = ((Comment) comments.get(i)).getDate();
				Label comment = new Label(date.toString()+
						" - " +
						((Comment) comments.get(i)).getComment());
				commentsLayout.addComponent(comment);
			}
		}

		dLayout.setFirstComponent(commentsLayout);
		
		VerticalLayout commentArea = new VerticalLayout();
		commentArea.setImmediate(true);
		commentArea.setMargin(true);
		commentArea.setSpacing(true);
		
		Label commentHead 		=	new Label("Enter new comment here:");
		commentHead.setStyleName(Reindeer.LABEL_H2);
		commentHead.setContentMode(Label.CONTENT_PREFORMATTED);
		final TextArea dataArea = 	new TextArea();
		dataArea.setRows(6);
		dataArea.setWidth("100%");
		Button submitComment	=	new Button("Add Comment", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(!dataArea.getValue().toString().isEmpty()) {
					java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
					
					Comment c = new Comment();
					c.setParent(dataSetId);
					c.setComment(dataArea.getValue().toString());
					c.setDate(date);
					FacadeFactory.getFacade().store(c);
					
					Label comment = new Label(date.toString()+
							" - " +
							dataArea.getValue().toString());
					commentsLayout.addComponent(comment);
					dataArea.setValue("");
				}
			}
		});
		submitComment.setClickShortcut(KeyCode.ENTER);
		commentArea.addComponent(commentHead);
		commentArea.setComponentAlignment(commentHead, Alignment.MIDDLE_LEFT);
		commentArea.addComponent(dataArea);
		commentArea.setComponentAlignment(dataArea, Alignment.MIDDLE_LEFT);
		commentArea.addComponent(submitComment);
		commentArea.setComponentAlignment(submitComment, Alignment.MIDDLE_LEFT);
		dLayout.setSecondComponent(commentArea);
		
		HorizontalSplitPanel infoSplit 	=  	new HorizontalSplitPanel();
		infoSplit.setSplitPosition(50);
		infoSplit.setSizeFull();
		infoSplit.setImmediate(true);
		infoSplit.setStyleName(Reindeer.SPLITPANEL_SMALL);
		infoSplit.setLocked(true);
				
		VerticalLayout dataHistory 	= 	new VerticalLayout();
		VerticalLayout expInfo		=	new VerticalLayout();
		
		dataHistory.setSizeUndefined();
		dataHistory.setMargin(true);
		dataHistory.setSpacing(true);
		dataHistory.setImmediate(true);

		Label historyHead 		=	new Label("Data History:");
		historyHead.setStyleName(Reindeer.LABEL_H2);
		historyHead.setContentMode(Label.CONTENT_PREFORMATTED);
		dataHistory.addComponent(historyHead);
		
		Map<String, Object> eParams 		= 	new HashMap<String, Object>();
		eParams.put("parent", dataSetId);

		List<?> histories =  FacadeFactory.getFacade().list("Select p from DataHistory as p where p.parent =:parent", eParams);
		for(int i=0; i<histories.size(); i++) {
			DataHistory dH = (DataHistory) histories.get(i);
			Label d = new Label((String) ObjectConversion.toObject(dH.getData()));
			d.setContentMode(Label.CONTENT_PREFORMATTED);
			dataHistory.addComponent(d);
		}
		
		expInfo.setSizeUndefined();
		expInfo.setMargin(true);
		expInfo.setSpacing(true);
		
		Label infoHead 		=	new Label("Experiment Information:");
		infoHead.setStyleName(Reindeer.LABEL_H2);
		infoHead.setContentMode(Label.CONTENT_PREFORMATTED);
		expInfo.addComponent(infoHead);
		
		Map<String, Object> iParams 		= 	new HashMap<String, Object>();
		iParams.put("parent", dataSetId);

		List<?> info =  FacadeFactory.getFacade().list("Select p from ExperimentInfo as p where p.parent =:parent", iParams);
		for(int i=0; i<info.size(); i++) {
			ExperimentInfo eI = (ExperimentInfo) info.get(i);
			Label d = new Label((String) ObjectConversion.toObject(eI.getInfo()));
			d.setContentMode(Label.CONTENT_PREFORMATTED);
			expInfo.addComponent(d);
		}
	
		infoSplit.setFirstComponent(dataHistory);
		infoSplit.setSecondComponent(expInfo);
		TabSheet data = new TabSheet();
		data.setStyleName(Reindeer.TABSHEET_SMALL);
		data.setSizeFull();
		data.setImmediate(true);
	
		data.addTab(infoSplit, "Data Information");
		data.addTab(dLayout, "User Comments");	
		return data;
	}
}

