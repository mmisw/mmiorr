package org.mmisw.ont.triplestore;

/**
 * Misc utilities.
 * @author Carlos Rueda
 */
public class TsUtil {
	
	public static String elapsedTime(long start) {
		long total = System.currentTimeMillis() - start;
		long min = total/60000;
		long msec = total%60000;
		double sec = msec/1000.0;
		String report;
		if (min > 0) {
			report = min + ":" + sec + " minutes:seconds";
		} else {
			report = sec + " seconds";
		}
		return report;
	}


	private TsUtil() {}
}
