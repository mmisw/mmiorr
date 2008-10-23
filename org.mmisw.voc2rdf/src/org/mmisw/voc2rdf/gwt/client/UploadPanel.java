package org.mmisw.voc2rdf.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Captures login info and starts the upload to the registry.
 * 
 * @author Carlos Rueda
 */
public class UploadPanel extends VerticalPanel {

	private MainPanel mainPanel;
	ConversionResult result;
	
	private Map<String, Widget> widgets = new HashMap<String, Widget>();
	
	private PushButton uploadButton;
	
	UploadPanel(MainPanel mainPanel, ConversionResult result) {
		super();
		this.mainPanel = mainPanel;
		this.result = result;
		
		add(createForm());
		formChanged();
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		
		int row = 0;
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(0, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		String[] attrNames =  { "userId", "userPassword" };
		String[] attrLabels = { "userId:", "Password:" };
		
		for ( int i = 0; i < attrNames.length; i++ ) {
			String attrName = attrNames[i];
			String attrLabel = attrLabels[i];
			
			Widget widget;
			final TextBox tb = "userPassword".equals(attrName) ? new PasswordTextBox() : new TextBox();
			tb.setName(attrName );
			tb.setWidth("200");
			
			tb.addChangeListener(new ChangeListener () {
				public void onChange(Widget sender) {
					formChanged();
				}
			});
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
		
		return panel;
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		uploadButton = new PushButton("Upload", new ClickListener() {
			public void onClick(Widget sender) {
				upload();
			}
		});
		panel.add(uploadButton);
		
		return panel;
	}
	
	private void formChanged() {
		if ( true ) {
			return;
		}
		for ( String attrName : widgets.keySet() ) {
			Widget widget = widgets.get(attrName);
			String value = null;
			if ( widget instanceof TextBoxBase ) {
				value = ((TextBoxBase) widget).getText();
			}
			
			if ( value == null || value.trim().length() == 0 ) {
				uploadButton.setEnabled(false);
				return;
			}
		}
		uploadButton.setEnabled(true);
	}

	private void upload() {
		Map<String, String> values = new HashMap<String, String>();
		for ( String attrName : widgets.keySet() ) {
			Widget widget = widgets.get(attrName);
			String value = null;
			if ( widget instanceof TextBoxBase ) {
				value = ((TextBoxBase) widget).getText();
			}
			
			if ( value == null || value.trim().length() == 0 ) {
				Window.alert(attrName+ "\n   A value is required.");
				return;
			}
			values.put(attrName, value.trim());
		}
		mainPanel.doUpload(result, values);
	}

}
