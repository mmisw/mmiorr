package org.mmisw.voc2rdf.gwt.client;

import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ResultPanel extends VerticalPanel {

	private MainPanel mainPanel;
	private ConversionResult result;
	
	
	ResultPanel(MainPanel mainPanel, ConversionResult result) {
		super();
		this.mainPanel = mainPanel;
		this.result = result;
		
		add(createContents());
	}

	private Widget createContents() {
		FlexTable panel = new FlexTable();
		
		String error = result.getError();
		
		int row = 0;
		panel.setWidget(row++, 0, new Label(error == null ? "Congratulations" : "Error"));
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(0, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		TextArea ta = new TextArea();
		ta.setReadOnly(true);
		if ( error == null ) {
			String rdf = result.getRdf();
			Main.log(rdf);
			ta.setText(rdf);
//			ta.setSelectionRange(0, Integer.MAX_VALUE);
		}
		else {
			ta.setText(error);
		}
	    ta.setSize("700px", "280px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(ta);
	    add(decPanel);
		panel.setWidget(row++, 0, decPanel);
		row++;
		
		return panel;
	}

	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);

		String error = result.getError();
		
		if ( error == null ) {
			PushButton uploadButton = new PushButton("Upload to MMI Registry", new ClickListener() {
				public void onClick(Widget sender) {
					mainPanel.upload(result);
				}
			});
			panel.add(uploadButton);
		}

		return panel;
	}
}
