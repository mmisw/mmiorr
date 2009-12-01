package org.mmisw.ontmd.gwt.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.vocabulary.Option;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.UIObject;


/**
 * Misc utilities.
 * 
 * @author Carlos Rueda
 */
public class Util {
	
	/**
	 * Regex from: https://wave.google.com/wave/?pli=1#restored:wave:googlewave.com!w%252BsFbGJUukA
	 * retrieved nov/30/2009.
	 * I included the necessary back slashes and removed all the (?#Label) statements.
	 * Also, removed the leading ^ and the trailing $ so the regex can eventually be also used
	 * to detect multiple URLs.
	 */
	private static final String URL_BASE_REGEX = 
		"(?:(?:ht|f)tp(?:s?)\\:\\/\\/|~\\/|\\/)?(?:\\w+:\\w+@)?(?:(?:[-\\w]+\\.)+" +
		"(?:com|org|net|gov|mil|biz|info|mobi|name|aero|jobs|edu|co\\.uk|ac\\.uk|museum|travel|[a-z]{2}))" +
		"(?::[\\d]{1,5})?(?:(?:(?:\\/(?:[-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
		"(?:(?:\\?(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?(?:[-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
		"(?:&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?(?:[-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
		"(?:#(?:[-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?"
	;
	
	/** Regex for a single URL */
	private static final String ONE_URL_REGEX = "^" +URL_BASE_REGEX+ "$";
	
	/** 
	 * Determines if the given string is a URL.
	 * Implementation based on the regex from <a href="https://wave.google.com/wave/?pli=1#restored:wave:googlewave.com!w%252BsFbGJUukA"
	 * >here</a>, retrieved on nov/30/2009.
	 * 
	 * @param str the string to examine
	 * @return true iff the given string is a URL.
	 */
	public static boolean isUrl(String str) {
		return str.matches(ONE_URL_REGEX);
	}
	
	

    static native String getLocationSearch() /*-{
	    return $wnd.location.search;
	}-*/ ;

    public static native String getLocationHost() /*-{
	    return $wnd.location.host;
	}-*/ ;

	public static native String getLocationProtocol() /*-{
	    return $wnd.location.protocol;
	}-*/ ;


	/** @return the map of given parameter. Never null */
	public static Map<String,String> getParams() {
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
	public static ButtonBase createButton(String str, String tooltip, ClickListener cl) {
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

    public static TextBoxBase createTextBoxBase(int nl, String width, 
			ChangeListener cl) {
		final TextBoxBase tb;
		if ( nl <=1 ) {
			tb = new TextBox();
			tb.setWidth(width);
		}
		else {
			// avoid huge textareas (TODO max 20 line is arbitrary)
			if ( nl > 20 ) {
				nl = 20;
			}
			tb = new TextArea();
			// TODO 16 is just a rough scaling factor
			tb.setSize(width, "" +(nl *16));
		}
		if ( cl != null ) {
			tb.addChangeListener(cl);
		}
		return tb;
	}

    public static ListBox createListBox(List<Option> options, ChangeListener cl) {
		final ListBox lb = new ListBox();
		for ( Option option : options ) {
			String lab = option.getLabel();
			if ( lab != null && lab.length() > 0 ) {
				lb.addItem(option.getLabel(), option.getName());
			}
			else {
				lb.addItem(option.getName());
			}
		}
		if ( cl != null ) {
			lb.addChangeListener(cl);
		}
		return lb;
	}
	
    public static boolean isTestingOntology(RegisteredOntologyInfo oi) {
    	String authority = oi.getAuthority();
    	return authority.equalsIgnoreCase("mmitest")
		    || authority.equalsIgnoreCase("testing");
    }
}
