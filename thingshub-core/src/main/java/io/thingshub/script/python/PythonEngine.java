package io.thingshub.script.python;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.TypeReference;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptType;
import jakarta.annotation.PostConstruct;

/**
 * <p>
 * Python引擎
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class PythonEngine implements ScriptEngine {

//	private PythonInterpreter pythonInterpreter;

	private final String RESULT_KEY = "result";

//	private final Map<String, PyCode> compiledScriptMap = new HashMap<>();

	@PostConstruct
	public void init() {
//		PySystemState systemState = new PySystemState();
//		systemState.setdefaultencoding("UTF-8");
//		this.pythonInterpreter = new PythonInterpreter(null, systemState);
	}

	@Override
	public void load(String scriptId, String script) {
//		try {
//			PyCode pyCode = (PyCode) compile(script);
//			compiledScriptMap.put(scriptId, pyCode);
//		} catch (Exception e) {
//			String errorMsg = StrUtil.format("Failed to load script [{}]. Error:", scriptId);
//			throw new ScriptException(errorMsg, e);
//		}
	}

	@Override
	public void unload(String scriptId) {
//		compiledScriptMap.remove(scriptId);
	}

	@Override
	public List<String> getScriptIds() {
//		return new ArrayList<>(compiledScriptMap.keySet());
		return null;
	}

	@Override
	public <T> T invoke(String scriptId, TypeReference<T> type, String func, Object... args) {
//		if (!compiledScriptMap.containsKey(scriptId)) {
//			String errorMsg = StrUtil.format("Script [{}] is not loaded", scriptId);
//			throw new ScriptException(errorMsg);
//		}
//
//		PyCode compiledScript = compiledScriptMap.get(scriptId);
//
//		pythonInterpreter.exec(compiledScript);
//
//		PyObject result = pythonInterpreter.get(RESULT_KEY);
//
//		if (result == null) {
//			return null;
//		}
//
//		pythonInterpreter.cleanup();

//		switch (wrap.getCmp().getType()) {
//		case BOOLEAN_SCRIPT:
//			return result.__tojava__(Boolean.class);
//		case FOR_SCRIPT:
//			return result.__tojava__(Integer.class);
//		default:
//			return result.__tojava__(Object.class);
//		}

		return null;
	}

	@Override
	public void cleanCache() {
//		compiledScriptMap.clear();
	}

	@Override
	public ScriptType scriptType() {
		return ScriptType.PYTHON;
	}

	@Override
	public Object compile(String script) throws Exception {
//		return pythonInterpreter.compile(convertScript(script));
		return null;
	}

	private String convertScript(String script) {
		String[] lineArray = script.split("\\n");
		List<String> noBlankLineList = Arrays.stream(lineArray).filter(s -> !StrUtil.isBlank(s)).collect(Collectors.toList());

		// 用第一行的缩进的空格数作为整个代码的缩进量
		String blankStr = ReUtil.getGroup0("^[ ]*", noBlankLineList.get(0));

		// 重新构建脚本
		StringBuilder scriptSB = new StringBuilder();
		noBlankLineList.forEach(s -> scriptSB.append(StrUtil.format("{}\n", s.replaceFirst(blankStr, StrUtil.EMPTY))));

		return scriptSB.toString().replace("return", RESULT_KEY + "=");
	}
}
