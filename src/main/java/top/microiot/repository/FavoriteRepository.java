package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.Favorite;

@Repository
public interface FavoriteRepository extends MongoRepository<Favorite, String>, CustomFavoriteRepository {
	public Page<Favorite> findByUser(String userId, Pageable page);
	public List<Favorite> deleteByUserId(String userId);
	public Page<Favorite> findByUserAndNameLike(String userId, String name, Pageable page);
	public void deleteByDomainId(String id);
}
