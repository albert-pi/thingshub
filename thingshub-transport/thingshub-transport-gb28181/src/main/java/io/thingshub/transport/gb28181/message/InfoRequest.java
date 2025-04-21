package io.thingshub.transport.gb28181.message;

import javax.sip.message.Request;

import cn.hutool.core.date.DateUtil;
import gov.nist.javax.sip.message.SIPMessage;
import io.thingshub.transport.TransportMessage;
import lombok.Getter;

@Getter
public class InfoRequest extends TransportMessage {

	private SIPMessage sipMessage;

	public InfoRequest(SIPMessage sipMessage) {
		this.packetId = 0;
		this.msgType = 3;
		this.msgName = Request.ACK;
		this.timestamp = DateUtil.current();

		this.sipMessage = sipMessage;
	}

}
