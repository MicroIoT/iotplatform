package top.microiot.repository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import top.microiot.domain.Favorite;

public class FavoriteRepositoryImpl implements CustomFavoriteRepository {
	@Autowired
	private PageRepository page;
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public Page<Favorite> queryFavorite(String userId, String name, String type, String domainId, Pageable pageable) {
		Query query = new Query();
		query.addCriteria(Criteria.where("user.$id").is(new ObjectId(userId)));
		query.addCriteria(Criteria.where("domain.$id").is(new ObjectId(domainId)));
		
		if(type != null && type.length() > 0)
			query.addCriteria(Criteria.where("mo.$ref").is(type));
		if(name != null)
			query.addCriteria(Criteria.where("name").regex(name));

		return page.getPage(Favorite.class, query, pageable);
	}

	public Favorite getFavorite(String userId, String moId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("mo.$id").is(new ObjectId(moId)));
		query.addCriteria(Criteria.where("user.$id").is(new ObjectId(userId)));

		return mongoTemplate.findOne(query, Favorite.class);
	}

	@Override
	public void deleteByMoId(String id) {
		Query query = Query.query(Criteria.where("mo.$id").is(new ObjectId(id)));
		mongoTemplate.remove(query, Favorite.class);
	}
}
