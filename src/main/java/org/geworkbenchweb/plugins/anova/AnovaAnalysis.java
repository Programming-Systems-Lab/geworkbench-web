package org.geworkbenchweb.plugins.anova;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;

import org.geworkbench.components.anova.data.AnovaInput;
import org.geworkbench.components.anova.data.AnovaOutput;
import org.geworkbench.components.anova.Anova;
import org.geworkbench.components.anova.AnovaException;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;

import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbenchweb.GeworkbenchRoot; 
import org.geworkbenchweb.utils.SubSetOperations;

 
/**
 * 
 * This class submits Anova Analysis from web application
 * 
 * @author Min You
 * 
 */
public class AnovaAnalysis {

	private static Log log = LogFactory.getLog(AnovaAnalysis.class);
	
	private static final String DEFAULT_WEB_SERVICES_URL = "http://afdev.c2b2.columbia.edu:9090/axis2/services/AnovaService";
	private static final String  ANOVA_WEBSERVICE_URL = "anova.webService.url";
	 
	private static String url = null;
	
	private DSMicroarraySet dataSet = null;
	private AnovaUI paramForm = null;
	private DSItemList<DSGeneMarker> selectedMarkers = null;
	private String[] selectedArraySetNames = null;
	
	public AnovaAnalysis(DSMicroarraySet dataSet, AnovaUI paramForm) {
		this.dataSet = dataSet;
		this.paramForm = paramForm;
	}

	public CSAnovaResultSet<DSGeneMarker> execute() {

		AnovaInput anovaInput = getAnovaInput();
		AnovaOutput output = computeAnovaRemote(anovaInput);

		/* Create panels and significant result sets to store results */
		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSSignificanceResultSet<DSGeneMarker>(
				dataSet, "Anova Analysis", new String[0],
				selectedArraySetNames, paramForm.getPValThreshold());

		CSAnovaResultSet<DSGeneMarker> anovaResultSet = null;

		int[] featuresIndexes = output.getFeaturesIndexes();
		double[] significances = output.getSignificances();		
		int[] significantPositions = new int[featuresIndexes.length];
		List<String> significantMarkerNames = new ArrayList<String>();

		for (int i = 0; i < featuresIndexes.length; i++) {
			DSGeneMarker item = selectedMarkers.get(featuresIndexes[i]);
			log.debug("SignificantMarker: " + item.getLabel()
					+ ", with apFM: " + significances[i]);

			sigSet.setSignificance(item, significances[i]);
			significantMarkerNames.add(item.getLabel());
			significantPositions[i] = item.getSerial();
		}

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
				dataSet);

		anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(dataView,
				"Anova Analysis Result Set", selectedArraySetNames,
				paramForm.getPValThreshold(), significantMarkerNames.toArray(new String[0]), output.getResult2DArray());
		log.debug(significantMarkerNames.size()
				+ " Markers added to anovaResultSet.");
		anovaResultSet.getSignificantMarkers().addAll(
				sigSet.getSignificantMarkers());
		log.debug(sigSet.getSignificantMarkers().size()
				+ " Markers added to anovaResultSet.getSignificantMarkers().");

