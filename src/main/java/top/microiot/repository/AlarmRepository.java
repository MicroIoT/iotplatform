package top.microiot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.Alarm;

@Repository
public interface AlarmRepository extends MongoRepository<Alarm, String>, CustomAlarmRepository{
	public int countByAlarmType(String alarmTypeId);
	public void deleteByDomainId(String id);
}
