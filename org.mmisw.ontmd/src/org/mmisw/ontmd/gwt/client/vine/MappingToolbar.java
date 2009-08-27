package org.mmisw.ontmd.gwt.client.vine;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.vine.util.TLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
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
	MappingToolbar(final IMappingRelationListener mapRelListener) {
		super();
		this.mapRelListener = mapRelListener;
		
		setHorizontalAlignment(ALIGN_CENTER);
		
//		setSpacing(4);
		
		add(new TLabel("", 
				"Once you have selected entities on both sides, choose the relationship you want " +
				"to establish between the corresponding entities. " +
				"<br/>" +
				"<br/>" +
				"Click the \"Config\" button to configure the available relations " +
				"(not implemented yet)."
		));
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    layout.setSpacing(2);
	    add(decPanel);
		
		//setWidth("10%");

		final PushButton configButton = new PushButton("Config");
		configButton.setTitle("Allows to configure the available relations (not implemented yet)");
		DOM.setElementAttribute(configButton.getElement(), "id", "my-button-id");

		configButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				// TODO
				Window.alert("sorry, not implemented yet");
			}
		});
		add(configButton);

	    update();
	
	}

	
	private void update() {
		layout.clear();
		layout.add(new HTML(
				"<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\">"
		));
		
		
		AsyncCallback<List<RelationInfo>> callback = new AsyncCallback<List<RelationInfo>>() {
			public void onFailure(Throwable thr) {
				layout.clear();
				layout.add(new HTML(thr.toString()));
			}

			public void onSuccess(List<RelationInfo> relInfos) {
				layout.clear();
				Main.log("getRelationInfos: retrieved " +relInfos.size()+ " relations");
				_gottenRelationInfos(relInfos);
			}
		};

		Main.log("Getting relations ...");
		Main.ontmdService.getVineRelationInfos(callback);
	}


	private void _gottenRelationInfos(List<RelationInfo> relInfos) {
		layout.clear();
		for ( final RelationInfo relInfo : relInfos ) {
			String imgUri = GWT.getModuleBaseURL()+ "images/" +relInfo.getIconUri();
			Main.log("Loading relation image: " +imgUri);
			Image img = new Image(imgUri);
			PushButton button = new PushButton(img, 
					new ClickListener() {
						public void onClick(Widget sender) {
							mapRelListener.clicked(relInfo);						
						}
			});
			button.setTitle(relInfo.getDescription());
			layout.add(button);
		}
	}
}
