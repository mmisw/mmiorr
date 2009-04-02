package org.mmisw.vine.gwt.client;

import java.util.Set;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Maintains the two vocabulary forms.
 * 
 * @author Carlos Rueda
 */
public class MapperPage extends DockPanel {
	
	VocabularyForm vocabularyFormLeft;
	VocabularyForm vocabularyFormRight;
	
	private MappingToolbar mappingToolbar;
	
	private MappingsPanel mappingsPanel;
	
	private MappingToolbar.IMappingRelationListener mapRelListener = 
		new MappingToolbar.IMappingRelationListener() {
		public void clicked(Image img) {
			// FIXME Need to replicate img for each created mapping
			SearchResultsForm searchResultsLeft = vocabularyFormLeft.getSearchResultsForm();
			SearchResultsForm searchResultsRight = vocabularyFormRight.getSearchResultsForm();
			
			Set<String> leftRowKeys = searchResultsLeft.getSelectedRows();
			for ( String leftKey: leftRowKeys ) {
				Set<String> rightRowKeys = searchResultsRight.getSelectedRows();
				for ( String rightKey: rightRowKeys ) {
					mappingsPanel.addMapping(leftKey, img, rightKey);
				}
			}
			
		}
	};
	
	MapperPage(MappingsPanel mappingsPanel) {
		super();
		this.mappingsPanel = mappingsPanel;
		
		int workingOntsSize = Main.getWorkingUris().size();
		int chooseLeft = workingOntsSize > 0 ? 0 : -1;
		int chooseRight = workingOntsSize > 1 ? 1 : chooseLeft;
		
		vocabularyFormLeft = new VocabularyForm(chooseLeft);
		vocabularyFormRight = new VocabularyForm(chooseRight);

		mappingToolbar = new MappingToolbar(mapRelListener);

		
		setSpacing(5);
		setVerticalAlignment(ALIGN_MIDDLE);
		
		add(vocabularyFormLeft, WEST);
		add(mappingToolbar, CENTER);
		add(vocabularyFormRight, EAST);
	}

	
	/** Call this to notify that a new ontology has been added to the working list */
	void notifyWorkingOntologyAdded(OntologyInfo ontologyInfo) {
		int workingOntsSize = Main.getWorkingUris().size();
		int chooseLeft = workingOntsSize > 0 ? 0 : -1;
		int chooseRight = workingOntsSize > 1 ? 1 : chooseLeft;
		vocabularyFormLeft.notifyWorkingOntologyAdded(chooseLeft);
		vocabularyFormRight.notifyWorkingOntologyAdded(chooseRight);
	}

}
