package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.MyDialog;
import org.mmisw.ontmd.gwt.client.ResourceTypeWidget;
import org.mmisw.ontmd.gwt.client.TLabel;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
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
//		"Column values should be separated by comma characters; empty fields are " +
//		"represented by two commas or tabs in a row. Each record (row) should be separated by a " +
//		"return or end of line character. " +
		"All term labels must be unique.";

	private static final String CONTENTS_DEFAULT = 
		"name,description\n" +
		" , \n"
		;

	private static final String CONTENTS_EXAMPLE = 
		"name,description,comment\n" +
		"sea surface salinity, sea water salinity, salinity at the sea surface (above 3m.)\n" +
		"sst, water temperature, temperature at the sea surface (above 3m.)\n" +
		"depth, measurement depth, derived from pressure\n"
		;

	private static final String INTRO = 
		"The class refers to the main theme associated with your vocabulary. " +
		"Each term is considered an instance of this class. " +
		"The terms are the 'words' (concepts, labels, unique IDs or code, or similar unique tags) of your vocabulary. " +
		"CLick the cells of the table for editing the contents. " +
		"The CSV button display the contents of the table in CSV format allowing direct editing on the text format. " 
//		"You can manually type in the contents of your vocabulary or paste it from " +
//		"your original text file. Use the check box at the bottom for a convenient " +
//		"(read-only) tabular view of the contents. Uncheck it to continue editing. " +
//		"The Example button will fill in the vocabulary contents with an example."
		;

	private CellPanel contentsContainer = new VerticalPanel();
	

	// resourceType
	private AttrDef resourceTypeAttrDef;
	private ResourceTypeWidget resourceTypeWidget;

	private ScrollPanel tableScroll = new ScrollPanel();
	
	private HTML statusLabel = new HTML();

	private TermTable termTable;
	
	private PushButton csvButton;
	

	
	ClassPanel(Voc2RdfMainPanel mainPanel) {
		setWidth("850");

		resourceTypeAttrDef = Voc2Rdf.baseInfo.getResourceTypeAttrDef();
		add(createForm());
		updateContents(CONTENTS_DEFAULT);
	}

	/**
	 * Creates the main form
	 */
	private Widget createForm() {
		contentsContainer.setBorderWidth(1);

		tableScroll.setSize("800", "180");
		
		FlexTable flexPanel = new FlexTable();
//		flexPanel.setBorderWidth(1);
		flexPanel.setWidth("850");
		int row = 0;
		
		// general information 
		HTML infoLabel = new HTML(INTRO);
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
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
//		row++;

		HorizontalPanel exPanel = new HorizontalPanel();
		exPanel.add(_createCsvButton());
		flexPanel.setWidget(row, 2, exPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_BOTTOM
		);
		row++;

		
		
		flexPanel.setWidget(row, 0, new TLabel("Terms:", true, "<b>Terms</b>:<br/>" +CONTENTS_TOOTIP));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		contentsContainer.setSize("600", "200");
		
		contentsContainer.add(tableScroll);
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 3);
		flexPanel.setWidget(row, 1, contentsContainer);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		
		return flexPanel;
	}
	
	private CellPanel _createCsvButton() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		csvButton = new PushButton("CSV", new ClickListener() {
			public void onClick(Widget sender) {
				importContents();
			}
		});
		csvButton.setTitle("Displays contents in CSV format");
		panel.add(csvButton);
		
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
		
		String error = termTable.check();

		if ( error != null ) {
			return error;
		}
			
		String csv = termTable.getCsv();
		values.put("ascii", csv);
		
		// always comma now.
		values.put("fieldSeparator", "csv"); 
