package org.geworkbenchweb.layout;

import java.util.ArrayList;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.analysis.CNKB.ui.UCNKBTab;
import org.geworkbenchweb.analysis.hierarchicalclustering.ui.UClustergramTab;
import org.geworkbenchweb.visualizations.Cytoscape;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Reindeer;

public class UVisualPlugin extends TabSheet {

	private static final long serialVersionUID 				= 	1L;

	private static final String DATA_OPERATIONS 			= 	"Data Operations";

	private static final String MICROARRAY_TABLE_CAPTION 	= 	"Tabular Microarray Viewer";

	private static final String MARKER_HEADER 				= 	"Marker";
	
	private static final String HEAT_MAP 					= 	"Heat Map";
	
	private static Table dataTable;

	private DSMicroarraySet maSet;

	public UVisualPlugin(Object dataSet, String dataType, String action) {

		setSizeFull();
		setStyleName(Reindeer.TABSHEET_SMALL);
		
		if(dataType.contentEquals("Expression File")) {

			maSet 							= 	(DSMicroarraySet) dataSet;
			UDataTab dataOp					= 	new UDataTab(maSet, action);
			UHeatMap heatMap				=	new UHeatMap(maSet);
			dataTable 						= 	new Table();

			
			dataOp.setCaption(DATA_OPERATIONS);
			addTab(dataOp); 
			
			dataTable.setSizeFull();
			dataTable.setImmediate(true);
			dataTable.setCaption(MICROARRAY_TABLE_CAPTION);
			dataTable.setContainerDataSource(tabularView(maSet));
			dataTable.setColumnWidth(MARKER_HEADER, 150);
			
			addTab(dataTable);
			
			heatMap.setCaption(HEAT_MAP);
			heatMap.setStyleName(Reindeer.LAYOUT_WHITE);
			
			addTab(heatMap);

		} else if(dataType.equalsIgnoreCase("CNKB")) {

			@SuppressWarnings("unchecked")
			Vector<CellularNetWorkElementInformation> hits 	=	(Vector<CellularNetWorkElementInformation>) dataSet;
			UCNKBTab cnkbTab 								= 	new UCNKBTab(hits);

			addTab(cnkbTab, "CNKB Results", null);		

			/* Preparing data for cytoscape */
			ArrayList<String> nodes = new ArrayList<String>();
			ArrayList<String> edges = new ArrayList<String>();

			for(CellularNetWorkElementInformation cellular: hits) {

				try {

					InteractionDetail[] interactions = cellular.getInteractionDetails();
					for(InteractionDetail interaction: interactions) {

						String edge = interaction.getdSGeneMarker1() 
								+ ","
								+ interaction.getdSGeneMarker2();

						String node1 = 	interaction.getdSGeneMarker1()
								+ ","
								+ interaction.getdSGeneName1();

						String node2 =	interaction.getdSGeneMarker2()
								+ ","
								+ interaction.getdSGeneName2();

						if(edges.isEmpty()) {

							edges.add(edge);

						}else if(!edges.contains(edge)) {

							edges.add(edge);
						}

						if(nodes.isEmpty()) {

							nodes.add(node1);
							nodes.add(node2);

						} else { 

							if(!nodes.contains(node1)) {

								nodes.add(node1);

							}

							if(!nodes.contains(node2)) {

								nodes.add(node2);

							}
						}

					}

				}catch (Exception e) {

					//TODO: Handle Null pointer exception
				}
			}	


			Cytoscape cy = new Cytoscape();

			cy.setImmediate(true);
			cy.setSizeFull();
			cy.setCaption("Cytoscape");

			String[] nodeArray = new String[nodes.size()];
			String[] edgeArray = new String[edges.size()];

			nodeArray = nodes.toArray(nodeArray);
			edgeArray = edges.toArray(edgeArray);

			cy.setNodes(nodeArray);
			cy.setEdges(edgeArray);

			addTab(cy);

		} else if(dataType.equalsIgnoreCase("Hierarchical Clustering")){

			CSHierClusterDataSet results 	=  	(CSHierClusterDataSet) dataSet;
			UClustergramTab dendrogramTab 	= 	new UClustergramTab(results);

			addTab(dendrogramTab, "Dendrogram", null);		

		} else if(dataType.equalsIgnoreCase("ARACne")) {

			AdjacencyMatrixDataSet adjMatrix = (AdjacencyMatrixDataSet) dataSet; 

			/* Preparing data for cytoscape */
			ArrayList<String> nodes = new ArrayList<String>();
			ArrayList<String> edges = new ArrayList<String>();

			for(int i=0; i<adjMatrix.getMatrix().getEdges().size(); i++) {

				String edge 	= 	adjMatrix.getMatrix().getEdges().get(i).node1.marker.getLabel() 
						+ "," 
						+ adjMatrix.getMatrix().getEdges().get(i).node2.marker.getLabel(); 


				String node1 	= adjMatrix.getMatrix().getEdges().get(i).node1.marker.getLabel() 
						+ "," 
						+ adjMatrix.getMatrix().getEdges().get(i).node1.marker.getGeneName(); 

				String node2 	= adjMatrix.getMatrix().getEdges().get(i).node2.marker.getLabel()
						+ ","
						+ adjMatrix.getMatrix().getEdges().get(i).node2.marker.getGeneName(); 


				if(edges.isEmpty()) {

					edges.add(edge);

				}else if(!edges.contains(edge)) {

					edges.add(edge);
				}

				if(nodes.isEmpty()) {

					nodes.add(node1);
					nodes.add(node2);

				} else { 

					if(!nodes.contains(node1)) {

						nodes.add(node1);

					}

					if(!nodes.contains(node2)) {

						nodes.add(node2);

					}
				}

			}

			Cytoscape cy = new Cytoscape();
			cy.setCaption("Cytoscape");
			cy.setImmediate(true);
			cy.setSizeFull();

			String[] nodeArray = new String[nodes.size()];
			String[] edgeArray = new String[edges.size()];

			nodeArray = nodes.toArray(nodeArray);
			edgeArray = edges.toArray(edgeArray);

			cy.setNodes(nodeArray);
			cy.setEdges(edgeArray);

			addTab(cy);

		}
	}

	public static IndexedContainer tabularView(DSMicroarraySet maSet) {

		String[] colHeaders 			= 	new String[(maSet.size())+1];
		IndexedContainer dataIn 		= 	new IndexedContainer();

		for(int j=0; j<maSet.getMarkers().size();j++) {

			Item item 					= 	dataIn.addItem(j);

			for(int k=0;k<=maSet.size();k++) {

				if(k == 0) {

					colHeaders[k] 		= 	MARKER_HEADER;
					dataIn.addContainerProperty(colHeaders[k], String.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getMarkers().get(j));

				} else {

					colHeaders[k] 		= 	maSet.get(k-1).toString();
					dataIn.addContainerProperty(colHeaders[k], Float.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getValue(j, k-1));

				}
			}
		}

		return dataIn;

	}
	
	public static void resetTableContainer(IndexedContainer data) {
		
		dataTable.removeAllItems();
		dataTable.setContainerDataSource(data);
		dataTable.setColumnWidth(MARKER_HEADER, 150);
		
	}

	public static void resetOriginalView(DSMicroarraySet dSet) {
		
		dataTable.removeAllItems();
		dataTable.setContainerDataSource(tabularView(dSet));
		dataTable.setColumnWidth(MARKER_HEADER, 150);
		
	}
}
