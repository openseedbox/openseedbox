package com.openseedbox.backend;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TorrentBackend {
	public String name();
}
