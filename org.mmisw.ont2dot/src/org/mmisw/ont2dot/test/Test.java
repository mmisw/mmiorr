package org.mmisw.ont2dot.test;

import org.mmisw.ont2dot.Ont2Dot;

public class Test {
	public static void main(String[] args) throws Exception {
		Ont2Dot.main(new String[] {
				"--separate" , 
				"http://mmisw.org/ont/mmi/device/" +
				    "{TypedValue,PhysicalProperty,ModelID, Manufacturer}",
				
				"file:/Users/carueda/mmiworkspace/devont/device/device.owl"
//				 "http://mmisw.org/ont/mmi/device"
		});
	}
}
