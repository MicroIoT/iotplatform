package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.DeviceGroup;

@Repository
public interface DeviceGroupRepository extends MongoRepository<DeviceGroup, String> {
	public List<DeviceGroup> findByDomainId(String id);
	public Page<DeviceGroup> findByDomain(String id, Pageable pageable);
	public boolean existsByDevicesContaining(List<String> devices);
	public List<DeviceGroup> findByDevicesId(String deviceId);
}
