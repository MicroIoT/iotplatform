package top.microiot.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import top.microiot.domain.Domain;
import top.microiot.domain.Role;
import top.microiot.domain.User;

@SuppressWarnings("serial")
public class CustomUserDetails implements UserDetails {
	private User user;
	private Domain domain;
	
	public User getUser() {
		return user;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public CustomUserDetails(User user){
		this.user = user;
	}
	
	public String getUserId(){
		return user.getId();
	}
	
	public boolean isSystem(){
		return user.isSystem();
	}
	
	public boolean isArea() {
		return user.isArea();
	}
	
	public boolean isDevice(){
		return user.isDevice();
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		List<Role> roles = user.getRoles();
		
		if(roles != null){
			for(Role role : roles){
				SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.toString());
				authorities.add(authority);
			}
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return user.getStatus() == User.Status.enable;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return user.getUsername().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CustomUserDetails) {
			return user.getUsername().equals(((CustomUserDetails)obj).getUsername());
		}
		else
			return false;
	}

}
