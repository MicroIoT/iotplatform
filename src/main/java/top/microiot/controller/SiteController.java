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

import top.microiot.domain.Site;
import top.microiot.domain.attribute.AttValueInfo;
import top.microiot.dto.SiteInfo;
import top.microiot.dto.SitePageInfo;
import top.microiot.dto.SiteRenameInfo;
import top.microiot.dto.SiteUpdateInfo;
import top.microiot.service.MOService;
import top.microiot.service.SiteService;

@RestController
@RequestMapping("/sites")
public class SiteController extends IoTController{
	@Autowired
	private SiteService siteService;
	@Autowired
	private MOService moService;
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("")
	public Site add(@RequestBody @Valid SiteInfo<AttValueInfo> info, BindingResult result) {
		throwError(result);
		return siteService.add(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{id}")
	public Site getSite(@PathVariable String id){
		return siteService.listSite(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}")
	public void deleteSite(@PathVariable String id){
		siteService.delete(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("")
	public Page<Site> getSites(@Valid SitePageInfo info, BindingResult result){
		throwError(result); 
		return siteService.listSite(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/count")
	public long getCount(@Valid SitePageInfo info, BindingResult result) {
		throwError(result);
		return siteService.countSite(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("/name")
	public Site renameSite(@RequestBody @Valid SiteRenameInfo info, BindingResult result) {
		throwError(result);
		return siteService.rename(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("")
	public Site updateSite(@RequestBody @Valid SiteUpdateInfo<AttValueInfo> info, BindingResult result) {
		throwError(result);
		return siteService.update(info);
	}
	
	@PreAuthorize("hasAuthority('AREA')")
	@GetMapping("/me")
	public List<Site> getMySites(){
		return siteService.getMySites();
	}
	
	@PreAuthorize("hasAuthority('AREA')")
	@GetMapping("/me/{siteId}")
	public boolean isMySite(@PathVariable String siteId){
		return moService.isMyMO(siteId);
	}
}
