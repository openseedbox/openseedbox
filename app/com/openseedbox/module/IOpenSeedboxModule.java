package com.openseedbox.module;

/**
 * The prime interface that all OpenSeedbox modules must implement
 * @author Erin Drummond
 */
public interface IOpenSeedboxModule {
	
	public void onAdminSettings();
	public void onSettings();
	
}
