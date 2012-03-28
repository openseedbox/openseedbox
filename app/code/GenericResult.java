package code;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.RenderJson;
import play.mvc.results.RenderXml;
import play.mvc.results.Result;

public class GenericResult extends Result {

	private Object _res;
	private Boolean _isError;

	public GenericResult(Object o) {
		this(o, false);
	}

	public GenericResult(Object o, Boolean isError) {
		_res = o;
		_isError = isError;
	}

	@Override
	public void apply(Request request, Response response) {
		String ext = request.params.get("ext");
		if (ext == null) {
			ext = "html";
		}
		if (_res == null) {
			_res = (Object) "null";
		}
		//for all response types except html, make a reliable return structure
		if (!ext.equals("html")) {
			Map<String, Object> m = new HashMap<String, Object>();
			if (_isError) {
				m.put("error", _res);
				m.put("success", false);
			} else {
				m.put("data", _res);
				m.put("success", true);
			}
			_res = m;
		}
		if (ext.equals("json")) {
			Gson gson = new GsonBuilder()
				.excludeFieldsWithModifiers(Modifier.TRANSIENT)  
				.create();			
			new RenderJson(gson.toJson(_res)).apply(request, response);
		} else if (ext.equals("xml")) {
			new RenderXml(_res).apply(request, response);
		} else if (ext.equals("jsonp")) {
			String callback = request.params.get("callback");
			if (callback == null || callback.isEmpty()) {
				callback = "callback";
			}
			response.print(String.format("%s(", callback));
			new RenderJson(_res).apply(request, response);
			response.print(")");
			response.contentType = "text/javascript";
		} else if (ext.equals("html")) {
			response.print(_res);
		} else {
			response.print("Unknown response type: " + ext);
		}
	}
}
