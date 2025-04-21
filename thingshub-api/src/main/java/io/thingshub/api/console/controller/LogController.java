package io.thingshub.api.console.controller;

import java.util.HashMap;
import java.util.Map;

import io.thingshub.api.console.params.LogRequestParams.QueryLogParams;
import io.thingshub.commons.Page;
import io.thingshub.logging.LogManager;
import io.thingshub.logging.LogModel;
import io.thingshub.logging.LogSearchParams;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestMapping;
import jakarta.inject.Inject;

/**
 * <p>
 * 日志管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class LogController {

	@Inject
	private LogManager logManager;

	@RequestMapping(path = "/log/query")
	public Page<LogModel> queryLogs(QueryLogParams params) {
		Map<String, Object> queryParams = new HashMap<>();

		LogSearchParams searchParams = new LogSearchParams();
//		return logManager.search(searchParams, params.getPage(), params.getSize());
		return null;
	}

}