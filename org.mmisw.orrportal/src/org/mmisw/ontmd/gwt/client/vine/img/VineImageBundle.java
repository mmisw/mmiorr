package org.mmisw.ontmd.gwt.client.vine.img;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

public interface VineImageBundle extends ImageBundle {
	
	public AbstractImagePrototype vine();
	public AbstractImagePrototype vinealpha();
	public AbstractImagePrototype loading();
	
	/** Reduced version of http://photos.icollector.com/photos/question_icon.gif */
	public AbstractImagePrototype question12();
	
	public AbstractImagePrototype exactMatch28();
	public AbstractImagePrototype closeMatch28();
	public AbstractImagePrototype broadMatch28();
	public AbstractImagePrototype narrowMatch28();
	public AbstractImagePrototype relatedMatch28();
	
	 public AbstractImagePrototype broaderThan();
	 public AbstractImagePrototype narrowerThan();
	 public AbstractImagePrototype sameAs();
	 public AbstractImagePrototype subClassOf();
	 public AbstractImagePrototype superClassOf();

	 public AbstractImagePrototype search();
	 
	 public AbstractImagePrototype explicit();
	 public AbstractImagePrototype inferred();
	 
	 public AbstractImagePrototype check();
	 public AbstractImagePrototype delete();
	 public AbstractImagePrototype metadata();

}
