package org.mmisw.iserver.core.util.charset;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.mmisw.iserver.core.util.Utf8Util.ICharsetDetector;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Charset detection based on <a href="http://site.icu-project.org/">ICU</a>.
 * 
 * See <a href="http://icu-project.org/apiref/icu4j/com/ibm/icu/text/CharsetDetector.html"
 * >this class</a> 
 * 
 * @author Carlos Rueda
 */
public class CharsetDetectorIcu implements ICharsetDetector {

	public Collection<String> detectCharset(byte[] bytes) {
		
		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);
		
		CharsetMatch[] matches = detector.detectAll();
		if ( matches == null || matches.length == 0 ) {
			return null;
		}
		
		Collection<String> charsets = new LinkedHashSet<String>();
		for ( CharsetMatch match : matches ) {
			charsets.add(match.getName());
		}
		
		return charsets;
	}
}
