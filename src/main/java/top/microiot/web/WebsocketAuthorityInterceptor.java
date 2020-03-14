package top.microiot.web;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.lang.reflect.Type;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import top.microiot.domain.Action;
import top.microiot.domain.Device;
import top.microiot.domain.Get;
import top.microiot.domain.Request;
import top.microiot.domain.Set;
import top.microiot.domain.Topic;
import top.microiot.domain.TopicType;
import top.microiot.exception.StatusException;
import top.microiot.security.CustomUserDetails;
import top.microiot.service.DeviceGroupService;
import top.microiot.service.DeviceService;
import top.microiot.service.MOService;
import top.microiot.service.TokenService;

@Component
public class WebsocketAuthorityInterceptor implements ChannelInterceptor {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DeviceService deviceService;
	@Autowired
	private DeviceGroupService deviceGroupService;
	@Autowired
	private MOService moService;
	@Autowired
	private TokenService tokenService;
	
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		MyStompHeaderAccessor headerAccessor = new MyStompHeaderAccessor(message);
		if(StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
			StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
			String token = accessor.getFirstNativeHeader(AUTHORIZATION);
			UserDetails user = tokenService.getUser(tokenService.extractToken(token));
			UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			accessor.setUser(userToken);
		}
		else if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
			Principal userPrincipal = headerAccessor.getUser();
			if (userPrincipal == null) {
				throw new IllegalArgumentException(
						"anonymous not allowed for this subscribe: " + headerAccessor.getDestination());
			}
			CustomUserDetails user = (CustomUserDetails) ((Authentication) userPrincipal).getPrincipal();

			if (!validateSubscription(user, headerAccessor.getDestination())) {
				throw new StatusException("user: " + user.getUsername() + " no permission for this subscribe: "
						+ headerAccessor.getDestination());
			}
		} else if (StompCommand.SEND.equals(headerAccessor.getCommand())) {
			Principal userPrincipal = headerAccessor.getUser();
			if (userPrincipal == null) {
				throw new StatusException(
						"anonymous not allowed to send to: " + headerAccessor.getDestination());
			}
			CustomUserDetails user = (CustomUserDetails) ((Authentication) userPrincipal).getPrincipal();

			if (!validateSend(user, headerAccessor.getDestination())) {
				throw new StatusException("user: " + user.getUsername() + " no permission to send to: "
						+ headerAccessor.getDestination());
			}
			Topic topic = new Topic(headerAccessor.getDestination());
			
			TopicType type = topic.getType();
			
			if(type == TopicType.Operation) {
				TopicType operation = topic.getOperationType();
				Type payloadType = null;
				if(operation == TopicType.GET)
					payloadType= Get.class;
				else if(operation == TopicType.SET)
					payloadType= Set.class;
				else if(operation == TopicType.ACTION)
					payloadType= Action.class;
				
				Class<?> resolvedType = ResolvableType.forType(payloadType).resolve();
				if (resolvedType == null) {
					throw new MessageConversionException("Unresolvable payload type [" + payloadType );
				}
				MessageConverter converter = new MappingJackson2MessageConverter();
				Request request = (Request) converter.fromMessage(message, resolvedType);
				request.setRequester(user.getUser());
				headerAccessor.updateSimpMessageHeadersFromStompHeaders();
				headerAccessor.setLeaveMutable(true);
				message = converter.toMessage(request, headerAccessor.getMessageHeaders());
				headerAccessor.updateStompHeadersFromSimpMessageHeaders();
			}
			
		}
		return message;
	}

	private boolean validateSubscription(CustomUserDetails user, String topicDestination) {
		logger.debug("user: " + user.getUsername() + " subscribe topic: " + topicDestination);
		Topic topic = new Topic(topicDestination);
		
		TopicType type = topic.getType();
		switch(type) {
		case Alarm:
			if(user.getUser().isArea() && (isMyTopic(topic)))
				return true;
			else if(user.getUser().isSystem())
				return true;
			else if(user.getUser().isDevice() && (isGroup(topic)))
				return true;
			else
				return false;
		case Result:
			if (user.getUser().isArea() && isMyTopic(topic)) 
				return true;
			else if(user.getUser().isSystem())
				return true;
			else if(user.getUser().isDevice() && (isGroup(topic)))
				return true;
			else
				return false;
		case Operation:
			if(user.getUser().isDevice() && isDeviceTopic(user, topic))
				return true;
			else
				return false;
		default:
			return false;
		}
	}

	private boolean isGroup(Topic topic) {
		String deviceId = topic.getNotifyObjectId();
		
		return deviceGroupService.isGroup(deviceId);
	}

	private boolean isMyTopic(Topic topic) {
		return moService.isMyMO(topic.getNotifyObjectId());
	}

	private boolean isDeviceTopic(CustomUserDetails user, Topic topic) {
		Device device = deviceService.listCurrentDevice();
		return device.getId().equals(topic.getNotifyObjectId()) || deviceService.isChild(topic.getNotifyObjectId());
	}

	private boolean validateSend(CustomUserDetails user, String topicDestination) {
		logger.debug("user: " + user.getUsername() + " send to topic: " + topicDestination);
		Topic topic = new Topic(topicDestination);
		
		TopicType type = topic.getType();
		switch(type) {
		case Alarm:
			if (user.getUser().isDevice() && isDeviceTopic(user, topic)) 
				return true;
			else
				return false;
		case Result:
			if (user.getUser().isDevice() && isDeviceTopic(user, topic)) 
				return true;
			else
				return false;
		case Operation:
			if(user.getUser().isArea() && isMyTopic(topic))
				return true;
			else if(user.getUser().isSystem() )
				return true;
			else if (user.getUser().isDevice() &&  (isGroup(topic))) 
				return true;
			else
				return false;
		default:
			return false;
		}
	}
}
