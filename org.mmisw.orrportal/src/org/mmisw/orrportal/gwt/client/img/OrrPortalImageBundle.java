package org.mmisw.orrportal.gwt.client.img;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * The images for this application.
 * @author Carlos Rueda
 */
public interface OrrPortalImageBundle extends ImageBundle {
	
	/** From: <a href="http://www.sanbaldo.com/wordpress/1/ajax_gif/
	 *  >http://www.sanbaldo.com/wordpress/1/ajax_gif/</a>
	 */
	public AbstractImagePrototype mozilla_blu();

	/** Reduced version of http://photos.icollector.com/photos/question_icon.gif */
	public AbstractImagePrototype question12t(); // transparent background
	public AbstractImagePrototype question12();  // opaque (white) background

	public AbstractImagePrototype search();
	 
	public AbstractImagePrototype voc2rdf2();
	
	
	public AbstractImagePrototype tridown();
	public AbstractImagePrototype triright();
	
	public AbstractImagePrototype arrow_asc();
	public AbstractImagePrototype arrow_desc();
}
