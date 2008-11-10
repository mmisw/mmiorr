/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Luis Bermudez, MBARI/MMI
 * Author email       bermudez@mbari.org
 * Package            org.mmi.ont.voc2owl.trans
 * Web                http://marinemetadata.org/mmitools
 * Filename           $RCSfile: TransProperties.java,v $
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
 * <p>
 * Holds the name of the properties of a transformation property file. This is a
 * convenient class that should be used to get the name of the properties
 * instead of using the properties names directly.
 * </p>
 * 
 * The following table presents in the first two columns a list of properties
 * and their definition. The third and fourth columns indicate whether the
 * property is mandatory when converting plain or hierarchical vocabularies.
 * <table summary="Properties for transformation" border="1" cellspacing="2"
 * cellpadding="2">
 * <tr>
 * <th> Name </th>
 * <th> Definition </th>
 * <th> Plain </th>
 * <th> Hierarchical </th>
 * </tr>
 * <tr>
 * <td> columnForPrimaryClass </td>
 * <td> In plain list, usually is the first column. Individuals will be created
 * from this column. </td>
 * <td> X </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td> contributor </td>
 * <td> Contributor of the original vocabulary </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * 
 * <tr>
 * <td> convertToClass </td>
 * <td> Columns form which resources will be generated. A class will be created
 * with the column name and individuals will be created in that column. If more
 * than one is to be created, separate the values by comma ",". If the source is
 * a plain list, this is not obligatotory. </td>
 * <td>
 * 
 * </td>
 * <td> X </td>
 * </tr>
 * 
 * <tr>
 * <td> createAllRelationsHierarchy </td>
 * <td> To convert relations upwards. Vocabularies in plain lists are presented
 * in table-liked format. For example Column 1 is related to column 2 and this
 * relation will be created (if both are to be converted to classes). If
 * createAllRelationsHierarchy is true then the inverse relation is also created
 * (column 2-1 relation). </td>
 * <td> Never used </td>
 * <td> X </td>
 * </tr>
 * 
 * <tr>
 * <td> description </td>
 * <td> Tells what the ontology is about. Few sentences is OK. </td>
 * <td>
 * 
 * </td>
 * <td>
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td> fileIn </td>
 * <td> The path of the file in ASCII in tab or comma separated value that
 * contains the vocabulary in simple format </td>
 * <td> X </td>
 * <td> X </td>
 * </tr>
 * 
 * <tr>
 * <td> fileOut </td>
 * <td> The path of the file where the OWL file will be created </td>
 * <td> X </td>
 * <td> X </td>
 * </tr>
 * 
 * 
 * <tr>
 * <td> format </td>
 * <td> Format of the input file (accepted values are tab or csv). The former
 * for tab separated values, the later for comma separated values. </td>
 * <td> X </td>
 * <td> X </td>
 * </tr>
 * 
 * <tr>
 * <td> generateAutoIds </td>
 * <td> In plain lists, an auto generated id can be created instead of using the
 * label in the column for columnForPrimaryClass </td>
 * <td> X </td>
 * <td> Never used </td>
 * </tr>
 * 
 * <tr>
 * <td> nameForPrimaryClass </td>
 * <td> Name of the primary class </td>
 * <td> X </td>
 * <td> Never used </td>
 * </tr>
 * 
 * <tr>
 * <td> NS </td>
 * <td> namespace of the ontology </td>
 * <td> X </td>
 * <td> X </td>
 * </tr>
 * 
 * <tr>
 * <td> source </td>
 * <td> Source of the vocabulary. Like the version, or the file or document the
 * simple format came from. </td>
 * <td>
 * 
 * </td>
 * <td>
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td> subject </td>
 * <td> Keywords to identify the ontology </td>
 * <td>
 * 
 * </td>
 * <td>
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td> title </td>
 * <td> Title of the ontology </td>
 * <td>
 * 
 * </td>
 * <td>
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td> treatAsHierarchy </td>
 * <td> If the vocabulary in simple format is a hierarchical vocabulary this
 * should be true, if plain then should be false </td>
 * <td> X </td>
 * <td> X </td>
 * </tr>
 * 
 * <tr>
 * <td> URLMoreInformation </td>
 * <td> URL for more information about this vocabulary </td>
 * <td>
 * 
 * </td>
 * <td>
 * 
 * </td>
 * </tr>
 * 
 * </table>
 * <hr>
 * 
 * 
 */

public interface TransProperties {
	public static final String title = "title";

	public static final String description = "description";

	public static final String contributor = "contributor";

	public static final String creator = "creator";

	public static final String source = "source";

	public static final String URLMoreInformation = "URLMoreInformation";

	public static final String subject = "subject";

	public static final String format = "format";// "csv" or "tab"

	public static final String fileIn = "fileIn";

	public static final String fileOut = "fileOut";

	public static final String NS = "NS";

	public static final String convertToClass = "convertToClass";

	public static final String columnForPrimaryClass = "columnForPrimaryClass";

	public static final String nameForPrimaryClass = "nameForPrimaryClass";

	public static final String treatAsHierarchy = "treatAsHierarchy";

	public static final String createAllRelationsHierarchy = "createAllRelationsHierarchy";

	public static final String generateAutoIds = "generateAutoIds";

	public static final String convertionType = "convertionType";

	public static final String[] properties = { title, description,
			contributor, source, URLMoreInformation, subject,
			
			format, fileIn, fileOut, NS,

			convertToClass, columnForPrimaryClass, nameForPrimaryClass,
			treatAsHierarchy, createAllRelationsHierarchy,

			generateAutoIds, convertionType };

}
