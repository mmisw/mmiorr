package org.mmisw.ont.util;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

// from Luis, 2008-10-13

public class XSLTCreator {

	public static String create(String inXML, File xslt) {
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

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

}
