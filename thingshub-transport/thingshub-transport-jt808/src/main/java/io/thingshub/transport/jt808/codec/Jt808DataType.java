package io.thingshub.transport.jt808.codec;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum Jt808DataType {
	BYTE(1, "无符号单字节整型(字节，8 位)", newHashSet(byte.class, Byte.class, int.class, Integer.class, Short.class, short.class)), //
	BYTES(0, "", newHashSet(byte[].class)), //
	WORD(2, "无符号双字节整型(字，16 位)", newHashSet(short.class, Short.class, int.class, Integer.class)), //
	DWORD(4, "无符号四字节整型(双字，32 位)", newHashSet(long.class, Long.class, int.class, Integer.class)), //
	BCD(0, "8421 码，n 字节", newHashSet(String.class)), //
	STRING(0, "GBK编码，若无数据，置空", newHashSet(String.class));

	@Accessors(fluent = true)
	@Getter
	private final int byteCount;

	@Accessors(fluent = true)
	@Getter
	private final String desc;

	@Accessors(fluent = true)
	@Getter
	private final Set<Class<?>> targetClasses;

	Jt808DataType(int byteCount, String desc, Set<Class<?>> targetClasses) {
		this.byteCount = byteCount;
		this.desc = desc;
		this.targetClasses = targetClasses;
	}

}