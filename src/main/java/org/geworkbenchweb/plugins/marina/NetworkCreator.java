package org.geworkbenchweb.plugins.marina;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.NetworkEdges;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class NetworkCreator {
	private MarinaUI ui;
	private Long dataSetId = null;
	public static final String SIF_FORMART = "sif format";
	public static final String ADJ_FORMART = "adj format";
	public static final String GENE_NAME = "gene name";
	public static final String ENTREZ_ID = "entrez id";
	public static final String PROBESET_ID = "probeset id";
	
	public NetworkCreator(MarinaUI ui){
		this.ui = ui;
		this.dataSetId = ui.dataSetId;
	}
	
	private static AdjacencyMatrix.Node token2node(String token,
			final String selectedRepresentedBy, final boolean isRestrict,
			final Map<String, String> map) {
		boolean found = false;
		if (selectedRepresentedBy.equals(PROBESET_ID)
				|| selectedRepresentedBy.equals(GENE_NAME)
				|| selectedRepresentedBy.equals(ENTREZ_ID)) {
			if(map.keySet().contains(token)) found = true;
		}

		AdjacencyMatrix.Node node = null;

		if (!found && isRestrict) {
			// we don't have this gene in our MicroarraySet
			// we skip it
			return null;
		} else if (!found && !isRestrict) {
			if (selectedRepresentedBy.equals(GENE_NAME))
				node = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, token);
			else
				node = new AdjacencyMatrix.Node(NodeType.STRING, token);
		} else {
			if (selectedRepresentedBy.equals(PROBESET_ID))
				node = new AdjacencyMatrix.Node(NodeType.PROBESET_ID, token);
			else {
				String markerName = map.get(token);
				// TODO the earlier code intends to handle multiple gene names for a given token
				node = new AdjacencyMatrix.Node(NodeType.PROBESET_ID, markerName);
			}
		}
		return node;
	}

	/* Maps geneSymbol/entezID to marker in original dataset if available */
	private Map<String, String> getAnnotationMap(String type) {
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, dataset.getDataId());
		String[] markerLabels = microarray.getMarkerLabels();

		Map<String, String> geneToMarker = new HashMap<String, String>();
		if(!type.equals(GENE_NAME) && !type.equals(ENTREZ_ID)) {
			for(String marker : markerLabels)
				geneToMarker.put(marker, null);
			return geneToMarker;
		}
		
		Set<String> datasetMarkers = new HashSet<String>(Arrays.asList(markerLabels));

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade().find(
				"SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameter);
		if (dataSetAnnotation != null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Annotation annotation = FacadeFactory.getFacade().find(Annotation.class, annotationId);

			for (AnnotationEntry entry : annotation.getAnnotationEntries()) {
				String marker = entry.getProbeSetId();
				if(!datasetMarkers.contains(marker)) continue;
				
				String gene = null;
				if(type.equals(GENE_NAME))      gene = entry.getGeneSymbol();
				else if(type.equals(ENTREZ_ID)) gene = entry.getEntrezId();

				if(!geneToMarker.containsKey(gene))
					geneToMarker.put(gene, marker);
			}
		}
		return geneToMarker;
	}

	public AdjacencyMatrix parseAdjacencyMatrix(String network,
			Map<String, String> interactionTypeSifMap, String format,
			String selectedRepresentedBy, boolean isRestrict)
			throws InputFileFormatException {

		AdjacencyMatrix matrix = new AdjacencyMatrix(ui.bean.getNetwork(), interactionTypeSifMap);

		Map<String, String> map = getAnnotationMap(selectedRepresentedBy);

		String[] lines = network.split("\n");
		if (lines == null || lines.length == 0) {
			throw new InputFileFormatException("empty network");
		}
		
		try {

			for (String line : lines) {
				// skip comments
				if (line.trim().equals("") || line.startsWith(">")
						|| line.startsWith("-"))
					continue;

				StringTokenizer tr = new StringTokenizer(line, "\t");

				AdjacencyMatrix.Node node = token2node(tr.nextToken(),
						selectedRepresentedBy, isRestrict, map);
				if (node == null)
					continue; // skip it when we don't have it
			 
				String interactionType = null;
				if (format.equals(SIF_FORMART) && tr.hasMoreTokens())
					interactionType = tr.nextToken().toLowerCase();			 
				while (tr.hasMoreTokens()) {

					String strGeneId2 = tr.nextToken();
					AdjacencyMatrix.Node node2 = token2node(strGeneId2,
							selectedRepresentedBy, isRestrict, map);
					if (node2 == null)
						continue; // skip it when we don't have it

					float mi = 0.8f;
					if (format.equals(ADJ_FORMART)) {
						if (!tr.hasMoreTokens())
							throw new InputFileFormatException(
									"invalid format around " + strGeneId2);
						mi = Float.parseFloat(tr.nextToken());
					}
				 
					matrix.add(node, node2, mi, interactionType);
				} // end of the token loop for one line			 
			} // end of reading while loop
		} catch (NumberFormatException ex) {
			throw new InputFileFormatException(ex.getMessage());
		} catch (Exception e) {
			throw new InputFileFormatException(e.getMessage());
		}

		return matrix;
	}
	
	public String getNetworkFromAdjMatrix(AdjacencyMatrix matrix){
		
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, id);
		List<String> markerLabels = Arrays.asList( microarray.getMarkerLabels() );
		int arrayNumber = microarray.getArrayNumber();
		float[][] rows = microarray.getExpressionValues();

		if (matrix==null) return null;
		Map<String, String> map = null;
		if(matrix.getNodeNumber()>0){
			NodeType nodeType = matrix.getNodes().get(0).type;
			if(nodeType != NodeType.PROBESET_ID){
				String type = nodeType == NodeType.GENE_SYMBOL ? GENE_NAME : ENTREZ_ID;
				map = getAnnotationMap(type);
			}
		}

		boolean goodNetwork = false;
		ui.allpos = true;
		
		StringBuilder builder = new StringBuilder();
		for (AdjacencyMatrix.Node node1 : matrix.getNodes()) {
			String marker1 = getMarkerInNode(node1, map);
			int marker1Index = markerLabels.indexOf(marker1);
			if (marker1 != null && marker1Index > -1) {
				double[] v1 = new double[arrayNumber];
				double[] v2 = new double[arrayNumber];
				float[] value1 = rows[marker1Index];
				for (int i = 0; i < arrayNumber; i++) {
					v1[i] = value1[i];
				}

				for (AdjacencyMatrix.Edge edge : matrix.getEdges(node1)) {
					String marker2 = getMarkerInNode(edge.node2, map);
					int marker2Index = markerLabels.indexOf(marker2);
					if (marker2 != null && marker2Index > -1) {
						double rho = 1, pvalue = 0;
						float[] value2 = rows[marker2Index];
						for (int i = 0; i < arrayNumber; i++) {
							v2[i] = value2[i];
						}
						if (v1 != null && v1.length > 0 && v2 != null
								&& v2.length > 0) {
							double[][] arrayData = new double[][] { v1, v2 };
							RealMatrix rm = new SpearmansCorrelation()
									.computeCorrelationMatrix(transpose(arrayData));
							if (rm.getColumnDimension() > 1)
								rho = rm.getEntry(0, 1);
							if (ui.allpos && rho < 0)
								ui.allpos = false;
							try {
								pvalue = new PearsonsCorrelation(rm, v1.length)
										.getCorrelationPValues().getEntry(0, 1);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						builder.append(marker1 + "\t");
						builder.append(marker2 + "\t" + edge.info.value + "\t" // Mutual information
								+ rho + "\t" // Spearman's correlation = 1
								+ pvalue + "\n"); // P-value for Spearman's correlation = 0
					}
				}
				if (!goodNetwork && builder.length() > 0)
					goodNetwork = true;
			}
		}
		if (!goodNetwork)
			return null;
		return builder.toString();
	}
	
	/* based on getNetworkFromAdjMatrix, which I doubt is completely correct */
	public String getNetworkString(Network network){
		
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, id);
		List<String> markerLabels = Arrays.asList( microarray.getMarkerLabels() );
		int arrayNumber = microarray.getArrayNumber();
		float[][] rows = microarray.getExpressionValues();

		boolean goodNetwork = false;
		ui.allpos = true;
		
		StringBuilder builder = new StringBuilder();
		String[] node1s = network.getNode1();
		NetworkEdges[] allEdges = network.getEdges();
		for (int index=0; index<node1s.length; index++) {
			String marker1 = node1s[index];
			NetworkEdges edges = allEdges[index];
			
			int marker1Index = markerLabels.indexOf(marker1);
			if (marker1 != null && marker1Index > -1) {
				double[] v1 = new double[arrayNumber];
				double[] v2 = new double[arrayNumber];
				float[] value1 = rows[marker1Index];
				for (int i = 0; i < arrayNumber; i++) {
					v1[i] = value1[i];
				}

				String[] node2s = edges.getNode2s();
				double[] weights = edges.getWeights();
				for (int index2=0; index2<node2s.length; index2++) {
					String marker2 = node2s[index2];
					int marker2Index = markerLabels.indexOf(marker2);
					if (marker2 != null && marker2Index > -1) {
						double rho = 1, pvalue = 0;
						float[] value2 = rows[marker2Index];
						for (int i = 0; i < arrayNumber; i++) {
							v2[i] = value2[i];
						}
						if (v1 != null && v1.length > 0 && v2 != null
								&& v2.length > 0) {
							double[][] arrayData = new double[][] { v1, v2 };
							RealMatrix rm = new SpearmansCorrelation()
									.computeCorrelationMatrix(transpose(arrayData));
							if (rm.getColumnDimension() > 1)
								rho = rm.getEntry(0, 1);
							if (ui.allpos && rho < 0)
								ui.allpos = false;
							try {
								pvalue = new PearsonsCorrelation(rm, v1.length)
										.getCorrelationPValues().getEntry(0, 1);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						builder.append(marker1 + "\t");
						builder.append(marker2 + "\t" + weights[index2] + "\t" // Mutual information
								+ rho + "\t" // Spearman's correlation = 1
								+ pvalue + "\n"); // P-value for Spearman's correlation = 0
					}
				}
				if (!goodNetwork && builder.length() > 0)
					goodNetwork = true;
			}
		}
		if (!goodNetwork)
			return null;
		return builder.toString();
	}

	private double[][] transpose(double[][] in){
		if (in==null || in.length==0 || in[0].length==0)
			return null;
		int row = in.length;
		int col = in[0].length;
		double[][] out = new double[col][row];
		for(int i=0; i<row; i++)
			for (int j=0; j<col; j++)
				out[j][i] = in[i][j];
		return out;
	}

	private String getMarkerInNode(AdjacencyMatrix.Node node, Map<String, String> map){
		if (node == null) return null;
		String nodeLabel = node.getStringId();
		if (map == null) return nodeLabel;
		if (!map.containsKey(nodeLabel)) return null;
		return map.get(nodeLabel);
	}
	
	public void printWarning() {
		if (ui.bean.getGseaTailNumber() == 2) {
			MessageBox mb = new MessageBox(
					ui.getWindow(),
					"Warning",
					MessageBox.Icon.WARN,
					"For 2-tailed GSEA with an adjacency matrix network, the expression node should be the complete dataset from "
							+ "which the network (adjacency matrix) was originally calculated.<p>If it is not, a 5-column-format "
							+ "network file (with regulon correlation values from the original dataset) should be loaded instead.",
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
		}
	}

}
