package org.mmisw.orrportal.gwt.client.portal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.*;
import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.EntityInfo;
import org.mmisw.orrclient.gwt.client.rpc.OntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.OtherOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.PropValue;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.table.IRow;
import org.mmisw.orrportal.gwt.client.util.table.IUtilTable;
import org.mmisw.orrportal.gwt.client.util.table.RowAdapter;
import org.mmisw.orrportal.gwt.client.util.table.UtilTableCreator;


/**
 * Panel for showing/capturing the contents of an "other" ontology, meaning
 * one not handled with voc2rdf or Vine.
 */
public class OtherOntologyContentsPanel extends BaseOntologyContentsPanel {

	private UploadLocalOntologyPanel uploadLocalOntologyPanel;
	private Widget contents;
	private final VerticalPanel widget = new VerticalPanel();

	private TempOntologyInfo tempOntologyInfo;


	private TempOntologyInfoListener myTempOntologyInfoListener;


	/**
	 * @param tempOntologyInfo If non-null, info for the new ontology is taken from here.
	 *
	 * TODO NOTE: This is a new parameter in this method while I complete the new "registration of
	 * external" ontology functionality.
	 */
	public OtherOntologyContentsPanel(
			TempOntologyInfo tempOntologyInfo,
			OtherOntologyData ontologyData,
			boolean readOnly
	) {
		super(readOnly);

		if ( tempOntologyInfo == null ) {
			this.myTempOntologyInfoListener = new TempOntologyInfoListener() {
				public void tempOntologyInfoObtained(TempOntologyInfo tempOntologyInfo) {
					// update my own data and then notify any interested "external" listener:
					_tempOntologyInfoObtained(tempOntologyInfo);
				}
			};
		}
		else {
			_tempOntologyInfoObtained(tempOntologyInfo);
		}

		BaseOntologyData baseData = ontologyData.getBaseOntologyData();
		if ( baseData != null ) {
			contents = _prepareOtherWidgetForExistingBaseData(ontologyData);
		}

		_updateInterface();
	}

	private void _tempOntologyInfoObtained(TempOntologyInfo tempOntologyInfo) {

		Orr.log("OtherOntologyContentsPanel: _tempOntologyInfoObtained: " +tempOntologyInfo);

		this.tempOntologyInfo = tempOntologyInfo;

		OntologyData ontologyData = tempOntologyInfo.getOntologyData();
		BaseOntologyData baseData = ontologyData.getBaseOntologyData();
		if ( baseData != null ) {
			contents = _prepareOtherWidgetForExistingBaseData(ontologyData);
		}

		_updateInterface();


		// notify any interested "external" listener:
		TempOntologyInfoListener tempOntologyInfoListener = PortalControl.getInstance().getTempOntologyInfoListener();
		if ( tempOntologyInfoListener != null ) {
			tempOntologyInfoListener.tempOntologyInfoObtained(tempOntologyInfo);
		}
	}

	private void _updateInterface() {
		widget.clear();

		if ( !isReadOnly() ) {
			if ( tempOntologyInfo == null ) {
				if ( uploadLocalOntologyPanel == null ) {
					uploadLocalOntologyPanel = new UploadLocalOntologyPanel(myTempOntologyInfoListener, true);
				}
				DecoratorPanel dec = new DecoratorPanel();
				dec.setWidget(uploadLocalOntologyPanel);
				//			widget.add(uploadLocalOntologyPanel);
				widget.add(dec);
			}
			//Else: do not add the upload panel--we already have tempOntologyInfo
		}

		if ( contents != null ) {
			widget.add(contents);
		}
	}


	public void setReadOnly(boolean readOnly) {
		Orr.log("OtherOntologyContentsPanel setReadOnly");
		super.setReadOnly(readOnly);

		_updateInterface();
	}




	@Override
	public OtherDataCreationInfo getCreateOntologyInfo() {
		OtherDataCreationInfo odci = new OtherDataCreationInfo();

		// TODO
		Orr.log("TODO OtherOntologyContentsPanel create OtherDataCreationInfo");
		odci.setTempOntologyInfo(tempOntologyInfo);

		return odci;
	}

	@Override
	public String checkData(boolean isNewVersion) {
		if ( tempOntologyInfo == null ) {
			if ( isNewVersion ) {
				// Ok. contents of the ontology are already known.
			}
			else {
				// this is a new ontology -- we need contents:
				return "No ontology has been uploaded into working space yet";
			}
		}
		else if ( tempOntologyInfo.getError() != null ) {
			return "There was a previous error with the uploading of the file (" +tempOntologyInfo.getError()+ ")";
		}
		return null;
	}

	@Override
	public Widget getWidget() {
		return widget;
	}

	//
	// TODO use IncrementalCommand's
	//
	@SuppressWarnings("unchecked")
	private Widget _prepareOtherWidgetForExistingBaseData(OntologyData ontologyData) {
		BaseOntologyData baseData = ontologyData.getBaseOntologyData();

		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);

		Object[] entityGroups = {
				"Subjects", baseData.getSubjects(),
				"Classes", baseData.getClasses(),
				"Properties", baseData.getProperties(),
				"Individuals", baseData.getIndividuals(),
		};

		for (int i = 0; i < entityGroups.length; i += 2) {
			String title = entityGroups[i].toString();
			List<?extends EntityInfo> entities = (List<?extends EntityInfo>) entityGroups[i + 1];
			title += " (" +entities.size()+ ")";
			DisclosurePanel disclosure = new DisclosurePanel(title);
			disclosure.setAnimationEnabled(true);
			Widget entsWidget = _createOtherWidgetForEntities(ontologyData, entities);
			disclosure.setContent(entsWidget);
			vp.add(disclosure);
		}

		return vp;
	}


	private Widget _createOtherWidgetForEntities(OntologyData ontologyData,
			List<? extends EntityInfo> entities) {


		if ( entities.size() == 0 ) {
			return new HTML();
		}

		Set<String> header = new HashSet<String>();

		for ( EntityInfo entity : entities ) {
			List<PropValue> props = entity.getProps();
			for ( PropValue pv : props ) {
				header.add(pv.getPropName());
			}
		}

		List<String> colNames = new ArrayList<String>();
		colNames.addAll(header);
		colNames.add(0, "Name");

		IUtilTable utilTable = UtilTableCreator.create(colNames);
		List<IRow> rows = new ArrayList<IRow>();

		for ( EntityInfo entity : entities ) {
			Orr.log("_createOtherWidgetForEntities: entity=" + entity.getUri());

			// vals: propName -> all corresp. values
			final Map<String, String> vals = new HashMap<String, String>();

			List<PropValue> props = entity.getProps();

			for ( PropValue pv : props ) {
				// Issue 310: multiple property values not shown
				// Simply put as value the concatenation of all the values for the same property:

				final String n = pv.getPropName();
				final String v = pv.getValueName();

				String rv = vals.get(n);
				//Orr.log("_createOtherWidgetForEntities: n=" + n + " v=" +v+ " rv=" +rv);
				if (rv == null) {
					rv = v;
				}
				else {
					rv += ", " + v;
				}
				vals.put(n, rv);
			}

			vals.put("Name", entity.getLocalName());

			rows.add(new RowAdapter() {
				public String getColValue(String sortColumn) {
					return vals.get(sortColumn);
				}
			});
		}
		utilTable.setRows(rows);

		return utilTable.getWidget();
	}


}
