package top.microiot.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.microiot.domain.Alarm;
import top.microiot.dto.AlarmInfo;
import top.microiot.dto.AlarmPageInfo;
import top.microiot.service.AlarmService;

@RestController
@RequestMapping("/alarms")
public class AlarmController extends IoTController{
	@Autowired
	private AlarmService alarmService;
	
	@PreAuthorize("hasAuthority('DEVICE')")
	@PostMapping("")
	public Alarm report(@RequestBody @Valid AlarmInfo info, BindingResult result) {
		throwError(result);
		return alarmService.report(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{id}")
	public Alarm getAlarm(@PathVariable String id){
		return alarmService.listAlarm(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("")
	public Page<Alarm> listAlarm(@Valid AlarmPageInfo info, BindingResult result){
		throwError(result);
		
		return alarmService.listAlarms(info);
	}
}
