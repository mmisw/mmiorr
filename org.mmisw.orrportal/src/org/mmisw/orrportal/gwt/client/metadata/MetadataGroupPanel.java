package org.mmisw.orrportal.gwt.client.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.OntologyMetadata;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrGroup;
import org.mmisw.orrclient.gwt.client.vocabulary.Option;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.FieldWithChoose;
import org.mmisw.orrportal.gwt.client.util.OrrUtil;
import org.mmisw.orrportal.gwt.client.util.TLabel;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The form.
 * 
 * @author Carlos Rueda
 */
public class MetadataGroupPanel extends VerticalPanel {

	private static class Elem {
		AttrDef attr;
		Widget widget;
		public Elem(AttrDef attr, Widget widget) {
			super();
			this.attr = attr;
			this.widget = widget;
		}
	}
	
	
	private static class ViewOnlyCell extends HorizontalPanel {
		// a TextBoxBase was used before.
//		TextBoxBase tb = Util.createTextBoxBase(nl, "900", cl);
//		widget = tb;

		private String text = "";
		private HTML html = new HTML();
		
		public ViewOnlyCell(int nl, String width) {
			super();
			setSpacing(5);
			setWidth(width);
			add(html);
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
			if ( OrrUtil.isUrl(text) ) {
				String link = "<a target=\"_blank\" href=\"" +text+ "\">" +text+ "</a>";
				html.setHTML(link);
			}
			else {
				html.setText(text);
			}
		}
	}
	
	
	private MetadataPanel metadataPanel;
	
	private AttrGroup attrGroup;
	
	private Map<String, Elem> widgets = new HashMap<String, Elem>();
	
	private PushButton exampleButton = new PushButton("Example", new ClickListener() {
		public void onClick(Widget sender) {
			example(true);
		}
	});

	private PushButton resetButton = new PushButton("Reset", new ClickListener() {
		public void onClick(Widget sender) {
			OntologyMetadata ontologyMetadata = metadataPanel.getOntologyPanel().getOntologyMetadata();
			
			if ( ontologyMetadata != null && ontologyMetadata.getError() == null ) {
				resetToOriginalOrNewValues(ontologyMetadata,true, true);
			}
			else {
				Window.alert("No ontology information available for this operation");
			}
		}
	});
	
	
	// special case
	private ResourceTypeWidget resourceTypeWidget;
//	private AttrDef resourceTypeAttrDef;
//	private FieldWithChoose resourceTypeFieldWithChoose;
//	private CheckBox resourceTypeIsMap;
//	private TextBoxBase resourceTypeRelatedField;


	
	MetadataGroupPanel(MetadataPanel metadataPanel, AttrGroup attrGroup, boolean editing) {
		this.metadataPanel = metadataPanel;
		this.attrGroup = attrGroup;
		
		add(createForm(editing));
	}

