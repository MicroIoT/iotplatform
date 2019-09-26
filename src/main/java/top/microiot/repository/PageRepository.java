package top.microiot.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class PageRepository {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	public <T> Page<T> getPage(Class<T> entityClass, Query query, Pageable pageable){
		long total = mongoTemplate.count(query, entityClass);
		
		query.skip(pageable.getOffset()); 
		query.limit(pageable.getPageSize()); 
		query.with(pageable.getSort());
		
		List<T> data = mongoTemplate.find(query, entityClass);  
	     
		return new PageImpl<T>(data, pageable, total);
	}
	
	public <T> long count(Class<T> entityClass, Query query){
		return mongoTemplate.count(query, entityClass);
	}
	
	public <T> List<T> query(Class<T> entityClass, Query query){
		return mongoTemplate.find(query, entityClass);
	}
}
