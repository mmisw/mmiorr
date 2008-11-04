package org.mmisw.voc2rdf.gwt.server;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.mmisw.ont.vocabulary.util.MdHelper;
import org.mmisw.voc2rdf.gwt.client.rpc.BaseInfo;
import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;
import org.mmisw.voc2rdf.gwt.client.rpc.Voc2RdfService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;



/**
 * Implementation of Voc2RdfService. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class Voc2RdfServiceImpl extends RemoteServiceServlet implements Voc2RdfService {
	private static final long serialVersionUID = 1L;

	
	private static Logger log = Logger.getLogger(Voc2RdfServiceImpl.class);
	
	private BaseInfo baseInfo = null;
	
	
	public BaseInfo getBaseInfo() {
		if ( baseInfo == null ) {
			prepareBaseInfo();
		}
		return baseInfo;
	}
	
	private void prepareBaseInfo() {
		log.info("preparing base info ...");
		baseInfo = new BaseInfo();
		baseInfo.setMainClassAttrDef(MdHelper.getMainClassAttrDef());
	}
	
	
	public ConversionResult convert(Map<String, String> values) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("convert: values:");
			for ( Entry<String, String> e : values.entrySet() ) {
				log.debug("    " +e.getKey()+ " = " +e.getValue());
			}
		}
		
		ConversionResult conversionResult = new ConversionResult();

		String namespaceRoot = values.get("namespaceRoot");
		values.remove("namespaceRoot");
		String ascii = values.get("ascii");
		values.remove("ascii");
		String fieldSeparator = values.get("fieldSeparator");
		values.remove("fieldSeparator");

		if ( namespaceRoot == null ) {
			conversionResult.setError("missing namespaceRoot");
			return conversionResult;
		}
		if ( ascii == null ) {
			conversionResult.setError("missing ascii");
			return conversionResult;
		}
		if ( fieldSeparator == null ) {
			conversionResult.setError("missing fieldSeparator");
			return conversionResult;
		}
		
		String orgAbbreviation = "_tmp_"; //values.get(OmvMmi.origMaintainerCode.getURI());

		if ( orgAbbreviation == null ) {
			conversionResult.setError("missing origMaintainerCode");
			return conversionResult;
		}
		
		String primaryClass = values.get("primaryClass");   //  Omv.acronym.getURI());
		if ( primaryClass == null ) {
			conversionResult.setError("missing acronym");
			return conversionResult;
		}

		
		
		Converter ontConverter = new Converter(
				namespaceRoot,
				orgAbbreviation,
				primaryClass,
				ascii,
				fieldSeparator,
				values);
		
		log.info("converter created.");
		
		
		String error;
		try {
			error = ontConverter.createOntology();
		}
		catch (Exception e1) {
			log.error(e1);
			conversionResult.setError(e1.getClass().getName()+ " : " +e1.getMessage());
			return conversionResult;
		}
		
		
		if ( error != null ) {
			conversionResult.setError(error);
			log.info("createOntology returned: " +error);
		}
		else {
			String finalUri = ontConverter.getFinalUri();
			conversionResult.setFinalUri(finalUri);
			conversionResult.setPathOnServer(ontConverter.getPathOnServer());
			String rdf = ontConverter.getOntologyStringXml();
			conversionResult.setRdf(rdf);
		}

		return conversionResult;
	}
	
}
