package io.thingshub.transport.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthData {

	private String clientId;

	private String username;

	private String password;

	private byte[] cert;

	private String remoteAddr;

	private int remotePort;

}
