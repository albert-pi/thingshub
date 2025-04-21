package io.thingshub.transport.jt808.codec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.ListUtil;
import io.thingshub.transport.jt808.Jt808Exception;
import lombok.Data;

public interface Jt808Payload {

	@Data
	static class FieldInfo {

		private int order;

		private Jt808DataType dataType;

		private int length;

		private Object val;
	}

	final Map<String, List<FieldInfo>> PAYLOAD_FIELDS_Map = new HashMap<>();

	default byte[] toBytes() {
		List<FieldInfo> fieldsOfPayload = PAYLOAD_FIELDS_Map.get(this.getClass().getSimpleName());
		if (fieldsOfPayload == null) {
			List<FieldInfo> fieldInfoList = new ArrayList<>();

			Field[] fields = this.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Jt808Field.class)) {
					Map<String, Object> valueMap = AnnotationUtil.getAnnotationValueMap(field, Jt808Field.class);

					FieldInfo fieldInfo = new FieldInfo();
					fieldInfo.setOrder((Integer) valueMap.get("order"));

					Jt808DataType fieldDataType = (Jt808DataType) valueMap.get("dataType");
					fieldInfo.setDataType(fieldDataType);
					if (fieldDataType.byteCount() > 0) {
						fieldInfo.setLength(fieldDataType.byteCount());
					} else {
						Integer fieldLength = (Integer) valueMap.get("length");
						if (fieldLength > 0) {
							fieldInfo.setLength(fieldLength);
						} else {
							throw new Jt808Exception("must set the length property of field " + field.getName());
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

			PAYLOAD_FIELDS_Map.put(this.getClass().getSimpleName(), ListUtil.sortByProperty(fieldInfoList, "order"));
			fieldsOfPayload = PAYLOAD_FIELDS_Map.get(this.getClass().getSimpleName());
		}

		Jt808PayloadBuilder builder = Jt808PayloadBuilder.newBuilder();
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