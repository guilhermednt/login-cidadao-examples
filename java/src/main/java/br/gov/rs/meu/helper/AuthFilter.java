package br.gov.rs.meu.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

@WebFilter(urlPatterns = { Utils.REDIRECT_URI })
public class AuthFilter implements Filter {
	
	protected List<String> whiteList = new ArrayList<String>();; 
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			// check whether session variable is set
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;
			HttpSession ses = req.getSession(false);
			/*
			 * allow user to proccede if user logged in or user is accessing any
			 * page in //public folder
			 */
			String reqURI = req.getRequestURI();
			if ((ses != null && ses.getAttribute("username") != null) || reqURI.contains("javax.faces.resource") || Utils.inArray(reqURI, whiteList)) {
				chain.doFilter(request, response);
			} else {
				if (ses != null && ses.getAttribute("lc.oauthParams") != null) {
					OAuthParams oauthParams = (OAuthParams) ses
							.getAttribute("lc.oauthParams");
					OAuthAuthzResponse oar = OAuthAuthzResponse
							.oauthCodeAuthzResponse(req);
					oauthParams.setAuthzCode(oar.getCode());
					Utils.getAuthorizationToken(oauthParams);
					Utils.getResource(oauthParams);

					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> person = mapper.readValue(
							oauthParams.getResource(),
							new TypeReference<Map<String, Object>>() {
							});
					ses.setAttribute("username", person);
					res.sendRedirect((String) ses.getAttribute("lc.origTarget"));
					ses.removeAttribute("lc.origTarget");
				} else {
					String origTarget = Utils.getFullRequestURL(req);
					OAuthParams oauthParams = Utils.prepareOAuthParams(req);
					OAuthClientRequest oauthRequest = OAuthClientRequest
							.authorizationLocation(
									oauthParams.getAuthzEndpoint())
							.setClientId(oauthParams.getClientId())
							.setRedirectURI(oauthParams.getRedirectUri())
							.setResponseType(ResponseType.CODE.toString())
							.setScope(oauthParams.getScope())
							.setState(oauthParams.getState())
							.buildQueryMessage();
					ses.setAttribute("lc.oauthParams", oauthParams);
					ses.setAttribute("lc.origTarget", origTarget);
					res.sendRedirect(oauthRequest.getLocationUri());
				}
			}

		} catch (Throwable t) {
			System.out.println(t.getMessage());
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		String whiteList = arg0.getInitParameter("whitelist");
		if (whiteList != null) {
			for (String string : whiteList.split(",")) {
				this.whiteList.add(string);
			}
		}
	}

}
