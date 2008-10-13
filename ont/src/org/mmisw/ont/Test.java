package org.mmisw.ont;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

public class Test {

	public static void main(String[] args) throws Exception {
		String connection = "jdbc:mysql://localhost/bioportal";
		
		if ( args.length > 0 ) {
			connection = args[0];
		}
		System.out.println(" using connection = " +connection);

		String DRIVER = "com.mysql.jdbc.Driver";
		String user = "root";
		String password = "msql1973";

		String table = "v_ncbo_ontology";
		int limit = 500;
		String where = "";
		
		Connection _con;
		Statement _stmt;

		Class.forName(DRIVER);


		Properties props = new Properties();
		props.put("CONNECTION", connection);
		props.put("user", user);
		props.put("password", password);
		_con = DriverManager.getConnection(connection, props);
		_stmt = _con.createStatement();


		String query = "select * from " +table+ " " +where+ " limit " +limit;
		
		ResultSet rs = _stmt.executeQuery(query);
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        for (int i = 0; i < cols; i++) {
            System.out.printf("%20s | ", md.getColumnLabel(i+1));
        }
        System.out.println();

        while ( rs.next() ) {
        	for (int i = 0; i < cols; i++) {
        		System.out.printf("%20s | ", rs.getObject(i+1));
        	}
        	System.out.println();
        }

	}
}
