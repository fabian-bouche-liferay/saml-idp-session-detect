package com.liferay.samples.fbo.saml.override;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.servlet.filters.BasePortalFilter;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.util.SamlHttpRequestUtil;

import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
		immediate = true,
		configurationPid = "com.liferay.samples.fbo.saml.override.DetectIDPSessionFilterConfiguration",
		property = {
			"after-filter=Virtual Host Filter", "dispatcher=FORWARD",
			"dispatcher=REQUEST",
			"init-param.url-regex-ignore-pattern=^/html/.+\\.(css|gif|html|ico|jpg|js|png)(\\?.*)?$",
			"servlet-context-name=", "servlet-filter-name=Detect IDP Session Filter",
			"url-pattern=/*"
		},
		service = Filter.class
	)
public class DetectIDPSessionFilter extends BasePortalFilter {

	@Override
	public boolean isFilterEnabled() {
		
		if(!_detectIDPSessionFilterConfiguration.detectIdpSessionEnabled()) {
			return false;
		}

		String cookieName = _detectIDPSessionFilterConfiguration.detectIdpSessionCookieName();

		if(cookieName == null || StringPool.BLANK.equals(cookieName)) {
			return false;
		}
		
		if (_samlProviderConfigurationHelper.isEnabled() &&
			_samlProviderConfigurationHelper.isRoleSp()) {

			return true;
		}
		

		return false;
	}
	
	@Override
	public boolean isFilterEnabled(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		if(!_detectIDPSessionFilterConfiguration.detectIdpSessionEnabled()) {
			return false;
		}
		
		String cookieName = _detectIDPSessionFilterConfiguration.detectIdpSessionCookieName();

		if(cookieName == null || StringPool.BLANK.equals(cookieName)) {
			return false;
		}

		if (!_samlProviderConfigurationHelper.isEnabled() ||
			!_samlProviderConfigurationHelper.isRoleSp()) {

			return false;
		}

		long userId = _portal.getUserId(httpServletRequest);
		
		if(userId != 0) {
			return false;
		}

		String requestPath = _samlHttpRequestUtil.getRequestPath(
			httpServletRequest);

		if (!requestPath.startsWith("/web")) {

			return false;
		}
		
		Cookie[] cookies = httpServletRequest.getCookies();
		if(cookies != null) {
			for(int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if(cookie.getName().equals(cookieName)) {
					return true;
				}
			}
		}

		return false;
	}	
	
	@Override
	protected void processFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws Exception {

		String redirect = _portal.getPathMain() + "/portal/login?redirect=" + _portal.escapeRedirect(_portal.getCurrentURL(httpServletRequest));
		
		httpServletResponse.sendRedirect(redirect);
		
	}
	
	@Override
	protected Log getLog() {
		return _log;
	}
	
	private static final Log _log = LogFactoryUtil.getLog(
			DetectIDPSessionFilter.class);

	@Reference
	private Portal _portal;
	
	@Reference
	private SamlHttpRequestUtil _samlHttpRequestUtil;
	
	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_detectIDPSessionFilterConfiguration = ConfigurableUtil.createConfigurable(
				DetectIDPSessionFilterConfiguration.class, properties);
	}

	private volatile DetectIDPSessionFilterConfiguration
		_detectIDPSessionFilterConfiguration;
	
}
