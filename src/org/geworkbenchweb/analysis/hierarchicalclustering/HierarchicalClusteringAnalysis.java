package org.geworkbenchweb.analysis.hierarchicalclustering;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;

import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * Hierarchical Clustering Analysis is submitted in this method.
 * @author Nikhil Reddy
 */

public class HierarchicalClusteringAnalysis {

	User user = SessionHandler.get();
	
	public HierarchicalClusteringAnalysis(DSMicroarraySet dataSet, String[] params, long dataSetId) {
		
		String clusterMethod 	= 	params[0];
		String clusterDim 		=	params[1];
		String clusterMetric	= 	params[2];
		
		try {	
			clusterMethod.isEmpty();
		} catch (Exception e) {
			clusterMethod = "Single Linkage";
		}

		try {
			clusterDim.isEmpty();			
		} catch(Exception e) {
			clusterDim = "Marker";
		}  

		try {	
			clusterMetric.isEmpty();
		} catch (Exception e) {
			clusterMetric = "Eucledian Distance";
		}
		
		int metric = parseDistanceMetric(clusterMetric);
		int method = parseMethod(clusterMethod);
		int dimension = parseDimension(clusterDim);
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> data
		= new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet);
		HierarchicalClusteringWrapper analysis 	= 	new HierarchicalClusteringWrapper(data, metric, method, dimension );
		HierCluster[] resultClusters = analysis.execute();
		CSHierClusterDataSet results = new CSHierClusterDataSet(resultClusters, null, false,
				"Hierarchical Clustering", data);

		ResultSet resultSet = 	new ResultSet();
		java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
		resultSet.setDateField(date);
		String dataSetName = "HC - " + new java.util.Date();
		resultSet.setName(dataSetName);
		resultSet.setType("Hierarchical Clustering");
		resultSet.setParent(dataSetId);
		resultSet.setOwner(user.getId());	
		resultSet.setData(ObjectConversion.convertToByte(results));
		FacadeFactory.getFacade().store(resultSet);	
		
		NodeAddEvent resultEvent = new NodeAddEvent(resultSet.getId(), dataSetName, "Result Node");
		GeworkbenchRoot.getBlackboard().fire(resultEvent);
	
	}
		

	// TODO these are temporary code. enum (or int) should be used instead String
	private static int parseDistanceMetric(String d) {
		if(d.equals("Eucledian Distance")) {
			return 0;
		} else if(d.equals("Pearson's Correlation")) {
			return 1;
		} else if(d.equals("Spearman's Rank Correlation")) {
			return 2;
		} else {
			return 0;
		}
	}
	private static int parseMethod(String method) {
		if(method.equals("Single Linkage")) {
			return 0;
		} else if(method.equals("Average Linkage")) {
			return 1;
		} else if(method.equals("Total linkage")) {
			return 2;
		} else {
			return 0;
		}
	}
	private static int parseDimension(String dim) {
		if(dim.equals("Marker")) {
			return 0;
		} else if(dim.equals("Microarray")) {
			return 1;
		} else if(dim.equals("Both")) {
			return 2;
		} else {
			return 0;
		}
	}
	
}
