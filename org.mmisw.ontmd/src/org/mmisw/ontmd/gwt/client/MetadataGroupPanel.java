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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
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
	private AttrDef shortNameAttrDef;
	private FieldWithChoose shortNameFieldWithChoose;
	private CheckBox shortNameIsMap;
	private TextBoxBase shortNameRelatedField;
//	private TextBox shortNameId;   // TODO not for now


	
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
			
			final List<Option> options = attr.getOptions();
			
			if ( editing &&  // not listBoxes if we are just viewing 
					options != null ) {
				
				if ( Main.baseInfo.getShortNameUri().equals(attr.getUri()) ) {
					// the special case for the "short name"
					widget = createShortNameWidget(attr, editing,
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
						ListBox lb = createListBox(attr, cl);
						widget = lb;
					}
				}
			}
			
			else {
				int nl = attr.getNumberOfLines();
				TextBoxBase tb = createTextBoxBase(nl, "500", cl);
				widget = tb;
			}
			
			if ( shortNameAttrDef == attr ) {
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
	
	// NOTE: this has a special handling in the GUI
	// (not very elegant but ...)
	private Widget createShortNameWidget(AttrDef attr, boolean editing, ChangeListener cl) {
		shortNameAttrDef = attr;

		Widget widget;
		
		VerticalPanel vp0 = new VerticalPanel();
		vp0.setBorderWidth(1);
		VerticalPanel vp = new VerticalPanel();
		vp0.add(vp);
		vp.setSpacing(5);
		widget = vp0;
		
		// see MdHelper:
		assert attr.isAllowUserDefinedOption() ;
		assert attr.isRequired() ;
		List<AttrDef> relatedAttrs = attr.getRelatedAttrs();
		assert relatedAttrs != null && relatedAttrs.size() > 0 ; 
		
		
		shortNameFieldWithChoose = new FieldWithChoose(attr, cl) {
			protected void optionSelected(Option option) {
				shortNameRelatedField.setText(option.getUri());
			}
		};
		
		shortNameIsMap = new CheckBox("Check here if this is a mapping ontology");

		shortNameFieldWithChoose.textBox.addKeyboardListener(new KeyboardListenerAdapter() {
			  public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				  String value = shortNameFieldWithChoose.textBox.getText().toLowerCase();
				  boolean isMap = value.matches(".*_[mM][aA][pP]($|_.*)");
				  shortNameIsMap.setChecked(isMap);
			  }
		});
		vp.add(shortNameFieldWithChoose);

		HorizontalPanel hp = new HorizontalPanel();
		vp.add(hp);

		hp.setSpacing(4);
		shortNameIsMap.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				boolean checked = shortNameIsMap.isChecked();
				String value = shortNameFieldWithChoose.textBox.getText().toLowerCase();
				boolean isMap = value.matches(".*_[mM][aA][pP]($|_.*)");
				if ( shortNameIsMap.isChecked() && ! isMap ) {
					shortNameFieldWithChoose.setValue(value.replaceAll("_+$", "")+ "_map");
				}
				else if ( !checked && isMap ) {
					value = value.replaceAll("_+[mM][aA][pP]_+", "_");
					value = value.replaceAll("_+[mM][aA][pP]$", "");
					shortNameFieldWithChoose.setValue(value);
				}
			}
			
		});
		
