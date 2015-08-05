package br.gov.rs.meu.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

public final class Utils {
	private Utils() {
	}

	private static final String OAUTH_CONFIGURATION_FILE = "oauth_configuration.properties";
	private static Properties oauthConfiguration;

	public static final String REDIRECT_URI = "/redirect";

	public static final String REQUEST_TYPE_QUERY = "queryParameter";
	public static final String REQUEST_TYPE_HEADER = "headerField";
	public static final String REQUEST_TYPE_BODY = "bodyParameter";

	public static final String DEFAULT_PROP_APPLICATION = "generic";
	public static final String PROP_APPLICATION = "application";
	public static final String PROP_AUTHZENDPOINT = "authz_endpoint";
	public static final String PROP_TOKENENDPOINT = "token_endpoint";
	public static final String PROP_RESOURCEURL = "resource_url";
	public static final String PROP_SCOPE = "scope";
	public static final String PROP_CLIENT_ID = "client_id";
	public static final String PROP_CLIENT_SECRET = "client_secret";
	public static final String PROP_REDIRECT_URI = "redirect_uri";

	public static OAuthParams prepareOAuthParams(HttpServletRequest request) throws Exception {
		loadConfig();
		OAuthParams oauthParams = new OAuthParams();
		oauthParams.setApplication(oauthConfiguration.getProperty(PROP_APPLICATION, DEFAULT_PROP_APPLICATION));
		oauthParams.setAuthzEndpoint(oauthConfiguration.getProperty(PROP_AUTHZENDPOINT));
		oauthParams.setTokenEndpoint(oauthConfiguration.getProperty(PROP_TOKENENDPOINT));
		oauthParams.setResourceUrl(oauthConfiguration.getProperty(PROP_RESOURCEURL));
		oauthParams.setScope(oauthConfiguration.getProperty(PROP_SCOPE));
		oauthParams.setClientId(oauthConfiguration.getProperty(PROP_CLIENT_ID));
		oauthParams.setClientSecret(oauthConfiguration.getProperty(PROP_CLIENT_SECRET));
		if (isEmpty(oauthParams.getClientId()) || isEmpty(oauthParams.getClientSecret())) {
			throw new Exception("Missing client_id and client_secret configuration");
		}
		oauthParams.setRedirectUri(oauthConfiguration.getProperty(PROP_REDIRECT_URI));
		if (isEmpty(oauthParams.getRedirectUri())) {
			oauthParams.setRedirectUri(getBaseUrl(request) + REDIRECT_URI);
		}
		oauthParams.setRequestType(REQUEST_TYPE_HEADER);
		return oauthParams;
	}

	
	public static void loadConfig() throws Exception {
		if (oauthConfiguration == null) {
			loadConfig(OAUTH_CONFIGURATION_FILE);
		}
	}

	public static void loadConfig(final String fileName) throws Exception {
		if (oauthConfiguration == null) {
			ClassLoader loader = Utils.class.getClassLoader();
			try {
				InputStream in = loader.getResourceAsStream(fileName);
				loadConfig(in);
			} catch (NullPointerException ne) {
				throw new FileNotFoundException(fileName
						+ " file is not found in your class path");
			}
		}
	}

	public static void loadConfig(final InputStream inputStream)
			throws Exception {
		if (oauthConfiguration == null) {
			oauthConfiguration = new Properties();
			try {
				oauthConfiguration.load(inputStream);
			} catch (IOException ie) {
				throw new IOException(
						"Could not load configuration from input stream");
			}
		}
	}

	public static String getBaseUrl(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + ":"
				+ request.getServerPort() + request.getContextPath();
	}

	public static String getFullRequestURL(HttpServletRequest request) {
		return request.getScheme()
				+ "://"
				+ request.getServerName()
				+ ("http".equals(request.getScheme())
						&& request.getServerPort() == 80
						|| "https".equals(request.getScheme())
						&& request.getServerPort() == 443 ? "" : ":"
						+ request.getServerPort())
				+ request.getRequestURI()
				+ (request.getQueryString() != null ? "?"
						+ request.getQueryString() : "");
	}

	public static boolean isEmpty(String value) {
		return value == null || "".equals(value);
	}
	
	public static boolean inArray(String needle , List<String> haystack) {
		for(String s : haystack)
		    if(s.trim().contains(needle)) return true;
		return false;
	}

	public static String findCookieValue(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(key)) {
				return cookie.getValue();
			}
		}
		return "";
	}

	public static void getAuthorizationToken(OAuthParams oauthParams)
			throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest oRequest = OAuthClientRequest
				.tokenLocation(oauthParams.getTokenEndpoint())
				.setClientId(oauthParams.getClientId())
				.setClientSecret(oauthParams.getClientSecret())
				.setRedirectURI(oauthParams.getRedirectUri())
				.setCode(oauthParams.getAuthzCode())
				.setGrantType(GrantType.AUTHORIZATION_CODE).buildBodyMessage();

		OAuthClient client = new OAuthClient(new URLConnectionClient());

		OAuthAccessTokenResponse oauthResponse = null;
		Class<? extends OAuthAccessTokenResponse> cl = OAuthJSONAccessTokenResponse.class;
		oauthResponse = client.accessToken(oRequest, cl);
		oauthParams.setAccessToken(oauthResponse.getAccessToken());
		oauthParams.setExpiresIn(oauthResponse.getExpiresIn());
		oauthParams.setRefreshToken(oauthResponse.getRefreshToken());
	}

	public static void getResource(OAuthParams oauthParams)
			throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest request = null;

		if (Utils.REQUEST_TYPE_QUERY.equals(oauthParams.getRequestType())) {
			request = new OAuthBearerClientRequest(oauthParams.getResourceUrl())
					.setAccessToken(oauthParams.getAccessToken())
					.buildQueryMessage();
		} else if (Utils.REQUEST_TYPE_HEADER.equals(oauthParams
				.getRequestType())) {
			request = new OAuthBearerClientRequest(oauthParams.getResourceUrl())
					.setAccessToken(oauthParams.getAccessToken())
					.buildHeaderMessage();
		} else if (Utils.REQUEST_TYPE_BODY.equals(oauthParams.getRequestType())) {
			request = new OAuthBearerClientRequest(oauthParams.getResourceUrl())
					.setAccessToken(oauthParams.getAccessToken())
					.buildBodyMessage();
		}

		OAuthClient client = new OAuthClient(new URLConnectionClient());
		OAuthResourceResponse resourceResponse = client.resource(request,
				oauthParams.getRequestMethod(), OAuthResourceResponse.class);

		if (resourceResponse.getResponseCode() == 200) {
			oauthParams.setResource(resourceResponse.getBody());
		} else {
			oauthParams.setErrorMessage("Could not access resource: "
					+ resourceResponse.getResponseCode() + " "
					+ resourceResponse.getBody());
		}
	}

	public static void showMeTrue(HttpServletResponse response,
			OAuthParams oauthParams) throws IOException {
		PrintWriter out = response.getWriter();
		StringBuilder string = new StringBuilder();
		string.append("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'><html><head><title>OAuth client results</title></head><body>");
		if (Utils.isEmpty(oauthParams.getErrorMessage())) {
			string.append(oauthParams.getResource());
		} else {
			string.append(oauthParams.getErrorMessage());
		}
		string.append("</body></html>");
		out.println(string.toString());
	}

}
