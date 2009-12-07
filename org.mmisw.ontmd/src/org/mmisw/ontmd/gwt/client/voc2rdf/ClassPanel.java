package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.metadata.ResourceTypeWidget;
import org.mmisw.ontmd.gwt.client.portal.IVocabPanel;
import org.mmisw.ontmd.gwt.client.portal.Portal;
import org.mmisw.ontmd.gwt.client.util.FieldWithChoose;
import org.mmisw.ontmd.gwt.client.util.MyDialog;
import org.mmisw.ontmd.gwt.client.util.TLabel;

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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Form elements for the contents of the vocabulary.
 * 
 * @author Carlos Rueda
 */
public class ClassPanel extends VerticalPanel implements TermTableInterface {

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
	
	private TermTable termTable;
	
	private PushButton importCsvButton;
	private PushButton exportCsvButton;
	
	private IVocabPanel vocabPanel;
	
	public ClassPanel(IVocabPanel vocabPanel) {
		this.vocabPanel = vocabPanel;
		setWidth("1000");

		resourceTypeAttrDef = Voc2Rdf.baseInfo.getResourceTypeAttrDef();
		add(createForm());
		updateContents(CONTENTS_DEFAULT);
	}

	/**
	 * Creates the main form
	 */
	private Widget createForm() {
		contentsContainer.setBorderWidth(1);

		tableScroll.setSize("1000", "180");
		
		FlexTable flexPanel = new FlexTable();
//		flexPanel.setBorderWidth(1);
//		flexPanel.setWidth("850");
		flexPanel.setWidth("100%");
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
//						statusLabel.setText("");
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
		exPanel.add(_createCsvButtons());
		flexPanel.setWidget(row, 2, exPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_BOTTOM
		);
		row++;

		
		
		flexPanel.setWidget(row, 0, new TLabel("Terms:", true, "<b>Terms</b>:<br/>" +CONTENTS_TOOTIP));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

//		contentsContainer.setSize("600", "200");
		
		contentsContainer.add(tableScroll);
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 3);
		flexPanel.setWidget(row, 1, contentsContainer);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
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
	
	
	FieldWithChoose getFieldWithChoose() {
		return resourceTypeWidget.resourceTypeFieldWithChoose;
	}
	
	String getClassName() {
		String primaryClass = resourceTypeWidget.resourceTypeFieldWithChoose.getValue().trim();
		return primaryClass;
	}

	CheckError putValues(Map<String, String> values) {

		// NOTE 3/21/09: take class name from the resourcetype field
//		String primaryClass = classNameTb.getText().trim();
		String primaryClass = getClassName();
		if ( primaryClass.length() == 0 ) {
			return new CheckError("Please, select a class for the terms in your vocabulary");
		}
		
		String classUri = resourceTypeWidget.getRelatedValue().trim();
		if ( classUri.length() > 0 ) {
			values.put("classUri", classUri);
		}
		
		CheckError error = termTable.check();

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
		TermTable tt = TermTableCreator.createTermTable(this, ',', contents, false, errorMsg);
		
		if ( errorMsg.length() > 0 ) {
//			statusLabel.setHTML("<font color=\"red\">" +errorMsg+ "</font>");
			return;
		}
		
		// OK:
		termTable = tt;
		tableScroll.setWidget(termTable);
		termTable.setScrollPanel(tableScroll);
	}

	


	void reset() {
//		statusLabel.setText("");

		resourceTypeWidget.reset();
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
		
		private boolean firstColIsUri;


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
					incrTermTable =  new TermTable(ClassPanel.this, 1, false);
					preDone();
					return true;
				}
				
				List<String> headerCols = TermTableCreator.parseLine(lines[0], separator);
				numHeaderCols = headerCols.size();
				incrTermTable = new TermTable(ClassPanel.this, numHeaderCols, false);
				
				firstColIsUri = numHeaderCols > 0 && headerCols.get(0).equalsIgnoreCase("uri");
				
				// header:
				for ( int c = 0; c < numHeaderCols; c++ ) {
					String str = headerCols.get(c).trim();
					incrTermTable.setHeader(c, str);
				}		
				
				if ( lines.length  == 1 ) {
					preDone();
					return true;
				}
				
				// row = row in termTable:
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
			statusHtml.setHTML("<font color=\"blue\">Importing ... (" +
					(1+rowInTermTable)+ ")" + "</font>");
		}
		
		private void preDone() {
			updateStatus();
			preDone = true;
			termTable = incrTermTable;
		}
		
		private void done() {
			tableScroll.setWidget(termTable);
			termTable.setScrollPanel(tableScroll);

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
					if ( c == 0 && firstColIsUri && str.length() > 0 ) {
						String link = Portal.portalBaseInfo.getOntServiceUrl()+ "?form=html&uri=" +str;
						str = "<a target=\"_blank\" href=\"" +link+ "\">" +text+ "</a>";
						incrTermTable.setCell(rowInTermTable, c, str, true);
					}
					else {
						incrTermTable.setCell(rowInTermTable, c, str);
					}
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
		tableScroll.setWidget(statusHtml);
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

		resourceTypeWidget.setExample();

		updateContents(CONTENTS_EXAMPLE);
	}
	
	
	void enable(boolean enabled) {
		resourceTypeWidget.enable(enabled);
		importCsvButton.setEnabled(enabled);
		exportCsvButton.setEnabled(enabled);
	}

	public void dispatchTableMenu(int left, int top) {
		// TODO Auto-generated method stub
		
	}

}
