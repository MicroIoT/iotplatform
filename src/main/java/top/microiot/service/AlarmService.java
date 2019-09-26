package top.microiot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import top.microiot.domain.Alarm;
import top.microiot.domain.Device;
import top.microiot.domain.Domain;
import top.microiot.domain.Topic;
import top.microiot.domain.attribute.AttributeType;
import top.microiot.domain.attribute.DataValue;
import top.microiot.dto.AlarmInfo;
import top.microiot.dto.AlarmPageInfo;
import top.microiot.exception.NotFoundException;
import top.microiot.repository.AlarmRepository;

@Service
public class AlarmService extends IoTService {
	@Autowired
	private AlarmRepository alarmRepository;
	@Autowired
	private SimpMessagingTemplate template;
	
	public Alarm report(AlarmInfo info) {
		Device device = getCurrentDevice();
		
		if(device.getDeviceType().getAlarmTypes() == null || !device.getDeviceType().getAlarmTypes().containsKey(info.getAlarmType()))
			throw new NotFoundException("alarm type: " + info.getAlarmType() + " in this device: " + device.getName());
		
		AttributeType alarmType = device.getDeviceType().getAlarmTypes().get(info.getAlarmType());
		DataValue value = DataValue.getDataValue(info.getAlarmInfo(), alarmType.getDataType());
		Alarm alarm = new Alarm(device, info.getAlarmType(), value, info.getReportTime());
		
		alarm = alarmRepository.save(alarm);
		
		String destination = Topic.TOPIC_ALARM + device.getId();
		template.convertAndSend(destination, alarm);
		return alarm;
	}
	
	public Alarm listAlarm(String id) {
		return alarmRepository.findById(id).get();
	}

	public Page<Alarm> listAll() {
		Pageable pageable = getPageable(null);   
        
		return alarmRepository.findAll(pageable);
	}

	public Page<Alarm> listAlarms(AlarmPageInfo info) {
		Pageable pageable = getPageable(info);   
        Domain domain = getCurrentDomain();
        
		return alarmRepository.queryAlarm(domain.getId(), info.getNotifyObjectId(), info.getAlarmType(), info.getReportFrom(), info.getReportTo(), info.getReceiveFrom(), info.getReceiveTo(), pageable);
	}
}
