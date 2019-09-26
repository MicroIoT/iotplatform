package top.microiot.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import top.microiot.domain.Event;

public interface CustomEventRepository {
	public Page<Event> queryEvent(String domainId, String notifyObjectId, String attribute, Date reportFrom, Date reportTo, Date receiveFrom, Date receiveTo, Pageable pageable);
	public void deleteByNotifyObjectId(String id);
}
