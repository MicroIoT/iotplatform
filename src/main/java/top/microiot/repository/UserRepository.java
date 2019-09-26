package top.microiot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	public User findByUsername(String username);
}
