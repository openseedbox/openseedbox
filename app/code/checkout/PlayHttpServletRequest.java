package code.checkout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import play.mvc.Http;
import play.mvc.Http.Request;


public class PlayHttpServletRequest implements HttpServletRequest {

	private Request req;

	public PlayHttpServletRequest(Request request) {
		req = request;
	}

	public Object getAttribute(String name) {
		return null;
	}

	public Enumeration getAttributeNames() {
		return null;

	}

	public String getCharacterEncoding() {
		return null;

	}

	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
	}

	public int getContentLength() {
		return -1;
	}

	public String getContentType() {
		return null;
	}

	public ServletInputStream getInputStream() throws IOException {
		return new PlayServletInputStream(req.body);
	}

	public String getParameter(String name) {
		return req.params.get(name);
	}

	public Enumeration getParameterNames() {
		return null;
	}

	public String[] getParameterValues(String name) {
		return null;

	}

	public Map getParameterMap() {
		return null;
	}

	public String getProtocol() {
		return null;
	}

	public String getScheme() {
		return null;
	}

	public String getServerName() {
		return null;
	}

	public int getServerPort() {
		return req.port;
	}

	public BufferedReader getReader() throws IOException {
		return null;
	}

	public String getRemoteAddr() {
		return null;
	}

	public String getRemoteHost() {
		return null;
	}

	public void setAttribute(String name, Object o) {
		req.params.put(name, String.valueOf(o));
	}

	public void removeAttribute(String name) {
		req.params.remove(name);
	}

	public Locale getLocale() {
		return null;
	}

	public Enumeration getLocales() {
		return null;
	}

	public boolean isSecure() {
		return req.secure;
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	public String getRealPath(String path) {
		return null;
	}

	public int getRemotePort() {
		return req.port;
	}

	public String getLocalName() {
		return req.domain;
	}

	public String getLocalAddr() {
		return req.host;
	}

	public int getLocalPort() {
		return req.port;
	}

	public String getAuthType() {
		return null;
	}

	public Cookie[] getCookies() {
		return null;
	}

	public long getDateHeader(String name) {
		return -1;
	}

	public String getHeader(String name) {
        Http.Header header = req.headers.get(name.toLowerCase());
        
        if (header == null) {
            return null;
        }
        
        return header.value();
	}

	public Enumeration getHeaders(String name) {
		return null;
	}

	public Enumeration getHeaderNames() {
		return null;
	}

	public int getIntHeader(String name) {
		return req.params.get(name, Integer.class);
	}

	public String getMethod() {
		return req.method;
	}

	public String getPathInfo() {
		return null;
	}

	public String getPathTranslated() {
		return req.path;
	}

	public String getContextPath() {
		return req.path;
	}

	public String getQueryString() {
		return req.querystring;
	}

	public String getRemoteUser() {
		return req.user;
	}

	public boolean isUserInRole(String role) {
		return false;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public String getRequestedSessionId() {
		return null;
	}

	public String getRequestURI() {
		return req.url;
	}

	public StringBuffer getRequestURL() {
		return new StringBuffer(req.url);
	}

	public String getServletPath() {
		return req.path;
	}

	public HttpSession getSession(boolean create) {
		return null;
	}

	public HttpSession getSession() {
		return null;
	}

	public boolean isRequestedSessionIdValid() {
		return false;

	}

	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;

	}

	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

}