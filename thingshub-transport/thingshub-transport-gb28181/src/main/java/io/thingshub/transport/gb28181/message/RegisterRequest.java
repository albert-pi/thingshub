package io.thingshub.transport.gb28181.message;

import javax.sip.message.Request;

import cn.hutool.core.date.DateUtil;
import gov.nist.javax.sip.message.SIPMessage;
import io.thingshub.transport.TransportMessage;
import lombok.Getter;

@Getter
public class RegisterRequest extends TransportMessage {

	private SIPMessage sipMessage;

	public RegisterRequest(SIPMessage sipMessage) {
		this.packetId = 0;
		this.msgType = 1;
		this.msgName = Request.REGISTER;
		this.timestamp = DateUtil.current();

		this.sipMessage = sipMessage;
	}

}