//				fieldSeparator_lb.getValue(fieldSeparator_lb.getSelectedIndex()));
		
		
		values.put("primaryClass", primaryClass);
		
		return null;
	}

	
	/**
	 * 
	 * @param contents
	 */
	private void updateContents(String contents) {
		
		StringBuffer errorMsg = new StringBuffer();
		TermTable tt = createTermTable(',', contents, errorMsg);
		
		if ( errorMsg.length() > 0 ) {
			statusLabel.setHTML("<font color=\"red\">" +errorMsg+ "</font>");
			return;
		}
		
		// OK:
		termTable = tt;
		tableScroll.setWidget(termTable);
	}

	/**
	 * Return a MyTable
	 * 
	 * @param errorMsg Any error is reported here.
	 * @return
	 */
	private static TermTable createTermTable(char separator, String ascii, StringBuffer errorMsg) {
		assert ascii.length() > 0;

		String[] lines = ascii.split("\n|\r\n|\r");
		if ( lines.length == 0 || lines[0].trim().length() == 0 ) {
			errorMsg.append("Empty vocabulary contents");
			// A 1-column table to allow the user to insert columns (make column menu will be available)
			return new TermTable(1);
		}
		
		boolean error = false;
		
		List<String> headerCols = parseLine(lines[0], separator);
		final int numHeaderCols = headerCols.size();

		TermTable termTable = new TermTable(numHeaderCols);
		
		Main.log("termTable created");
		
		// header:
		
		// to check not repeated column headers
		Set<String> usedColHeader = new HashSet<String>();
		
		for ( int c = 0; c < numHeaderCols; c++ ) {
			String str = headerCols.get(c).trim();
			if ( str.length() == 0 ) {
				if ( !error ) {
					error = true;
					errorMsg.append("empty column header: " +(c+1));
				}
			}
			else if ( usedColHeader.contains(str) ) {
				if ( !error ) {
					error = true;
					errorMsg.append("repeated column header: " +str);
				}
			}
			else {
				usedColHeader.add(str);
			}
			termTable.setHeader(c, str);
		}		
		
		if ( lines.length  == 1 ) {
			if ( !error ) {
				error = true;
				errorMsg.append("Only a header line is included");
			}
			return termTable;
		}
		

		// to check not repeated values for first column:
		Set<String> usedFirstColValue = new HashSet<String>();

		
		// row = row in termTable:
		int row = -1;

		// remaining rows:
		for ( int r = 1; r < lines.length; r++ ) {
			
			List<String> cols = parseLine(lines[r], separator);
			final int numCols = cols.size();

			// skip empty line
			boolean empty = true;
			for ( int c = 0; empty && c < numCols; c++ ) {
				String str = cols.get(c).trim();
				if ( str.length() > 0 ) {
					empty = false;
				}
			}
			if ( empty ) {
				continue;
			}
			
			row++;
			termTable.addRow(numCols);
			for ( int c = 0; c < numCols; c++ ) {
				String str = cols.get(c).trim();
				
				if ( c == 0 ) {
					if ( str.length() == 0 ) {
						if ( !error ) {
							error = true;
							errorMsg.append("Empty key in first column, line " +r);
						}
					}
					
					else if ( usedFirstColValue.contains(str) ) {
						if ( !error ) {
							error = true;
							errorMsg.append("repeated key in first column: " +str+ ", line " +r);
						}
					}
					else {
						usedFirstColValue.add(str);
					}
				}
				
				if ( c < numHeaderCols ) {
					termTable.setCell(row, c, str);
				}
				else {
					// more columns than expected
					if ( !error ) {
						error = true;
						errorMsg.append("more columns than expected, line " +r);
					}
					termTable.setCell(row, c, str);
				}
			}
			
			// any missing columns? 
			if ( numCols < numHeaderCols ) {
				if ( !error ) {
					error = true;
					errorMsg.append("missing columns according to header, line " +r);
				}
				
				for ( int c = numCols; c < numHeaderCols; c++ ) {
					termTable.setCell(row, c, "");
				}
			}
			
		}
		
		return termTable;
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


	void reset() {
		updateContents(CONTENTS_DEFAULT);
	}

	/**
	 * Dispatches the "import" action.
	 */
	private void importContents() {
		final MyDialog popup = new MyDialog(null) {
			public boolean onKeyUpPreview(char key, int modifiers) {
				// avoid ENTER close the popup
				if ( key == KeyboardListener.KEY_ESCAPE  ) {
					hide();
					return false;
				}
				return true;
			}
		};
		popup.setText("Table of terms in CSV format");
		
		final TextArea textArea = popup.addTextArea(null);
		textArea.setReadOnly(false);
		textArea.setText(termTable.getCsv());
		
		textArea.setSize("700", "250");

		/////////////////////////////////////
		// NOTE: Only comma is handled
		/////////////////////////////////////
//		
//		/////////////////////////////////////////////////
//		CellPanel separatorPanel = new HorizontalPanel();
//		final ListBox fieldSeparator_lb = new ListBox();
//		fieldSeparator_lb.addItem("Comma", "csv");
//		fieldSeparator_lb.addItem("Tab", "tab");
//		separatorPanel.add(new Label("Column separator:"));
//		separatorPanel.add(fieldSeparator_lb);
//		////////////////////////////////////////////////
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		vp.add(new HTML(
				"This area can also be used to edit and/or insert new contents into " +
				"the table; click the \"Import\" button to update the table." 
				)
		);
//		vp.add(separatorPanel);
		
		
		final HTML status = new HTML("");
		textArea.addKeyboardListener(new KeyboardListenerAdapter(){
			  public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				  status.setText("");
			  }
		});

		PushButton importButton = new PushButton("Import", new ClickListener() {
			public void onClick(Widget sender) {
				String text = textArea.getText().trim();
				if ( text.length() == 0 ) {
					status.setHTML("<font color=\"red\">Empty contents</font>");
					return;
				}
				
//				String separatorName = fieldSeparator_lb.getValue(fieldSeparator_lb.getSelectedIndex());
//				char separator = "csv".equalsIgnoreCase(separatorName) ? ',' : '\t';
				char separator = ',';
				
				StringBuffer errorMsg = new StringBuffer();
				TermTable tt = createTermTable(separator, text, errorMsg);
				
				if ( errorMsg.length() > 0 ) {
					status.setHTML("<font color=\"red\">" +errorMsg+ "</font>");
					return;
				}
				
				// OK:
				if ( Window.confirm("This action will update the term table") ) {
					termTable = tt;
					tableScroll.setWidget(termTable);
					popup.hide();
				}
			}
		});
		
		popup.getButtonsPanel().insert(importButton, 0);
		popup.getButtonsPanel().insert(status, 0);
		
		popup.center();
		popup.show();

	}

	void example() {
		statusLabel.setText("");

		resourceTypeWidget.setExample();

		updateContents(CONTENTS_EXAMPLE);
	}
	
	
	void enable(boolean enabled) {
		csvButton.setEnabled(enabled);
	}

}
