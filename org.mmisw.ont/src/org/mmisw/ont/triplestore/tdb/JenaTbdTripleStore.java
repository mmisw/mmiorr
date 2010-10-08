package org.mmisw.ont.triplestore.tdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.triplestore.jena.JenaTripleStore;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Triple store implementation based on Jena TDB.
 * 
 * <p>
 * Under preliminary testing
 * 
 * @author Carlos Rueda
 */
public class JenaTbdTripleStore extends JenaTripleStore {
	
	private static final String UTF8_ENC = "UTF-8";
	
	private static final String ASSEMBLER_NAME = "tdb-assembler.ttl";

	// token in TDB_ASSEMBLER_NAME to be replaced in created assembler file
	private static final String LOCATION_TOKEN = "@jena.tdb.dir@";


	private final Log log = LogFactory.getLog(JenaTbdTripleStore.class);
	
	
	private final String _directory;
	private final File _assembler;
	
	// TODO NOTE _infModel below is temporary -- should be replaced with
	// a proper handling using TDB
//	/** the corresponding inference model after a _doInitModel(true) call. */
//	private InfModel _infModel;
	
	/** the model with all the ontologies */
	private Model _model;
	
	
	/**
	 * Constructor.
	 *        
	 * @param db The database helper.
	 */
	public JenaTbdTripleStore(Db db) {
		super(db);
		this._directory = OntConfig.Prop.JENA_TDB_DIR.getValue();
		this._assembler = _prepareAssembler(OntConfig.Prop.JENA_TDB_ASSEMBLER.getValue());
		log.info(getClass().getSimpleName()+ " instance created. " +
				"directory=" +_directory+ "  assembler=" +_assembler);
	}

	/** 
	 * Prepares the given assembler file, if any.
	 * @param filename Name of the assembler file.
	 * @return assembler file object if preparation completed (either with creation of
	 *         the file from the template in this package, or just with the existing file);
	 *         null, is the file was not specified or any errors occured (which are logged out).
	 */
	private File _prepareAssembler(String filename) {
		if ( filename == null || filename.trim().length() == 0 ) {
			return null;
		}
		
		File testFile = new File(filename);
		if ( testFile.isFile() ) {
			// Check that it can be read:
			try {
				FileInputStream is = new FileInputStream(testFile);
				String assemblerContents = IOUtils.toString(is, UTF8_ENC);
				if ( assemblerContents.trim().length() > 0 ) {
					if ( log.isDebugEnabled() ) {
						log.debug("_prepareAssembler: " +filename+ " exists. Contents:\n" +
								assemblerContents
						);
					}
					// OK: existing file case.
					return testFile;
				}
				else {
					// Else: it's empty -- handle as non-existing
					if ( log.isDebugEnabled() ) {
						log.debug("_prepareAssembler: " +filename+ " is empty. Will be re-created.");
					}
				}
			}
			catch (IOException e) {
				log.warn("Cannot read existing assembler file: " +filename);
				return null;
			}
		}
		
		// file does not exist yet. Try to create it with a copy of
		// "tdb-assembler.ttl" in this package.
		// first, create any parent directory if it does not exist:
		File parentDir = testFile.getParentFile();
		if ( parentDir != null && ! parentDir.isDirectory() && ! parentDir.mkdirs() ) {
			log.warn("Cannot create parent directory: " +parentDir);
		}
		
		// now, create file
		FileOutputStream os;
		try {
			os = new FileOutputStream(testFile);
		}
		catch (FileNotFoundException e) {
			log.warn("Cannot create assembler file: " +filename);
			return null;
		}

		// get template contents for the creation:
		InputStream is = getClass().getResourceAsStream(ASSEMBLER_NAME);
		if ( is == null ) {
			// Should not happen.
			log.warn("Cannot get built-in resource: " +ASSEMBLER_NAME+ " Report this bug!");
			return null;
		}
		try {
			String assemblerContents = IOUtils.toString(is, UTF8_ENC);
			// replace @jena.tdb.dir@ with jenaTdbDir
			assemblerContents = assemblerContents.replaceAll(LOCATION_TOKEN, _directory);
			IOUtils.write(assemblerContents, os, UTF8_ENC);
			
			if ( log.isDebugEnabled() ) {
				log.debug("_prepareAssembler: " +testFile+ " prepared. Contents:\n" +
						assemblerContents
				);
			}
			// OK: file creation case.
			return testFile;
		}
		catch (IOException e) {
			log.warn("Cannot prepare new assembler file: " +filename);
			return null;
		}
		finally {
			IOUtils.closeQuietly(os);
		}
	}

