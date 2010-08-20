package org.mmisw.iserver.core.util.ontinfo;

/**
 * An (s,p,o) composite key.
 * 
 * <p>
 * See <a href="http://java.sun.com/developer/Books/effectivejava/Chapter3.pdf">Effective Java</a>, 
 * <a href="http://www.javaspecialists.eu/archive/Issue134.html">DRY Performance</a>
 * 
 * @author Carlos Rueda
 */
public final class StmtKey {
	private final String sjt;
	private final String prd;
	private final String obj;

	public StmtKey(String s, String p, String o) {
		this.sjt = s;
		this.prd = p;
		this.obj = o;
	}

	public final boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof StmtKey)) {
			return false;
		}
		StmtKey x = (StmtKey) other;
		if (sjt == null ? x.sjt != null : !sjt.equals(x.sjt)) {
			return false;
		}
		if (prd == null ? x.prd != null : !prd.equals(x.prd)) {
			return false;
		}
		if (obj == null ? x.obj != null : !obj.equals(x.obj)) {
			return false;
		}
		return true;
	}

	public final int hashCode() {
		int result;
		result = (sjt != null ? sjt.hashCode() : 0);
		result = (prd != null ? prd.hashCode() : 0) + 31 * result;
		result = (obj != null ? obj.hashCode() : 0) + 31 * result;
		return result;
	}

	public final String getSubject() {
		return sjt;
	}

	public final String getPredicate() {
		return prd;
	}
	public final String getObject() {
		return obj;
	}
}
