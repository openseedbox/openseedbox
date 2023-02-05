package com.openseedbox.mvc;

import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;

public class TemplateNameResolver implements Controller.ITemplateNameResolver {
	@Override
	public String resolveTemplateName(String templateName) {
		return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(templateName), '-')
				.replaceAll("-/-", "/")
				.replaceAll("-.-", ".")
				.toLowerCase();
	}
}
