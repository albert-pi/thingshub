package io.thingshub.transport.gb28181.message;

import javax.sip.message.Request;

import cn.hutool.core.date.DateUtil;
import gov.nist.javax.sip.message.SIPMessage;
import io.thingshub.transport.TransportMessage;
import lombok.Getter;

@Getter
public class ByeRequest extends TransportMessage {

	private SIPMessage sipMessage;

	public ByeRequest(SIPMessage sipMessage) {
		this.packetId = 0;
		this.msgType = 7;
		this.msgName = Request.BYE;
		this.timestamp = DateUtil.current();

		this.sipMessage = sipMessage;
	}

}
