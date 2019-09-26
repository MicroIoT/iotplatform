package top.microiot.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
	@Autowired
	private WebsocketSetting wsSetting;
	@Autowired
	private WebsocketAuthorityInterceptor interceptor;
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint(wsSetting.getEndpoint()).setAllowedOrigins("*");
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app");
        registry.enableStompBrokerRelay("/topic", "/queue/")
        	.setClientLogin(wsSetting.getUsername())
        	.setClientPasscode(wsSetting.getPassword())
        	.setSystemLogin(wsSetting.getUsername())
        	.setSystemPasscode(wsSetting.getPassword())
        	.setRelayHost(wsSetting.getHost())
        	.setRelayPort(wsSetting.getPort())
        	.setVirtualHost(wsSetting.getVirtualHost());
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(interceptor);
	}
}