//		hp.add(new Label("ID:"));
//		shortNameId = new TextBox();
//		hp.add(shortNameId);
		
		hp.add(shortNameIsMap);
		
		
		//////////////////////////////////////////////////////////////
		// handle the related attribute
		AttrDef attr2 = relatedAttrs.get(0);
		String label = attr2.getLabel();
		FlexTable panel = new FlexTable();
		int row = 0;
		int nl = attr2.getNumberOfLines();
		shortNameRelatedField = createTextBoxBase(nl, "400", cl);
		String tooltip = "<b>" +label+ "</b>:<br/>" + 
							attr2.getTooltip() +
							"<br/><br/><div align=\"right\">(" +attr2.getUri()+ ")</div>";
		panel.setWidget(row, 0, new TLabel(label, editing && attr2.isRequired(), tooltip ));
		panel.setWidget(row, 1, shortNameRelatedField);
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
		
		return widget;
	}

	private static ListBox createListBox(AttrDef attr, ChangeListener cl) {
		List<Option> options = attr.getOptions();
		final ListBox lb = new ListBox();
		for ( Option option : options ) {
			String lab = option.getLabel();
			if ( lab != null && lab.length() > 0 ) {
				lb.addItem(option.getLabel(), option.getName());
			}
			else {
				lb.addItem(option.getName());
			}
		}
		if ( cl != null ) {
			lb.addChangeListener(cl);
		}
		return lb;
	}
	
	
	private static class FieldWithChoose extends HorizontalPanel {
		AttrDef attr;
		TextBoxBase textBox;
		ListBox listBox;
		PushButton chooseButton;
		
		FieldWithChoose(AttrDef attr, ChangeListener cl) {
			this.attr = attr;
			int nl = 1;    /// attr.getNumberOfLines() is ignored
			textBox = createTextBoxBase(nl, "400", cl);

			add(textBox);
			listBox = createListBox(attr, cl);
			List<Option> options = attr.getOptions();
			listBox.setVisibleItemCount(Math.min(options.size(), 20));
			
			chooseButton = new PushButton("Choose", new ClickListener() {
				public void onClick(Widget sender) {
					choose();
				}
			});
			
			add(chooseButton);
		}

		/** nothing done here */
		protected void optionSelected(Option option) {
		}

		private void choose() {
			final MyDialog popup = new MyDialog(listBox);
			listBox.addChangeListener(new ChangeListener () {
				public void onChange(Widget sender) {
					String value = listBox.getValue(listBox.getSelectedIndex());
					textBox.setText(value);
					
					Option option = attr.getOptions().get(listBox.getSelectedIndex());
					optionSelected(option);
					popup.hide();
				}
			});
			popup.setText("Select " +attr.getLabel());
			popup.center();
			popup.show();

		}

		void enable(boolean enabled) {
			textBox.setEnabled(enabled);
//			lb.setEnabled(enabled);
			chooseButton.setEnabled(enabled);
		}

		public void setValue(String value) {
			textBox.setText(value);
//			lb.setSelectedIndex(0);
		}
	}
	
	private static TextBoxBase createTextBoxBase(int nl, String width, 
			ChangeListener cl) {
		final TextBoxBase tb;
		if ( nl <=1 ) {
			tb = new TextBox();
			tb.setWidth(width);
		}
		else {
			// avoid huge textareas (TODO max 20 line is arbitrary)
			if ( nl > 20 ) {
				nl = 20;
			}
			tb = new TextArea();
			// TODO 16 is just a rough scaling factor
			tb.setSize(width, "" +(nl *16));
		}
		if ( cl != null ) {
			tb.addChangeListener(cl);
		}
		return tb;
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
		if ( shortNameAttrDef != null ) {
			String value = shortNameFieldWithChoose.textBox.getText().trim();
			if ( value.length() == 0 ) {
				String error = "Please provide a value for the field with label: " +
							shortNameAttrDef.getLabel();
				return error;
			}
			
			if ( values != null ) {
				// do actual assignment
				String uri = shortNameAttrDef.getUri();
				values.put(uri, value);
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
				((TextBoxBase) elem.widget).setEnabled(enabled);
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
		
		if ( shortNameAttrDef != null ) {
			shortNameFieldWithChoose.enable(enabled);
			shortNameIsMap.setEnabled(enabled);
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
		
		if ( shortNameAttrDef != null ) {
			shortNameFieldWithChoose.setValue(value);
			shortNameIsMap.setChecked(false);
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
		
		if ( shortNameAttrDef != null ) {
			String example = shortNameAttrDef.getExample();
			if ( example == null ) {
				example = "";
			}
			shortNameFieldWithChoose.setValue(example);
			shortNameIsMap.setChecked(example.endsWith("_map"));
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
			if ( Main.baseInfo.getShortNameUri().equals(uri) ) {
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
		
		if ( shortNameAttrDef != null ) {
			String value = originalValues.get(shortNameAttrDef.getUri());
			if ( value == null ) {
				value = "";
			}
			shortNameFieldWithChoose.setValue(value);
			shortNameIsMap.setChecked(value.endsWith("_map"));
			
			// special case: ///////////////////////////////////////////
			List<AttrDef> relatedAttrs = shortNameAttrDef.getRelatedAttrs();
			assert relatedAttrs != null && relatedAttrs.size() > 0 ;
			String relatedUri = relatedAttrs.get(0).getUri();
			String relatedValue = originalValues.get(relatedUri);
			shortNameRelatedField.setText(relatedValue);
			//////////////////////////////////////////////////////////////
			
		}

	}
	
}
