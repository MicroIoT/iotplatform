package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.Device;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String>, CustomDeviceRepository {
	public int countByDeviceTypeInAndNameLike(List<String> typeIds, String deviceName);
	public Device findByDeviceAccountUsername(String username);
	public Page<Device> findByNameLike(String deviceName, Pageable page);
	public int countByNameLike(String deviceName);
	public int countByDeviceType(String deviceTypeId);
	public void deleteByDomainId(String id);
}
