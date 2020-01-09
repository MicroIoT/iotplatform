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

import top.microiot.domain.Device;
import top.microiot.domain.attribute.AttValueInfo;
import top.microiot.dto.DeviceInfo;
import top.microiot.dto.DeviceMoveInfo;
import top.microiot.dto.DevicePageInfo;
import top.microiot.dto.DeviceRenameInfo;
import top.microiot.dto.DeviceUpdateInfo;
import top.microiot.security.CustomUserDetails;
import top.microiot.service.DeviceManageService;
import top.microiot.service.DeviceService;
import top.microiot.service.MOService;

@RestController
@RequestMapping("/devices")
public class DeviceController extends IoTController{
	@Autowired
	private DeviceService deviceService;
	@Autowired
	private MOService moService;
	@Autowired
	private DeviceManageService deviceManageService;
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("")
	public Device register(@RequestBody @Valid DeviceInfo<AttValueInfo> info, BindingResult result) {
		throwError(result);
		return deviceManageService.register(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA') or hasAuthority('DEVICE')")
	@GetMapping("/{id}")
	public Device getDevice(@PathVariable String id){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		
		Device device;
		if(!user.isDevice())
			device = deviceService.getDevice(id);
		else
			device = deviceService.listDeviceByDevice(id);
		return device;
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA') ")
	@GetMapping("/username/{username}")
	public Device getDeviceByUsername(@PathVariable String username){
		Device device = deviceService.getDeviceByUsername(username);
		
		return device;
	}
	
	@PreAuthorize("hasAuthority('DEVICE')")
	@GetMapping("/me")
	public Device getDevice(){
		return deviceService.listCurrentDevice();
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("")
	public Page<Device> getDevices(@Valid DevicePageInfo info, BindingResult result){
		throwError(result);
        return deviceService.listDevice(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/count")
	public long getCount(@Valid DevicePageInfo info, BindingResult result) {
		throwError(result);
		return deviceService.countDevice(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("")
	public Device updateDevice(@RequestBody @Valid DeviceUpdateInfo<AttValueInfo> info, BindingResult result) {
		throwError(result);
		return deviceManageService.updateDevice(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("/site")
	public Device moveDevice(@RequestBody @Valid DeviceMoveInfo info, BindingResult result) {
		throwError(result);
		return deviceManageService.moveDevice(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("/name")
	public Device renameDevice(@RequestBody @Valid DeviceRenameInfo info, BindingResult result) {
		throwError(result);
		return deviceManageService.renameDevice(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}")
	public void delete(@PathVariable String id){
		deviceManageService.delete(id);
	}
	
	@PreAuthorize("hasAuthority('AREA')")
	@GetMapping("/area")
	public List<Device> getMyDevices(){
		return deviceService.getMyDevices();
	}
	
	@PreAuthorize("hasAuthority('AREA')")
	@GetMapping("/area/{deviceId}")
	public boolean isMyDevice(@PathVariable String deviceId){
		return moService.isMyMO(deviceId);
	}
}
