package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.ClassInfo;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyOntologyData.ClassData;
import org.mmisw.ontmd.gwt.client.portal.BaseOntologyContentsPanel;
import org.mmisw.ontmd.gwt.client.portal.IVocabPanel;
import org.mmisw.ontmd.gwt.client.util.MyDialog;
import org.mmisw.ontmd.gwt.client.util.StatusPopup;
import org.mmisw.ontmd.gwt.client.util.TLabel;
import org.mmisw.ontmd.gwt.client.util.OrrUtil;
import org.mmisw.ontmd.gwt.client.util.table.IRow;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
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
public class VocabClassPanel extends BaseOntologyContentsPanel implements TermTableInterface {

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
//		"Click the cells of the table for editing the contents. " +
//		"The CSV button display the contents of the table in CSV format allowing direct editing on the text format. " 
//		;

	
	private VerticalPanel widget = new VerticalPanel();

	private CellPanel contentsContainer = new VerticalPanel();
	

	private TextBoxBase classNameTextBox;
	
	private final StatusPopup statusPopup = new StatusPopup("250px", false);


	private final boolean useTableScroll = false;
	
	private ScrollPanel tableScroll = useTableScroll ? new ScrollPanel() : null;
	
	private TermTable termTable;
	
	private PushButton importCsvButton;
	private PushButton exportCsvButton;
	
	private IVocabPanel vocabPanel;
	
	private final ClassData classData;
	
	private HorizontalPanel classNamePanel;
	
	
	public VocabClassPanel(ClassData classData, IVocabPanel vocabPanel, boolean readOnly) {
		super(readOnly);
		this.classData = classData;
		this.vocabPanel = vocabPanel;
		widget.setWidth("100%");

		widget.add(createForm());
		updateContents(CONTENTS_DEFAULT);
	}
	
	public void setReadOnly(boolean readOnly) {
		if ( isReadOnly() == readOnly ) {
			return;
		}
		super.setReadOnly(readOnly);
		prepareClassNamePanel(classNamePanel);
		termTable.setReadOnly(readOnly);
	}
	
	private String getClassNameFromClassData() {
		String classUri = classData.getClassUri();
		ClassInfo classInfo = classData.getClassInfo();
		String className;
		if ( classInfo != null ) {
			className = classInfo.getLocalName();
		}
		else {
			// should not happen; extract simple name for URI? For now, just use the whole URI:
			className = classUri; 
		}
		return className;
	}
	
	private HorizontalPanel createClassNamePanel() {
		HorizontalPanel classNamePanel = new HorizontalPanel();
		classNamePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		prepareClassNamePanel(classNamePanel);
		return classNamePanel;
	}
	
	private void prepareClassNamePanel(HorizontalPanel classNamePanel) {
		String label = "Class name";
		String tooltip = "<b>" +label+ "</b>:<br/>" +CLASS_TOOTIP;

		String className = getClassNameFromClassData();
		
		classNamePanel.clear();
		classNamePanel.add(new TLabel(label, !isReadOnly(), tooltip));
		
		if ( isReadOnly() ) {
			Label lbl = new Label(className);
			lbl.setTitle(classData.getClassUri());
			classNamePanel.add(lbl);
		}
		else {
			classNameTextBox = OrrUtil.createTextBoxBase(1, "300", null);
			classNameTextBox.setText(className);
			classNamePanel.add(classNameTextBox);
		}
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
		
		
		
		classNamePanel = createClassNamePanel();

		
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 0, classNamePanel);
//		flexPanel.setWidget(row, 0, new TLabel(label, true, tooltip));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		row++;

		
		if ( useTableScroll ) {
			contentsContainer.add(tableScroll);
		}
		
		
		if ( true ) {
			VerticalPanel vp = new VerticalPanel();
//			vp.setBorderWidth(1);
			vp.setWidth("100%");
			vp.add(flexPanel);
			vp.add(contentsContainer);

			return vp;
		}
		else {

			flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
			flexPanel.setWidget(row, 0, contentsContainer);
			flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;

			return flexPanel;
		}
		
	}
	
