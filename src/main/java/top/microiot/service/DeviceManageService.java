package top.microiot.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Alarm;
import top.microiot.domain.AlarmType;
import top.microiot.domain.Device;
import top.microiot.domain.DeviceType;
import top.microiot.domain.Domain;
import top.microiot.domain.ManagedObject;
import top.microiot.domain.Role;
import top.microiot.domain.Topic;
import top.microiot.domain.User;
import top.microiot.domain.attribute.AttValueInfo;
import top.microiot.domain.attribute.AttributeValues;
import top.microiot.domain.attribute.DataValue;
import top.microiot.dto.DeviceInfo;
import top.microiot.dto.DeviceMoveInfo;
import top.microiot.dto.DeviceRenameInfo;
import top.microiot.dto.DeviceUpdateInfo;
import top.microiot.dto.SubDeviceInfo;
import top.microiot.exception.NotFoundException;
import top.microiot.exception.ValueException;
import top.microiot.repository.AlarmRepository;
import top.microiot.repository.ConfigRepository;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.DeviceTypeRepository;
import top.microiot.repository.EventRepository;
import top.microiot.repository.FavoriteRepository;
import top.microiot.repository.LoginUserRepository;

@Service
public class DeviceManageService extends IoTService{
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private DeviceTypeRepository typeRepository;
	@Autowired
	private SimpMessagingTemplate template;
	@Autowired
	private AlarmRepository alarmRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private FavoriteRepository favoriteRepository;
	@Autowired
	private ConfigRepository configRepository;
	@Autowired
	private LoginUserRepository loginUserRepository;
	@Autowired
	private MOService moService;

	@Transactional
	public Device register(DeviceInfo<AttValueInfo> info) {
		Domain domain = getCurrentDomain();
		DeviceType type = typeRepository.findByNameAndDomain(info.getDeviceType(), domain.getId());
		if(type == null)
			throw new NotFoundException("device type");
		
		ManagedObject location = moService.getLocation1(info.getLocationId());
		if(location == null || !location.getDomain().equals(domain))
			throw new NotFoundException("location");
		if(!moService.isMyMO(location))
			throw new AccessDeniedException(info.getLocationId());
		
		return doRegister(info.getName(), type, info.getAttInfos(), location);
	}
	
	private Device doRegister(String name, DeviceType type, Map<String, AttValueInfo> attValueInfos, ManagedObject location) {
		AttributeValues attributes = new AttributeValues(attValueInfos, type.getStaticAttDefinition());
		Map<String, DataValue> attributeValues = attributes.getAttributes();
		
		User user = getAccount();
		user.setShowPassword(true);
		
		Device device = new Device(name, type, attributeValues, location, user);
		
		return deviceRepository.save(device);
	}

	@Transactional
	public Device register(SubDeviceInfo<AttValueInfo> info) {
		Domain domain = getCurrentDomain();
		DeviceType type = typeRepository.findByNameAndDomain(info.getDeviceType(), domain.getId());
		if(type == null)
			throw new NotFoundException("device type");
		
		ManagedObject location = moService.getLocation1(info.getLocationId());
		if(location == null || !location.getDomain().equals(domain))
			throw new NotFoundException("location");
		if(!moService.isMyMO(location))
			throw new AccessDeniedException(info.getLocationId());
		
		Device gateway = deviceRepository.findById(info.getGatewayId()).get();
		if(!gateway.getDomain().getId().equals(domain.getId()))
			throw new ValueException("gateway value error");
		if(gateway.getDeviceAccount() == null)
			throw new ValueException("gateway has no account");
		
		return doRegister(info.getName(), type, info.getAttInfos(), location, gateway);
	}
	
	private Device doRegister(String name, DeviceType type, Map<String, AttValueInfo> attValueInfos, ManagedObject location, Device gateway) {
		AttributeValues attributes = new AttributeValues(attValueInfos, type.getStaticAttDefinition());
		Map<String, DataValue> attributeValues = attributes.getAttributes();
		
		Device device = new Device(name, type, attributeValues, location, gateway);
		
		return deviceRepository.save(device);
	}
	
	private User getAccount() {
		UUID id = UUID.randomUUID();
		String password = String.valueOf((int)((Math.random()*9+1)*10000000));
		
		List<Role> roles = new ArrayList<Role>();
		
		roles.add(Role.DEVICE);
		
		User user = new User(id.toString(), password, null, roles);
		return user;
	}
	
	@Transactional
	public Device updateDevice(DeviceUpdateInfo<AttValueInfo> info) {
		Device device = deviceRepository.findById(info.getId()).get();
		
		if(!moService.isMyMO(device))
			throw new AccessDeniedException("device");
		
		Map<String, DataValue> attributeValues = null;
		if(device.getDeviceType().getStaticAttDefinition() != null && info.getAttInfos() != null) {
			AttributeValues attributes = new AttributeValues(info.getAttInfos(), device.getDeviceType().getStaticAttDefinition());
			attributeValues = attributes.getAttributes();
		}
		
		device.setAttributes(attributeValues);
		device = deviceRepository.save(device);
		reportAttributeChangedAlarm(device);
		
		return device;
	}

	public void reportAttributeChangedAlarm(ManagedObject notifyObject) {
		Alarm alarm = new Alarm(notifyObject, AlarmType.ATTRIBUTE_CHANGED_ALARM, null, new Date());
		alarm = alarmRepository.save(alarm);
		String destination = Topic.TOPIC_ALARM + notifyObject.getId();
		template.convertAndSend(destination, alarm);
	}
	
	@Transactional
	public Device moveDevice(DeviceMoveInfo info) {
		Device device = deviceRepository.findById(info.getId()).get();
		
		if(!moService.isMyMO(device))
			throw new AccessDeniedException("device");
		
		ManagedObject site = moService.getLocation1(info.getLocationId());
		
		if(!moService.isMyMO(site))
			throw new AccessDeniedException("site moved to is not in my area");
		
		device.setLocation(site);
		device = deviceRepository.save(device);
		reportAttributeChangedAlarm(device);
		return device;
	}
	
	@Transactional
	public Device renameDevice(DeviceRenameInfo info){
		Device device = deviceRepository.findById(info.getId()).get();
		
		if(!moService.isMyMO(device))
			throw new AccessDeniedException("device");
		
		device.setName(info.getName());
		device = deviceRepository.save(device);
		reportAttributeChangedAlarm(device);
		
		return device;
	}
	
	@Transactional
	public void delete(String deviceId) {
		Device device = deviceRepository.findById(deviceId).get();
		if(!moService.isMyMO(device.getLocation()))
			throw new AccessDeniedException("device");
		moService.hasMOAccess(device);
		
		alarmRepository.deleteByNotifyObjectId(deviceId);
		eventRepository.deleteByNotifyObjectId(deviceId);
		favoriteRepository.deleteByMoId(deviceId);
		configRepository.deleteByNotifyObjectId(deviceId);
		deviceRepository.deleteById(deviceId);
		loginUserRepository.deleteByUsername(device.getDeviceAccount().getUsername());
	}
}
