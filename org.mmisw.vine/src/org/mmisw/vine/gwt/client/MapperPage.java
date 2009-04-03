package org.mmisw.vine.gwt.client;

import java.util.Set;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;
import org.mmisw.vine.gwt.client.rpc.RelationInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;

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
	
	
	MapperPage(MappingsPanel mappingsPanel) {
		super();
		this.mappingsPanel = mappingsPanel;
		
		int workingOntsSize = Main.getWorkingUris().size();
		int chooseLeft = workingOntsSize > 0 ? 0 : -1;
		int chooseRight = workingOntsSize > 1 ? 1 : chooseLeft;
		
		vocabularyFormLeft = new VocabularyForm(chooseLeft);
		vocabularyFormRight = new VocabularyForm(chooseRight);

		mappingToolbar = new MappingToolbar(new MappingToolbar.IMappingRelationListener() {
			public void clicked(RelationInfo relInfo) {
				_relButtonClicked(relInfo);
			}
		});

		
		setSpacing(5);
		setVerticalAlignment(ALIGN_MIDDLE);
		
		add(vocabularyFormLeft, WEST);
		add(mappingToolbar, CENTER);
		add(vocabularyFormRight, EAST);
	}
	
	private void _relButtonClicked(RelationInfo relInfo) {
		SearchResultsForm searchResultsLeft = vocabularyFormLeft.getSearchResultsForm();
		SearchResultsForm searchResultsRight = vocabularyFormRight.getSearchResultsForm();
		
		Set<String> leftRowKeys = searchResultsLeft.getSelectedRows();
		int numLeft = leftRowKeys.size();
		Set<String> rightRowKeys = searchResultsRight.getSelectedRows();
		int numRight = rightRowKeys.size();
		
		
		//
		// TODO: check for duplications
		//
		
		int totalToCreate = numLeft * numRight;
		if ( totalToCreate >= 20 ) {
			String msg = totalToCreate+ " mappings are about to be created";
			if ( ! Window.confirm(msg) ) {
				return;
			}
		}
		
		for ( String leftKey: leftRowKeys ) {
			for ( String rightKey: rightRowKeys ) {
				mappingsPanel.addMapping(leftKey, relInfo, rightKey);
			}
		}
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