//	private CellPanel _createCsvButtons() {
//		CellPanel panel = new HorizontalPanel();
//		panel.setSpacing(2);
//		
//		importCsvButton = new PushButton("Import", new ClickListener() {
//			public void onClick(Widget sender) {
//				dispatchImportAction();
//			}
//		});
//		importCsvButton.setTitle("Import contents in CSV format");
//		panel.add(importCsvButton);
//		
//		exportCsvButton = new PushButton("Export", new ClickListener() {
//			public void onClick(Widget sender) {
//				exportContents();
//			}
//		});
//		exportCsvButton.setTitle("Exports the contents in a CSV format");
//		panel.add(exportCsvButton);
//		
//		return panel;
//	}
//	
	
	public void dispatchTableMenu(int left, int top) {

	    MenuBar menu = new MenuBar(true);
	    final PopupPanel menuPopup = new PopupPanel(true);
	    
	    menu.addItem(new MenuItem("Import...", new Command() {
			public void execute() {
				menuPopup.hide();
				dispatchImportAction();
			}
	    }));
	    menu.addItem(new MenuItem("Export...", new Command() {
			public void execute() {
				menuPopup.hide();
				exportContents();
			}
	    }));
	    
	    menuPopup.setWidget(menu);
	    
	    menuPopup.setPopupPosition(left, top);
		menuPopup.show();
	}

	
	
	String getClassName() {
		String primaryClass = classNameTextBox.getText().trim();
		return primaryClass;
	}

	public String checkData(boolean isNewVersion) {
		// isNewVersion: ignored -- param introduced later on

		String primaryClass = getClassName();
		if ( primaryClass.length() == 0 ) {
			return "Please, select a class for the terms in your vocabulary";
		}
		
		// TODO do we still want the classUri ?
//		String classUri = resourceTypeWidget.getRelatedValue().trim();
//		if ( classUri.length() > 0 ) {
//			values.put("classUri", classUri);
//		}
		
		CheckError error = termTable.check();

		if ( error != null ) {
			return error.toString();
		}
			
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

		if ( classNameTextBox != null ) {
			classNameTextBox.setText("");
		}
		updateContents(CONTENTS_DEFAULT);
	}

	
	private void insertTermTable() {
		if ( useTableScroll ) {
			tableScroll.setWidget(termTable);
			termTable.setScrollPanel(tableScroll);
		}
		else {
			contentsContainer.clear();
			contentsContainer.add(termTable);
		}
	}
	

	
	/**
	 * Incremental command to create the resulting table.
	 */
	private class ImportCommand implements IncrementalCommand {
		private static final int rowIncrement = 34;
		
		private char separator;
		private String text;

//		private TermTable incrTermTable;
		private boolean firstStep = true;

		private int numHeaderCols;

		private String[] lines;

		private int rowInTermTable = -1;
		
		private int currFromRow;
		
		private boolean preDone;


		ImportCommand(char separator, String text) {
			assert text.length() > 0;
			this.separator = separator;
			this.text = text;
		}


		public boolean execute() {
			if ( preDone ) {
				done();
				return false;
			}
				
			if ( firstStep ) { // if ( incrTermTable == null ) {
				firstStep = false;
				
				lines = text.split("\n|\r\n|\r");
				if ( lines.length == 0 || lines[0].trim().length() == 0 ) {
					// A 1-column table to allow the user to insert columns (make column menu will be available)
//					incrTermTable =  
					termTable = new TermTable(VocabClassPanel.this, 1, false);
					insertTermTable();
					preDone();
					return true;
				}
				
				List<String> headerCols = TermTableCreator.parseLine(lines[0], separator);
				numHeaderCols = headerCols.size();
//				incrTermTable = 
				termTable = new TermTable(VocabClassPanel.this, numHeaderCols, isReadOnly());
				
				// header:
				for ( int c = 0; c < numHeaderCols; c++ ) {
					String str = headerCols.get(c).trim();
//					incrTermTable.
					termTable.setHeader(c, str);
				}		
				insertTermTable();
				
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
			statusPopup.setStatus("Preparing ... (" +(1+rowInTermTable)+ ")");
		}
		
		private void preDone() {
			updateStatus();
			preDone = true;
//			termTable = incrTermTable;
		}

		private void done() {
//			insertTermTable();
			statusPopup.hide();
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
//				incrTermTable.
				termTable.addRow(numCols);
				for ( int c = 0; c < numCols; c++ ) {
					String str = cols.get(c).trim();
//					incrTermTable.
					termTable.setCell(rowInTermTable, c, str);
				}
				
				// any missing columns? 
				if ( numCols < numHeaderCols ) {
					for ( int c = numCols; c < numHeaderCols; c++ ) {
//						incrTermTable.
						termTable.setCell(rowInTermTable, c, "");
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
				
				importContents(separatorPanel.getSelectedSeparator().charAt(0), text);
				
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
	public void importContents(List<String> headerCols, List<IRow> rows) {
		IncrementalCommand incrCommand = createImportContentsCommand(headerCols, rows);
		
		DeferredCommand.addCommand(incrCommand);
	}
	
	
	private IncrementalCommand createImportContentsCommand(final List<String> headerCols, List<IRow> rows) {
		String statusMsg = "Starting ...";
		
		statusPopup.show(0, 0); // TODO locate statusPopup
		statusPopup.setStatus(statusMsg);

		int numHeaderCols = headerCols.size();
		termTable = new TermTable(this, numHeaderCols, isReadOnly());
		vocabPanel.statusPanelsetWaiting(true);
		vocabPanel.statusPanelsetHtml(statusMsg);
		vocabPanel.enable(false);
		
		// header:
		for ( int c = 0; c < numHeaderCols; c++ ) {
			String str = headerCols.get(c).trim();
//			incrTermTable.
			termTable.setHeader(c, str);
		}		
		insertTermTable();

		final IncrementalCommand incrCommand = new PopulateTermTableCommand(termTable, rows ) {
			
			@Override
			boolean start() {
				return true;
			}

			@Override
			void done() {
				statusPopup.hide();
				vocabPanel.statusPanelsetWaiting(false);
				vocabPanel.statusPanelsetHtml("");
				vocabPanel.enable(true);
			}

			@Override
			boolean updateStatus() {
				statusPopup.setStatus(termTable.getNumRows()+ "");
				return ! cancelRequested();
			}

		};
		
		return incrCommand;

	}
	

	
	
	/**
	 * Imports the given text into the term table.
	 * @param separator
	 * @param text
	 */
	public void importContents(char separator, String text) {
		String statusMsg = "Importing ...";
		
		statusPopup.show(0, 0); // TODO locate statusPopup
		statusPopup.setStatus(statusMsg);
		
		// if using tableScroll
//		tableScroll.setWidget(statusHtml);
		
		termTable = null;
		vocabPanel.statusPanelsetWaiting(true);
		vocabPanel.statusPanelsetHtml(statusMsg);
		vocabPanel.enable(false);

		final IncrementalCommand incrCommand = new ImportCommand(separator, text);
		
		// the timer is to give a chance for pending UI changes to be reflected (eg., a popup to disappear)
		new Timer() {
			public void run() {
				DeferredCommand.addCommand(incrCommand);
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
				textArea.setText(termTable.getCsv(null, getSelectedSeparator()));
			}
		};
		vp.add(separatorPanel);
		
		popup.center();
		popup.show();

	}
	
	/** Helper class to capture desired separator for CSV contents */
	private static class SeparatorPanel extends HorizontalPanel implements ClickListener {
		private String selectedOption;
		// option -> separator
		private Map<String,String> optionSeparatorMap= new HashMap<String,String>();
		
		SeparatorPanel() {
			super();
			this.add(new Label("Separator:"));
			String[] separators = { 
					// label,         separator
					"Comma (,)",        ",",
					"Semi-colon (;)",   ";", 
					"Tab",              "\t",
					"Vertical bar (|)", "|", 
			};
			for (int i = 0; i < separators.length; i += 2 ) {
				String label = separators[i];
				String separator = separators[i + 1];
				optionSeparatorMap.put(label, separator);
				RadioButton rb = new RadioButton("separator", label);
				if ( i == 0 ) {
					rb.setChecked(true);
					selectedOption = label;
				}
				rb.addClickListener(this);
				this.add(rb);
			}
		}
		public void onClick(Widget sender) {
			RadioButton rb = (RadioButton) sender;
			selectedOption = rb.getText();
		}
		
		String getSelectedSeparator() {
			return optionSeparatorMap.get(selectedOption);
		}
	}

	void example() {
//		statusLabel.setText("");

		if ( classNameTextBox != null ) {
			classNameTextBox.setText(CLASS_NAME_EXAMPLE);
		}

		updateContents(CONTENTS_EXAMPLE);
	}
	
	
	void enable(boolean enabled) {
		if ( classNameTextBox != null ) {
			classNameTextBox.setReadOnly(!enabled);
		}
		importCsvButton.setEnabled(enabled);
		exportCsvButton.setEnabled(enabled);
	}

	public Widget getWidget() {
		return widget;
	}

	public VocabularyDataCreationInfo getCreateOntologyInfo() {
		VocabularyDataCreationInfo cvi = new VocabularyDataCreationInfo();

		cvi.setClassName(getClassName());
		
		cvi.setColNames(termTable.getHeaderCols());
		
		cvi.setRows(termTable.getRows());
		
		
		return cvi;
	}

}
