package top.microiot.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.microiot.domain.DeviceGroup;
import top.microiot.dto.DeviceGroupInfo;
import top.microiot.dto.DeviceGroupRenameInfo;
import top.microiot.dto.PageInfo;
import top.microiot.security.CustomUserDetails;
import top.microiot.service.DeviceGroupService;

@RestController
@RequestMapping("/devicegroups")
public class DeviceGroupController extends IoTController{
	@Autowired
	private DeviceGroupService deviceGroupService;

	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("")
	public DeviceGroup addDeviceGroup(@RequestBody @Valid DeviceGroupInfo info, BindingResult result) {
		throwError(result);
		return deviceGroupService.addDeviceGroup(info);
	}

	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA') or hasAuthority('DEVICE')")
	@GetMapping("/{id}")
	public DeviceGroup getDeviceGroup(@PathVariable String id) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		
		if(!user.isDevice())
			return deviceGroupService.listDeviceGroup(id);
		else
			return deviceGroupService.listDeviceGroupByDevice(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/page")
	public Page<DeviceGroup> getDeviceGroups( @Valid PageInfo info, BindingResult result){
		throwError(result);
		return deviceGroupService.listDeviceGroups(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/list")
	public List<DeviceGroup> getDeviceGroupsList(){
		return deviceGroupService.listDeviceGroups();
	}
	
	@PreAuthorize("hasAuthority('DEVICE')")
	@GetMapping("/me")
	public List<DeviceGroup> getMyDeviceGroups(){
		return deviceGroupService.listMyDeviceGroups();
	}

	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("/name")
	public DeviceGroup updateDeviceGroup(@RequestBody @Valid DeviceGroupRenameInfo info, BindingResult result) {
		throwError(result);
		return deviceGroupService.rename(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}")
	public void deleteDeviceGroup(@PathVariable String id){
		deviceGroupService.delete(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("/group/{groupId}/device/{deviceId}")
	public DeviceGroup addGroup(@PathVariable String groupId, @PathVariable String deviceId) {
		return deviceGroupService.addGroup(groupId, deviceId);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/group/{groupId}/device/{deviceId}")
	public DeviceGroup removeGroup(@PathVariable String groupId, @PathVariable String deviceId) {
		return deviceGroupService.removeGroup(groupId, deviceId);
	}
}
