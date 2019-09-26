package top.microiot.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Domain;
import top.microiot.domain.ManagedObject;
import top.microiot.domain.User;
import top.microiot.dto.DomainInfo;
import top.microiot.dto.DomainRenameInfo;
import top.microiot.exception.NotFoundException;
import top.microiot.repository.AlarmRepository;
import top.microiot.repository.ConfigRepository;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.DeviceTypeRepository;
import top.microiot.repository.DomainRepository;
import top.microiot.repository.EventRepository;
import top.microiot.repository.FavoriteRepository;
import top.microiot.repository.SiteRepository;
import top.microiot.repository.SiteTypeRepository;

@Service
public class DomainService extends IoTService{
	@Autowired
	private DomainRepository domainRepository;
	@Autowired
	private MOService moService;
	@Autowired
	private AlarmRepository alarmRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private FavoriteRepository favoriteRepository;
	@Autowired
	private ConfigRepository configRepository;
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private DeviceTypeRepository deviceTypeRepository;
	@Autowired
	private SiteTypeRepository siteTypeRepository;
	
	@Transactional
	public Domain addDomain(DomainInfo info) {
		Domain domain = new Domain(info.getName());
		
		return domainRepository.save(domain);
	}
	
	public Domain getByName(String name) {
		if(isMyDomain(name)) {
			Domain domain = domainRepository.findByName(name);
			if(domain == null)
				throw new NotFoundException("domain: " + name);
			return domain;
		}
		else
			throw new AccessDeniedException(name);
	}
	
	public Domain getById(String id) {
		Optional<Domain> domain = domainRepository.findById(id);
		if(domain.isPresent()) {
			if(isMyDomain(domain.get().getName())) {
				return domain.get();
			}
			else
				throw new AccessDeniedException(id);
		}
		else
			throw new NotFoundException(id);
	}

	@Transactional
	public Domain renameDomain(@Valid DomainRenameInfo info) {
		Domain domain = moService.hasDomainAccess();
		domain.setName(info.getName());
		return domainRepository.save(domain);
	}

	public List<Domain> getMyDomain() {
		User user = getCurrentUser();
		
		if(user.isSystem())
			return domainRepository.findAll();
		else if(user.isArea()) {
			List<ManagedObject> mos = user.getArea();
			Set<Domain> domains = new HashSet<Domain>();
			List<Domain> domainList = new ArrayList<Domain>();
			for(ManagedObject mo : mos) {
				Domain domain = mo.getDomain();
				domains.add(domain);
			}
			domainList.addAll(domains);
			return domainList;
		}
		else
			return null;
	}
	
	public boolean isMyDomain(String domain) {
		User user = getCurrentUser();
		
		return isMyDomain(domain, user);
	}

	public boolean isMyDomain(String domain, User user) {
		Domain dom = domainRepository.findByName(domain);
		if(dom == null)
			return false;
		if(user.isSystem())
			return true;
		else if(user.isArea()) {
			List<ManagedObject> mos = user.getArea();
			
			for(ManagedObject mo : mos) {
				Domain d = mo.getDomain();
				if(d.equals(dom))
					return true;
			}
			return false;
		}
		else
			return false;
	}

	@Transactional
	public void deleteDomain(String id) {
		Domain domain = domainRepository.findById(id).get();
		
		moService.hasMOAccess(domain);
		
		alarmRepository.deleteByDomainId(id);
		eventRepository.deleteByDomainId(id);
		favoriteRepository.deleteByDomainId(id);
		configRepository.deleteByDomainId(id);
		deviceRepository.deleteByDomainId(id);
		siteRepository.deleteByDomainId(id);
		deviceTypeRepository.deleteByDomainId(id);
		siteTypeRepository.deleteByDomainId(id);
		domainRepository.deleteById(id);
	}
}
