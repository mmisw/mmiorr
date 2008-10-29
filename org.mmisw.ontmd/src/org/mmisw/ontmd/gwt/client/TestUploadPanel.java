package org.mmisw.ontmd.gwt.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TestUploadPanel extends FormPanel {

	private static final String UPLOAD_ACTION = "http://localhost:8080/ont/?_upload";

	public TestUploadPanel() {
		setAction(UPLOAD_ACTION);
		setEncoding(FormPanel.ENCODING_MULTIPART);
		setMethod(FormPanel.METHOD_POST);

		VerticalPanel panel = new VerticalPanel();
		setWidget(panel);

		panel.add(new Label("Book name"));
		TextBox name = new TextBox();
		panel.add(name);

		panel.add(new Label("File"));
		FileUpload upload = new FileUpload();
		upload.setName("bookFile");
		panel.add(upload);

		Button submit = new Button("Submit");
		submit.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				submit();
			}
		});
		panel.add(submit);
	}
}
