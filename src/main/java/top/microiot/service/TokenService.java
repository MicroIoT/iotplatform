package top.microiot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import top.microiot.domain.Domain;
import top.microiot.domain.LoginUser;
import top.microiot.domain.Token;
import top.microiot.domain.User;
import top.microiot.dto.LoginInfo;
import top.microiot.dto.LoginUserPageInfo;
import top.microiot.exception.AuthenticationException;
import top.microiot.exception.ConflictException;
import top.microiot.exception.ValueException;
import top.microiot.repository.DomainRepository;
import top.microiot.repository.LoginUserRepository;
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
	@Autowired
	private LoginUserRepository loginUserRepository;
	
	
	public Token login(LoginInfo info, String ip) {
		try{
			CustomUserDetails user = (CustomUserDetails) userService.loadUserByUsername(info.getUsername());
			if(info.getPassword().equals(user.getPassword())) {
				if(user.isDevice() && loginUserRepository.existLoginUserNotExpire(info.getUsername()))
					throw new AuthenticationException("device has login");
				
				Token token =  createToken((CustomUserDetails) user, info.getDomain());
				loginUserRepository.removeLoginUserExpire(info.getUsername());
				
				saveLoginUser(ip, token);
				return token;
			}
			else
				throw new AuthenticationException("Authentication Failed");
		} catch (UsernameNotFoundException e) {
			throw new AuthenticationException("Authentication Failed");
		}
	}

	private void saveLoginUser(String ip, Token token) {
		CustomUserDetails user = (CustomUserDetails) getUser(token.getToken());
		LoginUser loginUser = new LoginUser(token.getToken(), token.getRefreshToken(), user.getUser().getUsername(), user.getUser().isDevice(), ip, tokenFactory.getExpire(token.getToken()), tokenFactory.getExpire(token.getRefreshToken()));

		try {
			loginUserRepository.save(loginUser);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("same account login");
		}
	}
	
	private Token createToken(CustomUserDetails u, String domain) {
		User user = u.getUser();
		return createToken(domain, user);
	}

	public Token refreshToken(String tokenStr) {
		String token = extractToken(tokenStr);
		String jti = tokenFactory.getJti(token);
		if(jti == null)
			throw new ValueException("illegal token");
		CustomUserDetails user = (CustomUserDetails) getUser(token);
		Token t;
		if(user.getDomain() == null)
			t = createToken(user, null);
		else
			t = createToken(user, user.getDomain().getName());
		
		LoginUser loginUser = loginUserRepository.findByRefreshToken(token);
		updateLoginUser(loginUser, t);
		return t;
	}
	
	private void updateLoginUser(LoginUser loginUser, Token token) {
		if(loginUser != null) {
			loginUser.setToken(token.getToken());
			loginUser.setRefreshToken(token.getRefreshToken());
			loginUser.setExpire(tokenFactory.getExpire(token.getToken()));
			loginUser.setRefreshExpire(tokenFactory.getExpire(token.getRefreshToken()));
			
			loginUserRepository.save(loginUser);
		}
	}
	
	private Token createToken(String domain, User user) {
		if(domain  != null && domain.length() > 0 && domainService.isMyDomain(domain, user))
			return tokenFactory.createToken(user.getUsername(), domain);
		else if(domain  == null || domain.length() == 0)
			return tokenFactory.createToken(user.getUsername(), null);
		else
			throw new ValueException(domain + " not exist or access denied.");
	}
	
	public Token updateToken(String domain, String tokenStr) {
		String token = extractToken(tokenStr);
		User user = this.getCurrentUser();
		Token t = createToken(domain, user);
		
		LoginUser loginUser = loginUserRepository.findByToken(token);
		updateLoginUser(loginUser, t);
		return t;
	}
	
	public UserDetails getUser(String token) {
		Jws<Claims> jwsClaims = tokenFactory.parseClaims(token);
		String username = tokenFactory.getUsername(jwsClaims);
		String domain =tokenFactory.getScope(jwsClaims);
		Domain d = null;
		if(domain != null)
			d = domainRepository.findByName(domain);
		
		CustomUserDetails user = (CustomUserDetails) userService.loadUserByUsername(username);
		user.setDomain(d);
		return user;
	}
	
	public UserDetails getCheckUser(String token) {
		UserDetails user = getUser(token);
		if(loginUserRepository.existsByToken(token))
			return user;
		else
			throw new AuthenticationException(AuthenticationException.TOKEN_NOT_EXIST);
	}
	public String extractToken(String token) {
		if(token == null || token.length() == 0)
			throw new AuthenticationServiceException("Authorization header cannot be blank!");
		if(!token.startsWith("Bearer"))
			throw new AuthenticationServiceException("Authorization header must start with Bearer!");
		token = token.substring("Bearer ".length(), token.length());
		return token;
	}
	
	public Page<LoginUser> queryLoginUser(LoginUserPageInfo info){
		Pageable pageable = getPageable(info);   
		return loginUserRepository.queryLoginUser(info.getIsDevice(), info.getIsExpire(), pageable);
	}
	
	public void delete(String id) {
		LoginUser user = loginUserRepository.findById(id).get();
		loginUserRepository.delete(user);
	}
	
	public LoginUser get(String id) {
		return loginUserRepository.findById(id).get();
	}
}
