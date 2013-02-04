package com.openseedbox.test.client;

import com.openseedbox.code.Util;
import org.junit.After;
import org.junit.Before;
import play.test.FunctionalTest;

public abstract class AuthenticatedFunctionalTest extends FunctionalTest {
	
	@Before
	public void setUp() {
		POST("/auth/authenticate", Util.convertToMap(new String[] { "email", "erin.dru@gmail.com"}));		
	}
	
	@After
	public void tearDown() {
		GET("/auth/logout");
	}
	
}
