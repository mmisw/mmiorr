package org.mmisw.ont.triplestore.virtuoso.test;

import virtuoso.jena.driver.VirtGraph;

class ClearTripleStore {

	private static String _host = "jdbc:virtuoso://mmi2.shore.mbari.org:1111";
	private static String _username = System.getProperty("virtuoso.username");
	private static String _password = System.getProperty("virtuoso.password");

	public static void main(String[] args) throws Exception {
//		_removeGraph(null);
		_removeGraph("");
	}
	
	private static void _removeGraph(String graphName) {
		VirtGraph set;
		if ( graphName != null ) {
			set = new VirtGraph(graphName, _host, _username, _password);
		}
		else {
			set = new VirtGraph (_host, _username, _password);
		}
		
		System.out.println("graphName=" +graphName+ " size = " +set.size());
		set.clear();
		System.out.println("graphName=" +graphName+ " size = " +set.size());
	}
}