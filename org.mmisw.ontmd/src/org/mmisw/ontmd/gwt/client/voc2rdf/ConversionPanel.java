package org.mmisw.ontmd.gwt.client.voc2rdf;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The panel for the resulting conversion.
 * It allows the user to upload the resulting vocabulary in ORR, or download the file directly.
 * 
 * @author Carlos Rueda
 */
public class ConversionPanel extends VerticalPanel {
	
	private static final String UPLOAD_ACTION = "/ontmd/";

	private static final String DOWNLOAD_ACTION = "/ontmd/download";

	
	private  HTML html = new HTML(); 
	
	
	ConversionPanel(Voc2RdfMainPanel mainPanel) {
		setWidth("750px");
		this.setSpacing(6);
		this.add(html);
	}

	
	void showProgressMessage(String msg) {
		html.setHTML(msg);
	}

	void setText(String text) {
		html.setHTML(text);
	}
	
	void updateForm(ConversionResult conversionResult, LoginResult loginResult) {

		if ( conversionResult == null ) {
			html.setHTML("");
			return;
		}

		String str = "<b>Congratulations!</b> Your vocabulary is now in RDF/XML format.<br/><br/> "; 

		String path = conversionResult.getPathOnServer();

		// for download:
		String name = conversionResult.getFinalShortName() + ".owl";  

		// NOTE: "application/rdf+xml" makes [firefox 3.0.8 on Intel Max OSX] crash after
		// repeatily opening multiple windows to handle the download request!
		// (Works perfect in Safari--Don't know with windows).
		// But "application/xml" works fine in both:
		String contentType = "application/xml";

		StringBuffer uploadAction = new StringBuffer();
		uploadAction.append(UPLOAD_ACTION 
				+ "?" 
				+ "_voc2rdf=" +path
				+ "&" 
				+ "_edit=y"
		);

		// add session information, if any:
		if ( loginResult != null ) {
			uploadAction.append("&sessionId=" +loginResult.getSessionId());
			uploadAction.append("&userId=" +loginResult.getUserId());
		}


		str +=
			"<form action=\"" +uploadAction+ "\" method=\"post\" >\n" +
			"<b>" +
			"<font color=\"green\">You can now upload your vocabulary in the MMI Registry and Repository:</font>" +
			"</b>" +
			" <input type=\"submit\" value=\"" +"Upload vocabulary"+ "\" />\n" +
			"<br/>" +
			"This button will open the MMI Ontology Metadata Editor " +
			"tool, which will allow you to prepare your vocabulary for registration. " +
			"You may need to log in. " +
			"</form>"
			;

		// download
		String downloadAction = DOWNLOAD_ACTION + "?ip=" +path+ "&xn=" +name+ "&ct=" +contentType;
		str +=
			"<br/>" +
			"<br/>" +
			"<form action=\"" +downloadAction+ "\" method=\"post\" >\n" +
			"Or, you can download the resulting contents directly: " +
			"<input type=\"submit\" value=\"" +"Download RDF file"+ "\" />\n" +
			"</form>"
			;

		html.setHTML(str);
	}

}
