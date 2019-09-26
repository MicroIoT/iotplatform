package top.microiot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import top.microiot.domain.Device;
import top.microiot.domain.User;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.UserRepository;

@Component
public class CustomUserService implements UserDetailsService {
	@Autowired
	private UserRepository repository;
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = repository.findByUsername(username);
		if(user != null)
			return new CustomUserDetails(user);
		else {
			Device device = deviceRepository.findByDeviceAccountUsername(username);
			if(device != null)
				return new CustomUserDetails(device.getDeviceAccount());
			else
				throw new UsernameNotFoundException(String.format("User: %s was not found", username));
		}
	} 
}
