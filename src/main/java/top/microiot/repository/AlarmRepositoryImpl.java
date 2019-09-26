package top.microiot.repository;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import top.microiot.domain.Alarm;

public class AlarmRepositoryImpl implements CustomAlarmRepository {
	@Autowired
	private PageRepository page;
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public Page<Alarm> queryAlarm(String domainId, String notifyObjectId, String alarmType, Date reportFrom, Date reportTo, Date receiveFrom, Date receiveTo, Pageable pageable) {
		Query query = new Query();
		query.addCriteria(Criteria.where("domain.$id").is(new ObjectId(domainId)));
		
		if(notifyObjectId != null)
			query.addCriteria(Criteria.where("notifyObject.$id").is(new ObjectId(notifyObjectId)));
		
		if(alarmType != null)
			query.addCriteria(Criteria.where("alarmType").is(alarmType));
		
		if(reportFrom != null && reportTo != null){
			Criteria c = new Criteria().andOperator(Criteria.where("reportTime").gt(reportFrom), Criteria.where("reportTime").lt(reportTo));
			query.addCriteria(c);
		}
		if(reportFrom != null && reportTo == null)
			query.addCriteria(Criteria.where("reportTime").gt(reportFrom));
		if(reportFrom == null && reportTo != null)
			query.addCriteria(Criteria.where("reportTime").lt(reportTo));
		
		if(reportFrom != null && reportTo != null){
			Criteria c = new Criteria().andOperator(Criteria.where("receiveTime").gt(reportFrom), Criteria.where("receiveTime").lt(reportTo));
			query.addCriteria(c);
		}
		if(reportFrom != null && reportTo == null)
			query.addCriteria(Criteria.where("receiveTime").gt(reportFrom));
		if(reportFrom == null && reportTo != null)
			query.addCriteria(Criteria.where("receiveTime").lt(reportTo));
			
		return page.getPage(Alarm.class, query, pageable);
	}
	
	@Override
	public void deleteByNotifyObjectId(String id) {
		Query query = Query.query(Criteria.where("notifyObject.$id").is(new ObjectId(id)));
		mongoTemplate.remove(query, Alarm.class);
	}
}
