package org.mmisw.orrportal.gwt.client;

import java.util.Date;

import com.google.gwt.user.client.Cookies;

/**
 * Helper for cookie stuff.
 * 
 * @author Carlos Rueda
 */
public class CookieMan {
	private static final String COOKIE_USERNAME = "mmiorrusername";

	private static final String COOKIE_PASSWORD = "mmiorruserpw";

	private static final long EXPIRATION = 365*24*60*60*1000;
	
	
	public static class UserInfo {
		private final String username;
		private String password;
		UserInfo(String username, String password) {
			this.username = username;
			this.password = password;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public String getUsername() {
			return username;
		}
		
	}

	public static void setUserInfo(String username, String password) {
		if ( username == null ) {
			setUserInfo(null);
			return;
		}
		
		Date date = new Date();
		date.setTime(date.getTime() + EXPIRATION);
		Cookies.setCookie(COOKIE_USERNAME, username, date);
		if ( password != null ) {
			Cookies.setCookie(COOKIE_PASSWORD, password, date);
		}
		else {
			Cookies.removeCookie(COOKIE_PASSWORD);
		}
	}
	
	public static UserInfo getUserInfo() {
		String username = Cookies.getCookie(COOKIE_USERNAME);
		if ( username == null ) {
			return null;
		}
		String password = Cookies.getCookie(COOKIE_PASSWORD);
		return new UserInfo(username, password);
	}

	public static void forgetPassword() {
		Cookies.removeCookie(COOKIE_PASSWORD);
	}

	public static void setUserInfo(UserInfo userInfo) {
		if ( userInfo == null ) {
			Cookies.removeCookie(COOKIE_USERNAME);
			Cookies.removeCookie(COOKIE_PASSWORD);
		}
		else {
			setUserInfo(userInfo.username, userInfo.password);
		}
		
	}
}
