package top.microiot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.Site;

@Repository
public interface SiteRepository extends MongoRepository<Site, String>, CustomSiteRepository {
	public Site findByNameAndSiteTypeIdAndLocation(String name, String siteTypeId, String parentId);
	public int countBySiteType(String siteTypeId);
	public void deleteByDomainId(String id);
}