	private Widget createForm(boolean editing) {
		FlexTable panel = new FlexTable();
		
		if ( ! editing ) {
			panel.setStylePrimaryName("MetadataPanel");
			panel.setBorderWidth(1);
		}
		
		int row = 0;

		if ( editing ) {
			String grpDescription = attrGroup.getDescription();
			if ( grpDescription != null && grpDescription.length() > 0 ) {
				Widget w = new Label(grpDescription);
				panel.getFlexCellFormatter().setColSpan(row, 0, 2);
				panel.setWidget(row, 0, w);
				panel.getFlexCellFormatter().setAlignment(row, 0, 
						HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
				);
				row++;
			}
		}

		
		
		
		ChangeListener cl = null;
		
		if ( editing ) {
			cl = new ChangeListener () {
				public void onChange(Widget sender) {
					formChanged();
				}
			};
			
			CellPanel buttons = createButtons();
			panel.getFlexCellFormatter().setColSpan(row, 0, 2);
			panel.setWidget(row, 0, buttons);
			panel.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;
		}
		

		AttrDef[] attrDefs = attrGroup.getAttrDefs();
		for ( AttrDef attr : attrDefs ) {
			
			if ( attr.isInternal() ) {
				continue;
			}
			
			Widget widget;
			
			// TODO: need to handle dynamic refresh of options? (Main.refreshOptions)
			final List<Option> options = attr.getOptions();
			
			final String optionsVocabulary = attr.getOptionsVocabulary();
			
			// not listBoxes if we are just viewing
			
			// Bofore, the following section was conditioned by: 
//			if ( editing && options.size() > 0 ) {
			// and now by:
			if ( editing && (optionsVocabulary != null || options.size() > 0) ) {
				
				if ( Orr.getMetadataBaseInfo().getResourceTypeUri().equals(attr.getUri()) ) {
					// the special case for the "resourceType"
					
					// #183: Don't ask user whether the submitted ontology is a mapping
					boolean includeIsMapCheck = false;
					
					widget = resourceTypeWidget = new ResourceTypeWidget(attr, editing, includeIsMapCheck,
						new ChangeListener () {
							public void onChange(Widget sender) {
									formChanged();
							}
						}
					);
				}
				else {
					boolean allowUserOption = attr.isAllowUserDefinedOption();
					if ( allowUserOption ) {
						widget = new FieldWithChoose(attr, cl);
					}
					else {
						ListBox listBox = OrrUtil.createListBox(options, cl);
						widget = listBox;
					}
				}
			}
			else if ( !editing ) {
				int nl = attr.getNumberOfLines();
				ViewOnlyCell voCell = new ViewOnlyCell(nl, "600");
				widget = voCell;
			}
			else {
				int nl = attr.getNumberOfLines();
				TextBoxBase tb = OrrUtil.createTextBoxBase(nl, "600", cl);
				widget = tb;
			}
			
			if ( resourceTypeWidget != null && resourceTypeWidget.resourceTypeAttrDef == attr ) {
				// we handle this as a special case.
			}
			else {
				widgets.put(attr.getUri(), new Elem(attr, widget));
			}
			
			String label = attr.getLabel();
			String tooltip = "<b>" +label+ "</b>:<br/>" + 
			                  attr.getTooltip() +
			                  "<br/><br/><div align=\"right\">(" +attr.getUri()+ ")</div>";
			panel.setWidget(row, 0, new TLabel(label, editing && attr.isRequired(), tooltip ));
			
//			panel.getFlexCellFormatter().addStyleName(row, 0, "MetadataPanel-header");
			panel.getFlexCellFormatter().setWidth(row, 0, "250");
			panel.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
			);

			panel.setWidget(row, 1, widget);
			panel.getFlexCellFormatter().setAlignment(row, 1, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;
		}
		

		return panel;
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		exampleButton.setTitle("Fills in fields in this section with example values");
		panel.add(exampleButton);
		
		resetButton.setTitle("Resets the fields in this section");
		panel.add(resetButton);
		
		return panel;
	}
	
	private void formChanged() {
		
		Map<String, String> values = new HashMap<String, String>();
		putValues(values, false);
		
		metadataPanel.getOntologyPanel().formChanged(values);
	}
	
	/** 
	 * Puts test values. These are the example values for the
	 * required, non-internal attributes.
	 */
	void putTestValues(Map<String, String> values) {
		AttrDef[] attrDefs = attrGroup.getAttrDefs();
		for ( AttrDef attr : attrDefs ) {
			if ( attr.isRequired() && ! attr.isInternal() ) {
				String value = attr.getExample();
				if ( value != null && value.trim().length() > 0 ) {
					values.put(attr.getUri(), value.trim());
				}
			}
		}
	}

	/**
	 * Puts the value in the map, only if value is not null and not empty.
	 */
	private static void _putValueIfNonEmpty(Map<String, String> values, String key, String value) {
		if ( value != null && value.trim().length() > 0 ) {
			values.put(key, value);
		}
	}

