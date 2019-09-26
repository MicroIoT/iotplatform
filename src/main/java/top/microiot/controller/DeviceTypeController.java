package top.microiot.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.microiot.domain.DeviceType;
import top.microiot.domain.attribute.AttTypeInfo;
import top.microiot.dto.ActionTypeInfo;
import top.microiot.dto.DeviceTypeInfo;
import top.microiot.dto.DeviceTypeRenameInfo;
import top.microiot.dto.PageInfo;
import top.microiot.service.DeviceTypeService;

@RestController
@RequestMapping("/devicetypes")
public class DeviceTypeController extends IoTController{
	@Autowired
	private DeviceTypeService deviceTypeService;

	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("")
	public DeviceType addDeviceType(@RequestBody @Valid DeviceTypeInfo info, BindingResult result) {
		throwError(result);
		return deviceTypeService.add(info);
	}

	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{id}")
	public DeviceType getDeviceType(@PathVariable String id) {
		return deviceTypeService.listDeviceType(id);
	}

	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/page")
	public Page<DeviceType> getDeviceTypes( @Valid PageInfo info, BindingResult result){
		throwError(result);
		return deviceTypeService.listDeviceType(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/list")
	public List<DeviceType> getDeviceTypesList(){
		return deviceTypeService.listDeviceTypes();
	}

	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("/name")
	public DeviceType updateDeviceType(@RequestBody @Valid DeviceTypeRenameInfo info, BindingResult result) {
		throwError(result);
		return deviceTypeService.rename(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}")
	public void deleteDeviceType(@PathVariable String id){
		deviceTypeService.delete(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("/{id}/attribute")
	public DeviceType addAttribute(@PathVariable String id, @RequestBody @Valid AttTypeInfo info, BindingResult result) {
		throwError(result);
		return deviceTypeService.addAttribute(info, id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}/attribute/{attribute}")
	public DeviceType delAttribute(@PathVariable String id, @PathVariable String attribute) {
		return deviceTypeService.delAttribute(attribute, id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("/{id}/alarmtype")
	public DeviceType addAlarmType(@PathVariable String id, @RequestBody @Valid AttTypeInfo info, BindingResult result) {
		throwError(result);
		return deviceTypeService.addAlarmType(info, id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}/alarmtype/{alarmtype}")
	public DeviceType delAlarmType(@PathVariable String id, @PathVariable String alarmtype) {
		return deviceTypeService.delAlarmType(alarmtype, id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("/{id}/actiontype")
	public DeviceType addActionType(@PathVariable String id, @RequestBody @Valid ActionTypeInfo info, BindingResult result) {
		throwError(result);
		return deviceTypeService.addActionType(info, id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}/actiontype/{actiontype}")
	public DeviceType delActionType(@PathVariable String id, @PathVariable String actiontype) {
		return deviceTypeService.delActionType(actiontype, id);
	}
}
