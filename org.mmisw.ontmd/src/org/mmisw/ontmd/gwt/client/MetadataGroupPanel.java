package org.mmisw.ontmd.gwt.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ontmd.gwt.client.vocabulary.Option;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
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
	
	
	private MainPanel mainPanel;
	
	private AttrGroup attrGroup;
	
	private Map<String, Elem> widgets = new HashMap<String, Elem>();
	
	private PushButton exampleButton = new PushButton("Example", new ClickListener() {
		public void onClick(Widget sender) {
			example(true);
		}
	});

	private PushButton resetButton = new PushButton("Reset", new ClickListener() {
		public void onClick(Widget sender) {
			OntologyInfo ontologyInfo = mainPanel.getOntologyInfo();
			if ( ontologyInfo != null && ontologyInfo.getError() == null ) {
				resetToOriginalOrNewValues(ontologyInfo,true, true);
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


	
	MetadataGroupPanel(MainPanel mainPanel, AttrGroup attrGroup, boolean editing) {
		this.mainPanel = mainPanel;
		this.attrGroup = attrGroup;
		
		add(createForm(editing));
	}

	private Widget createForm(boolean editing) {
		FlexTable panel = new FlexTable();
		
		int row = 0;

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
			
			if ( editing &&  // not listBoxes if we are just viewing 
					options.size() > 0 ) {
				
				if ( Main.baseInfo.getResourceTypeUri().equals(attr.getUri()) ) {
					// the special case for the "resourceType"
					widget = resourceTypeWidget = new ResourceTypeWidget(attr, editing, true,
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
						ListBox listBox = Util.createListBox(options, cl);
						widget = listBox;
					}
				}
			}
			
			else {
				int nl = attr.getNumberOfLines();
				TextBoxBase tb = Util.createTextBoxBase(nl, "500", cl);
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

			panel.setWidget(row, 1, widget);
			panel.getFlexCellFormatter().setWidth(row, 0, "250");
			panel.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
			);
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
		// TODO Auto-generated method stub
		
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
	 * Puts the non-empty values in the given map, if not null.
	 * @param values null to just do the checking of required attributes.
	 * @return null if OK; otherwise a message about the missing required attribute.
	 */
	String putValues(Map<String, String> values) {
		
		// special case:
		if ( resourceTypeWidget != null && resourceTypeWidget.resourceTypeAttrDef != null ) {
			String value = resourceTypeWidget.resourceTypeFieldWithChoose.textBox.getText().trim();
			if ( value.length() == 0 ) {
				String error = "Please provide a value for the field with label: " +
							resourceTypeWidget.resourceTypeAttrDef.getLabel();
				return error;
			}
			
			if ( values != null ) {
				// do actual assignment
				String uri = resourceTypeWidget.resourceTypeAttrDef.getUri();
				values.put(uri, value);
				Main.log("assigned: " +uri+ " = " +value);
				
				String relatedUri = resourceTypeWidget.resourceTypeAttrDef.getRelatedAttrs().get(0).getUri();
				String relatedValue = resourceTypeWidget.resourceTypeRelatedField.getText();
				values.put(relatedUri, relatedValue);
				Main.log("assigned: " +uri+ " = " +value);
			}
		}
		
		
		for ( Elem elem : widgets.values() ) {
			String value = "";
			
			if ( elem.widget instanceof TextBoxBase ) {
				value = ((TextBoxBase) elem.widget).getText();
			}
			else if ( elem.widget instanceof ListBox ) {
				ListBox lb = (ListBox) elem.widget;
				value = lb.getValue(lb.getSelectedIndex());
			}
			else if ( elem.widget instanceof FieldWithChoose ) {
				value = ((FieldWithChoose) elem.widget).textBox.getText();
			}
			
			value = value.trim();
			
			if ( value.trim().length() == 0 
			||   value.startsWith("--")
			) {
				if ( elem.attr.isRequired() ) {
					String error = "Please provide a value for the field with label: " +
						elem.attr.getLabel();
					return error;
				}
			}
			else {
				String uri = elem.attr.getUri();
				value = value.trim();
				if ( values != null ) {
					// do actual assignment
					values.put(uri, value);
					Main.log("assigned: " +uri+ " = " +value);
				}
			}
		}
		return null;
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

	}
	
	void resetToOriginalOrNewValues(OntologyInfo ontologyInfo, boolean originalVals, boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in this section") ) {
			return;
		}
		resetToEmpty();
		Map<String, String> originalValues = 
			originalVals ? ontologyInfo.getOriginalValues() :  ontologyInfo.getNewValues();
		
		for ( Elem elem : widgets.values() ) {
			String uri = elem.attr.getUri();
			String value = originalValues.get(uri);
			
			if ( value == null ) {
				continue;
			}
			
			Main.log("resetToOriginalOrNewValues: uri: " +uri+ " = " +value);

			// Special case: Omv.acronym/OmvMmi.shortNameUri
			if ( Main.baseInfo.getResourceTypeUri().equals(uri) ) {
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

	}
	
}
