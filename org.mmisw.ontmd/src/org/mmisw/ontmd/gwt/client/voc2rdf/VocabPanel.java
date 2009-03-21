package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.TLabel;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.Option;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
public class VocabPanel extends VerticalPanel {

	private static final String NAMESPACE_ROOT = "http://mmisw.org/ont";

	private static final String CLASS_TOOTIP =
		"The class for the terms defined in this vocabulary; should be a singular noun. " +
		"Each term is considered an example of this class; for example, if the selected class is Parameter, " +
		"each term is considered a Parameter. Terms from several controlled vocabularies are provided as " +
		"possible sources of the 'Class' for the ontology; if you want to select another term not in one " +
		"of these vocabularies, talk to MMI folks about how to make this change. " +
		"(It involves editing the resulting ontology.)";
		
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
	
	private ListBox primaryClass_lb;
	
	private TextArea ascii_ta = new TextArea();
	private ListBox fieldSeparator_lb;
	
	private final CheckBox tabular_cb = new CheckBox("Tabular view (check this for easier inspection)");
	
	private ScrollPanel table = new ScrollPanel();
	
	private HTML statusLabel = new HTML();
	private PushButton convertButton;


	private PushButton exampleButton;
	
	protected Voc2RdfMainPanel mainPanel;

	
	VocabPanel(Voc2RdfMainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setWidth("850");
		
//		add(new HTML("Use this panel to provide the contents of your vocabulary in text format"));
		add(createForm());
	}

