package top.microiot.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import top.microiot.domain.SiteType;
import top.microiot.domain.SiteTypeFile;
import top.microiot.domain.attribute.AttTypeInfo;
import top.microiot.dto.PageInfo;
import top.microiot.dto.SiteTypeRenameInfo;
import top.microiot.exception.StatusException;
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
	@PostMapping("/import")
	public SiteType importSiteType(@RequestParam("file") MultipartFile file) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			InputStream inputStream = new BufferedInputStream(file.getInputStream());
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			SiteTypeFile siteType = mapper.readValue(reader, SiteTypeFile.class);
			return siteTypeService.add(siteType);
		} catch (IOException e) {
			throw new StatusException(e.getMessage());
		}
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{id}")
	public SiteType getSiteType(@PathVariable String id){
		return siteTypeService.listSiteType(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/export/{id}")
	public void exportSiteType(@PathVariable String id, HttpServletResponse response) throws UnsupportedEncodingException {
		SiteType siteType = siteTypeService.listSiteType(id);
		
		response.setContentType("text/json;charset=utf-8");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=sitetype.json");
        
        SiteTypeFile file = new SiteTypeFile(siteType);
        ObjectMapper mapper = new ObjectMapper();
        
    	try {
			mapper.writeValue(response.getWriter(), file);
		} catch (IOException e) {
			throw new StatusException(e.getMessage());
		}
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
