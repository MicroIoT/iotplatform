package top.microiot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import top.microiot.domain.LoginUser;

public interface LoginUserRepository extends MongoRepository<LoginUser, String>, CustomLoginUserRepository {
	public LoginUser findByToken(String token);
	public LoginUser findByRefreshToken(String refreshToken);
	public boolean existsByToken(String token);
	public boolean existsByUsername(String username);
}
