package io.thingshub.api.console.controller;

import java.io.IOException;

import io.thingshub.transport.http.HttpRouter;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestMapping;
import io.thingshub.utils.ClassPathResource;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * <p>
 * 静态资源文件处理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class ResourceController {

	@RequestMapping(path = "/assets/**")
	public void getAssetFile(HttpServerRequest request, HttpServerResponse response) throws IOException {
		if (HttpRouter.ANT_PATH_MATCHER.match("assets/**/*.js", request.path())) {
			response.addHeader("Content-Type", "application/javascript;charset=UTF-8");
		} else if (HttpRouter.ANT_PATH_MATCHER.match("assets/**/*.css", request.path())) {
			response.addHeader("Content-Type", "text/css;charset=UTF-8");
		} else if (HttpRouter.ANT_PATH_MATCHER.match("assets/**/*.png", request.path())) {
			response.addHeader("Content-Type", "image/png");
		} else if (HttpRouter.ANT_PATH_MATCHER.match("assets/**/*.jpg", request.path())) {
			response.addHeader("Content-Type", "image/jpeg");
		} else if (HttpRouter.ANT_PATH_MATCHER.match("assets/**/*.jpeg", request.path())) {
			response.addHeader("Content-Type", "image/jpeg");
		}

		String path = HttpRouter.STATIC_ROOT_PATH + request.path();

		response.send(ClassPathResource.readFile(path)).then().subscribe();
	}

	@RequestMapping(path = "/favicon.png", produces = "image/png")
	public void getFaviconFile(HttpServerRequest request, HttpServerResponse response) throws IOException {
		String path = HttpRouter.STATIC_ROOT_PATH + "favicon.png";

		response.send(ClassPathResource.readFile(path)).then().subscribe();
	}

}