package io.thingshub.transport.jt808.codec;

import java.util.HashMap;
import java.util.Map;

import io.thingshub.transport.codec.MessageType;
import lombok.Getter;
import lombok.experimental.Accessors;

public enum Jt808MessageType implements MessageType {
	TERMINAL_GENERIC_REPLY(0x0001, "终端通用应答"), //
	SERV_GENERIC_REPLY(0x8001, "平台通用应答"), //
	HEARTBEAT(0x0002, "心跳"), //
	REGISTER(0x0100, "终端注册请求"), //
	REGISTER_ACK(0x8100, "平台响应终端注册请求"), //
	UNREGISTER(0x0003, "终端请求注销"), //
	AUTH(0x0102, "终端请求鉴权"), //
	LOCATION(0x0200, "终端上报位置信息"), //
	CAN(0x020A, "终端上报CAN广播数据流"), //
	TRANSMISSION(0x0900, "终端数据透传"), //
	VERSION(0x0205, "终端上报版本信息"), //
	VERSION_REPLY(0x8205, "平台响应终端版本上报"), //
	SETTING_TERMINAL(0x8103, "平台设置终端参数"), //
	SETTING_QRY(0x8104, "平台查询终端参数"), //
	SETTING_QRY_REPLY(0x0104, "终端响应平台查询参数"), //
	LOCATION_QRY(0x8201, "平台查询终端位置信息"), //
	LOCATION_QRY_REPLY(0x0201, "终端响应平台查询位置信息"), //
	TEXT(0x8300, "平台下发文本信息"), //
	TEXT_REPLY(0x6006, "终端上发文本信息"), //
	CTRL_TERMINAL(0x8105, "平台下发终端控制命令"), //
	CTRL_TERMINAL_REPLY(0x0105, "终端响应平台控制命令"), //
	UPGRADE_RESULT(0x0108, "终端上报升级结果"), //
	DRIVER_QRY(0x8702, "平台查询驾驶员信息"), //
	DRIVER(0x0702, "终端上报驾驶员信息"), //
	TEMP_LOCATION_TRACING(0x8202, "平台下发临时位置跟踪控制"), //
	;

	private static final Map<Integer, MessageType> msgTypeMap;

	static {
		final MessageType[] values = values();
		msgTypeMap = new HashMap<>();
		for (MessageType msgType : values) {
			final int msgTypeId = msgType.getId();
			if (msgTypeMap.get(msgTypeId) != null) {
				throw new AssertionError("message type id already in use: " + msgTypeId);
			}
			msgTypeMap.put(msgTypeId, msgType);
		}
	}

	@Accessors(fluent = true)
	@Getter
	private final int id;

	@Accessors(fluent = true)
	@Getter
	private final String desc;

	Jt808MessageType(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return this.name();
	}

	@Override
	public String getDesc() {
		return desc;
	}

	public static MessageType valueOf(int msgId) {
		return msgTypeMap.get(msgId);
	}
}