package com.liferay.samples.fbo.saml.override;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(
		category = "third-party",
		scope = ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
	id = "com.liferay.samples.fbo.saml.override.DetectIDPSessionFilterConfiguration",
    localization = "content/Language", name = "detect-idp-session-filter-configuration-name"	
)
public interface DetectIDPSessionFilterConfiguration {

	@Meta.AD(deflt = "", name = "detect-idp-session-cookie-name", required = false)
	public String detectIdpSessionCookieName();	

	@Meta.AD(deflt = "", name = "detect-idp-session-enabled", required = false)
	public boolean detectIdpSessionEnabled();	

}
