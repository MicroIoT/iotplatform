package top.microiot.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Domain;
import top.microiot.domain.SiteType;
import top.microiot.domain.attribute.AttTypeInfo;
import top.microiot.domain.attribute.AttributeType;
import top.microiot.domain.attribute.AttributeTypes;
import top.microiot.dto.PageInfo;
import top.microiot.dto.SiteTypeRenameInfo;
import top.microiot.exception.ConflictException;
import top.microiot.exception.NotFoundException;
import top.microiot.exception.StatusException;
import top.microiot.repository.SiteRepository;
import top.microiot.repository.SiteTypeRepository;

@Service
public class SiteTypeService extends IoTService{
	@Autowired
	private SiteTypeRepository siteTypeRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private DeviceTypeService deviceTypeService;
	@Autowired
	private MOService moService;
	
	@Transactional
	public SiteType add(AttTypeInfo info ){
		Domain domain = moService.hasDomainAccess();
		AttributeTypes attributes = new AttributeTypes(info.getAdditional());
		Map<String, AttributeType> attTypes = attributes.getAttTypes();
		
		SiteType siteType = new SiteType(info.getName(), info.getDescription(), domain, attTypes);
		try{
			siteType = siteTypeRepository.save(siteType);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("site type name");
		}
		
		deviceTypeService.createAttributeIndexs(siteType.getAttDefinition(), false);
		
		return siteType;
	}
	
	@Transactional
	public SiteType rename(SiteTypeRenameInfo info){
		moService.hasDomainAccess();
		
		SiteType type = siteTypeRepository.findById(info.getId()).get();
		type.setName(info.getName());
		type.setDescription(info.getDescription());
		
		try{
			return siteTypeRepository.save(type);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("site type name");
		}
	}
	
	public SiteType listSiteType(String id){
		SiteType type = siteTypeRepository.findById(id).get();
		return type;
	}
	
	public Page<SiteType> listSiteType(PageInfo info){
		Pageable pageable = getPageable(info);   
        Domain domain = getCurrentDomain();
        
		return siteTypeRepository.findByDomain(domain.getId(), pageable);
	}
	
	public List<SiteType> listSiteTypes(){
		Domain domain = getCurrentDomain();
        
		return siteTypeRepository.findByDomainId(domain.getId());
	}
	
	@Transactional
	public void delete(String id) {
		moService.hasDomainAccess();
		
		SiteType type = siteTypeRepository.findById(id).get();
		if(type == null)
			throw new NotFoundException("site type");
		int n = siteRepository.countBySiteType(id);
		
		if(n > 0){
			throw new StatusException("site type used");
		}
		else {
			siteTypeRepository.deleteById(id);
		}
	}
}
