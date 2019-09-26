package top.microiot.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import top.microiot.domain.Site;

public class SiteRepositoryImpl implements CustomSiteRepository {
	@Autowired
	private PageRepository page;
	
	@Override
	public Page<Site> querySite(String locationId, String domainId, String siteTypeId, String siteName, Pageable pageable) {
		Query query = getQuery(locationId, domainId, siteTypeId, siteName);
		
		Page<Site> p = page.getPage(Site.class, query, pageable);
		return p;
	}
	
	@Override
	public List<Site> listSite(String locationId, String domainId, String siteTypeId, String siteName) {
		Query query = getQuery(locationId, domainId, siteTypeId, siteName);
		
		return page.query(Site.class, query);
	}

	@Override
	public long countSite(String locationId, String domainId, String siteTypeId, String name) {
		Query query = getQuery(locationId, domainId, siteTypeId, name);
		
		return page.count(Site.class, query);
	}

	private Query getQuery(String locationId, String domainId, String siteTypeId, String siteName) {
		Query query = new Query();
		if(domainId != null && domainId.length() > 0)
			query.addCriteria(Criteria.where("domain.$id").is(new ObjectId(domainId)));
		
		if(siteTypeId != null && siteTypeId.length() > 0)
			query.addCriteria(Criteria.where("siteType.$id").is(new ObjectId(siteTypeId)));
		
		if(siteName != null && siteName.length() > 0)
			query.addCriteria(Criteria.where("name").regex(siteName));
		
		if(locationId != null) {
			query.addCriteria(Criteria.where("location.$id").is(new ObjectId(locationId)));
		}
		
		return query;
	}

	
}
