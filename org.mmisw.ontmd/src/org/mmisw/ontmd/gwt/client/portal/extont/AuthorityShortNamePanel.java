package org.mmisw.ontmd.gwt.client.portal.extont;

import java.util.Map;

import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.Portal;
import org.mmisw.ontmd.gwt.client.util.FieldWithChoose;
import org.mmisw.ontmd.gwt.client.util.TLabel;
import org.mmisw.ontmd.gwt.client.util.Util;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The panel for the autority abbreviatio and shortName to compose the URI of a
 * fully hosted ontology.
 * 
 * @author Carlos Rueda
 */
public class AuthorityShortNamePanel extends VerticalPanel {

	private RegisterExternalOntologyPageFullyHosted page;
	private AttrDef authorityAttrDef;
	
	private FieldWithChoose authorityWidget;
	private TextBoxBase shortNameTextBox;
	
	private HTML resultingUri = new HTML();
	
	
	private PushButton checkButton = new PushButton("Check", new ClickListener() {
		public void onClick(Widget sender) {
			updateResultingUri();
			_check(true);
		}
	});

	
	AuthorityShortNamePanel(RegisterExternalOntologyPageFullyHosted page) {
		this.page = page;
		authorityAttrDef = Main.getMetadataBaseInfo().getAuthorityAttrDef();
		assert authorityAttrDef != null;
		
		add(createForm());
		
		updateResultingUri();
	}

	private void _check(boolean fullCheck) {
		page.checkAuthorityShortName(
				fullCheck,
				authorityWidget.getValue().trim(),
				shortNameTextBox.getText().trim()
		);
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		
		int row = 0;

		ChangeListener cl = null;
		
		cl = new ChangeListener () {
			public void onChange(Widget sender) {
				formChanged();
			}
		};

		authorityWidget = new FieldWithChoose(authorityAttrDef, cl, "130px");
		shortNameTextBox = Util.createTextBoxBase(1, "200px", cl);
			
			
		String label = authorityAttrDef.getLabel();
		String tooltip = "<b>" +label+ "</b>:<br/>" + 
				authorityAttrDef.getTooltip() +
				"<br/><br/><div align=\"right\">(" +authorityAttrDef.getUri()+ ")</div>";
		panel.setWidget(row, 0, new TLabel(label, true, tooltip ));

		panel.setWidget(row, 1, authorityWidget);
		panel.getFlexCellFormatter().setWidth(row, 0, "250px");
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

	
		String label2 = "Ontology short name";
		String tooltip2 = "<b>" +label2+ "</b>:<br/>" + 
				"The short name of the ontology.";
		panel.setWidget(row, 0, new TLabel(label2, true, tooltip2 ));

		panel.setWidget(row, 1, shortNameTextBox);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
			

		String label3 = "URI";
		String tooltip3 = "<b>" +label3+ "</b>:<br/>" + 
				"The resulting URI for your ontology";
		panel.setWidget(row, 0, new TLabel(label3, false, tooltip3 ));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		panel.setWidget(row, 1, resultingUri);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
			
		panel.setWidget(row, 0, new HTML("Click to verify availability: "));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		CellPanel buttons = createButtons();
		panel.setWidget(row, 1, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		return panel;
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		checkButton.setTitle("Checks that the given fields can be used to create a new URI in the repository");
		panel.add(checkButton);
		
		return panel;
	}
	
	private void formChanged() {
		page.formChanged();
		_check(false);
		updateResultingUri();
	}
	
	private void updateResultingUri() {
		String ontServiceUrl = Portal.portalBaseInfo.getOntServiceUrl();
		
		String authority = "<font color=\"gray\">authority</font>";
		if ( authorityWidget.getValue().trim().length() > 0 ) {
			authority = authorityWidget.getValue().trim();
		}
		
		String shortName = "<font color=\"gray\">shortName</font>";
		if ( shortNameTextBox.getText().trim().length() > 0 ) {
			shortName = shortNameTextBox.getText().trim();
		}

		String uri = ontServiceUrl + "/" + authority + "/" + shortName;
		
		resultingUri.setHTML("<b>" +uri+ "</b>");
	}

	void initFields() {
		if ( shortNameTextBox.getText().trim().length() == 0
		&&   page.getWizard().tempOntologyInfo != null
		&&   page.getWizard().tempOntologyInfo.getShortName() != null
		) {
			shortNameTextBox.setText(page.getWizard().tempOntologyInfo.getShortName());
			formChanged();
		}
	}

	String getAuthority() {
		return authorityWidget.getValue().trim();
	}

	String getShortName() {
		return shortNameTextBox.getText().trim();
	}

	String getOntologyUri() {
		if ( authorityWidget.getValue().trim().length() == 0
		||   shortNameTextBox.getText().trim().length() == 0 ) {
			return null;
		}
		
		String ontServiceUrl = Portal.portalBaseInfo.getOntServiceUrl();
		String authority = authorityWidget.getValue().trim();
		String shortName = shortNameTextBox.getText().trim();
		
		String uri = ontServiceUrl + "/" + authority + "/" + shortName;
		return uri;
	}
	
	String putValues(Map<String, String> values, boolean checkMissing) {
		
		String authority = getAuthority();
		if ( checkMissing && authority.length() == 0 ) {
			return "Missing authority abbreviation";
		}
		String shortName = getShortName();
		if ( checkMissing && shortName.length() == 0 ) {
			return "Missing short name";
		}
		
		if ( values != null ) {
			if ( authority.length() > 0 ) {
				values.put(authorityAttrDef.getUri(), authority);
			}
			if ( shortName.length() > 0 ) {
				values.put("shortName", shortName);
			}
		}
		return null;
	}
}