	@Override
	protected void _createModel() {
		
		if ( _assembler != null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("Calling TDBFactory.assembleModel: " +_assembler);
			}
			_model = TDBFactory.assembleModel(_assembler.getAbsolutePath());
			if ( log.isDebugEnabled() ) {
				log.debug("_createModel: TDBFactory.assembleModel done.");
			}
		}
		else {
			if ( log.isDebugEnabled() ) {
				log.debug("Calling TDBFactory.createModel: " +_directory);
			}
			_model = TDBFactory.createModel(_directory);
			if ( log.isDebugEnabled() ) {
				log.debug("_createModel: TDBFactory.createModel done.");
			}
		}
	}


	@Override
	protected void _setInfModelNull() {
//		_infModel = null;
	}
	

	/**
	 * 1) load the skos properties model into the base model _model
	 * 2) create reasoner and InfModel.
	 * @return the created InfModel
	 */
	@Override
	protected void _createInfModel() {
//		//
//		// 1) load the skos properties model into the base model _model:
//		//
//		String propsSrc = Util.getResource(INF_PROPERTIES_MODEL_NAME_N3);
//		if ( propsSrc == null ) {
//			return;
//		}
//		
//		Model propsModel = ModelFactory.createDefaultModel();
//		StringReader sr = new StringReader(propsSrc);
//		propsModel.read(sr, "dummyBase", "N3");
//		_model.add(propsModel);
//		log.info("_createInfModel: Added properties model:\n\t" +propsSrc.replaceAll("\n", "\n\t"));
//
//		
//		//
//		// 2) create reasoner and InfModel:
//		//
//		String rulesSrc = Util.getResource(INF_RULES_NAME);
//		if ( rulesSrc == null ) {
//			return;
//		}
//		log.info("_createInfModel: Creating InfModel with rules:\n\t" +rulesSrc.replaceAll("\n", "\n\t"));
//		List<Rule> rules = Rule.parseRules(rulesSrc);
//		Reasoner reasoner = new GenericRuleReasoner(rules);
//		_infModel = ModelFactory.createInfModel(reasoner, _model);
	}

	@Override
	protected InfModel _getInfModel() {
//		return _infModel;
		return null;
	}

	@Override
	protected Model _getModel() {
		return _model;
	}

	
	/**
	 * The main init in this class simply checks the "connection" to the TDB directory.
	 */
	@Override
	protected void _mainInit() throws ServletException {
		log.info("_mainInit called.");
		_createModel();
		_createInfModel();
		log.info("_mainInit: complete.");
	}
	
	/** closes the model(s) so caches are flushed */
	public void destroy() throws ServletException {
//		if ( _infModel != null ) { 
//			_infModel.close();
//			_infModel = null;
//		}
		if ( _model != null ) { 
			_model.close();
			_model = null;
		}
	}

	
	/**
	 * FIXME NOT IMPLEMENTED YET -- it simply calls super._doClear();
	 *  
	 * In this class, the directory associated with the TDB store is removed.
	 * NOTE that there is no TDB API to "clear" or "reinit" a triple store; so,
	 * the implementation here is simply to remove the contents of the directory.
	 */
	@Override
	protected void _doClear() throws ServletException {
		super._doClear();
	}

	/**
	 * FIXME NOT IMPLEMENTED YET -- it simply calls super._doReInit();
	 *  
	 * In this class, the directory associated with the TDB store is removed.
	 * NOTE that there is no TDB API to "clear" or "reinit" a triple store; so,
	 * the implementation here is simply to remove the contents of the directory.
	 */
	@Override
	protected void _doReInit() throws ServletException {
		super._doReInit();
	}
	
}
