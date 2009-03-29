package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.ontmd.gwt.client.ResourceTypeWidget;
import org.mmisw.ontmd.gwt.client.TLabel;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
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
public class ClassPanel extends VerticalPanel {

	private static final String CLASS_TOOTIP =
		"The class for the terms defined in this vocabulary; should be a singular noun. " +
		"Each term is considered an instance of this class; for example, if the selected class is Parameter, " +
		"each term is considered a Parameter. Terms from several controlled vocabularies are provided as " +
		"possible sources of the 'Class' for the ontology; if you want to select another term not in one " +
		"of these vocabularies, talk to MMI folks about how to make this change. " +
		"(It involves editing the resulting ontology.)";
	
	private static final String CLASS_URI_TOOTIP =
		"Ideally the class is selected from, and described in, a controlled vocabulary with URIs " +
		"defined. If so, enter the URI naming the term in this field. " +
		"If the term is in a controlled vocabulary but does not have its own URI, enter the " +
		"controlled vocabulary URI. Otherwise, leave this field blank.";
		
	private static final String CONTENTS_TOOTIP =
		"The 'words' (concepts, labels, unique IDs or code, or similar unique tags) of your vocabulary. " +
		"The contents should contain a one line header with the descriptive titles for each column. " +
		"Each line (row) should contain the unique label for each term followed by a set of values, " +
		"corresponding to the header descriptive titles. The first column should contain the unique " +
		"label for each term. It will be used to create a unique identifier. (Typical column titles " +
		"include Description, Notes, See Also, or others -- these all add information to help " +
		"describe your terms. These are treated as annotations in the ontology.) " +
		"Column values should be separated either by comma or tab characters; empty fields are " +
		"represented by two commas or tabs in a row. Each record (row) should be separated by a " +
		"return or end of line character. All term labels must be unique.";


	private CellPanel contentsContainer = new VerticalPanel();
	

	// resourceType
	private AttrDef resourceTypeAttrDef;
	private ResourceTypeWidget resourceTypeWidget;

	private TextArea ascii_ta = new TextArea();
	private ListBox fieldSeparator_lb;
	
	private final CheckBox tabular_cb = new CheckBox("Tabular view (check this for easier inspection)");
	
	private ScrollPanel table = new ScrollPanel();
	
	private HTML statusLabel = new HTML();


	private PushButton importButton;
	

	
	ClassPanel(Voc2RdfMainPanel mainPanel) {
		setWidth("850");

		resourceTypeAttrDef = Voc2Rdf.baseInfo.getResourceTypeAttrDef();
		add(createForm());
	}

	/**
	 * Creates the main form
	 */
	private Widget createForm() {
		contentsContainer.setBorderWidth(1);

		ascii_ta.setSize("800", "200");
		table.setSize("800", "200");
		
		ascii_ta.addKeyboardListener(new KeyboardListenerAdapter(){
			  public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				  statusLabel.setText("");
			  }
		});
		
		FlexTable flexPanel = new FlexTable();
//		flexPanel.setBorderWidth(1);
		flexPanel.setWidth("850");
		int row = 0;
		
		// general information 
		HTML infoLabel = new HTML("The class refers to the main theme associated with your vocabulary. " +
				"You can manually type in the contents of your vocabulary or paste it from " +
				"your original text file. Use the check box at the bottom for a convenient " +
				"(read-only) tabular view of the contents. Uncheck it to continue " +
				"editing. The Example button will fill in the vocabulary contents with an " +
				"example."
		);
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 0, infoLabel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		
		// NOTE 3/21/09: use my CLASS_TOOLTIP instead of the original "resourceType" one
		resourceTypeAttrDef.setTooltip(CLASS_TOOTIP);
		resourceTypeAttrDef.setLabel("Class");
		
