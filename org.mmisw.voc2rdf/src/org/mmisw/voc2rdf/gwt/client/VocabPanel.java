package org.mmisw.voc2rdf.gwt.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.rpc.Attribute;
import org.mmisw.voc2rdf.gwt.client.rpc.ColumnSeparator;

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
 * Form elements for the contents of the vocabulary.
 * 
 * @author Carlos Rueda
 */
public class VocabPanel extends VerticalPanel {

	private static class Elem {
		Attribute attr;
		Widget widget;
		public Elem(Attribute attr, Widget widget) {
			super();
			this.attr = attr;
			this.widget = widget;
		}
	}
	
	private Map<String, Elem> widgets = new HashMap<String, Elem>();
	
	private PushButton exampleButton;
	
	private PushButton convertButton;

	protected MainPanel mainPanel;

	
	VocabPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		add(createForm());
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		
		int row = 0;
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(0, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		

		final List<Attribute> attrs = Main.baseInfo.getVocAttributes();
		for ( Attribute attr : attrs ) {
			Widget widget;
			final String attrName = attr.getName();
			if ( "ascii".equals(attrName) ) {
				final TextArea ta = new TextArea();
				ta.setName(attrName);
				ta.setSize("600", "200");
				ta.addChangeListener(new ChangeListener () {
					public void onChange(Widget sender) {
//						String text = ta.getText();
						formChanged();
					}
				});
				widget = ta;
			}
			else if ( "fieldSeparator".equals(attrName) ) {
				final ListBox lb = new ListBox();
				lb.setName(attrName);
				for ( ColumnSeparator comp : Main.baseInfo.getColumnSeparators() ) {
					lb.addItem(comp.getLabel(), comp.getName());
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
			
			Label lbl = new Label(attr.getLabel());
			lbl.setTitle(attr.getTooltip());
			widget.setTitle(attr.getTooltip());
			
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
		exampleButton = new PushButton("Example", new ClickListener() {
			public void onClick(Widget sender) {
				example();
			}
		});
		exampleButton.setTitle("Fills in example values in this section");
		panel.add(exampleButton);
		
		convertButton = new PushButton("Test", new ClickListener() {
			public void onClick(Widget sender) {
				mainPanel.convertTest();
			}
		});
		convertButton.setTitle("Tests the conversion of the current vocabulary contents");
		panel.add(convertButton);
		
		return panel;
	}
	
	private void formChanged() {
		// TODO Auto-generated method stub
		
	}

	String putValues(Map<String, String> values) {
		for ( Elem elem : widgets.values() ) {
			String value = null;
			if ( elem.widget instanceof TextBoxBase ) {
				value = ((TextBoxBase) elem.widget).getText();
			}
			else if ( elem.widget instanceof ListBox ) {
				ListBox lb = (ListBox) elem.widget;
				value = lb.getValue(lb.getSelectedIndex());
			}
			
			if ( value == null || value.trim().length() == 0 ) {
				if ( elem.attr.isRequired() ) {
					String error = elem.attr.getLabel()+ "\n   A value is required.";
					return error;
				}
			}
			values.put(elem.attr.getName(), value.trim());
		}
		return null;
	}

	
	void reset() {
		for ( Elem elem : widgets.values() ) {
			String value = "";
			if ( elem.widget instanceof TextBoxBase ) {
				((TextBoxBase) elem.widget).setText(value);
			}
		}
		
	}

	void example() {
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
}
