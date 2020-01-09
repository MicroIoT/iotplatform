package top.microiot.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import top.microiot.domain.LoginUser;
import top.microiot.domain.Token;
import top.microiot.dto.LoginInfo;
import top.microiot.dto.LoginUserPageInfo;
import top.microiot.service.TokenService;

@RestController
public class TokenController extends IoTController{
	@Autowired
	private TokenService tokenService;
	
	@PostMapping("/login")
	public Token login(HttpServletRequest request, @RequestBody @Valid LoginInfo info, BindingResult result) {
		throwError(result);
		String ip = request.getRemoteAddr();
		return tokenService.login(info, ip);
	}

	@GetMapping("/token")
	public Token refresh(HttpServletRequest request) {
		String token = request.getHeader(AUTHORIZATION);
		return tokenService.refreshToken(token);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@GetMapping("/sessions")
	public Page<LoginUser> queryLoginUser(@Valid LoginUserPageInfo info, BindingResult result) {
		throwError(result);
		
		return tokenService.queryLoginUser(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@DeleteMapping("/sessions/{id}")
	public void delete(@PathVariable String id){
		tokenService.delete(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@GetMapping("/sessions/{id}")
	public LoginUser getSession(@PathVariable String id){
		return tokenService.get(id);
	}
}
