package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import top.microiot.domain.DeviceType;

@Repository
public interface DeviceTypeRepository extends MongoRepository<DeviceType, String> { 
	public DeviceType findByNameAndDomain(String typeName, String domainId);
	public List<DeviceType> findByDomainId(String domainId);
	public Page<DeviceType> findByDomain(String domainId, Pageable pageable);
	public void deleteByDomainId(String id);
}
