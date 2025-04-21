package io.thingshub.transport.http.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

@Slf4j
public class InterceptorChain {

	private final List<Interceptor> interceptors = new ArrayList<>();

	private int interceptorIndex = -1;

	public InterceptorChain() {
		this((Interceptor[]) null);
	}

	public InterceptorChain(Interceptor... interceptors) {
		this((interceptors != null ? Arrays.asList(interceptors) : Collections.emptyList()));
	}

	public InterceptorChain(List<Interceptor> interceptors) {
		this.interceptors.addAll(interceptors);
	}

	public void addInterceptor(Interceptor interceptor) {
		this.interceptors.add(interceptor);
	}

	public void addInterceptor(int index, Interceptor interceptor) {
		this.interceptors.add(index, interceptor);
	}

	public void addInterceptors(Interceptor... interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
	}

	public List<Interceptor> getInterceptors() {
		return (!this.interceptors.isEmpty() ? Collections.unmodifiableList(this.interceptors) : Collections.emptyList());
	}

	boolean applyPreHandle(HttpServerRequest request, HttpServerResponse response) throws Exception {
		for (int i = 0; i < this.interceptors.size(); i++) {
			Interceptor interceptor = this.interceptors.get(i);
			if (!interceptor.preHandle(request, response)) {
				triggerAfterCompletion(request, response, null);
				return false;
			}
			this.interceptorIndex = i;
		}
		return true;
	}

	void applyPostHandle(HttpServerRequest request, HttpServerResponse response) throws Exception {

		for (int i = this.interceptors.size() - 1; i >= 0; i--) {
			Interceptor interceptor = this.interceptors.get(i);
			interceptor.postHandle(request, response);
		}
	}

	void triggerAfterCompletion(HttpServerRequest request, HttpServerResponse response, Exception ex) {
		for (int i = this.interceptorIndex; i >= 0; i--) {
			Interceptor interceptor = this.interceptors.get(i);
			try {
				interceptor.afterCompletion(request, response, ex);
			} catch (Throwable t) {
				log.error("HandlerInterceptor.afterCompletion threw exception", t);
			}
		}
	}

}
