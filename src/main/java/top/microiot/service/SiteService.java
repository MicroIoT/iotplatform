package top.microiot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Device;
import top.microiot.domain.Domain;
import top.microiot.domain.ManagedObject;
import top.microiot.domain.Site;
import top.microiot.domain.SiteType;
import top.microiot.domain.User;
import top.microiot.domain.attribute.AttValueInfo;
import top.microiot.domain.attribute.AttributeValues;
import top.microiot.domain.attribute.DataValue;
import top.microiot.dto.SiteInfo;
import top.microiot.dto.SitePageInfo;
import top.microiot.dto.SiteRenameInfo;
import top.microiot.dto.SiteUpdateInfo;
import top.microiot.exception.ConflictException;
import top.microiot.exception.NotFoundException;
import top.microiot.exception.StatusException;
import top.microiot.repository.AlarmRepository;
import top.microiot.repository.ConfigRepository;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.EventRepository;
import top.microiot.repository.FavoriteRepository;
import top.microiot.repository.SiteRepository;
import top.microiot.repository.SiteTypeRepository;

@Service
public class SiteService extends IoTService{
	@Autowired
	private SiteTypeRepository siteTypeRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private DeviceManageService deviceManageService;
	@Autowired
	private AlarmRepository alarmRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private FavoriteRepository favoriteRepository;
	@Autowired
	private ConfigRepository configRepository;
	@Autowired
	private MOService moService;
	
	@Transactional
	public Site add(SiteInfo<AttValueInfo> info){
		Domain domain = getCurrentDomain();
		SiteType siteType = siteTypeRepository.findByNameAndDomain(info.getSiteType(), domain.getId());
		if(siteType == null)
			throw new NotFoundException(info.getSiteType());
		ManagedObject location = moService.getLocation1(info.getLocationId());
		if(location == null || !location.getDomain().equals(domain))
			throw new NotFoundException("location");
		if(!moService.isMyMO(location))
			throw new AccessDeniedException("location");
		
		AttributeValues attributes = new AttributeValues(info.getAttInfos(), siteType.getAttDefinition());
		Map<String, DataValue> attributeValues = attributes.getAttributes();
		
		Site site = new Site(info.getName(), location, siteType, attributeValues);
		try{
			return siteRepository.save(site);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("site name");
		}
	}
	
	@Transactional
	public Site rename(SiteRenameInfo info){
		Site site = siteRepository.findById(info.getId()).get();
		if(!moService.isMyMO(site))
			throw new AccessDeniedException("device");
		
		site.setName(info.getName());
		try{
			site = siteRepository.save(site);
			deviceManageService.reportAttributeChangedAlarm(site);
			
			return site;
		} catch (DuplicateKeyException e) {
			throw new ConflictException("site name");
		}
	}

	@Transactional
	public Site update(SiteUpdateInfo<AttValueInfo> info) {
		Site site = siteRepository.findById(info.getId()).get();
		if(!moService.isMyMO(site))
			throw new AccessDeniedException("device");
		
		AttributeValues attributes = new AttributeValues(info.getAttInfos(), site.getSiteType().getAttDefinition());
		Map<String, DataValue> attributeValues = attributes.getAttributes();
		
		site.setAttributes(attributeValues);
		site = siteRepository.save(site);
		deviceManageService.reportAttributeChangedAlarm(site);
		
		return site;
	}
	
	@Transactional
	public void delete(String siteId) {
		Site site = siteRepository.findById(siteId).get();
		if(!moService.isMyMO(site.getLocation()))
			throw new AccessDeniedException("device");
		Domain domain = getCurrentDomain();
		
		long n1 = siteRepository.countSite(siteId, domain.getId(), null, null);
		long n2 = deviceRepository.countDevice(siteId, domain.getId(), null, null);
		
		if((n1 + n2) > 0){
			throw new StatusException("not empty in this site");
		}
		
		moService.hasMOAccess(site);
		
		alarmRepository.deleteByNotifyObjectId(siteId);
		eventRepository.deleteByNotifyObjectId(siteId);
		favoriteRepository.deleteByMoId(siteId);
		configRepository.deleteByNotifyObjectId(siteId);
		siteRepository.deleteById(siteId);
		
	}

	public Site listSite(String id){
		return siteRepository.findById(id).get();
	}
	
	public Page<Site> listSite(SitePageInfo info){
		Pageable pageable = getPageable(info);   
		Domain domain = getCurrentDomain();
	       
		return siteRepository.querySite(info.getLocationId(), domain.getId(), info.getSiteTypeId(), info.getName(), pageable);
	}

	public long countSite(SitePageInfo info) {
		Domain domain = getCurrentDomain();
	    return siteRepository.countSite(info.getLocationId(), domain.getId(), info.getSiteTypeId(), info.getName());
	}
	
	public List<Site> getMySites() {
		User user = getCurrentUser();
		
		if(user.getArea() == null || user.getArea().isEmpty())
			return null;
		
		List<Site> sites = new ArrayList<Site>();
		for(ManagedObject site: user.getArea()) {
			if(!(site instanceof Device))
				getSites(sites, site);
		}
		
		return sites;
	}
	
	private void getSites(List<Site> sites, ManagedObject site) {
		if(site instanceof Site)
			sites.add((Site)site);
		List<Site> ss = siteRepository.listSite(site.getId(), null, null, null);
		
		if(ss != null && !ss.isEmpty()) {
			for(Site s: ss) {
				getSites(sites, s);
			}
		}
	}
}
