package io.thingshub.transport.codec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.ListUtil;
import lombok.Data;

/**
 * <p>
 * 消息payload接口
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public interface MessagePayload {

	@Data
	static class FieldInfo {

		private int order;

		private MessageDataType dataType;

		private int length;

		private Object val;
	}

	final Map<String, List<FieldInfo>> PAYLOAD_FIELDS_MAP = new HashMap<>();

	/**
	 * 转换成字节数组
	 * 
	 * @return
	 */
	default byte[] toBytes() {
		List<FieldInfo> fieldsOfPayload = PAYLOAD_FIELDS_MAP.get(this.getClass().getSimpleName());
		if (fieldsOfPayload == null) {
			List<FieldInfo> fieldInfoList = new ArrayList<>();

			Field[] fields = this.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(MessageField.class)) {
					Map<String, Object> valueMap = AnnotationUtil.getAnnotationValueMap(field, MessageField.class);

					FieldInfo fieldInfo = new FieldInfo();
					fieldInfo.setOrder((Integer) valueMap.get("order"));

					MessageDataType fieldDataType = (MessageDataType) valueMap.get("dataType");
					fieldInfo.setDataType(fieldDataType);
					if (fieldDataType.getByteCount() > 0) {
						fieldInfo.setLength(fieldDataType.getByteCount());
					} else {
						Integer fieldLength = (Integer) valueMap.get("length");
						if (fieldLength > 0) {
							fieldInfo.setLength(fieldLength);
						} else {
							throw new EncodingException("must set the length property of field " + field.getName());
						}
					}

					try {
						field.setAccessible(true);
						fieldInfo.setVal(field.get(this));
					} catch (Exception e) {
					}

					fieldInfoList.add(fieldInfo);
				}
			}

			PAYLOAD_FIELDS_MAP.put(this.getClass().getSimpleName(), ListUtil.sortByProperty(fieldInfoList, "order"));
			fieldsOfPayload = PAYLOAD_FIELDS_MAP.get(this.getClass().getSimpleName());
		}

		MessagePayloadBuilder builder = MessagePayloadBuilder.newBuilder();
		for (FieldInfo fieldInfo : fieldsOfPayload) {
			if (fieldInfo.getVal() != null) {
				switch (fieldInfo.getDataType()) {
				case BYTE:
					builder.appendByte((Integer) fieldInfo.getVal());
					break;
				case WORD:
					builder.appendWord((Integer) fieldInfo.getVal());
					break;
				case DWORD:
					builder.appendDword((Integer) fieldInfo.getVal());
					break;
				case BYTES:
					builder.appendBytes(((String) fieldInfo.getVal()).getBytes());
					break;
				case BCD:
					builder.appendBytes(((String) fieldInfo.getVal()).getBytes());
					break;
				case STRING:
					builder.appendString((String) fieldInfo.getVal());
					break;
				}
			}
		}

		return builder.build();
	};

}