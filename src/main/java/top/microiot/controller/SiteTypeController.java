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

import top.microiot.domain.SiteType;
import top.microiot.domain.attribute.AttTypeInfo;
import top.microiot.dto.PageInfo;
import top.microiot.dto.SiteTypeRenameInfo;
import top.microiot.service.SiteTypeService;

@RestController
@RequestMapping("/sitetypes")
public class SiteTypeController extends IoTController{
	@Autowired
	private SiteTypeService siteTypeService;
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("")
	public SiteType addSitetype(@RequestBody @Valid AttTypeInfo info, BindingResult result) {
		throwError(result);
		return siteTypeService.add(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{id}")
	public SiteType getSiteType(@PathVariable String id){
		return siteTypeService.listSiteType(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/page")
	public Page<SiteType> getSiteTypes(@Valid PageInfo info, BindingResult result){
		throwError(result);
		return siteTypeService.listSiteType(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/list")
	public List<SiteType> getSiteTypesList(){
		return siteTypeService.listSiteTypes();
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("/name")
	public SiteType updateSiteType(@RequestBody @Valid SiteTypeRenameInfo info, BindingResult result) {
		throwError(result);
		return siteTypeService.rename(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}")
	public void deleteSiteType(@PathVariable String id){
		siteTypeService.delete(id);
	}
}
