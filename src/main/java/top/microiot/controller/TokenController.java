package top.microiot.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import top.microiot.domain.Token;
import top.microiot.dto.LoginInfo;
import top.microiot.exception.AuthenticationException;
import top.microiot.exception.ValueException;
import top.microiot.security.CustomUserDetails;
import top.microiot.security.CustomUserService;
import top.microiot.service.TokenService;

@RestController
public class TokenController extends IoTController{
	@Autowired
	private CustomUserService userService;
	@Autowired
	private TokenService tokenService;
	
	@PostMapping("/login")
	public Token login(@RequestBody @Valid LoginInfo info, BindingResult result) {
		throwError(result);
		try{
			UserDetails user = userService.loadUserByUsername(info.getUsername());
			if(info.getPassword().equals(user.getPassword())) {
				return tokenService.createToken((CustomUserDetails) user, info.getDomain());
			}
			else
				throw new AuthenticationException("Authentication Failed");
		} catch (UsernameNotFoundException e) {
			throw new AuthenticationException("Authentication Failed");
		}
	}

	@GetMapping("/token")
	public Token refresh(HttpServletRequest request, HttpServletResponse response) {
		String token = request.getHeader(AUTHORIZATION);
		token = tokenService.extractToken(token);
		String jti = tokenService.getJti(token);
		if(jti == null)
			throw new ValueException("illegal token");
		CustomUserDetails user = (CustomUserDetails) tokenService.getUser(token);
		if(user.getDomain() == null)
			return tokenService.createToken(user, null);
		else
			return tokenService.createToken(user, user.getDomain().getName());
	}
	
}
