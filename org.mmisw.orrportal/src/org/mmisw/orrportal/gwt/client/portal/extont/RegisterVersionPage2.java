package org.mmisw.orrportal.gwt.client.portal.extont;

import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.portal.PortalConsts;
import org.mmisw.orrportal.gwt.client.portal.extont.RegisterVersionWizard.Detail;
import org.mmisw.orrportal.gwt.client.portal.extont.RegisterVersionWizard.MdDetails;
import org.mmisw.orrportal.gwt.client.util.MyDialog;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This page is for determining how to initialize the metadata pages when there
 * are different values for the same metadata properties between the
 * loaded file and the registered ontology.
 * 
 * @author Carlos Rueda
 */
class RegisterVersionPage2 extends BasePage {
	
	enum MdInitMode {
		
		MERGE_PREFER_LOADED     ("Use values from both sources with new values overwriting registered values, if any"), 
		MERGE_PREFER_REGISTERED ("Use values from both sources with registered values overwriting new values, if any"),
		ONLY_FROM_LOADED        ("Use only values from the loaded ontology"),
		ONLY_FROM_REGISTERED    ("Use only values from the registered ontology"),
		;
		
		private String label;
		private MdInitMode(String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
	}

	private static final String DIFF_MARK = "<font color=\"red\"><b>!</b>&nbsp;</font>";
	
	private VerticalPanel contents = new VerticalPanel();
	private HTML infoHtml = new HTML();
	
