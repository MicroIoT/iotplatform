package top.microiot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.Domain;

@Repository
public interface DomainRepository extends MongoRepository<Domain, String> {
	public Domain findByName(String name);
}
