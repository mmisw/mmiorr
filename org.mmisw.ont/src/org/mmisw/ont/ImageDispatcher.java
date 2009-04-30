package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.util.Unfinished;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.ontology.OntModel;

import edu.drexel.util.rdf.JenaUtil;


/**
 * Dispatches an image output.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
@Unfinished
public class ImageDispatcher {
	
	private final Log log = LogFactory.getLog(ImageDispatcher.class);
	
	private OntConfig ontConfig;
	private Db db;
	
	ImageDispatcher(OntConfig ontConfig, Db db) {
		this.ontConfig = ontConfig;
		this.db = db;
	}

	void init() {
	}
	boolean dispatch(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, boolean unversionedRequest, Ontology ontology) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("ImageDispatcher: starting response.");
		}
		
		assert mmiUri.getTerm().length() == 0 ;
		
		boolean debug = Util.yes(request, "_dotdebug");
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		String foundUri = mmiUri.toString();
		
		if ( ontology == null ) {
			String[] foundUri_ = { null };
			ontology = db.getOntologyWithExts(mmiUri, foundUri_);
			if ( ontology == null ) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, 
						request.getRequestURI()+ ": not found");
				return true;
			}
			
			foundUri = foundUri_[0];
		}

		File file = UriResolver._getFullPath(ontology, ontConfig, log);
		
		if ( ! file.canRead() ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, 
					request.getRequestURI()+ ": not found");
			return true;
		}
		
		File dotFile = new File(file.getAbsolutePath() + ".dot");
		if ( dotFile.exists() ) {
			
		}
		
		

		PrintWriter out = response.getWriter();
		
		
		// original model:
		OntModel model = null;
		
		// corresponding unversioned model in case is requested: 
		OntModel unversionedModel = null;
		
		String uriFile = file.toURI().toString();

		if ( unversionedRequest ) {
			
			model = JenaUtil.loadModel(uriFile, false);

			unversionedModel = UnversionedConverter.getUnversionedModel(model, mmiUri);
			
			if ( unversionedModel != null ) {
				// but put both variables to the same unversioned model
				model = unversionedModel;
				if ( log.isDebugEnabled() ) {
					log.debug("dispatch: using obtained unversioned model");
				}
			}
			else {
				// error in conversion to unversioned version.
				// this is unexpected. 
				// Continue with original model, if necessary; see below.
				log.error("dispatch: unexpected: error in conversion to unversioned version.  But continuing with original model");
			}
		}
		else {
			model = JenaUtil.loadModel(uriFile, false);
		}
		

		
		return true;
	}
	
	
	

}
