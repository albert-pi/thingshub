package io.thingshub.script;

import java.util.List;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

/**
 * <p>
 * 脚本引擎
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public interface ScriptEngine {

	final TypeReference<Void> VOID_TYPE = new TypeReference<Void>() {
	};

	final TypeReference<String> STRING_TYPE = new TypeReference<String>() {
	};

	final TypeReference<JSONObject> JSONOBJECT_TYPE = new TypeReference<JSONObject>() {
	};

	final TypeReference<JSONArray> JSONARRAY_TYPE = new TypeReference<JSONArray>() {
	};

	final TypeReference<byte[]> BYTEARRAY_TYPE = new TypeReference<byte[]>() {
	};

	void load(String scriptId, String script);

	void unload(String scriptId);

	List<String> getScriptIds();

	<T> T invoke(String scriptId, TypeReference<T> type, String func, Object... args) throws ScriptException;

	void cleanCache();

	ScriptType scriptType();

	Object compile(String script) throws Exception;
}
