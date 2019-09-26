package top.microiot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import top.microiot.domain.Device;

public interface CustomDeviceRepository {
	public Page<Device> queryDevice(String locationId,  String domainId, String deviceName, String deviceTypeId, Pageable pageable);
	public List<Device> listDevice(String locationId,  String domainId, String deviceName, String deviceTypeId);
	public long countDevice(String locationId,  String domainId, String deviceName, String deviceTypeId);
}
