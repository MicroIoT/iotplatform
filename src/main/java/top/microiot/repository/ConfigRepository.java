package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.Configuration;

@Repository
public interface ConfigRepository extends MongoRepository<Configuration, String>, CustomConfigRepository {
	public List<Configuration> findByUserId(String userId);
	public Page<Configuration> findByUserId(String userId, Pageable pageable);
	public void deleteByDomainId(String id);
	public void deleteByUserId(String id);
}
