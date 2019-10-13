package top.microiot.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.microiot.domain.Configuration;
import top.microiot.dto.ConfigInfo;
import top.microiot.dto.ConfigListInfo;
import top.microiot.exception.ValueException;
import top.microiot.service.ConfigService;

@RestController
@RequestMapping("/configurations")
public class ConfigController extends IoTController{
	@Autowired
	private ConfigService configService;
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/list")
	public List<Configuration> listAll(@Valid ConfigListInfo info, BindingResult result){
		throwError(result);
		return configService.queryConfiguration(info.getTop(), info.getSilent(), info.getSubscribe());
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{moId}")
	public Configuration listOne(@PathVariable String moId){
		return configService.listOne(moId);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("")
	public Configuration config(@RequestBody @Valid ConfigInfo info, BindingResult result) {
		throwError(result);	
		if(info.getKey().equals("silent"))
			return configService.configSilent(info.getNotifyObjectId(), info.getValue());
		else if(info.getKey().equals("top"))
			return configService.configTop(info.getNotifyObjectId(), info.getValue());
		else if(info.getKey().equals("subscribe"))
			return configService.configSubscribe(info.getNotifyObjectId(), info.getValue());
		else
			throw new ValueException("illegal config: " + info.getKey());
	}
	
}
