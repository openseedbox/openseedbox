package code;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import models.Node;
import org.apache.commons.lang.StringUtils;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

public class WebRequest {
	
	private String ipAddress;
	private String port;
	private String path;
	
	public WebRequest(Node n) {
		this(n.ipAddress, null, "openseedbox-server");
	}
	
	public WebRequest(String ipAddress) {
		this(ipAddress, null);
	}
	
	public WebRequest(String ipAddress, String port) {
		this(ipAddress, port, "openseedbox-server");
	}
	
	public WebRequest(String ipAddress, String port, String path) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.path = path;
	}	
	
	public WebResponse getResponse(String page) throws WebRequestFailedException {
		return getResponse(page, null);
	}
	
	public WebResponse getResponse(String page, Map<String, String> parameters) throws WebRequestFailedException {
		return doWebResponse(page, parameters, "get");
	}
	
	public WebResponse postResponse(String page) throws WebRequestFailedException {
		return postResponse(page, null);
	}
	
	public WebResponse postResponse(String page, Map<String, String> parameters) throws WebRequestFailedException {
		return doWebResponse(page, parameters, "post");
	}	
	
	private WebResponse doWebResponse(String page, Map<String, String> parameters, String type) throws WebRequestFailedException {
		WSRequest req = getWebserviceUrl(page, parameters);
		req.timeout = 2;
		HttpResponse res;
		try {
			if (type.equals("get")) {
				res = req.get();
			} else {
				res = req.post();
			}
			if (res.getStatus() != 200) {
				throw new WebRequestFailedException("Failed to query webservice: " + req.url +
						" (" + req.parameters + "), status: " + res.getStatus(), res);
			}
			return new WebResponse(res.getString());		
		} catch (Exception ex) {
			throw new WebRequestFailedException("Unable to connect to server " + req.url, null);
		}
	}

	private WSRequest getWebserviceUrl(String page, Map<String, String> parameters) {
		WSRequest ret;
		if (!StringUtils.isEmpty(this.port)) {
			ret = WS.url("https://%s:%s/%s/%s.php",
				this.ipAddress, this.port, this.path, page);
		} else {
			ret = WS.url("https://%s/%s/%s.php",
				this.ipAddress, this.path, page);
		}
		if (parameters == null) {
			parameters = new HashMap<String, String>();
		}
		parameters.put("type", "json");
		ret.setParameters(parameters);
		return ret;
	}
	
	public class WebRequestFailedException extends Exception {
		
		private HttpResponse _res;
		
		public WebRequestFailedException(String message, HttpResponse res) {
			super(message);
		}
		
		public int getErrorCode() {
			return _res.getStatus();
		}
		
		public HttpResponse getHttpResponse() {
			return _res;
		}
	}
}
