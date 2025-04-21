package io.thingshub.transport;

import lombok.Getter;

@Getter
public abstract class TransportMessage {

	public static TransportMessage EMPTY_MESSAGE = new TransportMessage() {

		@Override
		public int getMsgType() {
			return -1;
		}

		@Override
		public String getMsgName() {
			return null;
		}

		@Override
		public int getPacketId() {
			return 0;
		}

		@Override
		public Long getTimestamp() {
			return null;
		}

	};

	protected int packetId;

	protected int msgType;

	protected String msgName;

	protected Long timestamp;

}
