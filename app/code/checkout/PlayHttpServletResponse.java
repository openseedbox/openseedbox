package code.checkout;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import play.mvc.Http;

public class PlayHttpServletResponse implements HttpServletResponse {
    private Http.Response response;

    public PlayHttpServletResponse(Http.Response response) {
        this.response = response;
    }

    public void addCookie(Cookie cookie) {
        response.setCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge(), cookie.getSecure(), false); 
    }

    public boolean containsHeader(String header) {
        return response.headers.containsKey(header);
    }

    public String encodeURL(String s) {
        return s;
    }

    public String encodeRedirectURL(String s) {
        return s;
    }

    public String encodeUrl(String s) {
        return s;
    }

    public String encodeRedirectUrl(String s) {
        return s;
    }

    public void sendError(int i, String s) throws IOException {
		
    }

    public void sendError(int i) throws IOException {
		
    }

    public void sendRedirect(String s) throws IOException {

    }

    public void setDateHeader(String s, long l) {
		
    }

    public void addDateHeader(String s, long l) {
		
    }

    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    public void addHeader(String name, String value) {
        response.setHeader(name, value);
    }

    public void setIntHeader(String name, int i) {
        setHeader(name, Integer.toString(i));
    }

    public void addIntHeader(String s, int i) {
        setIntHeader(s, i);
    }

    public void setStatus(int i) {
        response.status = i;
    }

    public void setStatus(int i, String s) {
        response.status = i;
    }

    public String getCharacterEncoding() {
        return response.encoding;
    }

    public String getContentType() {
        return response.contentType;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public void write(int i) throws IOException {
                response.out.write(i);
            }
        };
    }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(response.out);
    }

    public void setCharacterEncoding(String s) {
        response.encoding = s;
    }

    public void setContentLength(int i) {
    }

    public void setContentType(String s) {
        response.contentType = s;
    }

    public void setBufferSize(int i) {

    }

    public int getBufferSize() {
        return response.out.size();
    }

    public void flushBuffer() throws IOException {
        response.out.flush();
    }

    public void resetBuffer() {
        response.out.reset();
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
        response.reset();
    }

    public void setLocale(Locale locale) {

    }

    public Locale getLocale() {
        return null;
    }
}
