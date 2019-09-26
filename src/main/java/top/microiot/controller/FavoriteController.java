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

import top.microiot.domain.Favorite;
import top.microiot.dto.FavoriteExistInfo;
import top.microiot.dto.FavoriteInfo;
import top.microiot.dto.FavoritePageInfo;
import top.microiot.dto.FavoriteUpdateInfo;
import top.microiot.service.FavoriteService;

@RestController
@RequestMapping("/favorites")
public class FavoriteController extends IoTController{
	@Autowired
	private FavoriteService favoriteService;
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@PostMapping("")
	public Favorite add(@RequestBody @Valid FavoriteInfo info, BindingResult result) {
		throwError(result);
		return favoriteService.add(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@DeleteMapping("/{id}")
	public void delete(@PathVariable String id){
		favoriteService.delete(id);
	}
	
	@PreAuthorize("hasAuthority('AREA') or hasAuthority('SYSTEM')")
	@GetMapping("")
	public Page<Favorite> getAll(@Valid FavoritePageInfo info, BindingResult result){
		throwError(result);
       return favoriteService.listAll(info);
	}
	
	@PreAuthorize("hasAuthority('AREA') or hasAuthority('SYSTEM')")
	@GetMapping("/{id}")
	public Favorite getFavorite(@PathVariable String id){
		return favoriteService.listFavorite(id);
	}
	
	@PreAuthorize("hasAuthority('AREA') or hasAuthority('SYSTEM')")
	@PatchMapping("")
	public Favorite update(@RequestBody @Valid FavoriteUpdateInfo info, BindingResult result) {
		throwError(result);
		return favoriteService.update(info);
	}
	
	@PreAuthorize("hasAuthority('AREA') or hasAuthority('SYSTEM')")
	@GetMapping("/exist")
	public Favorite isExist(@Valid FavoriteExistInfo info, BindingResult result) {
		throwError(result);
		return favoriteService.get(info);
	}
}
