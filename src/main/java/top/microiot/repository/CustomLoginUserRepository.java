package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import top.microiot.domain.LoginUser;

public interface CustomLoginUserRepository {
	public Page<LoginUser> queryLoginUser(Boolean isDevice, Boolean isExpire, Pageable pageable);
	public List<LoginUser> removeLoginUserExpire(String username);
	public List<LoginUser> removeLoginUser(String username);
	public boolean existLoginUserNotExpire(String username);
}
