package top.microiot.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.ActionType;
import top.microiot.domain.Alarm;
import top.microiot.domain.Device;
import top.microiot.domain.DeviceType;
import top.microiot.domain.Domain;
import top.microiot.domain.Event;
import top.microiot.domain.Site;
import top.microiot.domain.attribute.AttTypeInfo;
import top.microiot.domain.attribute.AttributeType;
import top.microiot.domain.attribute.AttributeTypes;
import top.microiot.domain.attribute.DataType;
import top.microiot.domain.attribute.DeviceAttributeType;
import top.microiot.domain.attribute.DeviceAttributeTypes;
import top.microiot.dto.ActionTypeInfo;
import top.microiot.dto.DeviceTypeInfo;
import top.microiot.dto.DeviceTypeRenameInfo;
import top.microiot.dto.PageInfo;
import top.microiot.exception.ConflictException;
import top.microiot.exception.NotFoundException;
import top.microiot.exception.StatusException;
import top.microiot.exception.ValueException;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.DeviceTypeRepository;

@Service
public class DeviceTypeService extends IoTService{
	@Autowired
	private DeviceTypeRepository typeRepository;
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private MOService moService;
	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Transactional
	public DeviceType add(DeviceTypeInfo info) {
		Domain domain = moService.hasDomainAccess();
		DeviceAttributeTypes attributes = new DeviceAttributeTypes(info.getAdditional());
		Map<String, DeviceAttributeType> params = attributes.getAttTypes();
		AttributeTypes staticAttribute = new AttributeTypes(info.getStaticAttTypeInfo());
		Map<String, AttributeType> staticAttTypes = staticAttribute.getAttTypes();
		AttributeTypes alarmAttribute = new AttributeTypes(info.getAlarmTypeInfos());
		Map<String, AttributeType> alarmTypes = alarmAttribute.getAttTypes();
		Map<String, ActionType> actionTypes = getActionTypes(info.getActionTypeInfos());
		
		DeviceType type = new DeviceType(info.getName(), info.getDescription(), domain, params, staticAttTypes, alarmTypes, actionTypes);
		try{
			type = typeRepository.save(type);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("device type name");
		}
		
//		createAttributeIndexs(type.getStaticAttDefinition(), true);
//		createEventIndexs(type.getAttDefinition());
//		createAlarmIndexs(type.getAlarmTypes());
		
		return type;
	}

	public void createAlarmIndexs(Map<String, AttributeType> type) {
		if(type != null) {
			for (Map.Entry<String, AttributeType> entry : type.entrySet()) {
				createAlarmIndex(entry.getKey(), entry.getValue());
			}
		}
	}

	private void createAlarmIndex(String key, AttributeType value) {
		if(isLocation(value)) {
			mongoTemplate.indexOps(Alarm.class).ensureIndex(new GeospatialIndex("alarmInfo.value." + key));
		}
	}

	private boolean isLocation(AttributeType value) {
		return value.getDataType().getType() == DataType.Type.Location;
	}

	public void createEventIndexs(Map<String, DeviceAttributeType> map) {
		if(map != null) {
			for (Entry<String, DeviceAttributeType> entry : map.entrySet()) {
				createEventIndex(entry.getKey(), entry.getValue());
			}
		}
	}

	private void createEventIndex(String key, DeviceAttributeType value) {
		if(isLocation(value) && value.isReport()) {
			mongoTemplate.indexOps(Event.class).ensureIndex(new GeospatialIndex("value"));
		}
	}

	public void createAttributeIndexs(Map<String, AttributeType> type, boolean isDevice) {
		if(type != null) {
			for (Map.Entry<String, AttributeType> entry : type.entrySet()) {
				createAttributeIndex(entry.getKey(), entry.getValue(), isDevice);
			}
		}
	}

	private void createAttributeIndex(String key, AttributeType value, boolean isDevice) {
		if(isLocation(value)) {
			if(isDevice)
				mongoTemplate.indexOps(Device.class).ensureIndex(new GeospatialIndex("attributes." + key));
			else
				mongoTemplate.indexOps(Site.class).ensureIndex(new GeospatialIndex("attributes." + key));
		}
	}

	@Transactional
	public DeviceType rename(DeviceTypeRenameInfo info){
		moService.hasDomainAccess();
		
		DeviceType type = typeRepository.findById(info.getId()).get();
		type.setName(info.getName());
		type.setDescription(info.getDescription());
		
		try{
			return typeRepository.save(type);
		} catch (DuplicateKeyException e) {
			throw new ConflictException("device type name");
		}
	}
	
	private Map<String, ActionType> getActionTypes(List<ActionTypeInfo> actionTypeInfos) {
		Map<String, ActionType> actionTypes = null;
		
		if(actionTypeInfos != null && actionTypeInfos.size() > 0) {
			actionTypes = new HashMap<String, ActionType>();
			for(ActionTypeInfo actionType : actionTypeInfos){
				ActionType at = getActionType(actionType);
				actionTypes.put(actionType.getName(), at);
			}
		}
		return actionTypes;
	}

