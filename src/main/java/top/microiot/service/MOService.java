package top.microiot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import top.microiot.domain.Device;
import top.microiot.domain.Domain;
import top.microiot.domain.ManagedObject;
import top.microiot.domain.Site;
import top.microiot.domain.User;
import top.microiot.exception.NotFoundException;
import top.microiot.exception.StatusException;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.DomainRepository;
import top.microiot.repository.SiteRepository;
import top.microiot.repository.UserRepository;

@Service
public class MOService extends IoTService{
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private DomainRepository domainRepository;
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private UserRepository userRepository;
	
	public ManagedObject getLocation(String locationId) {
		Optional<Domain> location1 = domainRepository.findById(locationId);
		Optional<Site> location2;
		Optional<Device> location3;
		if(!location1.isPresent()) {
			location2 = siteRepository.findById(locationId);
			if(!location2.isPresent()) {
				location3 = deviceRepository.findById(locationId);
				if(!location3.isPresent())
					throw new NotFoundException("location id:" + locationId);
				else
					return location3.get();
			}
			else
				return location2.get();
		}
		else
			return location1.get();
	}
	
	public ManagedObject getLocation1(String locationId) {
		Optional<Domain> location1 = domainRepository.findById(locationId);
		Optional<Site> location2;
		if(!location1.isPresent()) {
			location2 = siteRepository.findById(locationId);
			if(!location2.isPresent()) 
				throw new NotFoundException("location id:" + locationId);
			else
				return location2.get();
		}
		else
			return location1.get();
	}
	
	public boolean isMyMO(String moId) {
		ManagedObject mo = getLocation(moId);
		return isMyMO(mo);
	}
	
	public boolean isMyMO(ManagedObject mo) {
		User user = getCurrentUser();
		
		if(user.isArea() && mo != null) {
			for(ManagedObject area: user.getArea()) {
				if(area.contain(mo))
					return true;
			}
			return false;
		}
		else if(user.isArea() && mo == null)
			return false;
		else
			return true;
	}
	
	public void hasMOAccess(ManagedObject object) {
		List<User> users = userRepository.findAll();
		
		for(User user: users) {
			if(user.isArea()) {
				List<ManagedObject> areas = user.getArea();
				for(ManagedObject mo: areas) {
					if(mo.equals(object))
						throw new StatusException(user.getUsername() + " has this access, " + mo.getString() + " can't be deleted");
				}
			}
		}
	}
	
	public Domain hasDomainAccess() {
		Domain domain = getCurrentDomain();
		if(!isMyMO(domain))
			throw new AccessDeniedException("domain");
		return domain;
	}
}
