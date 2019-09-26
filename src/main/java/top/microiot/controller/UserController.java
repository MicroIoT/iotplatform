package top.microiot.controller;

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

import top.microiot.domain.Role;
import top.microiot.domain.User;
import top.microiot.dto.PageInfo;
import top.microiot.dto.PasswordUpdateInfo;
import top.microiot.dto.UserInfo;
import top.microiot.dto.UserUpdateInfo;
import top.microiot.exception.ValueException;
import top.microiot.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController extends IoTController{
	@Autowired
	private UserService userService;
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@PostMapping("")
	public User add(@RequestBody @Valid UserInfo info, BindingResult result) {
		throwError(result);
		if(info.getRole() == Role.AREA)
			return userService.addUser(info.getUsername(), info.getPassword(), info.getEmail(), info.getArea());
		else if(info.getRole() == Role.SYSTEM)
			return userService.addAdmin(info.getUsername(), info.getPassword(), info.getEmail());
		throw new ValueException("error role");
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@PatchMapping("")
	public User update(@RequestBody @Valid UserUpdateInfo info, BindingResult result) {
		throwError(result);
		return userService.updateUser(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@DeleteMapping("/{id}")
	public void delete(@PathVariable String id) {
		userService.delete(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@GetMapping("/{username}")
	public User getUser(@PathVariable String username){
		return userService.listUserByUsername(username);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM')")
	@RequestMapping("")
	public Page<User> getUsers(@Valid PageInfo info, BindingResult result){
		throwError(result);
        return userService.listAll();
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/me")
	public User getUser(){
		return userService.listCurrentUser();
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PatchMapping("/password")
	public User updatePassword(@RequestBody @Valid PasswordUpdateInfo info, BindingResult result) {
		throwError(result);
		return userService.updatePassword(info.getPassword());
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/password")
	public boolean validatePassword(String password){
		return userService.validatePassword(password);
	}
}
