package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/** data set parser */
// class name DataSetParser has been used (not very properly)
public abstract class Loader {

	/**
	 * parse the file and store it (make it persistent)
	 * 
	 * @throws GeWorkbenchLoaderException
	 */
	// TODO return or indicate the type of data set thus created
	public abstract void load(File file) throws GeWorkbenchLoaderException;

	// this method was written like being shared mechanism (for now) to store
	// data
	// in fact, the behavior is overridden for expression file to implement
	// annotation.
	// TODO the return value is used only by expression file's annotation
	// implementation. this is an inconsistency in the design
	static Long storeData(DSDataSet<? extends DSBioObject> dataSet,
			String fileName) {
		// FIXME dependency on annotation should be re-designed.
		String annotationFileName = null;

		User user = SessionHandler.get();

		// TODO DataSet needs to be re-engineered to have some logic in it.
		DataSet dataset = new DataSet();

		dataset.setName(fileName);
		dataset.setType(dataSet.getClass().getName());
		dataset.setOwner(user.getId());
		dataset.setWorkspace(WorkspaceUtils.getActiveWorkSpace());
		FacadeFactory.getFacade().store(dataset);

		boolean success = UserDirUtils.saveDataSet(dataset.getId(), ObjectConversion.convertToByte(dataSet));
		if(!success) {
			System.out.println("something went wrong");
		}
		
		DataHistory dataHistory = new DataHistory();
		dataHistory.setParent(dataset.getId());
		StringBuilder data = new StringBuilder();
		data.append("Data File Name : " + dataSet.getLabel() + "\n");
		// TODO special things for CSMicroarraySet should be part of the overall design
		// not an aftermath fix
		if (dataSet.getClass()==CSMicroarraySet.class) {
			// (1)
			data.append("Annotation File - " + annotationFileName + "\n");
			data.append("Gene Ontology File - \n");
			
			// (2)
			ExperimentInfo experimentInfo = new ExperimentInfo();
			experimentInfo.setParent(dataset.getId());
			StringBuilder info = new StringBuilder();
			info.append("Number of phenotypes in the data set - "
					+ ((DSMicroarraySet) dataSet).size() + "\n");
			info.append("Number of markers in the data set - "
					+ ((DSMicroarraySet) dataSet).getMarkers().size() + "\n");
			experimentInfo.setInfo(ObjectConversion.convertToByte(info
					.toString()));
			FacadeFactory.getFacade().store(experimentInfo);
		}
		dataHistory.setData(ObjectConversion.convertToByte(data.toString()));
		FacadeFactory.getFacade().store(dataHistory);

		NodeAddEvent resultEvent = new NodeAddEvent(dataset);
		GeworkbenchRoot.getBlackboard().fire(resultEvent);

		return dataset.getId();
	}
}
