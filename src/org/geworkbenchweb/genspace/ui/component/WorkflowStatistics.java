package org.geworkbenchweb.genspace.ui.component;

import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.geworkbenchweb.utils.LayoutUtil;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;

public class WorkflowStatistics extends AbstractGenspaceTab implements GenSpaceTab {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private VerticalLayout verticalLayout_1;
	@AutoGenerated
	private VerticalLayout individualToolStatisticsContainer;
	@AutoGenerated
	private Label lblToolStatistics;
	@AutoGenerated
	private Select cmbSelectedTool;
	@AutoGenerated
	private Label label_3;
	@AutoGenerated
	private VerticalLayout mostPopularWorkflowsContainer;
	@AutoGenerated
	private Label label_4;
	@AutoGenerated
	private HorizontalLayout horizontalLayout_1;
	@AutoGenerated
	private VerticalLayout mostPopularToolsAtStartContainer;
	@AutoGenerated
	private Label label_2;
	@AutoGenerated
	private VerticalLayout mostPopularToolsContainer;
	@AutoGenerated
	private Label label_1;
	private Panel mainPanel = new Panel();
	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@Override
	public void loggedIn() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void loggedOut() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void tabSelected() {
		
		
	}
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public WorkflowStatistics(final GenSpaceLogin login) {
		super(login);
		buildMainLayout();
		//setCompositionRoot(mainLayout);
		setCompositionRoot(mainPanel);

		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				getDataAndSetupUI();
			}
		});
		t.start();

		cmbSelectedTool.addValueChangeListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				String ret = "";
				Tool tool = (Tool) cmbSelectedTool.getValue();
				String usageRate = "" + tool.getUsageCount();
				ret += "Total usage rate: " + usageRate + "<br>";

				String usageRateAsWFHead = "" + tool.getWfCountHead();
				ret += "Total usage rate at start of workflow: "
						+ usageRateAsWFHead + " <br>";
				Tool mostPopularNextTool = login.getGenSpaceServerFactory().getUsageOps().getMostPopularNextTool(tool.getId());
				if(mostPopularNextTool == null)
					ret += "No tools are used after this one"+ "<br>";
				else
					ret += "The most popular tool used next to this tool: "
						+ mostPopularNextTool.getName() + "<br>";

				Tool mostPopularPreviousTool = login.getGenSpaceServerFactory().getUsageOps().getMostPopularPreviousTool(tool.getId());
				if(mostPopularPreviousTool == null)
					ret += "No tools are used before this one"+ "<br>";
				else
					ret += "The most popular tool used before this tool: "
						+ mostPopularPreviousTool.getName();
				lblToolStatistics.setValue("<html>" + ret + "</html>");
			}
		});
	}

	private void getDataAndSetupUI()
	{
		int i =1;
		for(Tool t: login.getGenSpaceServerFactory().getUsageOps().getToolsByPopularity())
		{
			Label l = new Label(i+": " + t.getName());
			mostPopularToolsContainer.addComponent(l);
			i++;
			if(i > 10)
				break;
		}
		
		i =1;
		for(Tool t: login.getGenSpaceServerFactory().getUsageOps().getMostPopularWFHeads())
		{
			Label l = new Label(i+": " + t.getName());
			mostPopularToolsAtStartContainer.addComponent(l);
			i++;
			if(i > 10)
				break;
		}
		
		i =1;
		for(Workflow s: login.getGenSpaceServerFactory().getUsageOps().getWorkflowsByPopularity())
		{

			WorkflowWrapper w = new WorkflowWrapper(s);
			w.loadToolsFromCache();
			Label l = new Label(i+": " + w.toString());
			mostPopularWorkflowsContainer.addComponent(l);
			i++;
			if(i > 10)
				break;
		}
		
		for(Tool t : login.getGenSpaceServerFactory().getUsageOps().getAllTools())
		{
			cmbSelectedTool.addItem(t);
			cmbSelectedTool.setItemCaption(t, t.getName());
		}
		setImmediate(true);
	}
	@AutoGenerated
	private void buildMainLayout() {
		// common part: create layout
		/*mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.addComponent(mainPanel);
		mainLayout.setWidth("100%");
		mainLayout.setHeight(null);
		mainLayout.setHeight("650px");
		mainLayout.setMargin(false);*/
		
		mainPanel.setWidth("100%");
		mainPanel.setHeight("650px");
		
		// top-level component properties
		/*setWidth("100.0%");
		setHeight("100.0%");*/
		
		// verticalLayout_1
		verticalLayout_1 = buildVerticalLayout_1();
		Panel contentPanel = new Panel();
		contentPanel.setWidth("100%");
		contentPanel.setContent(verticalLayout_1);

		//mainLayout.addComponent(verticalLayout_1, "top:20.0px;left:10.0px;");
		mainPanel.setContent(LayoutUtil.addComponent(contentPanel));
		
		//return mainLayout;
	}
	@AutoGenerated
	private VerticalLayout buildVerticalLayout_1() {
		// common part: create layout
		verticalLayout_1 = new VerticalLayout();
		verticalLayout_1.setImmediate(false);
		/*verticalLayout_1.setWidth("100.0%");
		verticalLayout_1.setHeight("100.0%");*/
		verticalLayout_1.setMargin(true);
		
		Label emptyLabel = new Label();
		emptyLabel.setHeight("20px");
		
		// horizontalLayout_1
		horizontalLayout_1 = buildHorizontalLayout_1();
		verticalLayout_1.addComponent(horizontalLayout_1);
		verticalLayout_1.addComponent(emptyLabel);
		
		// mostPopularWorkflowsContainer
		mostPopularWorkflowsContainer = buildMostPopularWorkflowsContainer();
		verticalLayout_1.addComponent(mostPopularWorkflowsContainer);
		
		emptyLabel = new Label();
		emptyLabel.setHeight("20px");
		verticalLayout_1.addComponent(emptyLabel);
		
		// individualToolStatisticsContainer
		individualToolStatisticsContainer = buildIndividualToolStatisticsContainer();
		verticalLayout_1.addComponent(individualToolStatisticsContainer);

		return verticalLayout_1;
	}
	@AutoGenerated
	private HorizontalLayout buildHorizontalLayout_1() {
		// common part: create layout
		horizontalLayout_1 = new HorizontalLayout();
		horizontalLayout_1.setImmediate(false);
		horizontalLayout_1.setWidth("100.0%");
		horizontalLayout_1.setHeight("-1px");
		horizontalLayout_1.setMargin(false);
		
		// mostPopularToolsContainer
		mostPopularToolsContainer = buildMostPopularToolsContainer();
		horizontalLayout_1.addComponent(mostPopularToolsContainer);
		
		// mostPopularToolsAtStartContainer
		mostPopularToolsAtStartContainer = buildMostPopularToolsAtStartContainer();
		horizontalLayout_1.addComponent(mostPopularToolsAtStartContainer);
		
		return horizontalLayout_1;
	}
	@AutoGenerated
	private VerticalLayout buildMostPopularToolsContainer() {
		// common part: create layout
		mostPopularToolsContainer = new VerticalLayout();
		mostPopularToolsContainer.setImmediate(false);
		mostPopularToolsContainer.setWidth("50.0%");
		mostPopularToolsContainer.setHeight("-1px");
		mostPopularToolsContainer.setMargin(false);
		
		// label_1
		label_1 = new Label();
		label_1.setStyleName("h4");
		label_1.setImmediate(false);
		label_1.setWidth("-1px");
		label_1.setHeight("-1px");
		label_1.setValue("Most Popular Tools");
		mostPopularToolsContainer.addComponent(label_1);
		
		return mostPopularToolsContainer;
	}
	@AutoGenerated
	private VerticalLayout buildMostPopularToolsAtStartContainer() {
		// common part: create layout
		mostPopularToolsAtStartContainer = new VerticalLayout();
		mostPopularToolsAtStartContainer.setImmediate(false);
		mostPopularToolsAtStartContainer.setWidth("50.0%");
		mostPopularToolsAtStartContainer.setHeight("-1px");
		mostPopularToolsAtStartContainer.setMargin(false);
		
		// label_2
		label_2 = new Label();
		label_2.setStyleName("h4");
		label_2.setImmediate(false);
		label_2.setWidth("-1px");
		label_2.setHeight("-1px");
		label_2.setValue("Most Popular Tools at Start of Workflow");
		mostPopularToolsAtStartContainer.addComponent(label_2);
		
		return mostPopularToolsAtStartContainer;
	}
	@AutoGenerated
	private VerticalLayout buildMostPopularWorkflowsContainer() {
		// common part: create layout
		mostPopularWorkflowsContainer = new VerticalLayout();
		mostPopularWorkflowsContainer.setImmediate(false);
		mostPopularWorkflowsContainer.setWidth("100.0%");
		mostPopularWorkflowsContainer.setHeight("-1px");
		mostPopularWorkflowsContainer.setMargin(false);
		
		// label_4
		label_4 = new Label();
		label_4.setStyleName("h4");
		label_4.setImmediate(false);
		label_4.setWidth("-1px");
		label_4.setHeight("-1px");
		label_4.setValue("Most Popular Workflows");
		mostPopularWorkflowsContainer.addComponent(label_4);
		
		return mostPopularWorkflowsContainer;
	}
	@AutoGenerated
	private VerticalLayout buildIndividualToolStatisticsContainer() {
		// common part: create layout
		individualToolStatisticsContainer = new VerticalLayout();
		individualToolStatisticsContainer.setImmediate(false);
		individualToolStatisticsContainer.setWidth("-1px");
		individualToolStatisticsContainer.setHeight("100px");
		individualToolStatisticsContainer.setMargin(false);
		
		// label_3
		label_3 = new Label();
		label_3.setStyleName("h4");
		label_3.setImmediate(false);
		label_3.setWidth("-1px");
		label_3.setHeight("-1px");
		label_3.setValue("Individual Tool Statistics");
		individualToolStatisticsContainer.addComponent(label_3);
		
		// cmbSelectedTool
		cmbSelectedTool = new Select();
		cmbSelectedTool.setImmediate(true);
		cmbSelectedTool.setWidth("-1px");
		cmbSelectedTool.setHeight("-1px");
		individualToolStatisticsContainer.addComponent(cmbSelectedTool);
		
		// lblToolStatistics
		lblToolStatistics = new Label();
		lblToolStatistics.setImmediate(false);
		lblToolStatistics.setWidth("-1px");
		lblToolStatistics.setHeight("-1px");
		lblToolStatistics.setValue("Select a tool to see its statistics");
		lblToolStatistics.setContentMode(ContentMode.HTML);
		individualToolStatisticsContainer.addComponent(lblToolStatistics);
		
		return individualToolStatisticsContainer;
	}

}