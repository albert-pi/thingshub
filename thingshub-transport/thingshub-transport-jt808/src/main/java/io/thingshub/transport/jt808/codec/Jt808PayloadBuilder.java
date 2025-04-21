package io.thingshub.transport.jt808.codec;

import io.thingshub.utils.BcdOps;

public class Jt808PayloadBuilder {

	private static final int MASK = 0xFF;

	public static final int DEFAULT_INITIAL_CAPACITY = 128;

	public static final int MAX_CAPACITY = Integer.MAX_VALUE;

	protected byte[] array;
	protected int writeIndex = 0;
	private final int maxCapacity;

	public static Jt808PayloadBuilder newBuilder() {
		return new Jt808PayloadBuilder();
	}

	private Jt808PayloadBuilder() {
		this(DEFAULT_INITIAL_CAPACITY, MAX_CAPACITY);
	}

	private Jt808PayloadBuilder(int initialCapacity, int maxCapacity) {
		this.maxCapacity = maxCapacity;
		this.array = new byte[initialCapacity];
	}

	protected void ensureWritable(int minSize) {
		if (minSize <= writableBytes()) {
			return;
		}

		if (minSize > maxCapacity - writeIndex) {
			throw new IndexOutOfBoundsException(
					String.format("writeIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d): %s", writeIndex, minSize, maxCapacity, this));
		}

		final int calculateNewCapacity = calculateNewCapacity(minSize + writeIndex, maxCapacity);
		ensureCapacity(calculateNewCapacity);
	}

	protected void ensureCapacity(int newCapacity) {
		byte[] newArray = new byte[newCapacity];
		System.arraycopy(array, 0, newArray, 0, array.length);
		this.array = newArray;
	}

	public int capacity() {
		return array.length;
	}

	protected final int tableSizeFor(int cap) {
		int n = cap - 1;
		n |= n >>> 1;
		n |= n >>> 2;
		n |= n >>> 4;
		n |= n >>> 8;
		n |= n >>> 16;
		return (n < 0) ? 1 : (n >= maxCapacity) ? maxCapacity : n + 1;
	}

	int calculateNewCapacity(int minNewCapacity, int maxCapacity) {
		int newCapacity = tableSizeFor(minNewCapacity * 2);

		checkNewCapacity(newCapacity);
		return newCapacity;
	}

	protected final void checkNewCapacity(int newCapacity) {
		if (newCapacity < 0 || newCapacity > maxCapacity) {
			throw new IllegalArgumentException("newCapacity: " + newCapacity + " (expected: 0-" + maxCapacity + ')');
		}
	}

	public int writableBytes() {
		return capacity() - writeIndex;
	}

	protected void setByte(byte b) {
		array[writeIndex++] = b;
	}

	public Jt808PayloadBuilder append(byte[] bytes) {
		final int length = bytes.length;
		ensureWritable(length);
		System.arraycopy(bytes, 0, array, writeIndex, length);
		this.writeIndex += length;
		return this;
	}

	public Jt808PayloadBuilder appendByte(int b) {
		return this.appendByte((byte) b);
	}

	public Jt808PayloadBuilder appendByte(byte b) {
		ensureWritable(1);
		setByte(b);
		return this;
	}

	public Jt808PayloadBuilder appendBytes(byte[] bytes) {
		return this.append(bytes);
	}

	public Jt808PayloadBuilder appendWord(int data) {
		ensureWritable(2);
		setByte((byte) ((data >>> 8) & MASK));
		setByte((byte) (data & MASK));

		return this;
	}

	public Jt808PayloadBuilder appendDword(int data) {
		return this.appendDword((long) data);
	}

	public Jt808PayloadBuilder appendDword(long data) {
		ensureWritable(4);
		setByte((byte) ((data >>> 24) & MASK));
		setByte((byte) ((data >>> 16) & MASK));
		setByte((byte) ((data >>> 8) & MASK));
		setByte((byte) (data & MASK));
		return this;
	}

	public Jt808PayloadBuilder appendBcd(String bcd) {
		append(BcdOps.strToBcd(bcd));
		return this;
	}

	public Jt808PayloadBuilder appendString(String string) {
		append(string.getBytes(Jt808Message.JT808_STRING_ENCODING));
		return this;
	}

	public byte[] build() {
		byte[] result = new byte[writeIndex];
		System.arraycopy(array, 0, result, 0, writeIndex);
		return result;
	}

}