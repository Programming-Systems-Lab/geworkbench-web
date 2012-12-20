package org.geworkbenchweb.plugins.ttest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * TTest Analysis for microarray dataset
 * @author Nikhil Reddy
 */
public class TTestUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	
	private Long dataSetId;
	
	private Accordion tabs;
	
	private ComboBox selectCase;
	
	private ComboBox selectControl;
	
	private ComboBox pValue;
	
	private ComboBox logNorm;
	
	private TextField criticalValue;
	
	private ComboBox correctionMethod;
	
	private ComboBox stepMethod;
	
	private ComboBox groupVariances;
	
	private Button submit;
	
	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
	
	public TTestUI(Long dId) {
		
		this.dataSetId = dId;
		
		setSpacing(true);
		setImmediate(true);
		
		tabs 			= 	new Accordion();
		selectCase		=	new ComboBox();
		selectControl 	=	new ComboBox();	
		
		tabs.addTab(buildPValuePanel(), "P-Value Parameters", null);
		tabs.addTab(buildAlphaCorrections(), "Alpha Corrections", null);
		tabs.addTab(buildDegOfFreedom(), "Degree of Freedom", null);
		
		List<?> arraySubSets = SubSetOperations.getArraySets(dataSetId);
		
		selectCase.setNullSelectionAllowed(false);
		selectCase.setInputPrompt("Select Case from Phenotypes sets");
		selectCase.setWidth("400px");
		selectCase.setImmediate(true);
		for (int m = 0; m < (arraySubSets).size(); m++) {
			selectCase.addItem(((SubSet) arraySubSets.get(m)).getId());
			selectCase.setItemCaption(
					((SubSet) arraySubSets.get(m)).getId(),
					((SubSet) arraySubSets.get(m)).getName());
		}
	
		selectControl.setNullSelectionAllowed(false);
		selectControl.setWidth("400px");
		selectControl.setInputPrompt("Select Control from Phenotypes sets");
		selectControl.setImmediate(true);
		for (int m = 0; m < (arraySubSets).size(); m++) {
			selectControl.addItem(((SubSet) arraySubSets.get(m)).getId());
			selectControl.setItemCaption(
					((SubSet) arraySubSets.get(m)).getId(),
					((SubSet) arraySubSets.get(m)).getName());
		}
		
		submit = new Button("Submit", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(selectCase.getValue().equals(null) || selectControl.getValue().equals(null)) {
					MessageBox mb = new MessageBox(getWindow(), 
							"Warning", 
							MessageBox.Icon.INFO, 
							"Please select case array and control array ",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					return;
				}
				params.put(TTestParameters.CASEARRAY, (Serializable) selectCase.getValue());
				params.put(TTestParameters.CONTROLARRAY, (Serializable) selectControl.getValue());
				params.put(TTestParameters.ALPHA, (Serializable) criticalValue.getValue());
				params.put(TTestParameters.CORRECTIONMETHOD, (Serializable) correctionMethod.getValue());
				params.put(TTestParameters.LOGNORMALIZED, (Serializable) logNorm.getValue());
				//params.put(TTestParameters.ALLCOMBINATATIONS, value);
				params.put(TTestParameters.WELCHDIFF, (Serializable) groupVariances.getValue());
				//params.put(TTestParameters.NUMCOMBINATIONS, value);
				
				
				
				List<DataSet> data = DataSetOperations
						.getDataSet(dataSetId);
				DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion
						.toObject(data.get(0).getData());

				ResultSet resultSet = new ResultSet();
				java.sql.Date date = new java.sql.Date(System
						.currentTimeMillis());
				resultSet.setDateField(date);
				String dataSetName = "TTest - Pending";
				resultSet.setName(dataSetName);
				resultSet.setType("TTestResults");
				resultSet.setParent(dataSetId);
				resultSet.setOwner(SessionHandler.get().getId());
				FacadeFactory.getFacade().store(resultSet);

				NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
				GeworkbenchRoot.getBlackboard().fire(resultEvent);

				AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
						maSet, resultSet, params);
				GeworkbenchRoot.getBlackboard().fire(analysisEvent);
				
			}
		});
		addComponent(selectCase);
		addComponent(selectControl);
		addComponent(tabs);
		addComponent(submit);
	}

	/**
	 * Builds P-value tab
	 * @return GridLayout
	 */
	private GridLayout buildPValuePanel() {
		GridLayout a = new GridLayout();
		
		a.setColumns(2);
		a.setRows(2);
		a.setImmediate(true);
		a.setSpacing(true);
		a.setHeight("150px");
		a.setMargin(true);
		
		pValue = new ComboBox();
		pValue.setNullSelectionAllowed(false);
		pValue.setCaption("Select Correction Method");
		pValue.addItem("t-distribution");
		pValue.addItem("permutation");
		pValue.select("t-distribution");
		pValue.setImmediate(true);
		
		logNorm = new ComboBox();
		logNorm.setNullSelectionAllowed(false);
		logNorm.setCaption("Data is log2-transformed");
		logNorm.addItem("No");
		logNorm.addItem("Yes");
		logNorm.select("No");
		logNorm.setImmediate(true);
		
		criticalValue = new TextField();
		criticalValue.setCaption("Overall Alpha");
		criticalValue.setValue("0.02");
		criticalValue.setNullSettingAllowed(false);
		
		a.addComponent(pValue, 0, 0);
		a.addComponent(criticalValue, 1, 0);
		a.addComponent(logNorm, 0, 1);
		return a;
	}

	/**
	 * Builds Aplha corrections tab
	 * @return VerticalLayout
	 */
	private VerticalLayout buildAlphaCorrections() {
		VerticalLayout b = new VerticalLayout();
		
		b.setImmediate(true);
		b.setSpacing(true);
		b.setHeight("125px");
		b.setMargin(true);
		
		correctionMethod = new ComboBox();
		correctionMethod.setNullSelectionAllowed(false);
		correctionMethod.setCaption("Select Case from Phenotypes sets");
		correctionMethod.addItem("Just alpha (no-correction)");
		correctionMethod.addItem("Standard Bonferroni Correction");
		correctionMethod.addItem("Adjusted Bonferroni Correction");
		correctionMethod.select("Just alpha (no-correction)");
		correctionMethod.setImmediate(true);
		
		stepMethod = new ComboBox();
		stepMethod.setNullSelectionAllowed(false);
		stepMethod.setCaption("Step down westfall and young methods (for permutation only)");
		stepMethod.addItem("minP");
		stepMethod.addItem("maxT");
		stepMethod.addItem("minP");
		stepMethod.setImmediate(true);
		stepMethod.setEnabled(false);
		
		b.addComponent(correctionMethod);
		b.addComponent(stepMethod);
		return b;
	}

	/**
	 * Builds Degree of Freedom tab 
	 * @return VerticalLayout
	 */
	private VerticalLayout buildDegOfFreedom() {
		VerticalLayout c = new VerticalLayout();
		
		c.setImmediate(true);
		c.setSpacing(true);
		c.setHeight("100px");
		c.setMargin(true);
		
		groupVariances = new ComboBox();
		groupVariances.setNullSelectionAllowed(false);
		groupVariances.setCaption("Group Variences");
		groupVariances.addItem("Unequal (Welch approximation)");
		groupVariances.addItem("Equal");
		groupVariances.select("Unequal (Welch approximation)");
		groupVariances.setImmediate(true);
		
		c.addComponent(groupVariances);
		
		return c;
	}
}
