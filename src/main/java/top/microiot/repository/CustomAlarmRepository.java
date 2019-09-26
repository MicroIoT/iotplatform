package top.microiot.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import top.microiot.domain.Alarm;

public interface CustomAlarmRepository {
	public Page<Alarm> queryAlarm(String domainId, String notifyObjectId, String alarmType, Date reportFrom, Date reportTo, Date receiveFrom, Date receiveTo, Pageable pageable);
	public void deleteByNotifyObjectId(String id);
}
