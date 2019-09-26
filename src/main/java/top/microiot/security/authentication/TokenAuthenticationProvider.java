package top.microiot.security.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import top.microiot.service.TokenService;

@Component
public class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	@Autowired
	private TokenService tokenService;
	
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		// TODO Auto-generated method stub

	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		String token= (String) authentication.getPrincipal();
		try{
			return tokenService.getUser(token);
		} catch(Throwable e) {
			throw new AuthenticationServiceException(e.getMessage());
		}
		
	}

	

}
