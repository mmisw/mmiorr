package org.mmisw.voc2rdf.gwt.client;

import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;
import org.mmisw.voc2rdf.gwt.client.rpc.UploadResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class MainPanel extends VerticalPanel {

	private FormInputPanel formInputPanel;

	private CellPanel container = new VerticalPanel();
	
	MainPanel(final Map<String, String> params) {
		super();
		
		add(Main.images.voc2rdf().createImage());
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    formInputPanel = new FormInputPanel(this);
	    
	    container.add(formInputPanel);
	}

	public void doConversion(final Map<String, String> values) {
		AsyncCallback<ConversionResult> callback = new AsyncCallback<ConversionResult>() {
			public void onFailure(Throwable thr) {
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(ConversionResult result) {
				
				ResultPanel resultPanel = createResultPane(result);
				MyDialog popup = new MyDialog(resultPanel);
				popup.setText("Conversion to RDF completed");
				popup.center();
				popup.show();
			}
		};

		Main.log("Converting ...");
		Main.voc2rdfService.convert(values, callback);
	}
	
	
	private ResultPanel createResultPane(ConversionResult result) {
		return new ResultPanel(this, result);
	}

	
	public void upload(ConversionResult result) {
		UploadPanel uploadPanel = new UploadPanel(this, result);
		Main.log("uploadPanel created");
		MyDialog popup = new MyDialog(uploadPanel);
		popup.setText("User account");
		popup.center();
		popup.show();
	}

	
	void doUpload(ConversionResult result, Map<String, String> values) {
		AsyncCallback<UploadResult> callback = new AsyncCallback<UploadResult>() {
			public void onFailure(Throwable thr) {
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(UploadResult result) {
				String error = result.getError();
				
				String msg = error == null ? result.getInfo() : error;
				
				TextArea ta = new TextArea();
				ta.setSize("400px", "100px");
				ta.setReadOnly(true);
				ta.setText(msg);
				VerticalPanel vp = new VerticalPanel();
				vp.add(ta);
				final MyDialog popup = new MyDialog(vp);
				popup.setText(error == null ? "OK" : "Error");
				
				Main.log("Uploading result: " +msg);
				
				popup.center();
				popup.show();
			}
		};

		Main.log("Uploading ...");
		Main.voc2rdfService.upload(result, values, callback);
	}

}
