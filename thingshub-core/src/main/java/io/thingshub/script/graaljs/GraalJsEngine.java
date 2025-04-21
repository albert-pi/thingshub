package io.thingshub.script.graaljs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Lists;

import cn.hutool.core.util.StrUtil;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptException;
import io.thingshub.script.ScriptType;
import io.thingshub.utils.BcdOps;
import io.thingshub.utils.ByteOps;
import io.thingshub.utils.IntOps;
import io.thingshub.utils.StringOps;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * GraalJS引擎
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class GraalJsEngine implements ScriptEngine {

	private final Map<String, Source> scriptMap = new ConcurrentHashMap<>();

	private Engine graalEngine;

	@PostConstruct
	public void init() {
		graalEngine = Engine.create();
	}

	@Override
	public void load(String scriptId, String script) {
		try {
			scriptMap.put(scriptId, Source.create("js", (CharSequence) compile(script)));
		} catch (Exception e) {
			String errorMsg = StrUtil.format("Failed to load script [{}]. Error:", scriptId);
			throw new ScriptException(errorMsg, e);
		}
	}

	@Override
	public void unload(String scriptId) {
		scriptMap.remove(scriptId);
	}

	@Override
	public List<String> getScriptIds() {
		return Lists.newArrayList(scriptMap.keySet());
	}

	@Override
	public <T> T invoke(String scriptId, TypeReference<T> type, String func, Object... args) {
		if (!scriptMap.containsKey(scriptId)) {
			String errorMsg = StrUtil.format("Script [{}] is not loaded", scriptId);
			throw new ScriptException(errorMsg);
		}
		try (Context context = Context.newBuilder().allowAllAccess(true).engine(this.graalEngine).build()) {
			Value bindings = context.getBindings("js");
			bindings.putMember("ByteOps", ByteOps.class);
			bindings.putMember("IntOps", IntOps.class);
			bindings.putMember("BcdOps", BcdOps.class);
			bindings.putMember("StringOps", StringOps.class);

			Value theScript = context.eval(scriptMap.get(scriptId));
			Value member = theScript.getMember("invoke");
			Value result = member.execute(func, args);
			String str = result.asString();

			T t = JSON.parseObject(str, type);

			if (log.isDebugEnabled()) {
				StringBuilder strArgs = formatArgs(args);
				log.debug("invoke function={}, args={}, result={}", func, strArgs, t);
			}

			return t;
		} catch (Exception e) {
			throw e;
		}
	}

	private StringBuilder formatArgs(Object[] args) {
		StringBuilder sbArgs = new StringBuilder("[");
		for (int i = 0; i < args.length; i++) {
			args[i] = JSON.toJSONString(args[i]);
			sbArgs.append(args[i]).append(i != args.length - 1 ? "," : "");
		}
		sbArgs.append("]");

		return sbArgs;
	}

	@Override
	public void cleanCache() {
		scriptMap.clear();
	}

	@Override
	public ScriptType scriptType() {
		return ScriptType.JS;
	}

	@Override
	public Object compile(String script) throws Exception {
		StringBuilder wrappedScript = new StringBuilder();
		wrappedScript.append("new (function () {").append("\n");
		wrappedScript.append("\t").append("function hexToBytes(hex) {").append("\n");
		wrappedScript.append("\t\t").append("if (hex.length % 2 !== 0) {").append("\n");
		wrappedScript.append("\t\t\t").append("throw new Error('Invalid hex string. String must have an even number of characters.');").append("\n");
		wrappedScript.append("\t\t").append("}").append("\n\n");
		wrappedScript.append("\t\t").append("let byteArray = [];").append("\n");
		wrappedScript.append("\t\t").append("for (let i = 0; i < hex.length; i += 4) {").append("\n");
		wrappedScript.append("\t\t\t").append("byteArray.push(parseInt(hex.substr(i, 4), 16));").append("\n");
		wrappedScript.append("\t\t").append("}").append("\n\n");
		wrappedScript.append("\t\t").append("return byteArray;").append("\n");
		wrappedScript.append("\t").append("}").append("\n\n");
		wrappedScript.append("\t").append("function bytesToHex(bytes) {").append("\n");
		wrappedScript.append("\t\t").append("let hex = [];").append("\n");
		wrappedScript.append("\t\t").append("for (let i = 0; i < bytes.length; i++) {").append("\n");
		wrappedScript.append("\t\t\t").append("hex.push((bytes[i] >>> 4).toString(16));").append("\n");
		wrappedScript.append("\t\t\t").append("hex.push((bytes[i] & 0xF).toString(16));").append("\n");
		wrappedScript.append("\t\t").append("}").append("\n\n");
		wrappedScript.append("\t\t").append("return hex.join('');").append("\n");
		wrappedScript.append("\t").append("}").append("\n\n");
		wrappedScript.append(script).append("\n");
		wrappedScript.append("\t").append("this.invoke=function(f,args){").append("\n");
		wrappedScript.append("\t\t").append("print('f==========' + f);").append("\n");
		wrappedScript.append("\t\t").append("if (f == 'encode') {").append("\n");
		wrappedScript.append("\t\t\t").append("return bytesToHex(this[f].apply(this,args));").append("\n");
		wrappedScript.append("\t\t").append("} else if (f == 'decode') {").append("\n");
		wrappedScript.append("\t\t\t").append("return JSON.stringify(this[f].apply(this,args));").append("\n");
		wrappedScript.append("\t\t").append("};").append("\n");
		wrappedScript.append("\t").append("};").append("\n");
		wrappedScript.append("})()");
		Context context = Context.newBuilder().allowAllAccess(true).engine(graalEngine).build();
		context.parse(Source.create("js", wrappedScript));

		return wrappedScript;
	}

}
