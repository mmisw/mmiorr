package org.mmisw.ontmd.gwt.client;

import java.util.List;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.util.TLabel;
import org.mmisw.ontmd.gwt.client.util.Util;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.Option;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel for the resourceType elements.
 * 
 * @author Carlos Rueda
 */
public class ResourceTypeWidget extends VerticalPanel {

	public AttrDef resourceTypeAttrDef;
	public FieldWithChoose resourceTypeFieldWithChoose;
	private CheckBox resourceTypeIsMap;
	TextBoxBase resourceTypeRelatedField;

	
	/**
	 * 
	 * @param attr
	 * @param editing
	 * @param cl
	 */
	public ResourceTypeWidget(AttrDef attr, boolean editing, ChangeListener cl) {
		super();
		Main.log("Creating ResourceTypeWidget: " +attr+ ", " +editing);
		resourceTypeAttrDef = attr;

		// see MdHelper:
		assert attr.isAllowUserDefinedOption() ;
		assert attr.isRequired() ;
		List<AttrDef> relatedAttrs = attr.getRelatedAttrs();
		assert relatedAttrs != null && relatedAttrs.size() > 0 ; 
		
		
		resourceTypeFieldWithChoose = new FieldWithChoose(attr, cl) {
			protected void optionSelected(Option option) {
				resourceTypeRelatedField.setText(option.getUri());
			}
		};
		
		this.setBorderWidth(1);
		
		FlexTable flexPanel = new FlexTable();
		int row = 0;
		this.add(flexPanel);

		//////////////////////////////////////////////////////////////
		// handle the related attribute
		AttrDef attr2 = relatedAttrs.get(0);
		String label = attr2.getLabel();
		int nl = attr2.getNumberOfLines();
		resourceTypeRelatedField = Util.createTextBoxBase(nl, "400", cl);
		String tooltip = "<b>" +label+ "</b>:<br/>" + 
							attr2.getTooltip() +
							"<br/><br/><div align=\"right\">(" +attr2.getUri()+ ")</div>";

		flexPanel.setWidget(row, 0, new TLabel("Name:", editing && attr2.isRequired(), tooltip ));
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, resourceTypeFieldWithChoose);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		flexPanel.setWidget(row, 0, new TLabel(label, editing && attr2.isRequired(), tooltip ));
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, resourceTypeRelatedField);
//		flexPanel.getFlexCellFormatter().setWidth(row, 0, "250");
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		//////////////////////////////////////////////////////////////
		
	}

	
	
	/**
	 * 
	 * @param attr
	 * @param editing
	 * @param includeIsMapCheck
	 * @param cl
	 */
	public ResourceTypeWidget(AttrDef attr, boolean editing, boolean includeIsMapCheck, ChangeListener cl) {
		super();
		Main.log("Creating ResourceTypeWidget: " +attr+ ", " +editing);
		resourceTypeAttrDef = attr;

		// see MdHelper:
		assert attr.isAllowUserDefinedOption() ;
		assert attr.isRequired() ;
		List<AttrDef> relatedAttrs = attr.getRelatedAttrs();
		assert relatedAttrs != null && relatedAttrs.size() > 0 ; 
		
		
		resourceTypeFieldWithChoose = new FieldWithChoose(attr, cl) {
			protected void optionSelected(Option option) {
				resourceTypeRelatedField.setText(option.getUri());
			}
		};
		
		if ( includeIsMapCheck ) {
			resourceTypeIsMap = new CheckBox("Check here if this is a mapping ontology");

			// update this checkbox according to contents in the text field:
			resourceTypeFieldWithChoose.textBox.addKeyboardListener(new KeyboardListenerAdapter() {
				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
					String value = resourceTypeFieldWithChoose.textBox.getText().toLowerCase();
					boolean isMap = value.matches(".*_[mM][aA][pP]($|_.*)");
					resourceTypeIsMap.setChecked(isMap);
				}
			});
			
			resourceTypeIsMap.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					boolean checked = resourceTypeIsMap.isChecked();
					String value = resourceTypeFieldWithChoose.textBox.getText().toLowerCase();
					boolean isMap = value.matches(".*_[mM][aA][pP]($|_.*)");
					if ( resourceTypeIsMap.isChecked() && ! isMap ) {
						resourceTypeFieldWithChoose.setValue(value.replaceAll("_+$", "")+ "_map");
					}
					else if ( !checked && isMap ) {
						value = value.replaceAll("_+[mM][aA][pP]_+", "_");
						value = value.replaceAll("_+[mM][aA][pP]$", "");
						resourceTypeFieldWithChoose.setValue(value);
					}
				}

			});
		}
		
		this.setBorderWidth(1);
		VerticalPanel vp = new VerticalPanel();
		this.add(vp);
		vp.setSpacing(5);
		
		vp.add(resourceTypeFieldWithChoose);

		//////////////////////////////////////////////////////////////
		// handle the related attribute
		AttrDef attr2 = relatedAttrs.get(0);
		String label = attr2.getLabel();
		FlexTable panel = new FlexTable();
		int row = 0;
		int nl = attr2.getNumberOfLines();
		resourceTypeRelatedField = Util.createTextBoxBase(nl, "400", cl);
		String tooltip = "<b>" +label+ "</b>:<br/>" + 
							attr2.getTooltip() +
							"<br/><br/><div align=\"right\">(" +attr2.getUri()+ ")</div>";
		panel.setWidget(row, 0, new TLabel(label, editing && attr2.isRequired(), tooltip ));
		panel.setWidget(row, 1, resourceTypeRelatedField);
		panel.getFlexCellFormatter().setWidth(row, 0, "250");
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		vp.add(panel);
		//////////////////////////////////////////////////////////////
		
		HorizontalPanel hp = new HorizontalPanel();
		vp.add(hp);
		hp.setSpacing(4);
		if ( includeIsMapCheck ) {
			hp.add(resourceTypeIsMap);
		}
		
	}
	
	void enable(boolean enabled) {
		if ( resourceTypeAttrDef != null ) {
			resourceTypeFieldWithChoose.enable(enabled);
			resourceTypeRelatedField.setEnabled(enabled);
			if ( resourceTypeIsMap != null ) {
				resourceTypeIsMap.setEnabled(enabled);
			}
		}
	}

	void setValue(String value) {
		if ( resourceTypeAttrDef != null ) {
			resourceTypeFieldWithChoose.setValue(value);
			resourceTypeRelatedField.setText(value);
			if ( resourceTypeIsMap != null ) {
				resourceTypeIsMap.setChecked(false);
			}
		}
	}
	
	public void setExample() {
		if ( resourceTypeAttrDef != null ) {
			String example = resourceTypeAttrDef.getExample();
			if ( example == null ) {
				example = "";
			}
			resourceTypeFieldWithChoose.setValue(example);
			resourceTypeRelatedField.setText(resourceTypeAttrDef.getRelatedAttrs().get(0).getExample());
			if ( resourceTypeIsMap != null ) {
				resourceTypeIsMap.setChecked(example.endsWith("_map"));
			}
		}	
	}
	
	void resetFieldValues(Map<String, String> fromValues) {
		if ( resourceTypeAttrDef != null ) {
			String value = fromValues.get(resourceTypeAttrDef.getUri());
			if ( value == null ) {
				value = "";
			}
			resourceTypeFieldWithChoose.setValue(value);
			
			if ( resourceTypeIsMap != null ) {
				resourceTypeIsMap.setChecked(value.endsWith("_map"));
			}
			
			// special case: ///////////////////////////////////////////
			String relatedUri = resourceTypeAttrDef.getRelatedAttrs().get(0).getUri();
			String relatedValue = fromValues.get(relatedUri);
			if ( relatedValue == null ) {
				relatedValue = "";
			}
			resourceTypeRelatedField.setText(relatedValue);
			//////////////////////////////////////////////////////////////
			
		}

	}
	
	public String getRelatedValue() {
		return resourceTypeRelatedField.getText();
	}
}
