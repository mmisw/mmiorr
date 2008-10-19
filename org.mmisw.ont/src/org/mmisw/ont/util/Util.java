package org.mmisw.ont.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Misc utilities.
 * @author Carlos Rueda
 */
public class Util {

	/** @returns true iff the given param is defined in the request
	 * AND either no value is associated OR none of the values is equal to "n".
	 */
	public static boolean yes(HttpServletRequest request, String param) {
		List<String> values = getParamValues(request, param);
		return values != null && ! values.contains("n");
	}

	/** @returns the list of values associated to the given param.
	 * null if the param is not included in the request.
	 */
	public static List<String> getParamValues(HttpServletRequest request, String param) {
		Map<String, String[]> params = getParams(request);
		String[] vals = params.get(param);
		if ( null == vals ) {
			return null;
		}
		List<String> list = Arrays.asList(params.get(param));
		return list;
	}


	/** @returns The last value associated with the given parameter. If not value is 
	 * explicitly associated, it returns the given default value.
	 */
	public static String getParam(HttpServletRequest request, String param, String defaultValue) {
		Map<String, String[]> params = getParams(request);
		String[] array = params.get(param);
		if ( array == null || array.length == 0 ) {
			return defaultValue;
		}
		// return last value in the array: 
		return array[array.length -1];
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String[]> getParams(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
		return params;
	}

	private Util() {}
}
