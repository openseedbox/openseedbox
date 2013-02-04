package com.openseedbox.test.client;

import com.openseedbox.code.Util;
import org.junit.Test;
import play.mvc.Http.Response;

public class TestBasicClientFunctionality extends AuthenticatedFunctionalTest {
	
	@Test
	public void testAddTorrent() {
		String torrentUrls = "magnet:?xt=urn:btih:PIRCY5TF6KUX4QYD4I5XEV7PGTF7CMZF&tr=http://tracker.mininova.org/announce\n" +
				  "magnet:?xt=urn:btih:RTXYHEE43V2EJWAQWT3J3WOE27QIURFT&tr=http://tracker.mininova.org/announce";
		Response res = POST("/client/addTorrent", Util.convertToMap(new String[] { "urlOrMagnet", torrentUrls }));
		assertStatus(302, res);
		res = GET(res.getHeader("Location"), true);
		assertContentMatch("2 left to add", res);
	}
	
}
