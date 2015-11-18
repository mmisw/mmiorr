package org.mmisw.orrportal.gwt.client.vine;

import java.util.*;

import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.EntityInfo;
import org.mmisw.orrclient.gwt.client.rpc.OntologyData;
import org.mmisw.orrclient.gwt.client.rpc.PropValue;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.vine.util.TLabel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dispatches the search of entities.
 * It uses an oracle to remember the strings of sucessful searches.
 *
 * @author Carlos Rueda
 */
public class SearchGroup extends VerticalPanel {

	private SearchVocabularySelection vocabularySelection;
	private SearchResultsForm searchResultsForm;

	private MultiWordSuggestOracle oracle;
	private ToggleButton regex;
	private SuggestBox box;

	SearchGroup(SearchVocabularySelection vocabularySelection, SearchResultsForm searchResultsForm) {
		super();
		this.vocabularySelection = vocabularySelection;
		this.searchResultsForm = searchResultsForm;

		HorizontalPanel hp0 = new HorizontalPanel();
		hp0.setVerticalAlignment(ALIGN_MIDDLE);
		add(hp0);
		hp0.setSpacing(3);

		hp0.add(new TLabel("Search for:",
				"Enter the string you want to search in the selected ontologies and click " +
				"the search button. " +
				"Leave the field blank to retrieve all associated entities. " +
				"<br/>" +
				"Check the REGEX button if you are entering a regular expression for your search " +
				"(not implemented yet)."
		));


		// TODO implement REGEX search
		regex = new ToggleButton("REGEX");
		DOM.setElementAttribute(regex.getElement(), "id", "my-button-id");
		regex.setTitle("Check this to apply a regular expression search - NOT IMPLEMENTED YET");



		oracle = new MultiWordSuggestOracle("/");

		box = new SuggestBox(oracle);
		box.setWidth("250px");
		hp0.add(box);

		box.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				search();
			}
		});

		PushButton b = new PushButton(VineMain.images.search().createImage());
		b.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				search();
			}
		});

		hp0.add(b);

		hp0.add(regex);
	}

	private void search() {
		if ( VineMain.getWorkingUris() == null || VineMain.getWorkingUris().size() == 0 )  {
			Window.alert("No selected vocabularies to search");
			return;
		}

		final String text = box.getText().trim();

		Orr.log("searching: " +text);
		searchResultsForm.searching();

		new Timer() {
			public void run() {
				executeSearch(text);
			}
		}.schedule(300);

//		DeferredCommand.addCommand(new Command() {
//			public void execute() {
//				executeSearch(text);
//			}
//		});
	}

	private void executeSearch(String text) {
		List<BaseOntologyInfo> selectedVocabs = vocabularySelection.getSelectedVocabularies();
		List<EntityInfo> entities = search(text, selectedVocabs );

		Orr.log("search: retrieved " +entities.size()+ " terms");
		if ( text.length() > 0 ) {
			oracle.add(text);
		}
		searchResultsForm.updateEntities(entities);
	}

	private List<EntityInfo> search(String text, List<BaseOntologyInfo> selectedVocabs) {

		// TODO use a parameter to apply case-sensitive or not
		text = text.toLowerCase();

		// #311: vocabUris: used to exclude them as found entities.
		// Note, only for this purpose we capture these URIs without any trailing separator. See below.
		Set<String> vocabUris = new HashSet<String>();
		for (BaseOntologyInfo ont : selectedVocabs ) {
			vocabUris.add(ont.getUri().replaceAll("(/|#)+$", ""));
		}
		Orr.log("search: vocabUris (w/o any trailing separators): " +vocabUris);

		// TODO get these flags from parameters
		boolean useLocalName = true;
		boolean useProps = true;

		boolean includeSubjects = true;
		boolean includeIndividuals = true;
		boolean includeClasses = true;
		boolean includeProperties = true;

		List<EntityInfo> foundEntities = new ArrayList<EntityInfo>();
		for (BaseOntologyInfo ont : selectedVocabs ) {

			if ( ont.getError() != null ) {
				Orr.log("Error: " +ont.getError());
				continue;
			}

			OntologyData ontologyData = ont.getOntologyData();
			if ( ontologyData == null ) {
				if (Orr.isLogEnabled()) Orr.log("search: data not yet retrieved for " +ont.getUri());
				continue;
			}

			if (Orr.isLogEnabled()) Orr.log("search: searching in " +ont.getUri());

			BaseOntologyData baseOntologyData = ontologyData.getBaseOntologyData();

			Set<EntityInfo> entityArray = new LinkedHashSet<EntityInfo>();

			if (includeSubjects) {
				List<? extends EntityInfo> entities = baseOntologyData.getSubjects();
				if (Orr.isLogEnabled()) Orr.log("subjects (" + entities.size() + "): " + entities);
				entityArray.addAll(entities);
			}

			if (includeIndividuals) {
				List<? extends EntityInfo> entities = baseOntologyData.getIndividuals();
				if (Orr.isLogEnabled()) Orr.log("individuals (" + entities.size() + "): " + entities);
				entityArray.addAll(entities);
			}

			if (includeClasses) {
				List<? extends EntityInfo> entities = baseOntologyData.getClasses();
				if (Orr.isLogEnabled()) Orr.log("classes (" + entities.size() + "): " + entities);
				entityArray.addAll(entities);
			}

			if (includeProperties) {
				List<? extends EntityInfo> entities = baseOntologyData.getProperties();
				if (Orr.isLogEnabled()) Orr.log("properties (" + entities.size() + "): " + entities);
				entityArray.addAll(entities);
			}

			for (EntityInfo entityInfo : entityArray) {

				boolean add = false;

				// check localName
				if ((useLocalName && entityInfo.getLocalName().toLowerCase().indexOf(text) >= 0)) {
					add = true;
				}

				// check props
				if (!add && useProps) {
					List<PropValue> props = entityInfo.getProps();
					for (PropValue pv : props) {
						String str = pv.getValueName();
						add = str != null && str.toLowerCase().indexOf(text) >= 0;
						if (add) {
							break;
						}
					}
				}

				if (add) {
					// #311: do not add the entity if its URI is equal (modulo trailing separator) to any of the vocabUris:
					String entUri = entityInfo.getUri().replaceAll("(/|#)+$", "");
					if (!vocabUris.contains(entUri)) {
						foundEntities.add(entityInfo);
					}
				}
			}
		}

		Collections.sort(foundEntities);

		return foundEntities;
	}


}
