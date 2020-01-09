package top.microiot.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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

import top.microiot.domain.Domain;
import top.microiot.domain.Token;
import top.microiot.dto.DomainInfo;
import top.microiot.dto.DomainRenameInfo;
import top.microiot.service.DomainService;
import top.microiot.service.TokenService;

@RestController
@RequestMapping("/domains")
public class DomainController extends IoTController{
	@Autowired
	private DomainService domainService;
	@Autowired
	private TokenService tokenService;
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@PostMapping("")
	public Domain add(@RequestBody @Valid DomainInfo info, BindingResult result) {
		throwError(result);
		return domainService.addDomain(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("/{name}")
	public Token chooseDomain(HttpServletRequest request, @PathVariable String name){
		String token = request.getHeader(AUTHORIZATION);
		return tokenService.updateToken(name, token);
	}

	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/name/{name}")
	public Domain getDomainByName(@PathVariable String name){
		return domainService.getByName(name);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/id/{id}")
	public Domain getDomainById(@PathVariable String id){
		return domainService.getById(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("")
	public Domain getCurrentDomain(){
		return domainService.getCurrentDomain();
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@PatchMapping("")
	public Domain renameDomain(@RequestBody @Valid DomainRenameInfo info, BindingResult result) {
		throwError(result);
		return domainService.renameDomain(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/me")
	public List<Domain> getMyDomain(){
		return domainService.getMyDomain();
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@DeleteMapping("/{id}")
	public void deleteDomain(@PathVariable String id) {
		domainService.deleteDomain(id);
	}
}
