package top.microiot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import top.microiot.domain.Device;
import top.microiot.domain.Domain;
import top.microiot.domain.User;
import top.microiot.dto.PageInfo;
import top.microiot.exception.StatusException;
import top.microiot.repository.DeviceRepository;
import top.microiot.security.CustomUserDetails;

@Component
public abstract class IoTService {
	@Autowired
	private DeviceRepository deviceRepository;
	
	public User getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		return user.getUser();
	}
	
	public Device getCurrentDevice() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		return deviceRepository.findByDeviceAccountUsername(user.getUsername());
	}

	public Domain getCurrentDomain() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		Domain domain = user.getDomain();
		if (domain == null)
			throw new StatusException("Please choose domain first.");
		else
			return domain;
	}

	protected Pageable getPageable(PageInfo info) {
		if (info == null)
			info = new PageInfo();

		Sort sort = new Sort(Direction.ASC, "id");
		Pageable pageable = PageRequest.of(info.getCurrentPage(), info.getNumPerPage(), sort);
		return pageable;
	}
}
