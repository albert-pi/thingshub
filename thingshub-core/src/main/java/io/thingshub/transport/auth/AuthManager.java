package io.thingshub.transport.auth;

import io.thingshub.domain.Device;
import io.thingshub.domain.SysClient;
import io.thingshub.service.DeviceService;
import io.thingshub.service.SysClientService;
import io.thingshub.transport.auth.AuthResult.ResultType;
import jakarta.inject.Inject;

public class AuthManager {

	private DeviceService deviceService;

	private SysClientService sysClientService;

	@Inject
	public AuthManager(DeviceService deviceService, SysClientService sysClientService) {
		this.deviceService = deviceService;
		this.sysClientService = sysClientService;
	}

	public AuthResult authenticate(AuthData authData) {
		SysClient sysClient = sysClientService.getSysClient(authData.getUsername());
		if (sysClient != null) {
			return AuthResult.builder().tenant("sys").clientId(authData.getClientId()).result(ResultType.OK.value()).build();
		} else {
			Device device = deviceService.getBySn(authData.getClientId());
//			if (device == null) {
//				return AuthResult.builder().tenant("sys").clientId(authData.getClientId()).result(ResultType.NOT_AUTHORIZED.value()).build();
//			} else {
//				if (deviceService.auth(authData.getClientId(), authData.getUsername(), authData.getPassword())) {
//					return AuthResult.builder().tenant(device.getTenantCode()).clientId(authData.getClientId()).result(ResultType.OK.value()).build();
//				} else {
//				return AuthResult.builder().tenant(device.getTenantCode()).clientId(authData.getClientId()).result(ResultType.BAD_PASS.getValue()).build();
//				}

			if ("thingshub".equals(authData.getUsername()) && "t123456".equals(authData.getPassword())) {
				return AuthResult.builder().tenant("sys").clientId(authData.getClientId()).result(ResultType.OK.value()).build();
			} else {
				return AuthResult.builder().tenant("sys").clientId(authData.getClientId()).result(ResultType.BAD_PASS.value()).build();
			}
		}
//			}
//		}
	}

}
