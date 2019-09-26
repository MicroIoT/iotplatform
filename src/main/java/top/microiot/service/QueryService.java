package top.microiot.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import top.microiot.domain.DocumentAggregationOperation;
import top.microiot.domain.Domain;
import top.microiot.domain.IoTObject;

@Service
public class QueryService extends IoTService{
	@Autowired
	private MongoTemplate mongoTemplate;
	
	public <T> List<T> find(String filter, String sort, String collation, Class<T> entityClass) {
		BasicQuery query = getQuery(filter, sort, collation);
		
		List<T> list = mongoTemplate.find(query, entityClass);
		valideList(filter, list);
		return list;
	}
	
	public <T> Page<T> findPage(String filter, String sort, String collation, int pageNumber, int pageSize, Class<T> entityClass) {
		BasicQuery query = getQuery(filter, sort, collation);
		Pageable p = PageRequest.of(pageNumber, pageSize);
		query.with(p);
		
		List<T> data = mongoTemplate.find(query, entityClass);
		long total = count(filter, entityClass);
		
		PageImpl<T> page = new PageImpl<T>(data, p, total);
		List<T> list = page.getContent();
		valideList(filter, list);
		return page;
	}

	public <T> GeoResults<T> findGeo(String filter, String sort, String collation, double x, double y, double max, Metrics metrics, int pageNumber, int pageSize, Class<T> entityClass) {
		BasicQuery query = getQuery(filter, sort, collation);
		Pageable p = PageRequest.of(pageNumber, pageSize);
		
		NearQuery near = NearQuery.near(x, y, metrics).maxDistance(max);
		near.query(query);
		near.with(p);
		
		GeoResults<T> geo = mongoTemplate.geoNear(near, entityClass);
		List<GeoResult<T>> list = geo.getContent();
		Domain domain = getCurrentDomain();
		for(GeoResult<T> t: list) {
			IoTObject object = (IoTObject) t.getContent();
			if(!object.getDomain().equals(domain))
				throw new AccessDeniedException(filter);
		}
		return geo;
	}
	
	public <T> List<T> distinct(String filter, String sort, String collation, String field, Class<?> entityClass, Class<T> resultClass){
		BasicQuery query = getQuery(filter, sort, collation);
		
		List<T> list = mongoTemplate.findDistinct(query, field, entityClass, resultClass);
//		valideList(filter, list);
		return list;
	}

	public <T> List<T> aggregate(String filter, String collectionName, Class<T> outputType) {
		@SuppressWarnings("unchecked")
		List<Document> documents = (List<Document>) Document.parse("{\"json\":" + filter + "}").get("json");
		List<DocumentAggregationOperation> operations = new ArrayList<DocumentAggregationOperation>();
		for(Document document : documents) {
			operations.add(new DocumentAggregationOperation(document));
		}
		Aggregation aggregation = Aggregation.newAggregation(operations);
		
		AggregationResults<T> results = mongoTemplate.aggregate(aggregation, collectionName, outputType);
		List<T> list = results.getMappedResults();
		valideList(filter, list);
		return list;
	}
	
	public <T> T findOne(String filter, String sort, String collation, Class<T> entityClass) {
		BasicQuery query = getQuery(filter, sort, collation);
		T one = mongoTemplate.findOne(query, entityClass);
		valideOne(filter, one);
		return one;
	}

	public <T> T findById(String id, Class<T> entityClass) {
		T one = mongoTemplate.findById(id, entityClass);
		valideOne(id, one);
		return one;
	}
	
	public long count(String filter, Class<?> entityClass) {
		BasicQuery query = new BasicQuery(filter);
		return mongoTemplate.count(query, entityClass);
	}
	
	public boolean exist(String filter, Class<?> entityClass) {
		BasicQuery query = new BasicQuery(filter);
		return mongoTemplate.exists(query, entityClass);
	}
	
	private BasicQuery getQuery(String filter, String sort, String collation) {
		BasicQuery query = new BasicQuery(filter);
		if(sort != null && sort.length() > 0)
			query.setSortObject(Document.parse(sort));
		if(collation != null && collation.length() > 0)
			query.collation(Collation.from(Document.parse(collation)));
		return query;
	}
	
	private <T> void valideList(String filter, List<T> list) {
		Domain domain = getCurrentDomain();
		for(T t: list) {
			IoTObject object = (IoTObject) t;
			if(!object.getDomain().equals(domain))
				throw new AccessDeniedException(filter);
		}
	}
	
	private <T> void valideOne(String filter, T one) {
		IoTObject object = (IoTObject) one;
		Domain domain = getCurrentDomain();
		if(!object.getDomain().equals(domain))
			throw new AccessDeniedException(filter);
	}
	
	
}
