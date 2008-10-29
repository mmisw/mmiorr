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
				setOntologyInfo(ontologyInfo,true);
			}
			else {
				reset(true);
			}
		}
	});

	
	MetadataGroupPanel(MainPanel mainPanel, AttrGroup attrGroup) {
		this.mainPanel = mainPanel;
		this.attrGroup = attrGroup;
		
		add(createForm());
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		
		int row = 0;
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(0, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		

		AttrDef[] attrDefs = attrGroup.getAttrDefs();
		for ( AttrDef attr : attrDefs ) {
			
			if ( attr.isInternal() ) {
				continue;
			}
			
			Widget widget;
			String attrName = attr.getLocalName();
			
			final List<Option> options = attr.getOptions();
			if ( options != null ) {
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
				lb.addChangeListener(new ChangeListener () {
					public void onChange(Widget sender) {
//						int idx = lb.getSelectedIndex();
						formChanged();
					}

				});
				widget = lb;
			}
			else {
				final TextBox tb = new TextBox();
				tb.setName(attrName);
				tb.setWidth("500");
				tb.addChangeListener(new ChangeListener () {
					public void onChange(Widget sender) {
//						String text = tb.getText();
						formChanged();
					}
				});
				widget = tb;
			}
			
			widgets.put(attrName, new Elem(attr, widget));
			
			String label = attr.getLabel() + ":";
			if ( attr.isRequired() ) {
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
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		exampleButton.setTitle("Fills in example values in this section");
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
		}
		exampleButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
	}

	void reset(boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in this section") ) {
			return;
		}
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
		}
	}

	void setOntologyInfo(OntologyInfo ontologyInfo, boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in this section") ) {
			return;
		}
		reset(false);
		Map<String, String> values = ontologyInfo.getValues();
		
		for ( Elem elem : widgets.values() ) {
			String uri = elem.attr.getUri();
			String value = values.get(uri);
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
		}
		
	}
	
}
