package top.microiot.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import top.microiot.domain.Configuration;

public class ConfigRepositoryImpl implements CustomConfigRepository {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<Configuration> queryConfiguration(String domainId, String userId, Boolean top, Boolean silent, Boolean subscribe) {
		Query query = new Query();
		if(userId != null && userId.length() > 0)
			query.addCriteria(Criteria.where("user.$id").is(new ObjectId(userId)));
		
		if(top != null)
			query.addCriteria(Criteria.where("top").is(top));
		
		if(silent != null)
			query.addCriteria(Criteria.where("silent").is(silent));
		
		if(subscribe != null)
			query.addCriteria(Criteria.where("subscribe").is(subscribe));
		
		if(domainId != null ) {
			query.addCriteria(Criteria.where("domain.$id").is(new ObjectId(domainId)));
		}
		return mongoTemplate.find(query, Configuration.class); 
	}

	@Override
	public Configuration findByUserAndNotifyObject(String userId, String notifyObjectId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("notifyObject.$id").is(new ObjectId(notifyObjectId)));
		query.addCriteria(Criteria.where("user.$id").is(new ObjectId(userId)));
		
		return mongoTemplate.findOne(query, Configuration.class);
	}

	@Override
	public void deleteByNotifyObjectId(String id) {
		Query query = Query.query(Criteria.where("notifyObject.$id").is(new ObjectId(id)));
		mongoTemplate.remove(query, Configuration.class);
	}

}