		if (significantMarkerNames.size() > 0)
		{
			anovaResultSet.sortMarkersBySignificance();	
			java.util.Collections.sort(significantMarkerNames);
			SubSetOperations.storeSignificance(significantMarkerNames, paramForm.getDataSetId(), paramForm.getUserId());
		}		
		return anovaResultSet;
	}

	private AnovaOutput computeAnovaRemote(AnovaInput input) {
		AnovaOutput output = null;
		RPCServiceClient serviceClient;

		try {
			
			getWebServiceUrl();
			
			serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();

			long soTimeout =  24 * 60 * 60 * 1000; // 24 hours
			options.setTimeOutInMilliSeconds(soTimeout);


			EndpointReference targetEPR = new EndpointReference(url);
					 
			options.setTo(targetEPR);

			// notice that that namespace is in the required form
			QName opName = new QName(
					"http://service.anova.components.geworkbench.org",
					"execute");
			Object[] args = new Object[] { input };

			Class<?>[] returnType = new Class[] { AnovaOutput.class };

			Object[] response = serviceClient.invokeBlocking(opName, args,
					returnType);
			output = (AnovaOutput) response[0];

			return output;
		} catch (AxisFault e) {
			OMElement x = e.getDetail();
			if (x != null)
				log.debug(x);

			Throwable y = e.getCause();
			while (y != null) {
				y.printStackTrace();
				y = y.getCause();
			}

			log.debug("message: " + e.getMessage());
			log.debug("fault action: " + e.getFaultAction());
			log.debug("reason: " + e.getReason());
			e.printStackTrace();
		}

		return output;
	}

	private AnovaInput getAnovaInput() {
		String[] selectedMarkerSet = null;
		String[] selectedArraySet = null;

		@SuppressWarnings("unused")
		String GroupAndChipsString = "";

		selectedMarkerSet = paramForm.getSelectedMarkerSet();

		if (selectedMarkerSet == null) {
			selectedMarkers = dataSet.getMarkers();
		} else {
			selectedMarkers = new CSItemList<DSGeneMarker>();
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				ArrayList<String> temp = paramForm.getMarkerData(Long.parseLong(selectedMarkerSet[i].trim()));
				for(int m=0; m<temp.size(); m++) {
					String temp1 = ((temp.get(m)).split("\\s+"))[0].trim();					 
					DSGeneMarker marker = dataSet.getMarkers().get(temp1);
					if (marker != null)
						selectedMarkers.add(marker);
				}
				 
			} 
		}

		selectedArraySet = paramForm.getSelectedArraySet();
		selectedArraySetNames = paramForm.getSelectedArraySetNames();

		int selectedMarkersNum = selectedMarkers.size();
		int globleArrayIndex = 0;
		int numSelectedGroups = selectedArraySet.length;

		GroupAndChipsString += numSelectedGroups + " groups analyzed:\n";

		globleArrayIndex = paramForm.getTotalSelectedArrayNum();
		int[] groupAssignments = new int[globleArrayIndex];
		float[][] A = new float[selectedMarkersNum][globleArrayIndex];

		globleArrayIndex = 0;
		/* for each groups */

		log.debug("selectedMarkers.size() = " + selectedMarkers.size());
		for (int i = 0; i < numSelectedGroups; i++) {
			ArrayList<String> arrayPositions = paramForm.getArrayData(Long
					.parseLong(selectedArraySet[i].trim()));

			String groupLabel = selectedArraySetNames[i];
			/* put group label into history */
			GroupAndChipsString += "\tGroup " + groupLabel + " (" + arrayPositions.size()
					+ " chips)" + ":\n";

			/*
			 * for each array in this group
			 */
			for (int j = 0; j < arrayPositions.size(); j++) {
				GroupAndChipsString += "\t\t"
						+ arrayPositions.get(j) + "\n";
				/* for each marker in this array */
				for (int k = 0; k < selectedMarkersNum; k++) {
					A[k][globleArrayIndex] = (float) (dataSet.get(arrayPositions.get(j)))
							.getMarkerValue(selectedMarkers.get(k)).getValue();

				}
				groupAssignments[globleArrayIndex] = i + 1;
				globleArrayIndex++;
			}
		}

		AnovaInput anovaInput = new AnovaInput(A, groupAssignments,
				selectedMarkersNum, numSelectedGroups,
				paramForm.getPValThreshold(), paramForm.getPValueEstimation(),
				paramForm.getPermNumber(),
				paramForm.getFalseDiscoveryRateControl(),
				paramForm.getFalseSignificantGenesLimit());

		return anovaInput;
	}

	@SuppressWarnings("unused")
	private AnovaOutput computeAnovaLocal(AnovaInput input) {
		AnovaOutput output = null;
		try {
			Anova anova = new Anova(input);
			output = anova.execute();

		} catch (AnovaException AE) {

			log.debug(AE.getMessage());
		}
		return output;
	}
	
	 
	
	private void getWebServiceUrl()
	{
		if (url == null || url.trim().equals(""))
		{
			 		
				url  = GeworkbenchRoot.getAppProperty(ANOVA_WEBSERVICE_URL);
				if (url == null || url.trim().equals(""))
					url = DEFAULT_WEB_SERVICES_URL;
				
		}		
		 
	}
	
}