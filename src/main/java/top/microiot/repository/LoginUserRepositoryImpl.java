package top.microiot.repository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import top.microiot.domain.LoginUser;

public class LoginUserRepositoryImpl implements CustomLoginUserRepository {
	@Autowired
	private PageRepository page;
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public Page<LoginUser> queryLoginUser(Boolean isDevice, Boolean isExpire, Pageable pageable) {
		Query query = new Query();
		if(isDevice != null)
			query.addCriteria(Criteria.where("isDevice").is(isDevice));
		if(isExpire != null) {
			if(isExpire)
				query.addCriteria(Criteria.where("refreshExpire").lt(new Date()));
			else
				query.addCriteria(Criteria.where("refreshExpire").gt(new Date()));
		}
		return page.getPage(LoginUser.class, query, pageable);
	}

	@Override
	public List<LoginUser> removeLoginUserExpire(String username) {
		Query query = new Query();
		query.addCriteria(Criteria.where("username").is(username));
		query.addCriteria(Criteria.where("refreshExpire").lt(new Date()));
		return mongoTemplate.findAllAndRemove(query, LoginUser.class);
	}

	@Override
	public List<LoginUser> removeLoginUser(String username) {
		Query query = new Query();
		query.addCriteria(Criteria.where("username").is(username));
		return mongoTemplate.findAllAndRemove(query, LoginUser.class);
	}

	@Override
	public boolean existLoginUserNotExpire(String username) {
		Query query = new Query();
		query.addCriteria(Criteria.where("username").is(username));
		query.addCriteria(Criteria.where("refreshExpire").gt(new Date()));
		return mongoTemplate.exists(query, LoginUser.class);
	}
}
