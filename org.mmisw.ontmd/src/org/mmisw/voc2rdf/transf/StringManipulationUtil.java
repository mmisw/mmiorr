/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Luis Bermudez, MBARI/MMI
 * Author email       bermudez@mbari.org
 * Package            org.mmi.ont.voc2owl.trans
 * Web                http://marinemetadata.org/mmitools
 * Filename           $RCSfile: StringManipulationUtil.java,v $
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * Utility class that performs a string manipulation. It replaces this pattern
 * <i>[^a-zA-Z0-9-_]+</i> with =<i>_</i> and this pattern <i>(_+)$</i> with
 * nothing (deletes it). If it starts with a number an underscore is added. Also
 * it replaces any 2 consecutive underscore with one underscore.
 * </p>
 * <hr>
 * 
 * @author : $Author: luisbermudez $
 * @version : $Revision: 1.1 $
 * @since : Aug 8, 2006
 */

public class StringManipulationUtil implements StringManipulationInterface {

	private static final String[] patternStrings = { "[^a-zA-Z0-9-_]+", "(_+)$" };
	private static final String[] replace = { "_", "" };

	private static final Pattern[] patterns = new Pattern[patternStrings.length];
	
	static {
		for (int i = 0; i < patterns.length; i++) {
			patterns[i] = Pattern.compile(patternStrings[i]);
		}
	}
	
	private static final Pattern p_ = Pattern.compile("[^a-zA-Z_]");
	private static final Pattern p_2 = Pattern.compile("_{2,}");

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mmi.ont.voc2owl.trans.StringManipulationInterface#replaceString(java.lang.String)
	 */
	public String replaceString(String s) {
		String rep = s;

		for (int i = 0; i < patterns.length; i++) {
			Matcher m = patterns[i].matcher(rep);

			rep = m.replaceAll(replace[i]);
		}

		return clean(appendUnderScoreStart(rep));

	}

	private String appendUnderScoreStart(String s) {
		String rep = s;
		// if starts with a non a-z or A_Z

		Matcher m_ = p_.matcher(rep);

		if (m_.lookingAt()) {
			rep = "_" + rep;
		}
		return rep;

	}

	private String clean(String s) {
		String rep = s;
		Matcher m_ = p_2.matcher(rep);
		rep = m_.replaceAll("_");

		return rep;
	}

}