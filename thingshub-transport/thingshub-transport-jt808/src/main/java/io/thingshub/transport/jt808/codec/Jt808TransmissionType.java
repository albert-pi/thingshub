package io.thingshub.transport.jt808.codec;

import java.util.HashMap;
import java.util.Map;

public enum Jt808TransmissionType {
	DRIVING_DATA(0xF1, "驾驶行程数据"), FAULT_DATA(0xF2, "故障码数据"), SLEEPING_DATA(0xF3, "进入休眠状态数据"), WAKENING_DATA(0xF4, "休眠被唤醒数据");

	private static final Map<Integer, Jt808TransmissionType> transmissionTypeMap;

	static {
		final Jt808TransmissionType[] values = values();
		transmissionTypeMap = new HashMap<>();
		for (Jt808TransmissionType jt808transmissionType : values) {
			final int type = jt808transmissionType.type;
			if (transmissionTypeMap.get(type) != null) {
				throw new AssertionError("transmission type already in use: " + type);
			}
			transmissionTypeMap.put(type, jt808transmissionType);
		}
	}

	private final int type;

	private final String desc;

	Jt808TransmissionType(int type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public int type() {
		return type;
	}

	public String desc() {
		return desc;
	}

	public static Jt808TransmissionType valueOf(int type) {
		return transmissionTypeMap.get(type);
	}
}