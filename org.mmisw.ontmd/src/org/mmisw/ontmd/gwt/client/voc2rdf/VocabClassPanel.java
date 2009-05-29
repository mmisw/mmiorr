package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.List;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.portal.IVocabPanel;
import org.mmisw.ontmd.gwt.client.util.MyDialog;
import org.mmisw.ontmd.gwt.client.util.TLabel;
import org.mmisw.ontmd.gwt.client.util.Util;
import org.mmisw.ontmd.gwt.client.voc2rdf.VocabPanel.CheckError;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Timer;
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
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for showing/capturing the definition of a class of terms in a vocabulary.
 * 
 * <p>
 * Adapted (simplified) from ClassPanel
 * 
 * @author Carlos Rueda
 */
public class VocabClassPanel extends VerticalPanel {

	private static final String CLASS_TOOTIP =
		"The class for the terms defined in this vocabulary; should be a singular noun. " +
		"Each term is considered an instance of this class; for example, if the selected class is Parameter, " +
		"each term is considered a Parameter. Terms from several controlled vocabularies are provided as " +
		"possible sources of the 'Class' for the ontology; if you want to select another term not in one " +
		"of these vocabularies, talk to MMI folks about how to make this change. " +
		"(It involves editing the resulting ontology.)";
	
		// TODO do we still want the classUri?
//	private static final String CLASS_URI_TOOTIP =
//		"Ideally the class is selected from, and described in, a controlled vocabulary with URIs " +
//		"defined. If so, enter the URI naming the term in this field. " +
//		"If the term is in a controlled vocabulary but does not have its own URI, enter the " +
//		"controlled vocabulary URI. Otherwise, leave this field blank.";
		
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
		"All term labels must be unique. " +
		"<br/>" +
		"<br/>" +
		"Type Enter to edit a cell. Type Enter again to complete the change (or just move to a different " +
		"field in the form using the navigation keys or the mouse). " +
		"<br/>" +
		"<br/>" +
		"Use the Import button to set the contents of the table from CSV formatted text. <br/>" +
		"Use the Export button to get a text version of the current contents of the table. " 
		;

	private static final String CONTENTS_DEFAULT = 
		"name,description\n" +
		" , \n"
		;

	private static final String CLASS_NAME_EXAMPLE = "MyParameter";
	private static final String CONTENTS_EXAMPLE = 
		"name,description,comment\n" +
		"sea surface salinity, sea water salinity, salinity at the sea surface (above 3m.)\n" +
		"sst, water temperature, temperature at the sea surface (above 3m.)\n" +
		"depth, measurement depth, derived from pressure\n"
		;

//	private static final String INTRO = 
//		"The class refers to the main theme associated with your vocabulary. " +
//		"Each term is considered an instance of this class. " +
//		"The terms are the 'words' (concepts, labels, unique IDs or code, or similar unique tags) of your vocabulary. " +
//		"CLick the cells of the table for editing the contents. " +
//		"The CSV button display the contents of the table in CSV format allowing direct editing on the text format. " 
//		;


	private CellPanel contentsContainer = new VerticalPanel();
	

	private TextBoxBase classNameTextBox;

	private final boolean useTableScroll = false;
	
	private ScrollPanel tableScroll = useTableScroll ? new ScrollPanel() : null;
	
	private TermTable termTable;
	
	private PushButton importCsvButton;
	private PushButton exportCsvButton;
	
	private IVocabPanel vocabPanel;
	
	
	public VocabClassPanel(IVocabPanel vocabPanel) {
		this.vocabPanel = vocabPanel;
		setWidth("1000");

		add(createForm());
		updateContents(CONTENTS_DEFAULT);
	}

	/**
	 * Creates the main form
	 */
	private Widget createForm() {
//		contentsContainer.setBorderWidth(1);

		if ( useTableScroll ) {
			tableScroll.setSize("1000", "180");
		}
		
		FlexTable flexPanel = new FlexTable();
//		flexPanel.setBorderWidth(1);
//		flexPanel.setWidth("850");
		flexPanel.setWidth("100%");
		int row = 0;
		
		// general information 
//		HTML infoLabel = new HTML(INTRO);
//		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
//		flexPanel.setWidget(row, 0, infoLabel);
//		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
//				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
//		row++;
		
		
		
		ChangeListener cl = null;
		classNameTextBox = Util.createTextBoxBase(1, "200", cl );
		
		String label = "Class name";
		String tooltip = "<b>" +label+ "</b>:<br/>" +CLASS_TOOTIP;

		HorizontalPanel classNamePanel = new HorizontalPanel();
		classNamePanel.add(new TLabel(label, true, tooltip));
		classNamePanel.add(classNameTextBox);
		
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
		flexPanel.setWidget(row, 0, classNamePanel);
//		flexPanel.setWidget(row, 0, new TLabel(label, true, tooltip));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

//		flexPanel.setWidget(row, 1, classNameTextBox);
//		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
//				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
		row++;

		flexPanel.setWidget(row, 0, new TLabel("Terms:", true, "<b>Terms</b>:<br/>" +CONTENTS_TOOTIP));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		HorizontalPanel exPanel = new HorizontalPanel();
		exPanel.add(_createCsvButtons());
		flexPanel.setWidget(row, 2, exPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_BOTTOM
		);
		row++;

//		contentsContainer.setSize("600", "200");
		
		if ( useTableScroll ) {
			contentsContainer.add(tableScroll);
		}
		
		
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
		flexPanel.setWidget(row, 0, contentsContainer);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		
		return flexPanel;
	}
	
