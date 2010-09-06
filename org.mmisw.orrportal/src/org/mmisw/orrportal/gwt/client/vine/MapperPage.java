package org.mmisw.orrportal.gwt.client.vine;

import java.util.List;
import java.util.Set;

import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.vine.Mapping;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;

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
	
	
	MapperPage(List<RelationInfo> relInfos, MappingsPanel mappingsPanel) {
		super();
		this.mappingsPanel = mappingsPanel;
		
		int workingOntsSize = VineMain.getWorkingUris().size();
		int chooseLeft = workingOntsSize > 0 ? 0 : -1;
		int chooseRight = workingOntsSize > 1 ? 1 : chooseLeft;
		
		vocabularyFormLeft = new VocabularyForm(chooseLeft);
		vocabularyFormRight = new VocabularyForm(chooseRight);

		mappingToolbar = new MappingToolbar(relInfos,
			new MappingToolbar.IMappingRelationListener() {
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
		
		Set<String> leftKeys = searchResultsLeft.getSelectedRows();
		Set<String> rightKeys = searchResultsRight.getSelectedRows();
		
		List<Mapping> preMappings = mappingsPanel.preAddMappings(leftKeys, relInfo, rightKeys);
		
		if ( preMappings.size() == 0 ) {
			return;
		}
		
		if ( preMappings.size() >= 20 ) {
			String msg = preMappings.size()+ " mappings are about to be created.\nPlease confirm.";
			if ( ! Window.confirm(msg) ) {
				return;
			}
		}
		
		mappingsPanel.addMappings(relInfo, preMappings);
		
		searchResultsLeft.selectAll(false);
		searchResultsRight.selectAll(false);
	}

	
	/** Call this to notify that a new ontology has been added to the working list */
	void notifyWorkingOntologyAdded(RegisteredOntologyInfo ontologyInfo) {
		int workingOntsSize = VineMain.getWorkingUris().size();
		int chooseLeft = workingOntsSize > 0 ? 0 : -1;
		int chooseRight = workingOntsSize > 1 ? 1 : chooseLeft;
		vocabularyFormLeft.notifyWorkingOntologyAdded(chooseLeft);
		vocabularyFormRight.notifyWorkingOntologyAdded(chooseRight);
	}

}
