package org.mmisw.voc2rdf.gwt.client;

import java.util.Map;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Form elements for the contents of the vocabulary.
 * 
 * @author Carlos Rueda
 */
public class VocabPanel extends VerticalPanel {

	private CellPanel contentsContainer = new VerticalPanel();
	
	private TextArea ascii_ta = new TextArea();
	private ListBox fieldSeparator_lb;
	
	private final CheckBox tabular_cb = new CheckBox("Tabular view");
	
	private ScrollPanel table = new ScrollPanel();
	
	private PushButton exampleButton;
	
	private PushButton convertButton;

	protected MainPanel mainPanel;

	
	VocabPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setWidth("700");
		
		add(new HTML("Use this panel to provide the contents of your vocabulary in text format"));
		add(createForm());
	}

	private Widget createForm() {
		contentsContainer.setBorderWidth(1);
		ascii_ta.setSize("600", "200");
		table.setSize("600", "200");

		FlexTable panel = new FlexTable();
		
		int row = 0;
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(0, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		Label lbl = new Label("Contents of your vocabulary:");
		lbl.setTitle("Contents of your vocabulary");
		panel.setWidget(row, 0, lbl);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		contentsContainer.setSize("600", "200");
		contentsContainer.add(ascii_ta);
		panel.setWidget(row, 1, contentsContainer);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		CellPanel hp = new HorizontalPanel();
		lbl = new Label("Column separator:");
		fieldSeparator_lb = new ListBox();
		fieldSeparator_lb.addItem("Comma", "csv");
		fieldSeparator_lb.addItem("Tabulator", "tab");
		hp.add(lbl);
		hp.add(fieldSeparator_lb);
		fieldSeparator_lb.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				boolean tabular = tabular_cb.isChecked();
				if ( tabular ) {
					updateContents(contentsContainer, tabular);
				}
			}
		});
		
		hp.add(tabular_cb);
		tabular_cb.setTitle("Check this to show the contents in a tabular view for easier inspection");
		
		tabular_cb.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				boolean tabular = tabular_cb.isChecked();
				if ( tabular ) {
					String ascii = ascii_ta.getText().trim();
					if ( ascii.length() == 0 ) {
						tabular_cb.setChecked(false);
						return;
					}
				}

				updateContents(contentsContainer, tabular);
			}
		});
			
		panel.setWidget(row, 1, hp);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

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
	

	String putValues(Map<String, String> values) {
		String ascii = ascii_ta.getText().trim();
		if ( ascii.length() > 0 ) {
			values.put("ascii", ascii_ta.getText());
		}
		else {
			return "Empty vocabulary contents";
		}
		
		values.put("fieldSeparator", fieldSeparator_lb.getValue(fieldSeparator_lb.getSelectedIndex()));
		
		return null;
	}

	
	private void updateContents(CellPanel container, boolean tabular) {

		contentsContainer.clear();
		if ( tabular ) {
			String html = updateTabularView();
			table.setWidget(new HTML(html));
			contentsContainer.add(table);
		}
		else {
			contentsContainer.add(ascii_ta);
		}
		
	}

	
	private String updateTabularView() {
		String ascii = ascii_ta.getText().trim();
		assert ascii.length() > 0;

		String[] lines = ascii.split("\n|\r\n|\r");
		if ( lines.length == 0 || lines[0].trim().length() == 0 ) {
			return "";
		}
		
		String separatorName = fieldSeparator_lb.getValue(fieldSeparator_lb.getSelectedIndex());
		String separator = "csv".equalsIgnoreCase(separatorName) ? "," : "\t";
		
		StringBuffer sb = new StringBuffer();
		sb.append("<table class=\"inline\">");		

		// header:
		sb.append("<tr>\n");
		
		sb.append("<td align=\"right\"> <font color=\"gray\">" +0+ "</font> </td>");
		
		if ( lines[0].indexOf('\"') >= 0 || lines[0].indexOf('\'') >= 0  ) {
			sb.append("<td> <font color=\"red\">" +"Sorry, quotes not handled yet for the tabular view"+ "</font></td>");
			sb.append("</tr></table>\n");
			return sb.toString();
		}

		String[] headerCols = lines[0].split(separator);
		for ( int c = 0; c < headerCols.length; c++ ) {
			String str = headerCols[c];
			sb.append("<th>" +str+ "</th>");	
		}		
		
		sb.append("</tr>\n");
		
		// remaining rows:
		for ( int r = 1; r < lines.length; r++ ) {
			sb.append("<tr>\n");
			sb.append("<td align=\"right\"> <font color=\"gray\">" +r+ "</font> </td>");
			
			if ( lines[r].indexOf('\"') >= 0 || lines[r].indexOf('\'') >= 0  ) {
				sb.append("<td colspan=\"" +headerCols.length+ "\"> " +
						"<font color=\"red\">" +"Sorry, quotes not handled yet for the tabular view"+ "</font></td>");
				continue;
			}

			String[] cols = lines[r].split(separator);
			for ( int c = 0; c < cols.length; c++ ) {
				String str = cols[c].trim();
				if ( str.length() == 0 ) {
					str = "<font color=\"red\">" +"?"+ "</font>";
				}
				
				if ( c < headerCols.length ) {
					sb.append("<td>" +str+ "</td>");
				}
				else {
					// more columns than expected
					sb.append("<td> <font color=\"red\">" +str+ "</font></td>");
				}
			}
			
			// any missing columns? 
			for ( int c = cols.length; c < headerCols.length; c++ ) {
				sb.append("<td> <font color=\"red\">" +"?"+ "</font></td>");
			}
			
			sb.append("</tr>\n");
		}
		sb.append("</table>");
		
		return sb.toString();
	}

	void reset() {
		ascii_ta.setText("");
		fieldSeparator_lb.setSelectedIndex(0);
	}

	void example() {
		ascii_ta.setText(
				"name,description\n" +
				"sea surface salinity, salinity at the sea surface >10 m.\n" +
				"sst, sea surface temperature\n"
		);
		fieldSeparator_lb.setSelectedIndex(0);
	}
}
