package org.mmisw.mmiorr.client;


/**
 * Some addresses associated with the MMI ORR deployment being used/tested.
 * By default, these addresses are in reference to http://mmisw.org/, but
 * system properties can be used to specify the deployment:
 * <ul>
 * <li> ontService: http://mmisw.org/ont, by default.
 * <li> direg: http://mmisw.org/orr/direg, by default.
 * </ul>
 * 
 * If the system property "mmiorr.host" is defined and not blank, then only this property will 
 * be used as the host to define the properties above.
 * 
 * @author Carlos Rueda
 */
public class MmiOrr {
	/** URL of the Ont service servlet */
	public static String ontService;
	
	/** URL of the direg servlet for direct registration */
	public static String direg;
	
	static {
		
		// get mmiorr.host property (removing any trailing slashes)
		String mmiorr_host = System.getProperty("mmiorr.host", "").trim().replaceAll("/+$", "");
		if ( mmiorr_host.length() > 0 ) {
			ontService = mmiorr_host + "/ont";
			direg = mmiorr_host + "/orr/direg";
		}
		else {
			ontService = System.getProperty("ontService", "http://mmisw.org/ont");
			direg = System.getProperty("direg", "http://mmisw.org/orr/direg");
		}
	}
	
	public static void main(String[] _) {
		System.out.println("MmiOrr: ontService: " +ontService);
		System.out.println("MmiOrr:      direg: " +direg);
	}
	
	private MmiOrr() {}
}
