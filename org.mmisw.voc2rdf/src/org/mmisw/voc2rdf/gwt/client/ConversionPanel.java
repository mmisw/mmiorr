package org.mmisw.voc2rdf.gwt.client;

import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Contains the result of a conversion.
 * 
 * @author Carlos Rueda
 */
public class ConversionPanel extends VerticalPanel {

	private static final String CONVERT = "Convert to RDF";
	
	private static final String INTRO = 
		"Click the '" +CONVERT+ "' button to generate the RDF output. The generation will use both " +
		"the contents of the vocabulary and all the metadata explicitly provided.";

	// for now, not allowing the user to change the namespace root
	private static final boolean FIXED_NS_ROOT = true;
	
	private static final String DEFAULT_NS_ROOT = "http://mmisw.org/ont";
	
	private static final String NS_ROOT_TOOLTIP_1 = 
		"The namespace root used by the MMI Registry and Repository";
	
	private static final String NS_ROOT_TOOLTIP_2 = 
		"If you are going to upload your vocabulary to the MMI Registry and Repository, " +
		"you are encouraged to use " +DEFAULT_NS_ROOT+ " as the root of the namespace."
		;



	private MainPanel mainPanel;
	
	private TextBox namespaceRoot_tb;
	
	private PushButton convertButton;
	
	private final HTML msgLabel = new HTML();
	private final TextArea textArea = new TextArea();
	
	private final CellPanel resultPanel = new VerticalPanel();
	
	ConversionPanel(MainPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		
		add(createContents());
	}

	private Widget createContents() {
		FlexTable panel = new FlexTable();
		int row = 0;
		

		HTML intro = new HTML(INTRO);
		panel.getFlexCellFormatter().setWidth(row, 0, "700");
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, intro);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		CellPanel buttons = createButtons();
//		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		Widget form = createForm();
//		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 1, form);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		row++;
		
		textArea.setReadOnly(true);
	    textArea.setSize("800px", "350px");
	    
	    panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, resultPanel);
		row++;
		
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(textArea);
	    resultPanel.add(decPanel);

		
		return panel;
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
//		panel.setWidth("700");
		
		int row = 0;
		
		Label lbl = new Label("Namespace root:");
		namespaceRoot_tb = new TextBox();
		namespaceRoot_tb.setText(DEFAULT_NS_ROOT);
		namespaceRoot_tb.setWidth("160");
		
		if ( FIXED_NS_ROOT ) {
			lbl.setTitle(NS_ROOT_TOOLTIP_1);
			namespaceRoot_tb.setReadOnly(true);
			namespaceRoot_tb.setTitle(NS_ROOT_TOOLTIP_1);
		}
		else {
			lbl.setTitle(NS_ROOT_TOOLTIP_2);
			namespaceRoot_tb.setTitle(NS_ROOT_TOOLTIP_2);
		}
		
		panel.setWidget(row, 0, lbl);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);

			
		panel.setWidget(row, 1, namespaceRoot_tb);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
				
		if ( ! FIXED_NS_ROOT ) {
			PushButton useMmiButton = new PushButton(DEFAULT_NS_ROOT, new ClickListener() {
				public void onClick(Widget sender) {
					namespaceRoot_tb.setText(DEFAULT_NS_ROOT);
				}
			});
			useMmiButton.setTitle("Click here to use the MMI namespace root for your vocabulary ontology");
			panel.setWidget(row, 2, useMmiButton);
			panel.getFlexCellFormatter().setAlignment(row, 2, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
		}
		
		row++;


		return panel;
	}

	void prepareForConversion() {
		msgLabel.setHTML("<font color=\"blue\">Converting</font>");
		textArea.setText("");
//		resultPanel.clear();
//		resultPanel.add(new Label("Please, wait..."));	
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);

		convertButton = new PushButton(CONVERT, new ClickListener() {
			public void onClick(Widget sender) {
				String namespaceRoot = namespaceRoot_tb.getText().trim();
				if ( namespaceRoot.length() > 0 ) {
					prepareForConversion();
					mainPanel.convert(namespaceRoot);
				}
				else {
					updateContents(null);
					Window.alert("Namespace root not specified");
				}
			}
		});
		convertButton.setTitle("Performs the conversion using all the available information");
		
		// TODO handle the enable in general
		convertButton.setEnabled(true);
		
		panel.add(convertButton);
		
		panel.add(msgLabel);

		panel.setCellVerticalAlignment(msgLabel, ALIGN_BOTTOM);

		return panel;
	}
	
	void updateContents(ConversionResult result) {
		if ( result == null ) {
//			resultPanel.clear();
			msgLabel.setHTML("");
			textArea.setText("");
//			convertButton.setEnabled(false);
			return;
		}

		String error = result .getError();

		if ( error == null ) {
			msgLabel.setHTML("<font color=\"green\">Conversion completed</font>");
			String rdf = result.getRdf();
			Main.log(rdf);
			textArea.setText(rdf);
//			ta.setSelectionRange(0, Integer.MAX_VALUE);
//			convertButton.setEnabled(false);
			
//			resultPanel.clear();
//			DecoratorPanel decPanel = new DecoratorPanel();
//		    decPanel.setWidget(textArea);
//		    resultPanel.add(decPanel);
			
		}
		else {
			msgLabel.setHTML("<font color=\"green\">Error</font>");
			textArea.setText(error);
//			convertButton.setEnabled(false);
//			resultPanel.clear();
//		    resultPanel.add(textArea);
		}
		
	}
	
}
