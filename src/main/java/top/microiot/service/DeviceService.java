package top.microiot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import top.microiot.domain.Device;
import top.microiot.domain.Domain;
import top.microiot.domain.ManagedObject;
import top.microiot.domain.Site;
import top.microiot.domain.User;
import top.microiot.domain.attribute.DeviceAttributeType;
import top.microiot.dto.DevicePageInfo;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.SiteRepository;

@Service
public class DeviceService extends IoTService{
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private DeviceGroupService deviceGroupService;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private MOService moService;
	
	public Device getDevice(String id){
		Device device = deviceRepository.findById(id).get();
		
		if(!moService.isMyMO(device)) {
			Map<String, DeviceAttributeType> map = device.getDeviceType().getAttDefinition();
			for (String key : map.keySet()) {
				DeviceAttributeType type  = map.get(key);
			    type.setGet(false);
				type.setSet(false);
				type.setReport(false);
			}
			
		}
		
		return device;
	}
	
	public Device getDeviceByUsername(String username) {
		Device device = deviceRepository.findByDeviceAccountUsername(username);
		
		if(!moService.isMyMO(device)) {
			Map<String, DeviceAttributeType> map = device.getDeviceType().getAttDefinition();
			for (String key : map.keySet()) {
				DeviceAttributeType type  = map.get(key);
			    type.setGet(false);
				type.setSet(false);
				type.setReport(false);
			}
			
		}
		
		return device;
	}
	
	public Device listDeviceByDevice(String id){
		if (deviceGroupService.isGroup(id) || isChild(id)) {
			Device device = deviceRepository.findById(id).get();
			return device;
		}
		else
			throw new AccessDeniedException("device");
		
	}
	
	public Device listCurrentDevice() {
		return getCurrentDevice();
	}
	
	public Page<Device> listDevice(DevicePageInfo info){
		Pageable pageable = getPageable(info);   
        Domain domain = getCurrentDomain();
        
		return deviceRepository.queryDevice(info.getLocationId(), domain.getId(), info.getName(), info.getDeviceTypeId(), pageable);
	}
	
	public long countDevice(DevicePageInfo info) {
		Domain domain = getCurrentDomain();
        return deviceRepository.countDevice(info.getLocationId(), domain.getId(), info.getName(), info.getDeviceTypeId());
	}
	
	public List<Device> getMyDevices(){
		User user = getCurrentUser();
		if(user.getArea() == null || user.getArea().isEmpty())
			return null;
		
		List<Device> devices = new ArrayList<Device>();
		for(ManagedObject site: user.getArea()) {
			if(site instanceof Device)
				devices.add((Device)site);
			else
				getSiteDevices(devices, site);
		}
		
		return devices;
	}

	private void getSiteDevices(List<Device> devices, ManagedObject site) {
		List<Device> ds = deviceRepository.listDevice(site.getId(), null, null, null);
		
		if(ds != null && !ds.isEmpty())
			devices.addAll(ds);
		
		List<Site> ss = siteRepository.listSite(site.getId(), null, null, null);
		if(ss != null && !ss.isEmpty()) {
			for(Site s: ss) {
				getSiteDevices(devices, s);
			}
		}
	}
	
	public  boolean isChild(String deviceId) {
		Device d = getCurrentDevice();
		Device child = deviceRepository.findById(deviceId).get();
		if(child.getGateway().equals(d))
			return true;
		else
			return false;
	}
	
	public List<Device> getSubDevice(String id){
		return deviceRepository.findByGatewayId(id);
	}
	
	public List<Device> getMySubDevice(){
		Device d = getCurrentDevice();
		return deviceRepository.findByGatewayId(d.getId());
	}
}
