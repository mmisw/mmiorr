package org.mmisw.ont.util;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// from Luis, 2008-10-13

// carlos: 
//   - xslt param as InputStream 
//   - Logger

public class XSLTCreator {

	private final static Log log = LogFactory.getLog(XSLTCreator.class);
	
	public static String create(String inXML, InputStream xslt) {
		Source xmlSource = new StreamSource(new StringReader(inXML));
		Source xslSource = new StreamSource(xslt);
		System.setProperty("javax.xml.transform.TransformerFactory",
			"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		TransformerFactory transFact = TransformerFactory.newInstance();
		Transformer trans = null;
		try {
			trans = transFact.newTransformer(xslSource);
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			trans.transform(xmlSource, result);
			StringBuffer buff = sw.getBuffer();
			sw.close();
			return buff.toString();

		} catch (Exception ex) {
			log.warn("error while transforming", ex);
		}
		return null;

	}

}
