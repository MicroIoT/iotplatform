package top.microiot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Domain;
import top.microiot.domain.Favorite;
import top.microiot.domain.ManagedObject;
import top.microiot.domain.User;
import top.microiot.dto.FavoriteExistInfo;
import top.microiot.dto.FavoriteInfo;
import top.microiot.dto.FavoritePageInfo;
import top.microiot.dto.FavoriteUpdateInfo;
import top.microiot.exception.ConflictException;
import top.microiot.exception.NotFoundException;
import top.microiot.repository.FavoriteRepository;

@Service
public class FavoriteService extends IoTService{
	@Autowired
	private FavoriteRepository favoriteRepository;
	@Autowired
	private MOService moService;
	
	@Transactional
	public Favorite add(FavoriteInfo info) {
		User user = getCurrentUser();
		Domain domain = getCurrentDomain();
		
		ManagedObject mo = moService.getLocation(info.getId());
		if(!mo.getDomain().equals(domain))
			throw new NotFoundException(info.getId());
		
		Favorite favorite = new Favorite(info.getName(), mo, user, domain);
		try{
			return favoriteRepository.save(favorite);
		}catch (DuplicateKeyException e) {
			throw new ConflictException("favorite name");
		}
	}
	
	@Transactional
	public Favorite update(FavoriteUpdateInfo info) {
		Favorite favorite = favoriteRepository.findById(info.getId()).get();
		User user = getCurrentUser();
		
		if(favorite.getUser().getId().equals(user.getId()))
			favorite.setName(info.getNewName());
		else
			throw new NotFoundException("favorite");
		
		try{
			return favoriteRepository.save(favorite);
		}catch (DuplicateKeyException e) {
			throw new ConflictException("favorite name");
		}
	}
	
	@Transactional
	public void delete(String favoriteId) {
		Favorite favorite = favoriteRepository.findById(favoriteId).get();
		User user = getCurrentUser();
		
		if(favorite.getUser().getId().equals(user.getId()))
			favoriteRepository.delete(favorite);
		else
			throw new NotFoundException("favorite");
	}
	
	public Favorite listFavorite(String favoriteId) {
		return favoriteRepository.findById(favoriteId).get();
	}
	
	public Page<Favorite> listAll(FavoritePageInfo info) {
		User user = getCurrentUser();
		Pageable pageable = getPageable(info);   
        Domain domain = getCurrentDomain();
        
		Page<Favorite> fs = favoriteRepository.queryFavorite(user.getId(), info.getName(), info.getType(), domain.getId(), pageable);
		return fs;
	}
	
	public Favorite get(FavoriteExistInfo info) {
		User user = getCurrentUser();
		
		return favoriteRepository.getFavorite(user.getId(), info.getFavoriteId());
	}
}
