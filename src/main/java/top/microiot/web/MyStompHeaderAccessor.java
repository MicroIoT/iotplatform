package top.microiot.web;

import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

public class MyStompHeaderAccessor extends SimpMessageHeaderAccessor {
	private static final String STOMP_ID_HEADER = "id";

	private static final String STOMP_SUBSCRIPTION_HEADER = "subscription";

	private static final String STOMP_PASSCODE_HEADER = "passcode";

	private static final String STOMP_DESTINATION_HEADER = "destination";

	private static final String STOMP_CONTENT_TYPE_HEADER = "content-type";

	private static final String COMMAND_HEADER = "stompCommand";

	private static final String CREDENTIALS_HEADER = "stompCredentials";

	public MyStompHeaderAccessor(Message<?> message) {
		super(message);
		updateStompHeadersFromSimpMessageHeaders();
	}


	void updateSimpMessageHeadersFromStompHeaders() {
		if (getNativeHeaders() == null) {
			return;
		}
		String value = getFirstNativeHeader(STOMP_DESTINATION_HEADER);
		if (value != null) {
			super.setDestination(value);
		}
		value = getFirstNativeHeader(STOMP_CONTENT_TYPE_HEADER);
		if (value != null) {
			super.setContentType(MimeTypeUtils.parseMimeType(value));
		}
		StompCommand command = getCommand();
		if (StompCommand.MESSAGE.equals(command)) {
			value = getFirstNativeHeader(STOMP_SUBSCRIPTION_HEADER);
			if (value != null) {
				super.setSubscriptionId(value);
			}
		}
		else if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.UNSUBSCRIBE.equals(command)) {
			value = getFirstNativeHeader(STOMP_ID_HEADER);
			if (value != null) {
				super.setSubscriptionId(value);
			}
		}
		else if (StompCommand.CONNECT.equals(command) || StompCommand.STOMP.equals(command)) {
			protectPasscode();
		}
	}

	void updateStompHeadersFromSimpMessageHeaders() {
		String destination = getDestination();
		if (destination != null) {
			setNativeHeader(STOMP_DESTINATION_HEADER, destination);
		}
		MimeType contentType = getContentType();
		if (contentType != null) {
			setNativeHeader(STOMP_CONTENT_TYPE_HEADER, contentType.toString());
		}
		trySetStompHeaderForSubscriptionId();
	}

	// Redeclared for visibility within simp.stomp
	@Override
	@Nullable
	protected Map<String, List<String>> getNativeHeaders() {
		return super.getNativeHeaders();
	}

	/**
	 * Return the STOMP command, or {@code null} if not yet set.
	 */
	@Nullable
	public StompCommand getCommand() {
		return (StompCommand) getHeader(COMMAND_HEADER);
	}

	private void trySetStompHeaderForSubscriptionId() {
		String subscriptionId = getSubscriptionId();
		if (subscriptionId != null) {
			StompCommand command = getCommand();
			if (command != null && StompCommand.MESSAGE.equals(command)) {
				setNativeHeader(STOMP_SUBSCRIPTION_HEADER, subscriptionId);
			}
			else {
				SimpMessageType messageType = getMessageType();
				if (SimpMessageType.SUBSCRIBE.equals(messageType) || SimpMessageType.UNSUBSCRIBE.equals(messageType)) {
					setNativeHeader(STOMP_ID_HEADER, subscriptionId);
				}
			}
		}
	}

	private void protectPasscode() {
		String value = getFirstNativeHeader(STOMP_PASSCODE_HEADER);
		if (value != null && !"PROTECTED".equals(value)) {
			setHeader(CREDENTIALS_HEADER, new StompPasscode(value));
			setNativeHeader(STOMP_PASSCODE_HEADER, "PROTECTED");
		}
	}

	private static class StompPasscode {
		public StompPasscode(String passcode) {
			
		}

		@Override
		public String toString() {
			return "[PROTECTED]";
		}
	}
}
