package org.mmisw.voc2rdf.gwt.client;

import java.util.HashMap;
import java.util.Map;

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

	private MainPanel mainPanel;
	
	private Map<String, Widget> widgets = new HashMap<String, Widget>();
	
	private TextBox namespaceRoot_tb;
	
	private PushButton convertButton;
	
	private final Label msgLabel = new Label();
	private final TextArea textArea = new TextArea();
	
	private final CellPanel resultPanel = new VerticalPanel();
	
	ConversionPanel(MainPanel mainPanel) {
		super();
		setWidth("700");
		this.mainPanel = mainPanel;
		
		add(new HTML("This panel shows the generated RDF output. The generation will use both " +
				"the contents of the vocabulary and all the metadata explicitly provided. " +
				"If you are going to upload your vocabulary to the MMI Registry and Repository, " +
				"you are encouraged to use http://mmisw.org/ont as the root of the namespace."));
		
		add(createContents());
	}

	private Widget createContents() {
		FlexTable panel = new FlexTable();
		
		
		int row = 0;
		
		
		Widget form = createForm();
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, form);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
//		add(resultPanel);
		
		textArea.setReadOnly(true);
	    textArea.setSize("700px", "200px");
	    
//		DecoratorPanel decPanel = new DecoratorPanel();
//	    decPanel.setWidget(textArea);
//		panel.setWidget(row, 0, decPanel);
		
		panel.setWidget(row, 0, resultPanel);
		row++;
		
		return panel;
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
//		panel.setWidth("700");
		
		int row = 0;
		
		// NOTE: only one, which is namespaceRoot
		String[] attrNames =  { "namespaceRoot",  };
		String[] attrLabels = { "Namespace root:",  };
		String[] attrValues = { "http://mmisw.org/ont", };
		
		for ( int i = 0; i < attrNames.length; i++ ) {
			String attrName = attrNames[i];
			String attrLabel = attrLabels[i];
			String attrValue = attrValues[i];
			
			Widget widget;
			final TextBox tb = namespaceRoot_tb = new TextBox();
			tb.setName(attrName );
			tb.setText(attrValue );
			tb.setWidth("200");
			
			widget = tb;
				
			widgets.put(attrName, widget);
				
			Label lbl = new Label(attrLabel);
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
		
//		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
//		panel.setWidget(row, 0, buttons);
//		panel.getFlexCellFormatter().setAlignment(row, 0, 
//				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
//		row++;
		
		return panel;
	}

	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);

		convertButton = new PushButton("Convert to RDF", new ClickListener() {
			public void onClick(Widget sender) {
				String namespaceRoot = namespaceRoot_tb.getText().trim();
				if ( namespaceRoot.length() > 0 ) {
					msgLabel.setText("Converting");
					resultPanel.clear();
					resultPanel.add(new Label("Please, wait..."));
					mainPanel.convert(namespaceRoot);
				}
				else {
					updateContents(null);
					Window.alert("Namespace root not specified");
				}
			}
		});
		
		// TODO handle the enable in general
		convertButton.setEnabled(true);
		
		panel.add(convertButton);
		
		panel.add(msgLabel);

		panel.setCellVerticalAlignment(msgLabel, ALIGN_BOTTOM);

		return panel;
	}
	
	void updateContents(ConversionResult result) {
		if ( result == null ) {
			resultPanel.clear();
			msgLabel.setText("");
			textArea.setText("");
//			convertButton.setEnabled(false);
			return;
		}

		String error = result .getError();

		if ( error == null ) {
			msgLabel.setText("Congratulations");
			String rdf = result.getRdf();
			Main.log(rdf);
			textArea.setText(rdf);
//			ta.setSelectionRange(0, Integer.MAX_VALUE);
//			convertButton.setEnabled(false);
			
			resultPanel.clear();
			DecoratorPanel decPanel = new DecoratorPanel();
		    decPanel.setWidget(textArea);
		    resultPanel.add(decPanel);
			
		}
		else {
			msgLabel.setText("Error");
			textArea.setText(error);
//			convertButton.setEnabled(false);
			resultPanel.clear();
		    resultPanel.add(textArea);
		}
		
	}
	
}
