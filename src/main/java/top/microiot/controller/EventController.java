package top.microiot.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.microiot.domain.Event;
import top.microiot.dto.EventInfo;
import top.microiot.dto.EventPageInfo;
import top.microiot.service.EventService;

@RestController
@RequestMapping("/events")
public class EventController extends IoTController{
	@Autowired
	private EventService eventService;
	
	@PreAuthorize("hasAuthority('DEVICE')")
	@PostMapping("")
	public void report(@RequestBody @Valid EventInfo info, BindingResult result) {
		throwError(result);
		eventService.report(info);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("/{id}")
	public Event getEvent(@PathVariable String id){
		return eventService.listEvent(id);
	}
	
	@PreAuthorize("hasAuthority('SYSTEM') or hasAuthority('AREA')")
	@GetMapping("")
	public Page<Event> listEvent(@Valid EventPageInfo info, BindingResult result){
		throwError(result); 
		return eventService.listEvents(info);
	}
}
