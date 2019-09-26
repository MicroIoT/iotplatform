package top.microiot.repository;

import java.util.List;

import top.microiot.domain.Configuration;

public interface CustomConfigRepository {
	public List<Configuration> queryConfiguration(String userId, Boolean top, Boolean silent);
	public Configuration findByUserAndNotifyObject(String userId, String notifyObjectId);
	public void deleteByNotifyObjectId(String id);
}
