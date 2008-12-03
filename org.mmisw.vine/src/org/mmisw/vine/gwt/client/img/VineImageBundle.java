package org.mmisw.vine.gwt.client.img;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

public interface VineImageBundle extends ImageBundle {
	
	 public AbstractImagePrototype broaderThan();
	 public AbstractImagePrototype narrowerThan();
	 public AbstractImagePrototype sameAs();
	 public AbstractImagePrototype subClassOf();
	 public AbstractImagePrototype superClassOf();

	 public AbstractImagePrototype search();
	 
	 public AbstractImagePrototype explicit();
	 public AbstractImagePrototype inferred();

}
