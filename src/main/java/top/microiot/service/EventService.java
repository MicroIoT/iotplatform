package top.microiot.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import top.microiot.domain.Device;
import top.microiot.domain.Domain;
import top.microiot.domain.Event;
import top.microiot.domain.attribute.AttValueInfo;
import top.microiot.domain.attribute.DataType;
import top.microiot.domain.attribute.DataValue;
import top.microiot.domain.attribute.DeviceAttributeType;
import top.microiot.dto.EventInfo;
import top.microiot.dto.EventPageInfo;
import top.microiot.dto.SubDeviceEventInfo;
import top.microiot.exception.NotFoundException;
import top.microiot.exception.StatusException;
import top.microiot.exception.ValueException;
import top.microiot.repository.DeviceRepository;
import top.microiot.repository.EventRepository;

@Service
public class EventService extends IoTService{
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private DeviceService deviceService;
	
	@Transactional
	public void report(EventInfo info) {
		Device device = getCurrentDevice();
		reportEvent(info, device);
	}

	@Transactional
	public void report(SubDeviceEventInfo info) {
		Device subDevice = deviceRepository.findById(info.getDeviceId()).get();
		if(deviceService.isChild(subDevice.getId()))
			reportEvent(info, subDevice);
		else
			throw new StatusException("device id error");
	}
	
	private void reportEvent(EventInfo info, Device device) {
		Map<String, AttValueInfo> values = info.getValues();

		for (String name : values.keySet()) {
			AttValueInfo value = values.get(name);

			if (!device.getDeviceType().getAttDefinition().containsKey(name))
				throw new NotFoundException("attribute: " + name);

			DeviceAttributeType p = (DeviceAttributeType) device.getDeviceType().getAttDefinition().get(name);
			if(!p.isReport())
				throw new StatusException("attribute: "+ name + "can't be reported");
			DataType dataType = p.getDataType();
			if(!dataType.isValid(value))
				throw new ValueException("attribute: "+ name + " is not valid value");
			
			DataValue dataValue = DataValue.getDataValue(value, dataType);
			
			Event event = new Event(device, name, dataValue, info.getReportTime());

			eventRepository.save(event);
		}
	}

	public Event listEvent(String id) {
		return eventRepository.findById(id).get();
	}

	public Page<Event> listEvents(EventPageInfo info) {
		Pageable pageable = getPageable(info);   
        Domain domain = getCurrentDomain();
        
		return eventRepository.queryEvent(domain.getId(), info.getDeviceId(), info.getAttribute(), info.getReportFrom(), info.getReportTo(), info.getReceiveFrom(), info.getReceiveTo(), pageable);
	}
}
