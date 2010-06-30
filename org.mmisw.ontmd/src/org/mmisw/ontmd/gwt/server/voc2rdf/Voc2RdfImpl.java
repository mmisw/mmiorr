package org.mmisw.ontmd.gwt.server.voc2rdf;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfBaseInfo;
import org.mmisw.ontmd.gwt.server.PortalConfig;

import com.google.gwt.user.client.Window;



/**
 * The main Voc2Rdf back-end operations. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class Voc2RdfImpl  {

	private final Log log = LogFactory.getLog(Voc2RdfImpl.class);
	
	private final AppInfo appInfo = new AppInfo("MMI Voc2RDF");
	
	private Voc2RdfBaseInfo baseInfo = null;
	
	
	public Voc2RdfImpl() {
		log.info("initializing " +appInfo.getAppName()+ "...");
		appInfo.setVersion(PortalConfig.Prop.VERSION.getValue());
		appInfo.setBuild(PortalConfig.Prop.BUILD.getValue());

		log.info(appInfo.toString());
	}

	public AppInfo getAppInfo() {
		return appInfo;
	}
	

	public Voc2RdfBaseInfo getBaseInfo() {
		if ( baseInfo == null ) {
			prepareBaseInfo();
		}
		return baseInfo;
	}
	
	private void prepareBaseInfo() {
		Window.alert("Voc2RdfImpl: OLD prepareBaseInfo called.  Please report this bug!");
//		log.info("preparing base info ...");
//		
//		// prepare all the information (as in ontmd) even that voc2rdf is just going to use a few of the attributes:
//		MdHelper_OLD.prepareGroups(false);
//		
//		baseInfo = new Voc2RdfBaseInfo();
//		baseInfo.setResourceTypeAttrDef(MdHelper_OLD.getResourceTypeAttrDef());
//		
//		Map<String,AttrDef> attrDefMap = new HashMap<String,AttrDef>();
//		Map<String, AttrDef> uriAttrDefMap = MdHelper_OLD.getAttrDefMap();
//		attrDefMap.put("fullTitle", uriAttrDefMap.get(Omv.name.getURI()));
//		attrDefMap.put("creator", uriAttrDefMap.get(Omv.hasCreator.getURI()));
//		attrDefMap.put("description", uriAttrDefMap.get(Omv.description.getURI()));
//		attrDefMap.put("authority", uriAttrDefMap.get(OmvMmi.origMaintainerCode.getURI()));
//		baseInfo.setAttrDefMap(attrDefMap);
//		
//		log.info("preparing base info ... Done.  attrDefMap = " +attrDefMap);
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
		
		String orgAbbreviation = values.get("authority");

		if ( orgAbbreviation == null ) {
			conversionResult.setError("missing authority");
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
			log.error(e1, e1);
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
			conversionResult.setFinalShortName(ontConverter.getFinalShortName());
			conversionResult.setPathOnServer(ontConverter.getPathOnServer());
			
			// RDF string no longer sent to client (there is a new "download" action)
//			String rdf = ontConverter.getOntologyStringXml();
//			conversionResult.setRdf(rdf);
		}

		return conversionResult;
	}
	
}
