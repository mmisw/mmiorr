package org.mmisw.ontmd.gwt.client.portal;

import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

/**
 * ontology table.
 * 
 * @author Carlos Rueda
 */
public class OntologyTable extends FlexTable {

	private List<OntologyInfo> ontologyInfos;
	
	
	OntologyTable(final Map<String, String> params, List<OntologyInfo> ontologyInfos) {
		super();
		FlexTable flexPanel = this;
		
		this.ontologyInfos = ontologyInfos;
		
//		flexPanel.setBorderWidth(1);
		flexPanel.setWidth("850");
		flexPanel.setStylePrimaryName("OntologyTable");
		
		int row = 0;
		
		flexPanel.getRowFormatter().setStylePrimaryName(row, "OntologyTable-header");
		
//		flexPanel.getRowFormatter().addStyleName(row, "-header");
		
		// general information 
//		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
		flexPanel.setWidget(row, 0, new HTML("URI"));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		flexPanel.setWidget(row, 1, new HTML("Name"));
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		

		for ( OntologyInfo oi : this.ontologyInfos ) {
			flexPanel.getRowFormatter().setStylePrimaryName(row, "OntologyTable-row");
			
			String uri = oi.getUri();
			String link = uri;
			HTML html = new HTML("<a target=\"_blank\" href=\"" +link+ "\">" +uri+ "</a>");
			
			flexPanel.setWidget(row, 0, html);
			flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			flexPanel.setWidget(row, 1, new HTML("<i>" +oi.getDisplayLabel()+ "</i>"));
			flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;
		}
	    
	}
	

}
