package top.microiot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Configuration;
import top.microiot.domain.Device;
import top.microiot.domain.ManagedObject;
import top.microiot.domain.Site;
import top.microiot.domain.User;
import top.microiot.exception.NotFoundException;
import top.microiot.repository.ConfigRepository;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.SiteRepository;

@Service
public class ConfigService extends IoTService {
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private ConfigRepository configRepository;
	@Autowired
	private MOService moService;
	
	public List<Configuration> queryConfiguration(Boolean top, Boolean silent, Boolean subscribe){
		User user = getCurrentUser();
		
		return configRepository.queryConfiguration(user.getId(), top, silent, subscribe);
	}
	
	public Page<Configuration> listAll() {
		Pageable pageable = getPageable(null);   
		User user = getCurrentUser();
		
		return configRepository.findByUserId(user.getId(), pageable);
	}
	
	public Configuration listOne(String objectId) {
		User u = getCurrentUser();
		
		return configRepository.findByUserAndNotifyObject(u.getId(), objectId);
	}
	
	private Configuration getConfiguration(String objectId) {
		ManagedObject notifyObject = null;
		Optional<Device> device = deviceRepository.findById(objectId);
		if(device.isPresent()){
			if(!moService.isMyMO(device.get()))
				throw new AccessDeniedException("device");
			notifyObject = device.get();
		}else {
			Optional<Site> site = siteRepository.findById(objectId);
			if(site.isPresent()) {
				if(!moService.isMyMO((Site)site.get()))
					throw new AccessDeniedException("site");
				notifyObject = site.get();
			}
			else
				throw new NotFoundException("object");
		}
		User u = getCurrentUser();
		
		Configuration configuration = configRepository.findByUserAndNotifyObject(u.getId(), objectId);
		if(configuration == null){
			configuration = new Configuration(u, notifyObject);
		}
		return configuration;
	}
	
	@Transactional
	public Configuration configSilent(String objectId, boolean value) {
		Configuration configuration = getConfiguration(objectId);
		
		configuration.setSilent(value);
		
		return configRepository.save(configuration);
	}

	@Transactional
	public Configuration configTop(String objectId, boolean value) {
		Configuration configuration = getConfiguration(objectId);
		
		configuration.setTop(value);
		
		return configRepository.save(configuration);
	}
	
	@Transactional
	public Configuration configSubscribe(String objectId, boolean value) {
		Configuration configuration = getConfiguration(objectId);
		
		configuration.setSubscribe(value);
		
		return configRepository.save(configuration);
	}
}
