package io.thingshub.transport.codec;

import java.io.Serializable;

import com.alibaba.fastjson2.JSONObject;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 根据物模型定义构建的消息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Data
@Builder
public class ThingMessage implements Serializable {

	private static final long serialVersionUID = 3247418402019861071L;

	private String id;

	private String version;

	private String method;

	private Long timestamp;

	@Data
	public static class Ext {

	};

	private Ext ext;

	/**
	 * 请求参数
	 */
	private JSONObject params;

	/**
	 * 响应结果代码
	 */
	private Integer code;

	/**
	 * 响应信息
	 */
	private String message;

	/**
	 * 响应结果
	 */
	private JSONObject data;

}