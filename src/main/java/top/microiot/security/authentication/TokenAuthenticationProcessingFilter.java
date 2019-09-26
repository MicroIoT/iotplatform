package top.microiot.security.authentication;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import top.microiot.service.TokenService;

public class TokenAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
	private AuthenticationFailureHandler failureHandler;
	private TokenService tokenService;
	
	@Autowired
	public TokenAuthenticationProcessingFilter(TokenService tokenService, AuthenticationFailureHandler failureHandler, RequestMatcher requestMatcher) {
		super(requestMatcher);
		this.tokenService = tokenService;
		this.failureHandler = failureHandler;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		String token = request.getHeader(AUTHORIZATION);
		token = tokenService.extractToken(token);
        Authentication requestAuthentication = new UsernamePasswordAuthenticationToken(token, null);
        return getAuthenticationManager().authenticate(requestAuthentication);
	}

	

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(request, response, failed);
	}

}
