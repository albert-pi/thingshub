package io.thingshub.transport.auth;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Builder
public class AuthResult {

	public enum ResultType {

		OK(0), BAD_PASS(1), NOT_AUTHORIZED(2), ERROR(9);

		@Accessors(fluent = true)
		@Getter
		private final int value;

		private ResultType(int val) {
			this.value = val;
		}

		public static ResultType of(int val) {
			switch (val) {
			case 0:
				return OK;
			case 1:
				return BAD_PASS;
			case 2:
				return NOT_AUTHORIZED;
			case 9:
				return ERROR;
			default:
				return null;
			}
		}

	};

	@Default
	private int result = 0;

	private String tenant;

	private String clientId;

	public ResultType getType() {
		return ResultType.of(result);
	}

}
