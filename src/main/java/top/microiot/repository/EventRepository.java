package top.microiot.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.Event;

@Repository
public interface EventRepository extends MongoRepository<Event, String>, CustomEventRepository {
	public void deleteByDomainId(String id);
}
