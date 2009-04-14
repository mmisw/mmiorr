package org.mmisw.ontmd.gwt.client.voc2rdf;

import org.mmisw.ontmd.gwt.client.rpc.LoginResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The panel for the resulting conversion.
 * 
 * @author Carlos Rueda
 */
public class ConversionPanel extends VerticalPanel {

	final TextArea textArea = new TextArea();
	
	private HTML info = new HTML(
			"<b>Congratulations!</b> Your vocabulary is now in RDF/XML format. " 
			);
	
	protected Voc2RdfMainPanel mainPanel;
	
	private OntMdForm ontMdForm = new OntMdForm();

	
	ConversionPanel(Voc2RdfMainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setWidth("850px");
		
		textArea.setReadOnly(true);
	    textArea.setSize("100%", "200px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(textArea);
	    textArea.setText("");

		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(4);
		hp.add(info);
		
		FlexTable panel = new FlexTable();
		int row = 0;
		
//		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, hp);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		panel.setWidget(row, 0, ontMdForm);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP
		);
		row++;

//		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, textArea);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

	    
	    add(panel);
	}

	
	void showProgressMessage(String msg) {
		textArea.setText(msg);
	}


	void setText(String error) {
		textArea.setText(error);
	}
	
	
	void updateForm(String path, LoginResult loginResult) {
		ontMdForm.updateForm(path, loginResult);
	}
	
	
	private static final String UPLOAD_ACTION = 
//		"http://mmisw.org/ont/?showreq";
		GWT.isScript() ? "/ontmd/" : "/ontmd/";


	private static class OntMdForm extends VerticalPanel {
		HTML html;
		
		OntMdForm() {
//			setWidth("300");
			html = new HTML();
			add(html);
		}
		
		void updateForm(String path, LoginResult loginResult) {
			String str = "";
			if ( path != null ) {
				
				StringBuffer action = new StringBuffer();
				action.append(UPLOAD_ACTION 
					+ "?" 
					+ "_voc2rdf=" +path
					+ "&" 
					+ "_edit=y"
				);
				
				// add session information, if any:
				if ( loginResult != null ) {
					action.append("&sessionId=" +loginResult.getSessionId());
					action.append("&userId=" +loginResult.getUserId());
				}
				
				
				str =
					"<table>" +
					"<tr>" +
					"<td>" +
					"<div align=\"center\">" +
					"<form action=\"" +action+ "\" method=\"post\" >\n" +
					"<b>" +
					"<font color=\"green\">You can now upload your vocabulary in the MMI Registry and Repository</font>" +
					"</b>" +
					"<br/>" +
					"<input type=\"submit\" value=\"" +"Register my vocabulary"+ "\" />\n" +
					"</form>" +
					"</div>\n" +
					"</td>" +
					"<td>" +
					"This button will open the <font color=\"green\">MMI Ontology Metadata Editor</font> " +
					"tool, which you can use to proceed with preparing your ontology for registration in " +
					"the MMI Registry and Repository. " +
					"You may need to log in. " +
					"</td>" +
					"</tr>" +
					"</table>" +
					
					"<br/>" +
					"<br/>" +
					"You can also just use the resulting contents and make a copy in a file on your computer."
				;
				
//				Main.log("<pre>\n" +str.replaceAll("\\<", "&lt;")+ "</pre>");
			}
			html.setHTML(str);
		}		
	}

}
