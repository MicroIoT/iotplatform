package top.microiot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import top.microiot.domain.Domain;
import top.microiot.domain.Token;
import top.microiot.domain.User;
import top.microiot.exception.ValueException;
import top.microiot.repository.DomainRepository;
import top.microiot.security.CustomUserDetails;
import top.microiot.security.CustomUserService;
import top.microiot.security.authentication.TokenFactory;

@Service
public class TokenService extends IoTService{
	@Autowired
	private DomainService domainService;
	@Autowired
	private DomainRepository domainRepository;
	@Autowired
	private CustomUserService userService;
	@Autowired
	private TokenFactory tokenFactory;
	
	public Token createToken(CustomUserDetails u, String domain) {
		User user = u.getUser();
		return createToken(domain, user);
	}

	private Token createToken(String domain, User user) {
		if(domain  != null && domain.length() > 0 && domainService.isMyDomain(domain, user))
			return tokenFactory.createToken(user.getUsername(), domain);
		else if(domain  == null || domain.length() == 0)
			return tokenFactory.createToken(user.getUsername(), null);
		else
			throw new ValueException(domain + " not exist or access denied.");
	}
	
	public Token updateToken(String domain) {
		User user = this.getCurrentUser();
		return createToken(domain, user);
	}
	
	public UserDetails getUser(String token) {
		Jws<Claims> jwsClaims = tokenFactory.parseClaims(token);
		String subject = jwsClaims.getBody().getSubject();
		String domain =tokenFactory.getScope(jwsClaims);
		Domain d = null;
		if(domain != null)
			d = domainRepository.findByName(domain);
		
		CustomUserDetails user = (CustomUserDetails) userService.loadUserByUsername(subject);
		user.setDomain(d);
		return user;
	}
	
	public String getJti(String token) {
		Jws<Claims> jwsClaims = tokenFactory.parseClaims(token);
		return jwsClaims.getBody().getId();
	}
	public String extractToken(String token) {
		if(token == null || token.length() == 0)
			throw new AuthenticationServiceException("Authorization header cannot be blank!");
		if(!token.startsWith("Bearer"))
			throw new AuthenticationServiceException("Authorization header must start with Bearer!");
		token = token.substring("Bearer ".length(), token.length());
		return token;
	}
}
