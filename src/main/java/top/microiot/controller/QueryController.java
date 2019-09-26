package top.microiot.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.geo.GeoResults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import top.microiot.domain.Alarm;
import top.microiot.domain.Device;
import top.microiot.domain.DeviceGroup;
import top.microiot.domain.DeviceType;
import top.microiot.domain.Event;
import top.microiot.domain.IoTObject;
import top.microiot.domain.Site;
import top.microiot.domain.SiteType;
import top.microiot.dto.DistinctInfo;
import top.microiot.dto.QueryInfo;
import top.microiot.dto.QueryNearPageInfo;
import top.microiot.dto.QueryPageInfo;
import top.microiot.exception.ValueException;
import top.microiot.service.QueryService;

@RestController
public class QueryController extends IoTController{
	@Autowired
	private QueryService queryService;
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/id/{id}")
	public <T> Object queryObject(@PathVariable String queryObject, @PathVariable String id) {
		return queryService.findById(id, getClass(queryObject));
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/one")
	public <T> Object queryObject(@PathVariable String queryObject, @Valid QueryInfo info, BindingResult result) {
		throwError(result);
		return queryService.findOne(info.getFilter(), info.getSort(), info.getCollation(), getClass(queryObject));
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/list")
	public List<?> queryObjectList(@PathVariable String queryObject, @Valid QueryInfo info, BindingResult result) {
		throwError(result);
		return queryService.find(info.getFilter(), info.getSort(), info.getCollation(), getClass(queryObject));
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/page")
	public Page<?> queryObjectPage(@PathVariable String queryObject, @Valid QueryPageInfo info, BindingResult result) {
		throwError(result);
		return queryService.findPage(info.getFilter(), info.getSort(), info.getCollation(), info.getPageNumber(), info.getPageSize(), getClass(queryObject));
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/geo")
	public GeoResults<?> queryObjectGeo(@PathVariable String queryObject, @Valid QueryNearPageInfo info, BindingResult result) {
		throwError(result);
		return queryService.findGeo(info.getFilter(), info.getSort(), info.getCollation(), 
				info.getX(), info.getY(), info.getMaxDistance(), info.getMetrics(),
				info.getPageNumber(), info.getPageSize(), getClass(queryObject));
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/aggregate")
	public List<?> queryObjectAggregate(@PathVariable String queryObject, @Valid QueryInfo info, BindingResult result) {
		throwError(result);
		return queryService.aggregate(info.getFilter(), getClass(queryObject).getSimpleName().toLowerCase(), getClass(queryObject));
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/distinct")
	public List<?> queryObjectDistinct(@PathVariable String queryObject, @Valid DistinctInfo info, BindingResult result) {
		throwError(result);
		return queryService.distinct(info.getFilter(), info.getSort(), info.getCollation(), info.getField(), getClass(queryObject), getClass(info.getReturnClass()));
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/count")
	public long queryObjectCount(@PathVariable String queryObject, @Valid QueryInfo info, BindingResult result) {
		throwError(result);
		return queryService.count(info.getFilter(), getClass(queryObject));
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{queryObject}/query/exist")
	public boolean queryObjectExist(@PathVariable String queryObject, @Valid QueryInfo info, BindingResult result) {
		throwError(result);
		return queryService.exist(info.getFilter(), getClass(queryObject));
	}
	
	private Class<? extends IoTObject> getClass(String className){
		if(className.equals("sites"))
			return Site.class;
		else if(className.equals("devices"))
			return Device.class;
		else if(className.equals("devicegroups"))
			return DeviceGroup.class;
		else if(className.equals("sitetypes"))
			return SiteType.class;
		else if(className.equals("devicetypes"))
			return DeviceType.class;
		else if(className.equals("alarms"))
			return Alarm.class;
		else if(className.equals("events"))
			return Event.class;
		else
			throw new ValueException("illegal query object: " + className);
	}
}