	/**
	 * Puts the non-empty values in the given map, if not null.
	 * 
	 * @param values null to just do the checking of required attributes.
	 * @param checkMissing true to do the checking. So, if just want to get the current values without
	 *         complains about missing values, pass a non-null values map and set checkMissing == false.
	 *         
	 * @return Only non-null then called with checkMissing == true and there is some missing required attribute.
	 */
	String putValues(Map<String, String> values, boolean checkMissing) {
		
		// special case:
		if ( resourceTypeWidget != null && resourceTypeWidget.resourceTypeAttrDef != null ) {
			String value = resourceTypeWidget.resourceTypeFieldWithChoose.getTextBox().getText().trim();
			
			if ( checkMissing && value.length() == 0 ) {
				String error = "Please provide a value for the field with label: " +
							resourceTypeWidget.resourceTypeAttrDef.getLabel();
				return error;
			}
			
			if ( values != null ) {
				// do actual assignment
				String uri = resourceTypeWidget.resourceTypeAttrDef.getUri();
				_putValueIfNonEmpty(values, uri, value);
				
				String relatedUri = resourceTypeWidget.resourceTypeAttrDef.getRelatedAttrs().get(0).getUri();
				String relatedValue = resourceTypeWidget.resourceTypeRelatedField.getText();
				_putValueIfNonEmpty(values, relatedUri, relatedValue);
			}
		}
		
		
		for ( Elem elem : widgets.values() ) {
			String value = "";
			
			if ( elem.widget instanceof TextBoxBase ) {
				value = ((TextBoxBase) elem.widget).getText();
			}
			else if ( elem.widget instanceof ViewOnlyCell ) {
				value = ((ViewOnlyCell) elem.widget).getText();
			}
			else if ( elem.widget instanceof ListBox ) {
				ListBox lb = (ListBox) elem.widget;
				value = lb.getValue(lb.getSelectedIndex());
			}
			else if ( elem.widget instanceof FieldWithChoose ) {
				value = ((FieldWithChoose) elem.widget).getTextBox().getText();
			}
			
			value = value.trim();
			
			if ( value.trim().length() == 0 
			||   value.startsWith("--")
			) {
				if ( checkMissing && elem.attr.isRequired() ) {
					String error = "Please provide a value for the field with label: " +
						elem.attr.getLabel();
					return error;
				}
			}
			else {
				String uri = elem.attr.getUri();
				value = value.trim();
				
				if ( checkMissing ) {
					// actually the flag is used to check the value is well-formed.
					// TODO review useage of the checkMissing flag and rename appropriately.
					
					// special cases because they form part of the URI: acronym, authority abbreviation
					// TODO a more general mechanism to handle this kind of verifications
					if ( "http://omv.ontoware.org/2005/05/ontology#acronym".equals(uri)
					||   "http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origMaintainerCode".equals(uri)
					) {
						String error = _checkUriComponent(value, elem.attr.getLabel());
						if (error != null) {
							return error;
						}
					}
				}
				
				
				if ( values != null ) {
					// do actual assignment
					_putValueIfNonEmpty(values, uri, value);
				}
			}
		}
		return null;
	}
	
	/**
	 * Checks the value for one of the ontology URI components: acronym, authority-abbreviation.
	 * 
	 * @param value        Assumed to be a trimmed string
	 * @param fieldLabel   use for the error message
	 * 
	 * @return null iff check passes; otherwise an error message.
	 */
	private String _checkUriComponent(String value, String fieldLabel) {
		/*
		 * value must start with a letter or underscore and be only composed of letters, digits, underscores or hyphens..
		 */
		if (value.matches("[a-zA-Z_](\\w|-)*")) {
			/*
			 * .. and must contain at least one letter or digit:
			 */
			if (value.matches(".*([a-zA-Z]|\\d).*")) {
				return null;  // OK
			}
			else {
				return "Value must at least contain a letter or digit for: " + fieldLabel;
			}
		}
		
		return "Invalid value for: " + fieldLabel + ".\n" +
				"The value must start with a letter or underscore and be only composed of letters, digits, underscores or hyphens.";
	}
	

