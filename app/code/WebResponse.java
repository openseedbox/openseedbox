package code;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import play.Logger;

public class WebResponse {
	
	private String _response;
	private JsonObject _parsedResponse;
	
	public WebResponse(String fullResponse) {
		_response = fullResponse;
	}
	
	private JsonObject getParsedResponse() {
		try {
			if (_parsedResponse == null) {
				JsonParser p = new JsonParser();
				_parsedResponse = p.parse(_response).getAsJsonObject();
			}
		} catch (JsonSyntaxException ex) {
			Logger.error(ex, "Bad JSON response: " + _response);
		}
		return _parsedResponse;
	}
	
	public JsonObject getResultJsonObject() {
		return getParsedResponse().getAsJsonObject("result");
	}
	
	public String getResultString() {
		return getParsedResponse().get("result").getAsString();
	}
	
	public boolean getResultBoolean() {
		return getParsedResponse().get("result").getAsBoolean();
	}
	
	public int getResultInt() {
		return getParsedResponse().get("result").getAsInt();
	}
	
	public String getFullResult() {
		return _response;
	}
	
	public String getErrorMessage() {
		JsonElement error = getParsedResponse().get("error");
		if (error != null) {
			return error.getAsString();
		}
		return null;
	}
	
	public boolean isSuccessful() {
		return StringUtils.isEmpty(getErrorMessage());
	}
	
}

