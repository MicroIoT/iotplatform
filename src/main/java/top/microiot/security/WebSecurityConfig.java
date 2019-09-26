package top.microiot.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import top.microiot.security.authentication.SkipPathRequestMatcher;
import top.microiot.security.authentication.TokenAuthenticationProcessingFilter;
import top.microiot.security.authentication.TokenAuthenticationProvider;
import top.microiot.service.TokenService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired 
	private TokenService tokenService;
	@Autowired 
	private AuthenticationManager authenticationManager;
	@Autowired 
	private AuthenticationFailureHandler failureHandler;
	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;
	
	@Value("${microiot.websocket.endpoint}")
	private String ws;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
			.formLogin().disable()
			.logout().disable()
			.csrf().disable()
			.cors()
			.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and().exceptionHandling()
			.and().authorizeRequests()
			.antMatchers(permitAll()).permitAll()
			.requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority("SYSTEM")
			.anyRequest().authenticated()
			.and().authenticationProvider(tokenAuthenticationProvider).addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
	}

	private String[] permitAll() {
		return  new String[] { "/", "/"+ws, "/login", "/token", "/error" };
	}
	
	@Bean
	public CorsFilter corsFilter() {
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true); 
	    config.addAllowedOrigin("*");
	    config.addAllowedHeader("*");
	    config.addAllowedMethod("OPTIONS");
	    config.addAllowedMethod("HEAD");
	    config.addAllowedMethod("GET");
	    config.addAllowedMethod("PUT");
	    config.addAllowedMethod("POST");
	    config.addAllowedMethod("DELETE");
	    config.addAllowedMethod("PATCH");
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}
	@Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
	
	private TokenAuthenticationProcessingFilter tokenAuthenticationFilter() {
		List<String> permitAllList = Arrays.asList(permitAll());
		SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(permitAllList, "/**");
		TokenAuthenticationProcessingFilter filter =  new TokenAuthenticationProcessingFilter(tokenService, failureHandler, matcher);
		filter.setAuthenticationManager(authenticationManager);
		return filter;
	}
}
