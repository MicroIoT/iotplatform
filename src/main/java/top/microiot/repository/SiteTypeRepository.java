package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.SiteType;

@Repository
public interface SiteTypeRepository extends MongoRepository<SiteType, String> {
	public SiteType findByNameAndDomain(String name, String domainId);
	public List<SiteType> findByDomainId(String domainId);
	public Page<SiteType> findByDomain(String domainId, Pageable pageable);
	public void deleteByDomainId(String id);
}
