package org.mmisw.ontmd.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.UIObject;


/**
 * Misc utilities.
 * 
 * @author Carlos Rueda
 */
public class Util {
    static native String getLocationSearch() /*-{
	    return $wnd.location.search;
	}-*/ ;

    static native String getLocationHost() /*-{
	    return $wnd.location.host;
	}-*/ ;

	static native String getLocationProtocol() /*-{
	    return $wnd.location.protocol;
	}-*/ ;


	/** @return the map of given parameter. Never null */
	static Map<String,String> getParams() {
	    Map<String,String> params = new HashMap<String,String>();
	    String locSearch = URL.decode(Util.getLocationSearch());
	    Main.log("getParams: locSearch=" +locSearch);
	    if ( locSearch != null && locSearch.trim().length() > 0 ) {
	        // skip ? and get &-separated chunks:
	        locSearch = locSearch.substring(1);
	        String[] chunks = locSearch.split("&");
	        for (int i = 0; i < chunks.length; i++) {
	            String chunk = chunks[i];
	            Main.log("getParams: " +i+ ": chunk=" +chunk);
	            String[] toks = chunk.split("=");
	            if ( toks.length == 2 ) {
	                params.put(toks[0], toks[1]);
	            }
	        }
	    }
	    return params;
	}
	
	static ButtonBase createButton(String str) {
	    PushButton obj = new PushButton(str);
	    setFontSize(obj, 10);
	    return obj;
	}
	static ButtonBase createButton(String str, String tooltip, ClickListener cl) {
	    ButtonBase obj = createButton(str);
	    obj.setTitle(tooltip);
	    if ( cl != null ) {
	        obj.addClickListener(cl);
	    }
	    return obj;
	}
	
	/** Ad hoc utility */
	public static HTML createHtml(String str, int fontFize) {
	    HTML obj = new HTML(str);
	    setFontSize(obj, fontFize);
	    return obj;
	}

    /** Copied from GChart and made public */
    static void setFontSize(UIObject uio, int fontSize) {
           DOM.setIntStyleAttribute(
              uio.getElement(), "fontSize", fontSize);
    }

    /** Complements setFontSize */
    static int getFontSize(UIObject uio) {
           return DOM.getIntStyleAttribute(
              uio.getElement(), "fontSize");
    }
    
    
    public static Panel createHtmlPre(String w, String h, String str, int fontSize) {
		final HTML contents = Util.createHtml("<pre>" + str + "</pre>", 10);
		ScrollPanel scroller = new ScrollPanel(contents);
	    scroller.setSize(w, h);

		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(scroller);
	    return decPanel;
    }


}