	private final HorizontalPanel detailsPanel = new HorizontalPanel();
	private PushButton detailsButton;
	
	
	private class RButton extends RadioButton {
		RButton(final MdInitMode initMdMode) {
			super("ht", initMdMode.getLabel());
			addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					getWizard().InitMdModeSelected(initMdMode);
					nextButton.setEnabled(true);
					statusHtml.setText("Click Next to edit metadata for the new version");
				}
			});
		}
	}
	private RadioButton[] rbs;

	/**
	 * Creates the ontology panel where the initial ontology can be loaded
	 * and its original contents displayed.
	 * 
	 * @param tempOntologyInfoListener
	 * @param allowLoadOptions
	 */
	RegisterVersionPage2(RegisterVersionWizard wizard) {
		super(wizard, true, true);
		addContents(contents);
		
		createDetailsPanel();
		
		rbs = new RadioButton[MdInitMode.values().length];
		int i = 0;
		for ( MdInitMode hostingType: MdInitMode.values() ) {
			rbs[i++] = new RButton(hostingType);
		}
		nextButton.setEnabled(false);
		updateInfoHtml(null);
		recreate();
	}
	
	
	private void showDetails() {
		MdDetails mdDetails = getWizard().getMdDetails();
		List<Detail> details = mdDetails.details;
		
		if ( details.size() == 0 ) {
			Window.alert("No details are available");
			return;
		}
		
		FlexTable table = new FlexTable();
		table.setStylePrimaryName("inline");
		table.setCellSpacing(6);
		int row = 0;
		
		
		table.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(row, 0, new HTML("<b>Metadata property</b>".replaceAll(" ", "&nbsp;")));
		
		table.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(row, 1, new HTML("<b>Value in uploaded file</b>".replaceAll(" ", "&nbsp;")));
		
		table.getFlexCellFormatter().setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(row, 2, new HTML("<b>Value in registered ontology</b>".replaceAll(" ", "&nbsp;")));
		
		row++;
		
		Map<String, AttrDef> uriAttrDefMap = Orr.getMetadataBaseInfo().getUriAttrDefMap();
		
		for ( Detail detail : details ) {
			HTML propLabel = new HTML();
			String uri = detail.key;
			AttrDef attrDef = uriAttrDefMap.get(uri);
			
			if ( attrDef == null || attrDef.isInternal() || attrDef.getLabel() == null ) {
				// we are only interested in the fields that are shown in the metadata sections;
				// so, ignore this detail:
				continue;
			}
			
			String lbl = attrDef.getLabel().replaceAll(" ", "&nbsp;");
			propLabel.setHTML(lbl);
			propLabel.setTitle(uri);
			
			table.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
			table.setWidget(row, 0, propLabel);
			
			
			if ( detail.loadedValue.equals(detail.registeredValue) ) {
				table.getFlexCellFormatter().setColSpan(row, 1, 2);
				table.getFlexCellFormatter().setAlignment(row, 1, 
						HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
				table.setWidget(row, 1, new Label(detail.loadedValue));
			}
			else {
				if ( detail.loadedValue.length() > 0 && detail.registeredValue.length() > 0 ) {
					// non-empty, different values:
					propLabel.setHTML(DIFF_MARK +lbl);
				}
				
				table.getFlexCellFormatter().setAlignment(row, 1, 
						HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
				table.setWidget(row, 1, new Label(detail.loadedValue));
				
				table.getFlexCellFormatter().setAlignment(row, 2, 
						HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
				table.setWidget(row, 2, new Label(detail.registeredValue));
			}
			
			row++;
		}
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("400");
		vp.setSpacing(10);
		vp.add(table);
		vp.add(new HTML(DIFF_MARK+ "= Metadata attribute with different, non-blank values."));
		final MyDialog popup = new MyDialog(vp);
		popup.setText("Metadata values from the two sources");
		popup.center();
		popup.show();
	}

	
	private void createDetailsPanel() {
		detailsPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		detailsPanel.setSpacing(5);
		detailsButton = new PushButton("Details", new ClickListener() {
			public void onClick(Widget sender) {
				showDetails();
			}
		});
		DOM.setElementAttribute(detailsButton.getElement(), "id", "my-button-id");
		detailsButton.setTitle("Shows the values that are different");
	}

	private void updateInfoHtml(String msg) {
		String info = "" +
			"<br/>In the next pages you will be able to edit metadata attributes for this new version " +
			"of the ontology." +
			"<br/>" +
			"<br/>" +
			"The fields in those pages can be initialized by using the available values from both the " +
			"registered ontology and the just uploaded ontology. " +
			"Specify how you want these fields to be initialized. " +
			"(Note that you will be able to edit the fields regardless of how they are initialized.)" +
			"<br/>" +
			"<br/>"
		; 
		
		detailsPanel.clear();
		detailsPanel.add(detailsButton);
		detailsPanel.add(new HTML("Click this button to see the metadata values from the two sources." +
				(msg != null ? "<br/>" +msg : "")));
		
		infoHtml.setHTML(info);
	}
	
	private void recreate() {
		contents.clear();
		
		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
		int row = 0;
		
		panel.setWidget(row, 0, infoHtml);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		contents.add(panel);
		contents.add(createWidget());
	}

	
	private Widget createWidget() {
		FlexTable panel = new FlexTable();
		int row = 0;
		for ( RadioButton rb : rbs ) {
			panel.getFlexCellFormatter().setColSpan(row, 0, 2);
			panel.setWidget(row, 0, rb);
			panel.getFlexCellFormatter().setAlignment(row, 1, 
					HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;
		}
		
		panel.setWidget(row, 0, detailsPanel);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		String info2 = 
			"<br/>" +
			"<br/>" +
			"See this <a target=\"_blank\" href=\"" +PortalConsts.REG_TYPE_HELP_PAGE+ "\"" +
					">manual page</a> for more information."
		;
		panel.setWidget(row, 0, new HTML(info2));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		
		return panel;
	}


	@Override
	public RegisterVersionWizard getWizard() {
		return (RegisterVersionWizard) wizard;
	}


	void diffsUpdated() {
		MdDetails mdDetails = getWizard().getMdDetails();
		if ( mdDetails.noDiffs > 0 ) {
			if ( mdDetails.noDiffs == 1 ) {
				updateInfoHtml("Note: there is one common metadata attribute with different, non-blank values");
			}
			else {
				updateInfoHtml("Note: there are " +mdDetails.noDiffs+ " common metadata attributes with different, non-blank values");
			}
		}
	}

}
