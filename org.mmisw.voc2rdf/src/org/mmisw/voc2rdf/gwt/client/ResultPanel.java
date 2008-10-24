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

/**
 * Contains the result of a conversion.
 * 
 * @author Carlos Rueda
 */
public class ResultPanel extends VerticalPanel {

	private MainPanel mainPanel;
	
	private PushButton convertButton;
	
	private final Label msgLabel = new Label();
	private final TextArea ta = new TextArea();
	
	ResultPanel(MainPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		
		add(createContents());
	}

	private Widget createContents() {
		FlexTable panel = new FlexTable();
		
		
		int row = 0;
		panel.setWidget(row++, 0, msgLabel);
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(0, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		ta.setReadOnly(true);
	    ta.setSize("700px", "250px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(ta);
	    add(decPanel);
		panel.setWidget(row++, 0, decPanel);
		row++;
		
		return panel;
	}

	void updateContents(ConversionResult result) {
		if ( result == null ) {
			msgLabel.setText("");
			ta.setText("");
//			convertButton.setEnabled(false);
			return;
		}

		String error = result .getError();

		if ( error == null ) {
			msgLabel.setText("Congratulations");
			String rdf = result.getRdf();
			Main.log(rdf);
			ta.setText(rdf);
//			ta.setSelectionRange(0, Integer.MAX_VALUE);
//			convertButton.setEnabled(false);
		}
		else {
			msgLabel.setText("Error");
			ta.setText(error);
//			convertButton.setEnabled(false);
		}
		
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);

		convertButton = new PushButton("Do conversion", new ClickListener() {
			public void onClick(Widget sender) {
				mainPanel.convert();
			}
		});
		
		// TODO handle the enable in general
		convertButton.setEnabled(true);
		
		panel.add(convertButton);

		return panel;
	}
}
