package org.mmisw.ontmd.gwt.client.voc2rdf;

import org.mmisw.ontmd.gwt.client.Main;

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
 * Form elements for the contents of the vocabulary.
 * 
 * @author Carlos Rueda
 */
public class ConversionPanel extends VerticalPanel {

	final TextArea textArea = new TextArea();
	
	private HTML info = new HTML(
			"Upon successful conversion, this panel will show the resulting vocabulary in RDF/XML format. "
			);
	
	protected Voc2RdfMainPanel mainPanel;
	
	private OntMdForm ontMdForm = new OntMdForm();

	
	ConversionPanel(Voc2RdfMainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setWidth("850");
		
		
		textArea.setReadOnly(true);
	    textArea.setSize("600px", "350px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(textArea);
	    textArea.setText("");

		
		FlexTable panel = new FlexTable();
		int row = 0;
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setWidth("850");
		hp.setSpacing(4);
		hp.add(info);
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, hp);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
//		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, textArea);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);

		panel.setWidget(row, 1, ontMdForm);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP
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
	
	
	void updateForm(String path) {
		ontMdForm.updateForm(path);
	}
	
	
	private static final String UPLOAD_ACTION = 
//		"http://mmisw.org/ont/?showreq";
		GWT.isScript() ? "/ontmd/" : "/ontmd/";


	private static class OntMdForm extends VerticalPanel {
		HTML html;
		
		OntMdForm() {
			setWidth("300");
			html = new HTML();
			add(html);
		}
		
		void updateForm(String path) {
			String str = "";
			if ( path != null ) {
				String action = UPLOAD_ACTION 
					+ "?" 
					+ "_voc2rdf=" +path
					+ "&" 
					+ "_edit=y"
				;
				str = 
					"<b>Congratulations!</b> Your vocabulary is now in RDF/XML format. " +
					"You can now copy and paste the resulting contents in a file on your " +
					"computer." +
					"<br/>" +
					"<br/>" +
					"<br/>" +
					"<b>" +
					"<font color=\"green\">Do you want to upload your vocabulary in the MMI Registry right away?</font>" +
					"</b>" +
					"<br/>" +
					"<br/>" +
					"<div align=\"center\">" +
					"<form action=\"" +action+ "\" method=\"post\" >\n" +
					"<input type=\"submit\" value=\"" +"Yes, register my vocabulary"+ "\" />\n" +
					"</form>" +
					"</div>\n" +
					"This button will open the " +
					"<font color=\"green\">MMI Ontology Metadata Editor</font> " +
					"tool, which you can use " +
					"to proceed with preparing your ontology for registration in " +
					"the MMI Registry and Repository. " +
					"You will need to log in. " 
				;
				
				Main.log("<pre>\n" +str.replaceAll("\\<", "&lt;")+ "</pre>");
			}
			html.setHTML(str);
		}		
	}

}
