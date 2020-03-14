package top.microiot.service;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Domain;
import top.microiot.domain.ManagedObject;
import top.microiot.domain.Role;
import top.microiot.domain.User;
import top.microiot.dto.DomainInfo;
import top.microiot.dto.PageInfo;
import top.microiot.dto.RegisterInfo;
import top.microiot.dto.UserUpdateInfo;
import top.microiot.exception.ConflictException;
import top.microiot.exception.ValueException;
import top.microiot.repository.ConfigRepository;
import top.microiot.repository.FavoriteRepository;
import top.microiot.repository.UserRepository;

@Service
public class UserService extends IoTService{
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private FavoriteRepository favoriteRepository;
	@Autowired
	private ConfigRepository configRepository;
	@Autowired
	private MOService moService;
	@Autowired
	private DomainService domainService;
	
	@Transactional
	public User addAdmin(String username, String password, String email ) {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.SYSTEM);
		
		User user = new User(username, password, email, roles);
		
		try{
			userRepository.save(user);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("user name:" + user.getUsername());
		}
		
		return user;
	}
	
	@Transactional
	public User addUser(String username, String password, String email, List<String> area) {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.AREA);
		List<ManagedObject> sites = getSites(area);
		
		User user = new User(username, password, email, roles, sites);
				
		try{
			userRepository.save(user);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("user name:" + username);
		}
		
		return user;
	}

	@Transactional
	public User register(RegisterInfo info) {
		DomainInfo d = new DomainInfo();
		d.setName(info.getEmail());
		Domain domain;
		try {
			domain = domainService.addDomain(d);
		} catch (ConflictException e) {
			throw new ValueException("email:" + info.getEmail() + " existed");
		}
		
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.AREA);
		List<ManagedObject> sites = new ArrayList<ManagedObject>();
		sites.add(domain);
		
		User user = new User(info.getEmail(), info.getPassword(), info.getEmail(), roles, sites);
		
		try{
			userRepository.save(user);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("user name:" + info.getEmail());
		}
		
		return user;
	}
	private List<ManagedObject> getSites(List<String> areas) {
		if(areas == null || areas.isEmpty())
			return null;
		List<ManagedObject> sites = new ArrayList<ManagedObject>();
		
		for(int i = 0; i < areas.size(); i++) {
			String area = areas.get(i);
			
			ManagedObject a = moService.getLocation(area);

			for(int j = i+1; j < areas.size(); j++) {
				String location = areas.get(j);
				ManagedObject b = moService.getLocation(location);
				if (a.contain(b) || b.contain(a))
					throw new ValueException("area: " + a.getString() + " area: " + b.getString() + " overlap");
			}
			sites.add(a);
		}
		return sites;
	}
	
	@Transactional
	public User updateUser(@Valid UserUpdateInfo info) {
		User user = userRepository.findById(info.getUserId()).get();
		if(!user.isArea())
			throw new ValueException("user has no area role");
		
		List<ManagedObject> sites = getSites(info.getArea());
		user.setArea(sites);
		
		userRepository.save(user);
		return user;
	}
	
	@Transactional
	public void delete(String userId) {
		User user = userRepository.findById(userId).get();
		User u = getCurrentUser();
		
		if(user.getUsername().equals("admin"))
			throw new ValueException("admin can't be deleted");
		
		if(user.getUsername().equals(u.getUsername()))
			throw new ValueException("can't delete self");
		
		favoriteRepository.deleteByUserId(userId);
		configRepository.deleteByUserId(userId);
		userRepository.deleteById(userId);
	}
	
	public User listUser(String id){
		User user = userRepository.findById(id).get();
		return user;
	}
	
	public User listUserByUsername(String username) {
		return userRepository.findByUsername(username);
	}
	
	public User listCurrentUser(){
		User user = getCurrentUser();
				
		return user;
	}
	
	public Page<User> listAll(PageInfo info){
		Pageable pageable = getPageable(info);
		return userRepository.findAll(pageable);
	}
	
	@Transactional
	public User updatePassword(String password, String original) {
		User user = getCurrentUser();
		
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//		user.setPassword(encoder.encode(password));
		if(user.getPassword().equals(original)) {
			user.setPassword(password);
			return userRepository.save(user);
		}else
			throw new ValueException("original password error");
	}
}
