package org.geworkbenchweb.analysis.hierarchicalclustering;

import java.awt.Color;
import java.lang.reflect.Array;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbenchweb.visualizations.Dendrogram;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class DendrogramTab extends VerticalLayout{

	
	 /**
     * The underlying micorarray set used in the hierarchical clustering
     * analysis.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = null;

    /**
     * The current marker cluster being rendered in the marker Dendrogram
     */
    private MarkerHierCluster currentMarkerCluster = null;

    /**
     * The leaf marker clusters in <code>currentMarkerCluster</code>.
     */
    private Cluster[] leafMarkers = null;

    /**
     * The current array cluster being rendered in the marker Dendrogram
     */
    private MicroarrayHierCluster currentArrayCluster = null;

    /**
     * The leaf microarrays clusters in <code>currentArrayCluster</code>.
     */
    private Cluster[] leafArrays = null;

    private double intensity = 1.0;
    
    /**
     * The String is passed to the client side
     * Marker String
     */
    private static String markerString = null; 
    
    /**
     * The string is passed to the client side
     * Array String
     */
    private static String arrayString = null; 
    
    User user = SessionHandler.get();

    
    
    private transient Object lock = new Object();
	
	@SuppressWarnings({ "unchecked", "unused" })
	public DendrogramTab(CSHierClusterDataSet dataSet) {
		
		setSizeFull();
        microarraySet = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) dataSet.getDataSetView();
        
        currentMarkerCluster = (MarkerHierCluster)dataSet.getCluster(0);
    	currentArrayCluster = (MicroarrayHierCluster)dataSet.getCluster(1);
    	
    	
    	if (currentMarkerCluster != null) {
            java.util.List<Cluster> leaves = currentMarkerCluster.getLeafChildren();
            leafMarkers = (Cluster[]) Array.newInstance(Cluster.class, leaves.size());
            leaves.toArray(leafMarkers);
        }
    	
    	 if (currentArrayCluster != null) {
             java.util.List<Cluster> leaves = currentArrayCluster.getLeafChildren();
             leafArrays = (Cluster[]) Array.newInstance(Cluster.class, leaves.size());
             leaves.toArray(leafArrays);
         }
    	 
        int geneNo = 0;

		if (currentMarkerCluster == null) {
			
			geneNo = microarraySet.markers().size();
			
		
		} else {
		
			geneNo = leafMarkers.length;
		
		}

		int chipNo = 0;

		if (currentArrayCluster == null) {
			
			chipNo = microarraySet.items().size();
			
			
		} else {
			
			chipNo = leafArrays.length;
		
		}
		
		String[] markerNames 	= 	new String[geneNo];
		String[] colors 		= 	new String[chipNo*geneNo];
		int k = 0;
		
		for (int i = 0; i < geneNo; i++) {
			DSGeneMarker stats = null;

			if (leafMarkers != null) {
				stats = ((MarkerHierCluster) leafMarkers[i]).getMarkerInfo();
			} else {
				stats = microarraySet.markers().get(i);
			}

			markerNames[i] = stats.getLabel();
			for (int j = 0; j < chipNo; j++) {
				
				DSMicroarray mArray = null;

				if (leafArrays != null) {
					mArray = ((MicroarrayHierCluster) leafArrays[j])
							.getMicroarray();
				} else {
					mArray = microarraySet.get(j);
				}
				
				DSMarkerValue marker = mArray.getMarkerValue(stats);
				
				Color color = getMarkerValueColor(marker, stats, (float) intensity);
				String rgb = Integer.toHexString(color.getRGB());
				rgb = rgb.substring(2, rgb.length());
				colors[k] = rgb;
				k++;
			}
		}
		
		int size = dataSet.getNumberOfClusters();
		// size should always be 2
		ClusterNode clusterNode = null; // TODO try one cluster first
		for(int index=0; index<size; index++) {
			HierCluster cluster = dataSet.getCluster(index);
			if(cluster==null) {
				
			} else {
				clusterNode = convert(cluster);
			}
		}
		Dendrogram dendrogram = new Dendrogram();
		dendrogram.setHeight(((geneNo*5) + 400) + "px");
        dendrogram.setWidth(((chipNo*20) + 600) + "px");
		dendrogram.setColors(colors);
		dendrogram.setArrayNumber(chipNo);
		dendrogram.setMarkerNumber(geneNo);
		dendrogram.setMarkerLabels(markerNames);

		if(markerString != null) {
			dendrogram.setMarkerCluster(markerString);
		}
		addComponent(dendrogram);
	}

	private static ClusterNode convert(Cluster hierCluster) {
		if(hierCluster==null) return null;

		markerString = markerString + "(";
		if(! (hierCluster instanceof MarkerHierCluster) ){
			// TODO
			return new ClusterNode("not implemented for array cluster yet");
		}
		ClusterNode cluster = null;
		if(hierCluster.isLeaf()) {
			markerString = markerString + ")";
		} else {	
			Cluster[] child = hierCluster.getChildrenNodes();
			ClusterNode c1 = convert(child[0]);
			ClusterNode c2 = convert(child[1]);
			cluster = new ClusterNode(c1, c2);
			markerString = markerString + ")";
		}
		return cluster;
	}
	
	public Color getMarkerValueColor(DSMarkerValue mv, DSGeneMarker mInfo, float intensity) {

		//      intensity *= 2;
		intensity = 2 / intensity; 
		double value = mv.getValue();
		if (lock == null)
			lock = new Object();
		synchronized (lock) {

			org.geworkbench.bison.util.Range range = ((DSRangeMarker) mInfo).getRange();
			double mean = range.norm.getMean(); //(range.max + range.min) / 2.0;
			double foldChange = (value - mean) / (range.norm.getSigma() + 0.00001); //Math.log(change) / Math.log(2.0);
			if (foldChange < -intensity) {
				foldChange = -intensity;
			}
			if (foldChange > intensity) {
				foldChange = intensity;
			}

			double colVal = foldChange / intensity;
			if (foldChange > 0) {
				return new Color(1.0F, (float) (1 - colVal), (float) (1 - colVal));
			} else {
				return new Color((float) (1 + colVal), (float) (1 + colVal), 1.0F);
			}
		}

	}
	
	
	@SuppressWarnings("deprecation")
	public Object toObject(byte[] bytes){ 
		
		Object object = null; 
		
		try{ 
			
			object = new java.io.ObjectInputStream(new 
					java.io.ByteArrayInputStream(bytes)).readObject(); 
		
		}catch(java.io.IOException ioe){ 
			
			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					ioe.getMessage()); 
		
		}catch(java.lang.ClassNotFoundException cnfe){ 
			
			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					cnfe.getMessage()); 
		
		} 
		
		return object; 
	
	}
	
	

    
	
}
