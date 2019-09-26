package top.microiot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import top.microiot.domain.Favorite;

public interface CustomFavoriteRepository {
	public Page<Favorite> queryFavorite(String userId, String name, String type, String domainId, Pageable pageable);
	public Favorite getFavorite(String userId, String moId);
	public void deleteByMoId(String id);
}
