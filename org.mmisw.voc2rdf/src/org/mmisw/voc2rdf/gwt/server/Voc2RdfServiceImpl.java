package org.mmisw.voc2rdf.gwt.server;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.mmisw.ont.vocabulary.util.MdHelper;
import org.mmisw.voc2rdf.gwt.client.rpc.Attribute;
import org.mmisw.voc2rdf.gwt.client.rpc.BaseInfo;
import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;
import org.mmisw.voc2rdf.gwt.client.rpc.UploadResult;
import org.mmisw.voc2rdf.gwt.client.rpc.Voc2RdfService;
import org.mmisw.voc2rdf.gwt.client.vocabulary.AttrDef;
import org.mmisw.voc2rdf.gwt.client.vocabulary.AttrGroup;

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
		
		baseInfo.removeAttributes();
		
		
		AttrGroup[] attrGroups = MdHelper.getAttrGroups();
		
		baseInfo.setAttrGroups(attrGroups);
		
		
		// to be moved
		for ( AttrGroup attrGroup: attrGroups ) {
			AttrDef[] attrDefs = attrGroup.getAttrDefs();
			for ( AttrDef attrDef: attrDefs ) {
				
				String ns = attrDef.getNameSpace();
				String localName = attrDef.getLocalName();
				
				String prefix = MdHelper.getPreferredPrefix(ns);
				
				Attribute attr = new Attribute();
				attr.setName(prefix+ ":" +localName );
				attr.setTooltip(prefix+ ":" +localName+ " = " + attrDef.getUri());
				
				String label = localName.length() > 0 
						? Character.toUpperCase(localName.charAt(0)) + localName.substring(1)
						: localName;
				if ( attrDef.isRequired() ) {
					label = "*" + label;
				}
				attr.setLabel(label);

				baseInfo.addAttribute(attr);
			}
		}
		
		
//		MdHelper mdHelper = new MdHelper();
//		
//		Collection<AttributeValue> attrVals = mdHelper.getAttributes();
//		for ( AttributeValue attrVal : attrVals ) {
//			Property prop = attrVal.getProperty();
//			String ns = prop.getNameSpace();
//			String localName = prop.getLocalName();
//			
//			String prefix = MdHelper.getPreferredPrefix(ns);
//			
//			Attribute attr = new Attribute();
//			attr.setName(prefix+ ":" +localName );
//			attr.setTooltip(prefix+ ":" +localName );
//			
//			String label = localName.length() > 0 
//					? Character.toUpperCase(localName.charAt(0)) + localName.substring(1)
//					: localName;
//			if ( attrVal.isRequired() ) {
//				label = "*" + label;
//			}
//			attr.setLabel(label);
//
//			baseInfo.addAttribute(attr);
//		}
		
	}
	
	
	public ConversionResult convert(Map<String, String> values) {
		
		log.info("convert: values:");
		for ( Entry<String, String> e : values.entrySet() ) {
			log.info("    " +e.getKey()+ " = " +e.getValue());
		}
		
		ConversionResult result = new ConversionResult();

		String namespaceRoot = values.get("namespaceRoot");
		String primaryClass = values.get("acronym");
		String orgAbbreviation = values.get("origMaintainerCode");
		String ascii = values.get("ascii");
		String fieldSeparator = values.get("fieldSeparator");

		if ( namespaceRoot == null ) {
			result.setError("missing namespaceRoot");
		}
		if ( orgAbbreviation == null ) {
			result.setError("missing origMaintainerCode");
		}
		if ( ascii == null ) {
			result.setError("missing ascii");
		}
		if ( fieldSeparator == null ) {
			result.setError("missing fieldSeparator");
		}
		if ( primaryClass == null ) {
			result.setError("missing acronym");
		}
		
		if ( result.getError() != null ) {
			return result;
		}

		
		Converter ontConverter = new Converter(
				namespaceRoot,
				primaryClass,
				orgAbbreviation,
				ascii,
				fieldSeparator,
				values);
		
		log.info("converter created.");
		
		
		String res;
		try {
			res = ontConverter.createOntology();
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			result.setError(e1.getClass().getName()+ " : " +e1.getMessage());
			return result;
		}
		
		log.info("createOntology returned: " +res);
		
		if ( res.equals("failure") ) {
			result.setError(res);
		}
		else {
			String finalUri = ontConverter.getFinalUri();
			result.setFinalUri(finalUri);
			String rdf = ontConverter.getOntologyStringXml();
			result.setRdf(rdf);
		}

		return result;
	}
	
	
	public UploadResult upload(ConversionResult conversionResult, Map<String,String> values) {
		
		log.info(": uploading ...");
		UploadResult uploadResult = new UploadResult();
		String sessionId;
		
		String userName = values.get("userId");
		String userPassword = values.get("userPassword");
		
		try {
			Login login = new Login(userName, userPassword);
			sessionId = login.getSessionId();

			String uri = conversionResult.getFinalUri();
			String fileName;
			fileName = new URL(uri).getPath();
			String rdf = conversionResult.getRdf();
			OntologyUploader createOnt = new OntologyUploader(uri, fileName, rdf , userName, sessionId);
			String res = createOnt.create();
			
			if ( res.startsWith("OK") ) {
				uploadResult.setInfo(res);
			}
			else {
				uploadResult.setError(res);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			uploadResult.setError(ex.getClass().getName()+ ": " +ex.getMessage());
		}
		
		log.info("uploadResult = " +uploadResult);
		
		return uploadResult;
	}

}