		resourceTypeAttrDef.getRelatedAttrs().get(0).setTooltip(CLASS_URI_TOOTIP);
		resourceTypeAttrDef.getRelatedAttrs().get(0).setLabel("URI of class");
		
		
		boolean editing = true;
		resourceTypeWidget = new ResourceTypeWidget(resourceTypeAttrDef, editing, 
				new ChangeListener () {
					public void onChange(Widget sender) {
						statusLabel.setText("");
					}
				}
		);
		
		String label = resourceTypeAttrDef.getLabel();
		String tooltip = "<b>" +label+ "</b>:<br/>" + 
		                  resourceTypeAttrDef.getTooltip() +
		                  "<br/><br/><div align=\"right\">(" +resourceTypeAttrDef.getUri()+ ")</div>";
		flexPanel.setWidget(row, 0, new TLabel(label, true, tooltip));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, resourceTypeWidget);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		
		
		
		flexPanel.setWidget(row, 0, new TLabel("Terms:", true, "<b>Terms</b>:<br/>" +CONTENTS_TOOTIP));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		contentsContainer.setSize("600", "200");
		
		contentsContainer.add(ascii_ta);
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, contentsContainer);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		CellPanel hp = new HorizontalPanel();
		fieldSeparator_lb = new ListBox();
		fieldSeparator_lb.addItem("Comma", "csv");
		fieldSeparator_lb.addItem("Tab", "tab");
		hp.add(new Label("Column separator:"));
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
		
			
//		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, hp);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		
		HorizontalPanel exPanel = new HorizontalPanel();
		exPanel.add(createImportButton());
		flexPanel.setWidget(row, 2, exPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		row++;

		return flexPanel;
	}
	
	private CellPanel createImportButton() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		importButton = new PushButton("Import", new ClickListener() {
			public void onClick(Widget sender) {
				importContents(true);
			}
		});
		importButton.setTitle("Imports CSV contents into the table");
		panel.add(importButton);
		
		return panel;
	}
	


	String putValues(Map<String, String> values) {

		// NOTE 3/21/09: take class name from the resourcetype field
//		String primaryClass = classNameTb.getText().trim();
		String primaryClass = resourceTypeWidget.resourceTypeFieldWithChoose.getValue().trim();
		if ( primaryClass.length() == 0 ) {
			return "Please, select a class for the terms in your vocabulary";
		}
		
		String classUri = resourceTypeWidget.getRelatedValue().trim();
		if ( classUri.length() > 0 ) {
			values.put("classUri", classUri);
		}
		
		String ascii = ascii_ta.getText().trim();
		if ( ascii.length() > 0 ) {
			StringBuffer errorMsg = new StringBuffer();
			updateTabularView(errorMsg);
			if ( errorMsg.length() > 0 ) {
				return errorMsg.toString();
			}
			
			values.put("ascii", ascii_ta.getText());
		}
		else {
			return "Empty vocabulary contents";
		}
		
		values.put("fieldSeparator", fieldSeparator_lb.getValue(fieldSeparator_lb.getSelectedIndex()));
		
		
		values.put("primaryClass", primaryClass);
		
		return null;
	}

	
	
	
	private void updateContents(CellPanel container, boolean tabular) {

		contentsContainer.clear();
		if ( tabular ) {
			Widget widget = null;
			StringBuffer errorMsg = new StringBuffer();
			if ( false ) {
				String html = updateTabularView(errorMsg);
				widget = new HTML(html);
			}
			else {
				widget = updateMyTable(errorMsg);
			}
			table.setWidget(widget);
			contentsContainer.add(table);
		}
		else {
			contentsContainer.add(ascii_ta);
		}
		
	}

	/**
	 * Return a MyTable
	 * 
	 * @param errorMsg Any error is reported here.
	 * @return
	 */
	private Widget updateMyTable(StringBuffer errorMsg) {
		String ascii = ascii_ta.getText().trim();
		assert ascii.length() > 0;

		TermTable termTable = new TermTable();
		
		String[] lines = ascii.split("\n|\r\n|\r");
		if ( lines.length == 0 || lines[0].trim().length() == 0 ) {
			errorMsg.append("Empty vocabulary contents");
			return null;
		}
		
		if ( lines.length  == 1 ) {
			errorMsg.append("Only a header line is included");
			return null;
		}
		
		boolean error = false;
		
		String separatorName = fieldSeparator_lb.getValue(fieldSeparator_lb.getSelectedIndex());
		char separator = "csv".equalsIgnoreCase(separatorName) ? ',' : '\t';
	
		// header:
		termTable.setHtml(0, 0, ""); // "<font color=\"gray\">" +0+ "</font>");
		
		// to check not repeated column headers
		Set<String> usedColHeader = new HashSet<String>();
		
		List<String> headerCols = parseLine(lines[0], separator);
		final int numHeaderCols = headerCols.size();
		for ( int c = 0; c < numHeaderCols; c++ ) {
			String str = headerCols.get(c).trim();
			if ( str.length() == 0 ) {
				if ( !error ) {
					error = true;
					errorMsg.append("empty column header: " +(c+1));
				}
				str = "<font color=\"red\">" +"empty column header"+ "</font>";
			}
			else if ( usedColHeader.contains(str) ) {
				if ( !error ) {
					error = true;
					errorMsg.append("repeated column header: " +str);
				}
				str = str+ "<font color=\"red\">" +"(repeated)"+ "</font>";
			}
			else {
				usedColHeader.add(str);
			}
			termTable.setHtml(0, c + 1, str);
		}		
		
		
		// to check not repeated values for first column:
		Set<String> usedFirstColValue = new HashSet<String>();
		

		// remaining rows:
		for ( int r = 1; r < lines.length; r++ ) {
			
			termTable.setHtml(r, 0, "<font color=\"gray\">" +r+ "</font>");
			
			List<String> cols = parseLine(lines[r], separator);
			final int numCols = cols.size();
			for ( int c = 0; c < numCols; c++ ) {
				String str = cols.get(c).trim();
				
//				if ( str.length() == 0 ) {
//					str = "<font color=\"red\">" +"?"+ "</font>";
//				}
				
				if ( c == 0 ) {
					if ( str.length() == 0 ) {
						if ( !error ) {
							error = true;
							errorMsg.append("Empty key in first column, line " +r);
						}
						str = str+ "<font color=\"red\">" +"(empty)"+ "</font>";
					}
					
					else if ( usedFirstColValue.contains(str) ) {
						if ( !error ) {
							error = true;
							errorMsg.append("repeated key in first column: " +str+ ", line " +r);
						}
						str = str+ "<font color=\"red\">" +"(repeated)"+ "</font>";
					}
					else {
						usedFirstColValue.add(str);
					}
				}
				
				if ( c < numHeaderCols ) {
					termTable.setHtml(r, c + 1, str);
				}
				else {
					// more columns than expected
					if ( !error ) {
						error = true;
						errorMsg.append("more columns than expected, line " +r);
					}
					termTable.setHtml(r, c + 1, "<font color=\"red\">" +str+ "</font>");
				}
			}
			
			// any missing columns? 
			if ( numCols < numHeaderCols ) {
				if ( !error ) {
					error = true;
					errorMsg.append("missing columns according to header, line " +r);
				}
				
				for ( int c = numCols; c < numHeaderCols; c++ ) {
					termTable.setHtml(r, c + 1, "<font color=\"red\">" +"?"+ "</font>");
				}
			}
			
		}
		
		termTable.prepareStyles();
		return termTable;
	}
	

	
	
	
	/**
	 * Returns the HTML for the tabular view.
	 * 
	 * @param errorMsg Any error is reported here.
	 * @return
	 */
	private String updateTabularView(StringBuffer errorMsg) {
		String ascii = ascii_ta.getText().trim();
		assert ascii.length() > 0;

		String[] lines = ascii.split("\n|\r\n|\r");
		if ( lines.length == 0 || lines[0].trim().length() == 0 ) {
			errorMsg.append("Empty vocabulary contents");
			return "";
		}
		
		if ( lines.length  == 1 ) {
			errorMsg.append("Only a header line is included");
			return "";
		}
		
		boolean error = false;
		
		String separatorName = fieldSeparator_lb.getValue(fieldSeparator_lb.getSelectedIndex());
		char separator = "csv".equalsIgnoreCase(separatorName) ? ',' : '\t';
	
		StringBuffer sb = new StringBuffer();
		sb.append("<table class=\"inline\">");		

		// header:
		sb.append("<tr>\n");
		
		sb.append("<td align=\"right\"> <font color=\"gray\">" +0+ "</font> </td>");
		
		// to check not repeated column headers
		Set<String> usedColHeader = new HashSet<String>();
		
		List<String> headerCols = parseLine(lines[0], separator);
		final int numHeaderCols = headerCols.size();
		for ( int c = 0; c < numHeaderCols; c++ ) {
			String str = headerCols.get(c).trim();
			if ( str.length() == 0 ) {
				if ( !error ) {
					error = true;
					errorMsg.append("empty column header: " +(c+1));
				}
				str = "<font color=\"red\">" +"empty column header"+ "</font>";
			}
			else if ( usedColHeader.contains(str) ) {
				if ( !error ) {
					error = true;
					errorMsg.append("repeated column header: " +str);
				}
				str = str+ "<font color=\"red\">" +"(repeated)"+ "</font>";
			}
			else {
				usedColHeader.add(str);
			}
			sb.append("<th>" +str+ "</th>");	
		}		
		
		sb.append("</tr>\n");
		
		
		// to check not repeated values for first column:
		Set<String> usedFirstColValue = new HashSet<String>();
		

		// remaining rows:
		for ( int r = 1; r < lines.length; r++ ) {
			sb.append("<tr>\n");
			sb.append("<td align=\"right\"> <font color=\"gray\">" +r+ "</font> </td>");
			
			List<String> cols = parseLine(lines[r], separator);
			final int numCols = cols.size();
			for ( int c = 0; c < numCols; c++ ) {
				String str = cols.get(c).trim();
				
//				if ( str.length() == 0 ) {
//					str = "<font color=\"red\">" +"?"+ "</font>";
//				}
				
				if ( c == 0 ) {
					if ( str.length() == 0 ) {
						if ( !error ) {
							error = true;
							errorMsg.append("Empty key in first column, line " +r);
						}
						str = str+ "<font color=\"red\">" +"(empty)"+ "</font>";
					}
					
					else if ( usedFirstColValue.contains(str) ) {
						if ( !error ) {
							error = true;
							errorMsg.append("repeated key in first column: " +str+ ", line " +r);
						}
						str = str+ "<font color=\"red\">" +"(repeated)"+ "</font>";
					}
					else {
						usedFirstColValue.add(str);
					}
				}
				
				if ( c < numHeaderCols ) {
					sb.append("<td>" +str+ "</td>");
				}
				else {
					// more columns than expected
					if ( !error ) {
						error = true;
						errorMsg.append("more columns than expected, line " +r);
					}
					sb.append("<td> <font color=\"red\">" +str+ "</font></td>");
				}
			}
			
			// any missing columns? 
			if ( numCols < numHeaderCols ) {
				if ( !error ) {
					error = true;
					errorMsg.append("missing columns according to header, line " +r);
				}
				
				for ( int c = numCols; c < numHeaderCols; c++ ) {
//					sb.append("<td> <font color=\"red\">" +"?"+ "</font></td>");
					sb.append("<td></td>");
				}
			}
			
			sb.append("</tr>\n");
		}
		sb.append("</table>");
		
		return sb.toString();
	}
	
	/**
	 * Parses the line using the given separator and respecting quoted strings, 
	 * which are, however, returned without the quotes (the only handled quoted is the
	 * double quote (")).
	 * 
	 * <p>
	 * Note that the removal of quotes step is very simplistic (no nested quoted
	 * substring or escaped quotes handling is performed).
	 * <br/>Examples: 
	 * <table border=1>
	 *   <tr> <th>input</th> <th>output</th> </tr> 
	 *   <tr> <td>string with no quotes</td> <td>string with no quotes</td> </tr>
	 *   <tr> <td>"a quoted string"</td> <td>a quoted string</td> </tr>
	 *   <tr> <td>"hello "world""</td> <td>"hello "world""</td> </tr>
	 *   <tr> <td>"unbalanced string</td> <td>"unbalanced string</td> </tr>
	 * </table>
	 */
	private static List<String> parseLine(String line, char separator) {
		List<String> toks = new ArrayList<String>();
		
		// contents of current token under analysis:
		StringBuffer currTok = new StringBuffer();
		
		boolean inQuote = false;
		
		for ( int i = 0; i < line.length(); i++ ) {
			char chr = line.charAt(i);
			
			if ( chr == '"' ) {
				inQuote = !inQuote; 
				currTok.append(chr);
			}
			else if ( chr == separator ) {
				if ( inQuote ) {
					currTok.append(chr);
				}
				else {
					// token completed.
					toks.add(removeMatchingQuotes(currTok.toString()));
					currTok.setLength(0);
				}
			}
			else {
				currTok.append(chr);
			}
		}
		
		// pending token?
		if ( currTok.length() > 0 ) {
			toks.add(removeMatchingQuotes(currTok.toString()));
		}

		return toks;
	}

	/**
	 * Removes the surrounding quotes in the string if they are the only ones, ie., the 
	 * string does not have any other internal quotes. Otherwise, the string is returned
	 * without modification. More details: {@link #parseLine(String, char)}.
	 */
	private static String removeMatchingQuotes(String str) {
		String chkStr = str.trim();
		if ( chkStr.startsWith("\"") && chkStr.endsWith("\"") ) {
			chkStr = chkStr.substring(1, chkStr.length() -1);
			if ( chkStr.indexOf('"') < 0 ) {
				return chkStr;
			}
		}
		return str;
	}


	void reset(boolean confirm) {
		if ( confirm
		&&  ascii_ta.getText().trim().length() > 0
		&&  ! Window.confirm("This action will replace the current values") ) {
			return;
		}
		ascii_ta.setText("");
		fieldSeparator_lb.setSelectedIndex(0);
		tabular_cb.setChecked(false);
		updateContents(contentsContainer, false);
	}

	private void importContents(boolean confirm) {
		if ( confirm 
		&&  ascii_ta.getText().trim().length() > 0
		&&  ! Window.confirm("This action will replace the current table contents") ) {
			return;
		}

		// TODO
	}

	void example(boolean confirm) {
		if ( confirm 
		&&  ascii_ta.getText().trim().length() > 0
		&&  ! Window.confirm("This action will replace the current values") ) {
			return;
		}
		
		statusLabel.setText("");

		resourceTypeWidget.setExample();
		ascii_ta.setText(
				"name,description,comment\n" +
				"sea surface salinity, sea water salinity, salinity at the sea surface (above 3m.)\n" +
				"sst, water temperature, temperature at the sea surface (above 3m.)\n" +
				"depth, measurement depth, derived from pressure\n"
		);

		fieldSeparator_lb.setSelectedIndex(0);
		boolean tabular = tabular_cb.isChecked();
		if ( tabular ) {
			updateContents(contentsContainer, tabular);
		}
	}
	
	
	void enable(boolean enabled) {
		importButton.setEnabled(enabled);
		tabular_cb.setEnabled(enabled);
		fieldSeparator_lb.setEnabled(enabled);
	}

}
