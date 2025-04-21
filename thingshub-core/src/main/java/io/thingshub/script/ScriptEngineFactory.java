package io.thingshub.script;

import java.util.Set;

import cn.hutool.core.util.StrUtil;
import jakarta.inject.Inject;
import lombok.NonNull;

/**
 * <p>
 * Script引擎工厂
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ScriptEngineFactory {

	private Set<ScriptEngine> scriptEngines;

	@Inject
	public ScriptEngineFactory(Set<ScriptEngine> scriptEngines) {
		this.scriptEngines = scriptEngines;
	}

	public ScriptEngine getScriptEngine(@NonNull String lang) {
		ScriptType scriptType = ScriptType.of(lang);

		ScriptEngine theScriptEngine = null;
		for (ScriptEngine scriptEngine : scriptEngines) {
			if (scriptEngine.scriptType() == scriptType) {
				theScriptEngine = scriptEngine;
			}
		}
		if (theScriptEngine == null) {
			throw new ScriptException(StrUtil.format("Not supported script language {}!", lang));
		}

		return theScriptEngine;
	}

	public void clean() {
		scriptEngines.forEach(se -> se.cleanCache());
	}

}
