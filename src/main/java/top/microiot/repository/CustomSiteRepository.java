package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import top.microiot.domain.Site;

public interface CustomSiteRepository {
	public Page<Site> querySite(String locationId, String domainId, String siteTypeId, String siteName, Pageable pageable);
	public List<Site> listSite(String locationId, String domainId, String siteTypeId, String siteName);
	public long countSite(String locationId, String domainId, String siteTypeId, String siteName);
}
