package top.microiot.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

import top.microiot.domain.Role;
import top.microiot.domain.Topic;

@Configuration
public class WebsocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
	private String topicAlarm = Topic.TOPIC_ALARM + "*";
	private String topicResult = Topic.TOPIC_RESULT + "*.*.*";
	private String topicOperation = Topic.TOPIC_OPERATION + "*.*";

	@Override
	protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
		messages
			.simpSubscribeDestMatchers(topicAlarm).hasAnyAuthority(Role.SYSTEM.toString(), Role.AREA.toString(), Role.DEVICE.toString())
	        .simpSubscribeDestMatchers(topicOperation).hasAuthority(Role.DEVICE.toString())
	        .simpSubscribeDestMatchers(topicResult).hasAnyAuthority(Role.SYSTEM.toString(), Role.AREA.toString(), Role.DEVICE.toString())
	        .simpMessageDestMatchers(topicOperation).hasAnyAuthority(Role.SYSTEM.toString(), Role.AREA.toString(), Role.DEVICE.toString())
	        .simpMessageDestMatchers(topicResult).hasAuthority(Role.DEVICE.toString())
	        .anyMessage().permitAll();
	}
	
	@Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
