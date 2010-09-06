package org.mmisw.orrportal.gwt.client.vine;

import java.util.List;

import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.vine.util.TLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Manages the set of relations available for the mappings.
 * 
 * @author Carlos Rueda
 */
public class MappingToolbar extends VerticalPanel {
	
	interface IMappingRelationListener {
		void clicked(RelationInfo relInfo);
	}
	
	
	private VerticalPanel layout = new VerticalPanel();
	private IMappingRelationListener mapRelListener;
	
	/**
	 * 
	 * @param mapRelListener
	 */
	MappingToolbar(List<RelationInfo> relInfos, final IMappingRelationListener mapRelListener) {
		super();
		this.mapRelListener = mapRelListener;
		
		setHorizontalAlignment(ALIGN_CENTER);
		
//		setSpacing(4);
		
		add(new TLabel("", 
				"Once you have selected entities on both sides, choose the relationship you want " +
				"to establish between the corresponding entities. " +
				"<br/>" 
//				"<br/>" +
//				"Click the \"Config\" button to configure the available relations " +
//				"(not implemented yet)."
		));
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    layout.setSpacing(2);
	    add(decPanel);
		
	    _gottenRelationInfos(relInfos);
	}

	private void _gottenRelationInfos(List<RelationInfo> relInfos) {
		layout.clear();
		for ( final RelationInfo relInfo : relInfos ) {
			String imgUri = GWT.getModuleBaseURL()+ "images/" +relInfo.getIconUri();
			Orr.log("Loading relation image: " +imgUri+ " for URI=" +relInfo.getUri());
			Image img = new Image(imgUri);
			PushButton button = new PushButton(img, 
					new ClickListener() {
						public void onClick(Widget sender) {
							mapRelListener.clicked(relInfo);						
						}
			});
			// NOTE: firefox does not put the newlines in the tooltip (perhaps firefox takes this as html?),
			// so, I'm replacing each "\n" with " \n", so at least I see a space:
			button.setTitle(relInfo.getDescription().replaceAll("\n", " \n"));
			layout.add(button);
		}
	}
}
