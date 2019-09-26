package top.microiot.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import top.microiot.domain.Device;

public class DeviceRepositoryImpl implements CustomDeviceRepository {
	@Autowired
	private PageRepository page;

	@Override
	public Page<Device> queryDevice(String locationId,  String domainId, String deviceName, String deviceTypeId, Pageable pageable) {
		Query query = getQuery(locationId, domainId, deviceName, deviceTypeId);

		Page<Device> p = page.getPage(Device.class, query, pageable);
		return p;
	}

	@Override
	public long countDevice(String locationId,  String domainId, String name, String deviceTypeId) {
		Query query = getQuery(locationId, domainId, name, deviceTypeId);

		return page.count(Device.class, query);
	}

	private Query getQuery(String locationId,  String domainId, String name, String deviceTypeId) {
		Query query = new Query();
		if (deviceTypeId != null && deviceTypeId.length() > 0) {
			query.addCriteria(Criteria.where("deviceType.$id").is(new ObjectId(deviceTypeId)));
		}
		
		if (name != null && name.length() > 0)
			query.addCriteria(Criteria.where("name").regex(name));

		if(locationId != null && locationId.length() > 0) {
			query.addCriteria(Criteria.where("location.$id").is(new ObjectId(locationId)));
		}
		
		if(domainId != null ) {
			query.addCriteria(Criteria.where("domain.$id").is(new ObjectId(domainId)));
		}
		return query;
	}

	@Override
	public List<Device> listDevice(String siteId,  String domainId, String deviceName, String deviceTypeId) {
		Query query = getQuery(siteId, domainId, deviceName, deviceTypeId);

		return page.query(Device.class, query);
	}
}
