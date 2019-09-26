package top.microiot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Device;
import top.microiot.domain.DeviceGroup;
import top.microiot.domain.Domain;
import top.microiot.dto.DeviceGroupInfo;
import top.microiot.dto.DeviceGroupRenameInfo;
import top.microiot.dto.PageInfo;
import top.microiot.exception.ConflictException;
import top.microiot.exception.NotFoundException;
import top.microiot.exception.ValueException;
import top.microiot.repository.DeviceGroupRepository;
import top.microiot.repository.DeviceRepository;

@Service
public class DeviceGroupService extends IoTService{
	@Autowired
	private DeviceGroupRepository deviceGroupRepository;
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private MOService moService;
	
	@Transactional
	public DeviceGroup addDeviceGroup(DeviceGroupInfo info) {
		Domain domain = moService.hasDomainAccess();
		
		List<Device> devices = new ArrayList<Device>();
		for(String deviceId : info.getDevices()) {
			Device device = deviceRepository.findById(deviceId).get();
			if(!device.getDomain().equals(domain))
				throw new NotFoundException("device: " + deviceId);
			devices.add(device);
		}
		
		DeviceGroup group = new DeviceGroup(info.getName(), devices, domain);
		return deviceGroupRepository.save(group);
	}
	
	@Transactional
	public DeviceGroup rename(DeviceGroupRenameInfo info){
		moService.hasDomainAccess();
		
		DeviceGroup group = deviceGroupRepository.findById(info.getId()).get();
		group.setName(info.getName());
		
		try{
			return deviceGroupRepository.save(group);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("device group name");
		}
	}
	
	public DeviceGroup listDeviceGroup(String id){
		DeviceGroup group = deviceGroupRepository.findById(id).get();
		return group;
	}
	
	public Page<DeviceGroup> listDeviceGroups(PageInfo info){
		Pageable pageable = getPageable(info);   
		Domain domain = moService.hasDomainAccess();
        
		return deviceGroupRepository.findByDomain(domain.getId(), pageable);
	}
	
	public List<DeviceGroup> listDeviceGroups(){
		Domain domain = moService.hasDomainAccess();
        return deviceGroupRepository.findByDomainId(domain.getId());
	}
	
	@Transactional
	public void delete(String id) {
		Domain domain = moService.hasDomainAccess();
		
		DeviceGroup group = deviceGroupRepository.findById(id).get();
		if(group == null || !group.getDomain().equals(domain))
			throw new NotFoundException("deivce group");
		deviceGroupRepository.deleteById(id);
	}
	
	@Transactional
	public DeviceGroup addGroup(String groupId, String deviceId) {
		Domain domain = moService.hasDomainAccess();
		
		Device device = deviceRepository.findById(deviceId).get();
		if(!device.getDomain().equals(domain))
			throw new NotFoundException("device:  " + deviceId);
		
		DeviceGroup group = deviceGroupRepository.findById(groupId).get();
		if(!group.getDomain().equals(domain))
			throw new NotFoundException("device group:  " + groupId);
		if(group.getDevices().contains(device))
			throw new ValueException("device: " + deviceId + " in grooup: " + groupId);
		
		List<Device> devices = group.getDevices();
		devices.add(device);
		
		return deviceGroupRepository.save(group);
	}
	
	@Transactional
	public DeviceGroup removeGroup(String groupId, String deviceId) {
		Domain domain = moService.hasDomainAccess();
		Device device = deviceRepository.findById(deviceId).get();
		if(!device.getDomain().equals(domain))
			throw new NotFoundException("device:  " + deviceId);
		
		DeviceGroup group = deviceGroupRepository.findById(groupId).get();
		if(!group.getDomain().equals(domain))
			throw new NotFoundException("device group:  " + groupId);
		if(!group.getDevices().contains(device))
			throw new ValueException("device: " + deviceId + " not in grooup: " + groupId);
		
		List<Device> devices = group.getDevices();
		devices.remove(device);
		
		return deviceGroupRepository.save(group);
	}
	
	public boolean isGroup(String device) {
		Device d = getCurrentDevice();
		if(!d.getId().equals(device)) {
			List<String> devices = new ArrayList<String>();
			devices.add(d.getId());
			devices.add(device);
			return deviceGroupRepository.existsByDevicesContaining(devices);
		}
		else
			throw new ValueException("device is same");
	}

	public List<DeviceGroup> listMyDeviceGroups() {
		Device device = getCurrentDevice();
		return deviceGroupRepository.findByDevicesId(device.getId());
	}

	public DeviceGroup listDeviceGroupByDevice(String id) {
		Device device = getCurrentDevice();
		DeviceGroup group = deviceGroupRepository.findById(id).get();
		for(Device d : group.getDevices()) {
			if(d.equals(device))
				return group;
		}
		throw new AccessDeniedException(id);
	}
}
