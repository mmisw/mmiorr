/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Luis Bermudez, MBARI/MMI
 * Author email       bermudez@mbari.org
 * Package            org.mmi.ont.voc2owl.trans
 * Web                http://marinemetadata.org/mmitools
 * Filename           $RCSfile: StringManipulationInterface.java,v $
 * Revision           $Revision: 1.1 $
 *
 * Last modified on   $Date: 2007/12/11 18:13:24 $
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

package org.mmisw.voc2rdf.transf;

/**
 *<p> Interface for String Manipulation when creating URIs from arbitrary stings. </p><hr>
 * @author  : $Author: luisbermudez $
 * @version : $Revision: 1.1 $
 * @since   : Aug 8, 2006
*/

public interface StringManipulationInterface {

	/**<p> 
	 * Answers a String that has no spaces and that conforms to the local part name of
	 *  a qualified XML Qualified Name. ( More information here: 
	 *  <a href="http://www.w3.org/TR/REC-xml-names/#NT-LocalPart">http://www.w3.org/TR/REC-xml-names/#NT-LocalPart</a>). 
	 *  The returned String (<i>rs</i>)  can be used to create a URL, given a namespace N. For example <i>N#rs</i>. 
	 *  </p>
	 *  
	 * @param s arbitrary String that can include spaces and other characters
	 * @return string with no spaces and that can be used to construct the local part of a URI
	 */
	public abstract String replaceString(String s);

}