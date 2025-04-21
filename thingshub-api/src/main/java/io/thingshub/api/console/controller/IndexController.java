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
 * 控制台入口
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class IndexController {

	@RequestMapping(path = "/", produces = "text/html;charset=UTF-8")
	public void index(HttpServerRequest request, HttpServerResponse response) throws IOException {
		String path = HttpRouter.STATIC_ROOT_PATH + "index.html";

		response.send(ClassPathResource.readFile(path)).then().subscribe();
	}

}