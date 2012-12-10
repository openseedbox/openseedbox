package com.openseedbox.module.whatcd;

import com.openseedbox.module.OpenSeedboxModuleBase;
import play.Logger;

public class WhatCdModule extends OpenSeedboxModuleBase {

	public void onAdminSettings() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void onSettings() {
		
	}

	@Override
	protected String getModuleSlug() {
		return "what-cd";
	}

	@Override
	public void onApplicationStart() {
		super.onApplicationStart();
		Logger.info("What.CD module loaded");
	}
	
}
