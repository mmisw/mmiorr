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
			String attrName = attr.getLocalName();
			
			final List<Option> options = attr.getOptions();
			
			if ( editing &&  // not listBoxes if we are just viewing 
					options != null ) {
				
				boolean allowUserOption = attr.isAllowUserDefinedOption();
				if ( allowUserOption ) {
					widget = new FieldWithChoose(attr, cl);
				}
				else {
					ListBox lb = createListBox(attr, cl);
					widget = lb;
				}
			}
			else {
				int nl = attr.getNumberOfLines();
				TextBoxBase tb = createTextBoxBase(nl, "500", attrName, cl);
				widget = tb;
			}
			
			widgets.put(attrName, new Elem(attr, widget));
			
			String label = attr.getLabel() + ":";
			if ( editing && attr.isRequired() ) {
				label += "*";
			}
			Label lbl = new Label(label);
			lbl.setTitle(attr.getUri());
			widget.setTitle(attr.getUri());
			
			panel.setWidget(row, 0, lbl);
			panel.setWidget(row, 1, widget);
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
	
	private static ListBox createListBox(AttrDef attr, ChangeListener cl) {
		String attrName = attr.getLocalName();
		List<Option> options = attr.getOptions();
		final ListBox lb = new ListBox();
		lb.setName(attrName);
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
		TextBoxBase tb;
		ListBox lb;
		PushButton chooseButton;
	
		FieldWithChoose(AttrDef attr, ChangeListener cl) {
			this.attr = attr;
			int nl = 1;    /// attr.getNumberOfLines() is ignored
			String attrName = attr.getLocalName();
			tb = createTextBoxBase(nl, "400", attrName, cl );

			add(tb);
			lb = createListBox(attr, cl);
			
			chooseButton = new PushButton("Choose", new ClickListener() {
				public void onClick(Widget sender) {
					choose();
				}
			});
			
			add(chooseButton);
		}

		private void choose() {
			final MyDialog popup = new MyDialog(lb);
			lb.addChangeListener(new ChangeListener () {
				public void onChange(Widget sender) {
					tb.setText(lb.getValue(lb.getSelectedIndex()));
					popup.hide();
				}
			});
			popup.setText("Select " +attr.getLabel());
			popup.center();
			popup.show();

		}

		void enable(boolean enabled) {
			tb.setEnabled(enabled);
//			lb.setEnabled(enabled);
			chooseButton.setEnabled(enabled);
		}

		public void setValue(String value) {
			tb.setText(value);
//			lb.setSelectedIndex(0);
		}
	}
	
	private static TextBoxBase createTextBoxBase(int nl, String width, String attrName,
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
		tb.setName(attrName);
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
				value = ((FieldWithChoose) elem.widget).tb.getText();
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
				}
				Main.log("assigned: " +uri+ " = " +value);
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
	}

	/**
	 * Sets all widget values to empty (textbox) and first option (listbox). 
	 */
	private void resetToEmpty() {
		for ( Elem elem : widgets.values() ) {
			String value = "";
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
		
	}
	
}