	private CellPanel _createCsvButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		importCsvButton = new PushButton("Import", new ClickListener() {
			public void onClick(Widget sender) {
				dispatchImportAction();
			}
		});
		importCsvButton.setTitle("Import contents in CSV format");
		panel.add(importCsvButton);
		
		exportCsvButton = new PushButton("Export", new ClickListener() {
			public void onClick(Widget sender) {
				exportContents();
			}
		});
		exportCsvButton.setTitle("Exports the contents in a CSV format");
		panel.add(exportCsvButton);
		
		return panel;
	}
	
	
	String getClassName() {
		String primaryClass = classNameTextBox.getText().trim();
		return primaryClass;
	}

	CheckError putValues(Map<String, String> values) {

		// NOTE 3/21/09: take class name from the resourcetype field
//		String primaryClass = classNameTb.getText().trim();
		String primaryClass = getClassName();
		if ( primaryClass.length() == 0 ) {
			return new CheckError("Please, select a class for the terms in your vocabulary");
		}
		
		// TODO do we still want the classUri ?
//		String classUri = resourceTypeWidget.getRelatedValue().trim();
//		if ( classUri.length() > 0 ) {
//			values.put("classUri", classUri);
//		}
		
		VocabPanel.CheckError error = termTable.check();

		if ( error != null ) {
			return error;
		}
			
		// NOTE: Need to put "" for the missing values so the original
		// voc2rdf scheme to make the conversion (which is based on the com.infomata.data library)
		// works with no ArrayOutOfBoundsException's.  
		String csv = termTable.getCsv("\"\"", ",");
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
		TermTable tt = TermTableCreator.createTermTable(',', contents, false, errorMsg);
		
		if ( errorMsg.length() > 0 ) {
//			statusLabel.setHTML("<font color=\"red\">" +errorMsg+ "</font>");
			return;
		}
		
		// OK:
		termTable = tt;
		
		if ( useTableScroll ) {
			tableScroll.setWidget(termTable);
			termTable.setScrollPanel(tableScroll);
		}
		else {
			contentsContainer.clear();
			contentsContainer.add(termTable);
		}
	}

	


	void reset() {
//		statusLabel.setText("");

		classNameTextBox.setText("");
		updateContents(CONTENTS_DEFAULT);
	}

	
	/**
	 * Incremental command to create the resulting table.
	 */
	private class ImportCommand implements IncrementalCommand {
		private static final int rowIncrement = 34;
		
		private char separator;
		private String text;

		private TermTable incrTermTable;

		private int numHeaderCols;

		private String[] lines;

		private int rowInTermTable = -1;
		
		private int currFromRow;
		
		private HTML statusHtml;
		
		private boolean preDone;


		ImportCommand(char separator, String text, HTML statusHtml) {
			assert text.length() > 0;
			this.separator = separator;
			this.text = text;
			this.statusHtml = statusHtml;
		}


		public boolean execute() {
			if ( preDone ) {
				done();
				return false;
			}
				
			if ( incrTermTable == null ) {
				// first step.
				
				lines = text.split("\n|\r\n|\r");
				if ( lines.length == 0 || lines[0].trim().length() == 0 ) {
					// A 1-column table to allow the user to insert columns (make column menu will be available)
					incrTermTable =  new TermTable(1, false);
					preDone();
					return true;
				}
				
				List<String> headerCols = TermTableCreator.parseLine(lines[0], separator);
				numHeaderCols = headerCols.size();
				incrTermTable = new TermTable(numHeaderCols, false);
				
				// header:
				for ( int c = 0; c < numHeaderCols; c++ ) {
					String str = headerCols.get(c).trim();
					incrTermTable.setHeader(c, str);
				}		
				
				if ( lines.length  == 1 ) {
					preDone();
					return true;
				}
				
				rowInTermTable = -1;

				currFromRow = 1;
			}

			// add a chunk of rows:
			if ( _addRows(currFromRow, currFromRow + rowIncrement) ) {
				preDone();
			}
			else {
				updateStatus();
				currFromRow += rowIncrement;
			}
			return true;
		}
		
		private void updateStatus() {
			statusHtml.setHTML("<font color=\"blue\">Preparing ... (" +
					(1+rowInTermTable)+ ")" + "</font>");
		}
		
		private void preDone() {
			updateStatus();
			preDone = true;
			termTable = incrTermTable;
		}
		
		private void done() {
			if ( useTableScroll ) {
				tableScroll.setWidget(termTable);
				termTable.setScrollPanel(tableScroll);
			}
			else {
				contentsContainer.clear();
				contentsContainer.add(termTable);
			}

			vocabPanel.statusPanelsetWaiting(false);
			vocabPanel.statusPanelsetHtml("");
			vocabPanel.enable(true);
		}
		
		
		private boolean _addRows(int fromRow, int toRow) {
			int rr = fromRow;
			for ( ; rr < lines.length && rr < toRow; rr++ ) {
				
				List<String> cols = TermTableCreator.parseLine(lines[rr], separator);
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
				
				rowInTermTable++;
				incrTermTable.addRow(numCols);
				for ( int c = 0; c < numCols; c++ ) {
					String str = cols.get(c).trim();
					incrTermTable.setCell(rowInTermTable, c, str);
				}
				
				// any missing columns? 
				if ( numCols < numHeaderCols ) {
					for ( int c = numCols; c < numHeaderCols; c++ ) {
						incrTermTable.setCell(rowInTermTable, c, "");
					}
				}
			}
			
			return rr >= lines.length;   // DONE
		}
	}

	private void dispatchImportAction() {
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
		popup.setText("Import terms");
		
		final TextArea textArea = popup.addTextArea(null);
		textArea.setReadOnly(false);
		
		textArea.setSize("800", "270");

		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		vp.add(new HTML(
				"Select the separator character, insert the new contents into the text area, " +
				"and click the \"Import\" button to update the table." 
				)
		);
		
		final SeparatorPanel separatorPanel = new SeparatorPanel();
		vp.add(separatorPanel);
		
		
		final HTML status = new HTML("");
		textArea.addKeyboardListener(new KeyboardListenerAdapter(){
			  public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				  status.setText("");
			  }
		});

		PushButton importButton = new PushButton("Import", new ClickListener() {
			public void onClick(Widget sender) {
				final String text = textArea.getText().trim();
				if ( text.length() == 0 ) {
					status.setHTML("<font color=\"red\">Empty contents</font>");
					return;
				}
				
				if ( ! Window.confirm("This action will update the term table. (Previous contents will be discarded.)") ) {
					return;
				}
					
				popup.hide();
				
				importContents(separatorPanel.separator.charAt(0), text);
				
			}
		});
		
		popup.getButtonsPanel().insert(importButton, 0);
		popup.getButtonsPanel().insert(status, 0);
		
		popup.center();
		popup.show();

	}
	
	/**
	 * Imports the given text into the term table.
	 * @param separator
	 * @param text
	 */
	public void importContents(char separator, String text) {
		String importingHtml = "<font color=\"blue\">" + "Importing ..." + "</font>";
		
		HTML statusHtml = new HTML(importingHtml);
		
		// if using tableScroll
//		tableScroll.setWidget(statusHtml);
		
		contentsContainer.add(statusHtml);
		
		termTable = null;
		vocabPanel.statusPanelsetWaiting(true);
		vocabPanel.statusPanelsetHtml(importingHtml);
		vocabPanel.enable(false);

		final ImportCommand importCommand = new ImportCommand(separator, text, statusHtml);
		
		// the timer is to give a chance for pending UI changes to be reflected (eg., a popup to disappear)
		new Timer() {
			public void run() {
				DeferredCommand.addCommand(importCommand);
			}
		}.schedule(1000);		

	}
	
	/**
	 * Dispatches the "export" action.
	 */
	private void exportContents() {
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
		textArea.setReadOnly(true);
		textArea.setText(termTable.getCsv(null, ","));
		
		textArea.setSize("800", "270");

		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		vp.add(new HTML(
				"Select the separator character for your CSV formatted contents. " 
				)
		);
		
		CellPanel separatorPanel = new SeparatorPanel() {
			public void onClick(Widget sender) {
				super.onClick(sender);
				textArea.setText(termTable.getCsv(null, separator));
			}
		};
		vp.add(separatorPanel);
		
		popup.center();
		popup.show();

	}
	
	/** Helper class to capture desired separator for CSV contents */
	private static class SeparatorPanel extends HorizontalPanel implements ClickListener {
		String separator = ",";
		
		SeparatorPanel() {
			super();
			String[] separators = { "Comma (,)", "Semi-colon (;)", "Tab", "Vertical bar (|)" };
			for (int i = 0; i< separators.length; i++ ) {
				String separator = separators[i];
				RadioButton rb = new RadioButton("separator", separator);
				if ( i == 0 ) {
					rb.setChecked(true);
				}
				rb.addClickListener(this);
				this.add(rb);
			}
		}
		public void onClick(Widget sender) {
			RadioButton rb = (RadioButton) sender;
			separator = rb.getText();
		}
	}

	void example() {
//		statusLabel.setText("");

		classNameTextBox.setText(CLASS_NAME_EXAMPLE);

		updateContents(CONTENTS_EXAMPLE);
	}
	
	
	void enable(boolean enabled) {
		classNameTextBox.setReadOnly(!enabled);
		importCsvButton.setEnabled(enabled);
		exportCsvButton.setEnabled(enabled);
	}

}
