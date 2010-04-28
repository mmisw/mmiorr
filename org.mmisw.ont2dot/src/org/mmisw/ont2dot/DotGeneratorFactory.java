package org.mmisw.ont2dot;

import org.mmisw.ont2dot.impl.jena.DotGeneratorJenaImpl;


/**
 * Creates instances of {@link IDotGenerator}.
 * 
 * @author Carlos Rueda
 */
public class DotGeneratorFactory {

	/** 
	 * Creates a dot generator.
	 */
	public static IDotGenerator createInstance() {
		IDotGenerator dg = new DotGeneratorJenaImpl();
		return dg;
	}

}