	private Widget createForm() {
		contentsContainer.setBorderWidth(1);
		
		
		primaryClass_lb = new ListBox();
		primaryClass_lb.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				  statusLabel.setText("");
			}
		});

		AttrDef resourceTypeAttrDef = Voc2Rdf.baseInfo.getResourceTypeAttrDef();
		
		// TODO: need to handle dynamic refresh of options? (Main.refreshOptions)
		final List<Option> options = resourceTypeAttrDef.getOptions();

		primaryClass_lb.addItem("-- Select --");
		for ( Option option : options ) {
			String name = option.getName();
			String label = option.getLabel();
			primaryClass_lb.addItem(label, name);
		}

		ascii_ta.setSize("800", "300");
		table.setSize("800", "300");
		
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
		
		
		
		CellPanel buttons = createConvertButton();
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 0, buttons);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		flexPanel.setWidget(row, 0, new TLabel("Class:", "<b>Class</b>:<br/>" +CLASS_TOOTIP));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, primaryClass_lb);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;


		
		
		
		flexPanel.setWidget(row, 0, new TLabel("Terms:", "<b>Terms</b>:<br/>" +CONTENTS_TOOTIP));
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
		exPanel.add(createExampleButton());
		flexPanel.setWidget(row, 2, exPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		row++;

		return flexPanel;
	}
	
	private CellPanel createExampleButton() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		exampleButton = new PushButton("Example", new ClickListener() {
			public void onClick(Widget sender) {
				example(true);
			}
		});
		exampleButton.setTitle("Fills in example values in this section");
		panel.add(exampleButton);
		
		return panel;
	}
	
	private CellPanel createConvertButton() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		panel.add(statusLabel);
		convertButton = new PushButton("Convert to RDF", new ClickListener() {
			public void onClick(Widget sender) {
				convert2Rdf();
			}
		});
		convertButton.setTitle("Converts the current vocabulary contents into RDF format.");
		panel.add(convertButton);
		
		return panel;
	}
	

	String putValues(Map<String, String> values) {
		
		String primaryClass = primaryClass_lb.getValue(primaryClass_lb.getSelectedIndex());
		if ( primaryClass.startsWith("--") ) {
			return "Please, select a class for the terms in your vocabulary";
		}
		
		values.put("namespaceRoot", NAMESPACE_ROOT);

		String ascii = ascii_ta.getText().trim();
		if ( ascii.length() > 0 ) {
			String[] lines = ascii.split("\n|\r\n|\r");
			if ( lines.length  == 1 ) {
				return "Only a header line is included";
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
		char separator = "csv".equalsIgnoreCase(separatorName) ? ',' : '\t';
	
		StringBuffer sb = new StringBuffer();
		sb.append("<table class=\"inline\">");		

		// header:
		sb.append("<tr>\n");
		
		sb.append("<td align=\"right\"> <font color=\"gray\">" +0+ "</font> </td>");
		
//		if ( lines[0].indexOf('\"') >= 0 || lines[0].indexOf('\'') >= 0  ) {
//			sb.append("<td> <font color=\"red\">" +"Sorry, quotes not handled yet for the tabular view"+ "</font></td>");
//			sb.append("</tr></table>\n");
//			return sb.toString();
//		}
//
//		String[] headerCols = lines[0].split(separator);

		List<String> headerCols = parseLine(lines[0], separator);
		final int numHeaderCols = headerCols.size();
		for ( int c = 0; c < numHeaderCols; c++ ) {
			String str = headerCols.get(c);
			if ( str.trim().length() == 0 ) {
				str = "<font color=\"red\">" +"empty column header"+ "</font>";
			}
			sb.append("<th>" +str+ "</th>");	
		}		
		
		sb.append("</tr>\n");
		
		// remaining rows:
		for ( int r = 1; r < lines.length; r++ ) {
			sb.append("<tr>\n");
			sb.append("<td align=\"right\"> <font color=\"gray\">" +r+ "</font> </td>");
			
//			if ( lines[r].indexOf('\"') >= 0 || lines[r].indexOf('\'') >= 0  ) {
//				sb.append("<td colspan=\"" +headerCols.length+ "\"> " +
//						"<font color=\"red\">" +"Sorry, quotes not handled yet for the tabular view"+ "</font></td>");
//				continue;
//			}
//			String[] cols = lines[r].split(separator);

			List<String> cols = parseLine(lines[r], separator);
			final int numCols = cols.size();
			for ( int c = 0; c < numCols; c++ ) {
				String str = cols.get(c).trim();
				
//				if ( str.length() == 0 ) {
//					str = "<font color=\"red\">" +"?"+ "</font>";
//				}
				
				if ( c < numHeaderCols ) {
					sb.append("<td>" +str+ "</td>");
				}
				else {
					// more columns than expected
					sb.append("<td> <font color=\"red\">" +str+ "</font></td>");
				}
			}
			
			// any missing columns? 
			for ( int c = numCols; c < numHeaderCols; c++ ) {
//				sb.append("<td> <font color=\"red\">" +"?"+ "</font></td>");
				sb.append("<td></td>");
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

	void example(boolean confirm) {
		if ( confirm 
		&&  ascii_ta.getText().trim().length() > 0
		&&  ! Window.confirm("This action will replace the current values") ) {
			return;
		}
		
		statusLabel.setText("");

// Nonong's suggestion (remove "see also" column):
//		ascii_ta.setText(
//				"name,description,see also,comment\n" +
//				"sea surface salinity, sea water salinity, salinity at the sea surface (above 3m.), an example comment\n" +
//				"sst, water temperature, ocean temperature, temperature at the sea surface (above 3m.)\n" +
//				"depth, measurement depth, , derived from pressure\n"
//		);
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
	
	
	/**
	 * Runs the "test conversion" on the vocabulary contents and with
	 * ad hoc metadata atttributes.
	 */
	void convert2Rdf() {
		Map<String, String> values = new HashMap<String, String>();

		// error only possibly from the vocabPanel:
		String error;
		if ( (error = putValues(values)) != null ) {
			statusLabel.setHTML("<font color=\"red\">" + error+ "</font>");
			mainPanel.conversionError(error);
			return;
		}
		
		statusLabel.setHTML("<font color=\"blue\">" + "Converting ..." + "</font>");
		enable(false);
		mainPanel.converting();
		
		Main.log("convert2Rdf: values = " +values);

		// do test conversion
		AsyncCallback<ConversionResult> callback = new AsyncCallback<ConversionResult>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Main.log("convertTest: error: " +error);
				mainPanel.conversionError(error);
				statusLabel.setHTML("<font color=\"red\">" +"Error"+ "</font>");
				enable(true);
			}

			public void onSuccess(ConversionResult conversionResult) {
				String error = conversionResult.getError();
				if ( error != null ) {
					Main.log("convertTest: error: " +error);
					statusLabel.setHTML("<font color=\"red\">" +"Error"+ "</font>");
					mainPanel.conversionError(error);
				}
				else {
					Main.log("convertTest: OK: " +conversionResult.getRdf());
					statusLabel.setHTML("<font color=\"green\">" + "Conversion complete" + "</font>");
					mainPanel.conversionOk(conversionResult);
				}
				enable(true);
			}
		};

		Main.log("convertTest: converting ... ");
		
//		Voc2Rdf.voc2rdfService.convert(values, callback);
		Main.ontmdService.convert2Rdf(values, callback);

	}

	private void enable(boolean enabled) {
		convertButton.setEnabled(enabled);
		exampleButton.setEnabled(enabled);
		tabular_cb.setEnabled(enabled);
		fieldSeparator_lb.setEnabled(enabled);
	}

}
