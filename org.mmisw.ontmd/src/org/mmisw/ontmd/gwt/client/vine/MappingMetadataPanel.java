package org.mmisw.ontmd.gwt.client.vine;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for visualizing/editing a mapping's metadata.
 * 
 * @author Carlos Rueda
 */
public class MappingMetadataPanel {

	private static final String RDFS_COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";
	
	private static final String VINE_CONFIDENCE = "http://mmisw.org/ont/mmi/vine/confidence";
	
	private static final String STYLE = "MappingMetadataPanel";
	

	/** GUI info for each metadata property */
	private static class GuiInfo {
		String uri;
		String label;
		int numLines;
		String width;
		
		GuiInfo(String uri, String label, int numLines, String width) {
			this.uri = uri;
			this.label = label;
			this.numLines = numLines;
			this.width = width;
		}
	}

	private static Map<String, GuiInfo> propGui = new LinkedHashMap<String, GuiInfo>();
	static {
		GuiInfo[] guis = { 
				new GuiInfo(RDFS_COMMENT, "Comment:", 4, "350px"),
				new GuiInfo(VINE_CONFIDENCE, "Confidence:", 1, "50px"),
		};
		for (GuiInfo guiInfo : guis) {
			propGui.put(guiInfo.uri, guiInfo);
		}
	}
	
	private Mapping mapping;
	private boolean readOnly;
	
	private FlexTable flexTable = new FlexTable();

	/**
	 * Creates a panel for visualizing/editing metadata for a mapping.
	 */
	MappingMetadataPanel(Mapping mapping, boolean readOnly) {
		this.mapping = mapping;
		this.readOnly = readOnly;
		_setup();
	}
	
	Widget getWidget() {
		return flexTable;
	}
	
	private void _setup() {
		int row = 0;
		for ( String propUri : propGui.keySet() ) {
			GuiInfo gui = propGui.get(propUri);
			Label label = new Label(gui.label);

			String value = "";
			Map<String, String> md = mapping.getMetadata();
			if ( md != null ) {
				String val = md.get(propUri);
				if ( val != null ) {
					value = val.trim();
				}
			}
			
			Widget valueWidget = _createWidgetForValue(gui, value);
			
			flexTable.getRowFormatter().setStyleName(row, STYLE);
			flexTable.setWidget(row, 0, label);
			flexTable.setWidget(row, 1, valueWidget);
			row++;
		}
	}
	
	private Widget _createWidgetForValue(final GuiInfo gui, String value) {
		int visLines = readOnly ? Math.min(gui.numLines, _countLines(value)) : gui.numLines;
		TextBoxBase tb;
		if ( visLines == 1 ) {
			tb = new TextBox();
		}
		else {
			TextArea ta = new TextArea();
			ta.setVisibleLines(visLines);
			tb = ta;
		}
		tb.setText(value);
		tb.setReadOnly(readOnly);
		tb.setWidth(gui.width);
		
		if ( ! readOnly ) {
			// capture changes in the field to update the corresponding metadata
			// property for the mapping:
			final TextBoxBase tbb = tb;
			tb.addChangeListener(new ChangeListener() {
				public void onChange(Widget sender) {
					String value = tbb.getText().trim();
					Map<String, String> md = mapping.getMetadata();
					if ( md == null ) {
						mapping.setMetadata(new HashMap<String, String>());
					}
					md = mapping.getMetadata();
					md.put(gui.uri, value);
				}
			});
			
			// workaround
			tb.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					// TODO Auto-generated method stub
					tbb.setFocus(true);
				}
				
			});
		}
		return tb;
	}

	private static int _countLines(String str) {
		String[] lines = str.split("\n|\r\n|\r");
		return lines.length;
	}
}