	private ActionType getActionType(ActionTypeInfo actionType) {
		String requestName = null;
		AttributeType request = null;
		String responseName = null;
		AttributeType response = null;
		
		if(actionType.getRequestInfo() != null) {
			requestName = actionType.getRequestInfo().getName();
			request = new AttributeType(actionType.getRequestInfo());
		}
		if(actionType.getResponseInfo() != null) {
			responseName = actionType.getResponseInfo().getName();
			response = new AttributeType(actionType.getResponseInfo());
		}
		ActionType at = new ActionType(requestName, request, responseName, response, actionType.getDescription());
		return at;
	}
	
	public DeviceType listDeviceType(String id){
		DeviceType type = typeRepository.findById(id).get();
		return type;
	}
	
	public Page<DeviceType> listDeviceType(PageInfo info){
		Pageable pageable = getPageable(info);   
        Domain domain = getCurrentDomain();
        
		return typeRepository.findByDomain(domain.getId(), pageable);
	}
	
	public List<DeviceType> listDeviceTypes(){
		Domain domain = getCurrentDomain();
        return typeRepository.findByDomainId(domain.getId());
	}
	
	@Transactional
	public void delete(String id) {
		moService.hasDomainAccess();
		
		DeviceType type = typeRepository.findById(id).get();
		if(type == null)
			throw new NotFoundException("deivce type");
		int n = deviceRepository.countByDeviceType(id);
		
		if(n > 0){
			throw new StatusException("device type used");
		}
		else {
			typeRepository.deleteById(id);
		}
	}
	
	@Transactional
	public DeviceType addAttribute(AttTypeInfo info, String id) {
		moService.hasDomainAccess();
		
		DeviceType type = typeRepository.findById(id).get();
		
		if(type.getAttDefinition() != null && type.getAttDefinition().containsKey(info.getName()))
			throw new ValueException("attribute: " + info.getName() + " existed.");
		else if(type.getAttDefinition() == null)
			type.setAttDefinition(new HashMap<String, DeviceAttributeType>());
		
		DeviceAttributeType value = new DeviceAttributeType(info);
		type.getAttDefinition().put(info.getName(), value);
		
		type = typeRepository.save(type);
		
		createEventIndex(info.getName(), value);
		
		return type;
	}
	
	@Transactional
	public DeviceType delAttribute(String attribute, String id) {
		moService.hasDomainAccess();
		
		DeviceType type = typeRepository.findById(id).get();
		
		if((type.getAttDefinition() != null && !type.getAttDefinition().containsKey(attribute)) || type.getAttDefinition() == null)
			throw new NotFoundException("attribute: " + attribute);
		
		type.getAttDefinition().remove(attribute);
		
		return typeRepository.save(type);
	}
	
	@Transactional
	public DeviceType addAlarmType(AttTypeInfo info, String id) {
		moService.hasDomainAccess();
		
		DeviceType type = typeRepository.findById(id).get();
		
		if(type.getAlarmTypes() != null && type.getAlarmTypes().containsKey(info.getName()))
			throw new ValueException("alarm type: " + info.getName() + " existed.");
		else if(type.getAlarmTypes() == null)
			type.setAlarmTypes(new HashMap<String, AttributeType>());
		
		AttributeType value = new AttributeType(info);
		type.getAlarmTypes().put(info.getName(), value);
		
		type = typeRepository.save(type);
		
		createAlarmIndex(info.getName(), value);
		
		return type;
	}

	@Transactional
	public DeviceType delAlarmType(String alarmType, String id) {
		moService.hasDomainAccess();
		
		DeviceType type = typeRepository.findById(id).get();
		
		if((type.getAlarmTypes() != null && !type.getAlarmTypes().containsKey(alarmType)) || type.getAlarmTypes() == null)
			throw new NotFoundException("alarm type: " + alarmType);
		
		type.getAlarmTypes().remove(alarmType);
		
		return typeRepository.save(type);
	}
	
	@Transactional
	public DeviceType addActionType(ActionTypeInfo info, String id) {
		moService.hasDomainAccess();
		
		DeviceType type = typeRepository.findById(id).get();
		
		if(type.getActionTypes() != null && type.getActionTypes().containsKey(info.getName()))
			throw new ValueException("action type: " + info.getName() + " existed.");
		else if(type.getActionTypes() == null)
			type.setActionTypes(new HashMap<String, ActionType>());
		
		type.getActionTypes().put(info.getName(), getActionType(info));
		
		return typeRepository.save(type);
	}
	
	@Transactional
	public DeviceType delActionType(String actionType, String id) {
		moService.hasDomainAccess();
		
		DeviceType type = typeRepository.findById(id).get();
		
		if((type.getActionTypes() != null && !type.getActionTypes().containsKey(actionType)) || type.getActionTypes() == null)
			throw new NotFoundException("action type: " + actionType);
		
		type.getActionTypes().remove(actionType);
		
		return typeRepository.save(type);
	}
	
	
	
}