	void enable(boolean enabled) {
		for ( Elem elem : widgets.values() ) {
			if ( elem.widget instanceof TextBoxBase ) {
				((TextBoxBase) elem.widget).setReadOnly(!enabled);
			}
			else if ( elem.widget instanceof ListBox ) {
				ListBox lb = (ListBox) elem.widget;
				lb.setEnabled(enabled);
			}
			else if ( elem.widget instanceof FieldWithChoose ) {
				((FieldWithChoose) elem.widget).enable(enabled);
			}
		}
		exampleButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
		
		if ( resourceTypeWidget != null ) {
			resourceTypeWidget.enable(enabled);
		}
	}

	/**
	 * Sets all widget values to empty (textbox) and first option (listbox). 
	 */
	private void resetToEmpty() {
		String value = "";
		for ( Elem elem : widgets.values() ) {
			if ( elem.widget instanceof TextBoxBase ) {
				((TextBoxBase) elem.widget).setText(value);
			}
			else if ( elem.widget instanceof ListBox ) {
				ListBox lb = (ListBox) elem.widget;
				int idx = 0;
				lb.setSelectedIndex(idx);
			}
			else if ( elem.widget instanceof FieldWithChoose ) {
				((FieldWithChoose) elem.widget).setValue(value);
			}
		}
		
		if ( resourceTypeWidget != null ) {
			resourceTypeWidget.setValue(value);
		}
		
	}

	void example(boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in this section") ) {
			return;
		}
		for ( Elem elem : widgets.values() ) {
			String value = elem.attr.getExample();
			if ( elem.widget instanceof TextBoxBase ) {
				((TextBoxBase) elem.widget).setText(value);
			}
			else if ( elem.widget instanceof ListBox ) {
				ListBox lb = (ListBox) elem.widget;
				int idx = 0;
				for ( int i = 0; i < lb.getItemCount(); i++ ) {
					if ( value.equals(lb.getValue(i)) ) {
						idx = i;
						break;
					}
				}
				lb.setSelectedIndex(idx);
			}
			else if ( elem.widget instanceof FieldWithChoose ) {
				((FieldWithChoose) elem.widget).setValue(value);
			}
		}
		
		if ( resourceTypeWidget != null ) {
			resourceTypeWidget.setExample();
		}

		formChanged();
	}
	
	void resetToOriginalOrNewValues(OntologyMetadata ontologyMetadata, boolean originalVals, boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in this section") ) {
			return;
		}
		resetToEmpty();
		formChanged();
		Map<String, String> originalValues = 
			originalVals 
			? ontologyMetadata.getOriginalValues() 
			: ontologyMetadata.getNewValues();
		
		for ( Elem elem : widgets.values() ) {
			String uri = elem.attr.getUri();
			String value = originalValues.get(uri);
			
			if ( value == null ) {
				continue;
			}
			
//			Main.log("resetToOriginalOrNewValues: uri: " +uri+ " = " +value);

			// Special case: Omv.acronym/OmvMmi.shortNameUri
			if ( Orr.getMetadataBaseInfo().getResourceTypeUri().equals(uri) ) {
				List<AttrDef> relatedAttrs = elem.attr.getRelatedAttrs();
				assert relatedAttrs != null && relatedAttrs.size() > 0 ;
				String relatedUri = relatedAttrs.get(0).getUri();
				String relatedValue = originalValues.get(relatedUri);
				if ( relatedValue != null ) {
					value += "   (" +relatedValue+ ")" ;
				}
			}
			
			if ( elem.widget instanceof TextBoxBase ) {
				((TextBoxBase) elem.widget).setText(value);
			}
			if ( elem.widget instanceof ViewOnlyCell ) {
				((ViewOnlyCell) elem.widget).setText(value);
			}
			else if ( elem.widget instanceof ListBox ) {
				ListBox lb = (ListBox) elem.widget;
				int idx = 0;
				for ( int i = 0; i < lb.getItemCount(); i++ ) {
					if ( value.equals(lb.getValue(i)) ) {
						idx = i;
						break;
					}
				}
				lb.setSelectedIndex(idx);
			}
			else if ( elem.widget instanceof FieldWithChoose ) {
				((FieldWithChoose) elem.widget).setValue(value);
			}
		}
		
		if ( resourceTypeWidget != null ) {
			resourceTypeWidget.resetFieldValues(originalValues);
		}

		formChanged();
	}
}
