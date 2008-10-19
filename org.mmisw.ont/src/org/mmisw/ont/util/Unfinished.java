package org.mmisw.ont.util;

import java.lang.annotation.*;

/**
 * An annotation for unfinished implementations.
 * See <a href="http://www.oracle.com/technology/pub/articles/hunter_meta_2.html"
 * >http://www.oracle.com/technology/pub/articles/hunter_meta_2.html</a>
 * 
 * @author Carlos Rueda
 */
@Unfinished      // the Unfinished annotation is itself unfinished ;)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Unfinished {
	public enum Priority { LOW, MEDIUM, HIGH }

	String[] owners() default "TBD";
	Priority priority() default Priority.MEDIUM;
}
