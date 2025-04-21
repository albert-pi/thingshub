package io.thingshub.service;

import java.util.List;

import com.google.common.collect.Lists;

import cn.hutool.db.sql.Condition;
import io.thingshub.domain.ScriptInfo;
import io.thingshub.script.ScriptEngineFactory;
import io.thingshub.service.base.BaseService;
import jakarta.inject.Inject;

/**
 * <p>
 * 脚本信息管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ScriptInfoService extends BaseService<ScriptInfo> {

	private ScriptEngineFactory scriptEngineFactory;

	@Inject
	public ScriptInfoService(ScriptEngineFactory scriptEngineFactory) {
		this.scriptEngineFactory = scriptEngineFactory;
	}

	public void createScriptInfo(String code, String lang, String content) {
		ScriptInfo scriptInfo = new ScriptInfo();
		scriptInfo.setCode(code);
		scriptInfo.setLang(lang);
		scriptInfo.setContent(content);
		scriptInfo.setDeletedStatus(DeletedStatus.NOT_DELETED.value());

		this.save(scriptInfo);

		scriptEngineFactory.getScriptEngine(lang).load(code, content);
	}

	public void updateScriptInfo(String code, String lang, String content) {
		List<Condition> queryConditions = Lists.newArrayList(new Condition("code", code));
		ScriptInfo scriptInfo = this.getOne(queryConditions);
		if (scriptInfo != null) {
			scriptInfo.setCode(code);
			scriptInfo.setLang(lang);
			scriptInfo.setContent(content);

			this.updateById(scriptInfo);

			scriptEngineFactory.getScriptEngine(lang).load(code, content);
		}
	}

	public List<ScriptInfo> getScriptInfosByLang(String lang) {
		return this.query(Lists.newArrayList(new Condition("lang", lang)));
	}

	public ScriptInfo getScriptInfo(String code) {
		return this.getOne(Lists.newArrayList(new Condition("code", code), new Condition("deleted_status", DeletedStatus.NOT_DELETED.value())));
	}

}
