package com.openseedbox.code.libs.oidc;

import java.util.HashMap;
import java.util.Map;

public enum ResponseMode {
	QUERY("query"),
	FRAGMENT("fragment"),
	FORM_POST("form_post"),
	QUERY_JWT("query.jwt"),
	FRAGMENT_JWT("fragment.jwt"),
	FORM_POST_JWT("form_post.jwt"),
	JWT("jwt");

	private static final Map<String, ResponseMode> BY_LABEL = new HashMap<>();

	static {
		for (ResponseMode m: values()) {
			BY_LABEL.put(m.label, m);
		}
	}

	private String label;

	ResponseMode(String label) {
		this.label = label;
	}

	public static ResponseMode valueOfLabel(String label) {
		return BY_LABEL.get(label);
	}

	@Override
	public String toString() {
		return this.label;
	}
}
