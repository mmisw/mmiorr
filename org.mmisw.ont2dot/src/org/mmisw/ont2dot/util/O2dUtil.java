package org.mmisw.ont2dot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Misc utilities.
 * @author Carlos Rueda
 */
public class O2dUtil {
	
	/**
	 * Does glob expansion of "{elems,...}" patterns in a string. Examples:
	 * <br>
	 * <br>Input: <code>p{FOO,BAR}s</code>
	 * <br>Output: <code>[pFOOs, pBARs]</code>, 
	 * <br>
	 * <br>Input: <code>p{FOO,BAR{1,2}}s{Z,W}</code>
	 * <br>Output: <code>[pFOOsZ, pBAR1sZ, pFOOsZ, pBAR2sZ, pFOOsW, pBAR1sW, pFOOsW, pBAR2sW]</code>, 
	 * <br>
	 * <p>
	 * NOTE: the curly braces are always intepreted, ie., not yet any way to escape them.
	 * 
	 * @param input
	 * @return
	 */
	public static List<String> expandGlob(String input) {
		List<String> list = new ArrayList<String>();
		_expand(input, list);
		return list;
	}

	private static List<String> _expand(String input, List<String> list) {
		Matcher m = pattern.matcher(input);
		
		if ( m.matches() ) {
			int start = m.start(1);
			int end = m.end(1);
			
			// -1,+1 to drop the curly braces
			String prefix = input.substring(0, start-1);
			String suffix = input.substring(end+1);
			
			// the set of elements within the braces
			String set = input.substring(start, end);
			
			for ( String elem : set.split(",") ) {
				elem = elem.trim();
				// recursive call for each element in the set
				_expand(prefix + elem + suffix, list);
			}
		}
		else {
			list.add(input);
		}
		
		return list;
	}

	public static void main(String[] args) throws Exception {
		String[] tests = {
				"p{FOO,BAR}s",
				"p{FOO, BAR{1, 2} }s{Z,W}",
				"p{}s",
		};
		for ( String input : tests ) {
			System.out.println(input+ " -> " +expandGlob(input));
		}
	}

	// private
	private O2dUtil() {}
	
	
	private static Pattern pattern = Pattern.compile(".*\\{([^\\}]*)\\}.*");
	
}
