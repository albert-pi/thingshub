package io.thingshub.script.jsr223;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.alibaba.fastjson2.TypeReference;

import cn.hutool.core.util.StrUtil;
import io.thingshub.script.ScriptException;
import io.thingshub.utils.BcdOps;
import io.thingshub.utils.ByteOps;
import io.thingshub.utils.IntOps;
import io.thingshub.utils.StringOps;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * JSR223规范脚本引擎
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public abstract class JSR223ScriptEngine implements io.thingshub.script.ScriptEngine {

	private ScriptEngine scriptEngine;

	private final Map<String, CompiledScript> compiledScriptMap = new ConcurrentHashMap<>();

	@PostConstruct
	protected void init() {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		scriptEngine = scriptEngineManager.getEngineByName(this.scriptType().title());
	}

	protected String convertScript(String script) {
		return script;
	}

	@Override
	public void load(String scriptId, String script) {
		try {
			compiledScriptMap.put(scriptId, (CompiledScript) compile(script));
		} catch (Exception e) {
			String errorMsg = StrUtil.format("Failed to load script [{}]. Error:", scriptId);
			throw new ScriptException(errorMsg, e);
		}
	}

	@Override
	public void unload(String scriptId) {
		compiledScriptMap.remove(scriptId);
	}

	@Override
	public List<String> getScriptIds() {
		return new ArrayList<>(compiledScriptMap.keySet());
	}

	@Override
	public <T> T invoke(String scriptId, TypeReference<T> type, String func, Object... args) {
		if (!compiledScriptMap.containsKey(scriptId)) {
			String errorMsg = StrUtil.format("Script [{}] is not loaded", scriptId);
			throw new ScriptException(errorMsg);
		}

		CompiledScript compiledScript = compiledScriptMap.get(scriptId);
		Bindings bindings = new SimpleBindings();
		bindings.put("ByteOps", ByteOps.class);
		bindings.put("IntOps", IntOps.class);
		bindings.put("BcdOps", BcdOps.class);
		bindings.put("StringOps", StringOps.class);

//		return compiledScript.eval(bindings);
		return null;
	}

	@Override
	public void cleanCache() {
		compiledScriptMap.clear();
	}

	@Override
	public Object compile(String script) throws ScriptException {
		if (scriptEngine == null) {
			log.error("script engine has not init");
		}
		try {
			return ((Compilable) scriptEngine).compile(convertScript(script));
		} catch (javax.script.ScriptException e) {
			throw new ScriptException("", e);
		}
	}

}
