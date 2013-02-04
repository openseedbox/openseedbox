package com.openseedbox.test.client;

import com.openseedbox.code.Util;
import java.util.regex.Pattern;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class TestLogin extends FunctionalTest {
	
	@Test
	public void testLoggingIn() {
		Response res = POST("/auth/authenticate", Util.convertToMap(new String[] { "email", "erin.dru@gmail.com"}));
		assertStatus(302, res);
		String clientPage = res.getHeader("Location");
		assertEquals("/client", clientPage);
		Response res1 = GET("/client", true);
		assertIsOk(res1);		
		assertContentMatch(Pattern.quote("Erin Drummond"), res1);		
	}
	
}
