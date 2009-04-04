package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

/**
 * 
 * @author Carlos Rueda
 */
public class VocabularyForm extends VerticalPanel {
	
	private static final boolean usePopup4ResourceViewer = true;
	
	
	private MyDialog popupResourceViewer;
	
	private final SearchVocabularySelection vocabularySelection;
	private final SearchGroup searchGroup;
	private final SearchResultsForm searchResultsForm;
	private final ResourceViewer resourceViewer;
	
	
	VocabularyForm(int searchIndex) {
		super();
		
		vocabularySelection = new SearchVocabularySelection(searchIndex);
		
		boolean useDecoratorPanel = ! usePopup4ResourceViewer;
		boolean useScroller = true;
		resourceViewer = new ResourceViewer(useDecoratorPanel, useScroller);
		resourceViewer.setSize("400", "100");
		
		searchResultsForm = new SearchResultsForm(this);
		searchGroup = new SearchGroup(vocabularySelection, searchResultsForm);
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    
	    add(decPanel);
		
	    layout.setSpacing(5);
	    
	    layout.add(vocabularySelection);
	    layout.add(searchGroup);
		layout.add(searchResultsForm);
		
		if ( usePopup4ResourceViewer ) {
			popupResourceViewer = new MyDialog(resourceViewer, false);
		}
		else {
			layout.add(resourceViewer);
		}
	}
	
	void entityFocused(EntityInfo entityInfo) {
		if ( usePopup4ResourceViewer ) {
			popupResourceViewer.setText(entityInfo.getDisplayLabel());
			final int x = getAbsoluteLeft();
//			final int y = getAbsoluteTop();
			popupResourceViewer.setPopupPositionAndShow(new PositionCallback() {
				public void setPosition(int offsetWidth, int offsetHeight) {
					popupResourceViewer.setPopupPosition(
							x, 
							offsetHeight >> 1
					);
				}
			});
		}
		resourceViewer.update(entityInfo);
	}
	
	/** Call this to notify that a new ontology has been added to the working list
	 * 
	 * @param searchIndex
	 */
	void notifyWorkingOntologyAdded(int searchIndex) {
		vocabularySelection.setToggleButtons(searchIndex);
	}

	SearchResultsForm getSearchResultsForm() {
		return searchResultsForm;
	}


}
