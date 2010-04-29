/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Luis Bermudez, MBARI/MMI
 * Author email       bermudez@mbari.org
 * Package            org.mmi.ont.watchdog
 * Web                http://marinemetadata.org/mmitools
 * Filename           $RCSfile: SKOS.java,v $
 * Revision           $Revision: 1.1 $
 *
 * Last modified on   $Date: 2007/12/11 18:06:14 $
 *               by   $Author: luisbermudez $
 *
 * (c) Copyright 2005, 2006 Monterey Bay Aquarium Research Institute
 * Marine Metadata Interoperability (MMI) Project http://marinemetadata.org
 *
 * License Information
 * ------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, you can download it from 
 *  http://marinementadata.org/gpl or write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *********************************************************************************/

package org.mmisw.watchdog.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 *<p> Class that contains selected resources to create an SKOS model. More info here: http://www.w3.org/2004/02/skos/core#. </p><hr>
 * @author  : $Author: luisbermudez $
 * @version : $Revision: 1.1 $
 * @since   : Aug 7, 2006
*/

public class SKOS {
	private static Model model = ModelFactory.createDefaultModel();

	public static String NS = "http://www.w3.org/2004/02/skos/core#";

//	public static Resource ConceptScheme = model.createResource(NS
//			+ "ConceptScheme", RDFS.Class);
//
	public static Resource Concept = model.createResource(NS + "Concept",
			RDFS.Class);
	
	public static Property definition = model.createProperty(NS + "definition");
	
	
	public static Property hasTopConcept = 
		model.createProperty(NS + "hasTopConcept");


	public static Property broader = 
			model.createProperty(NS + "broader");
	public static Property narrower = 
		model.createProperty(NS + "narrower");
	public static Property related = 
		model.createProperty(NS + "related");
	
	public static Property prefLabel = 
		model.createProperty(NS + "prefLabel");
	
	public static Model getAnSKOSModel(){
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefix("skos", SKOS.NS);
		m.add(SKOS.model);
		
//		m.add(SKOS.broader, OWL.inverseOf, SKOS.narrower);
//		m.add(SKOS.broader, RDF.type,  OWL.TransitiveProperty);
//		m.add(SKOS.broader, RDFS.range,  OWL.TransitiveProperty);
//		m.add(SKOS.narrower, RDF.type,  OWL.TransitiveProperty);
//		m.add(SKOS.related, RDF.type, OWL.SymmetricProperty);
		
		return m;
	}
	
	
	

	public static void main(String[] args) {
	
	}
}
