package org.mmisw.watchdog.util;

import java.util.Iterator;
import java.util.List;

import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Text;


/**
 * Some utilities.
 * 
 * @author Carlos Rueda
 */
public class XmlUtil  {
	
	/**
	 * Collects the leading comments of the given element.
	 * Leading means that only the initial comments before any other type
	 * of content (except org.jdom.Text, which is ignored/skipped) are considered.
	 * 
	 * @param ele  the element.
	 * @return A string of all the comments separated by "\n";  null if no comments.
	 */
	public static String getComment(Element ele) {
		StringBuffer comment = new StringBuffer();
		
		List<?> content = ele.getContent();
		String nl = "";
		for (Iterator<?> iterator2 = content.iterator(); iterator2.hasNext();) {
			Object obj = iterator2.next();
		
			if ( obj instanceof Comment ) {
				String str = ((Comment) obj).getText().trim();
				if ( str.length() > 0 ) {
					comment.append(str + nl);
					nl = "\n";
				}
			}
			else if ( obj instanceof Text ) {
				// ok; igone and continue
			}
			else {
				// other type of content; stop here.
				break;
			}
		}
		
		return comment.length() > 0 ? comment.toString() : null;
	}
	
	
	private XmlUtil() {}
}
