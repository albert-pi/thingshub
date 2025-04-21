package io.thingshub.transport.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.validation.groups.Default;

import org.reactivestreams.Publisher;
import org.reflections.Reflections;

import com.alibaba.fastjson2.JSON;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.core.util.StrUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.thingshub.Broker;
import io.thingshub.commons.Result;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestBody;
import io.thingshub.transport.http.annotation.RequestMapping;
import io.thingshub.transport.http.annotation.RequestParam;
import io.thingshub.transport.http.annotation.Validated;
import io.thingshub.transport.http.validation.ValidationException;
import io.thingshub.transport.http.validation.ValidationUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

/**
 * <p>
 * HTTP请求路由
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class HttpRouter implements Consumer<HttpServerRoutes> {

	@Data
	@AllArgsConstructor
	public static class MethodParameter {

		private String name;

		private Class<?> type;
	}

	@Data
	public static class MethodWrapper {

		private Method method;

		private RequestMapping requestMapping;

		private LinkedHashMap<String, MethodParameter> queryParameters;

		private String queryParameterObjectName;

		private Class<?> queryParameterObjectType;

		private Field[] queryParameterObjectFields;

		private String bodyParameterObjectName;

		private Class<?> bodyParameterObjectType;

		private LinkedHashMap<String, Class<?>[]> validationGroups;

	}

	private static Field[] getAllFieldsOfClass(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		Class<?> parent = clazz.getSuperclass();
		if (parent != null) {
			Field[] parentFields = getAllFieldsOfClass(parent);
			Field[] allFields = new Field[fields.length + parentFields.length];
			System.arraycopy(fields, 0, allFields, 0, fields.length);
			System.arraycopy(parentFields, 0, allFields, fields.length, parentFields.length);
			fields = allFields;
		}

		return fields;
	}

	public static final String STATIC_ROOT_PATH = "/static/";

	public static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

	private static final Map<String, MethodWrapper> REQUEST_METHODS = new HashMap<>();

	static {
		Reflections reflections = new Reflections(Broker.class.getPackage().getName());
		Set<Class<?>> controllerClazzs = reflections.getTypesAnnotatedWith(Controller.class);
		if (CollUtil.isNotEmpty(controllerClazzs)) {
			ClassPool clazzPool = ClassPool.getDefault();
			for (Class<?> controllerClazz : controllerClazzs) {
				try {
					CtClass ctClass = clazzPool.getCtClass(controllerClazz.getName());
					Method[] methods = controllerClazz.getMethods();
					for (Method method : methods) {
						if (!method.isAnnotationPresent(RequestMapping.class)) {
							continue;
						}

						final Class<?>[] parameterTypes = method.getParameterTypes();

						MethodWrapper methodWrapper = new MethodWrapper();
						methodWrapper.setMethod(method);
						methodWrapper.setRequestMapping(method.getAnnotation(RequestMapping.class));
						methodWrapper.setQueryParameters(new LinkedHashMap<>());
						methodWrapper.setValidationGroups(new LinkedHashMap<>());

						CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
						int len = parameterTypes.length;
						int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;

						MethodInfo methodInfo = ctMethod.getMethodInfo();
						Object[][] allParameterAnnotations = ctMethod.getParameterAnnotations();
						CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
						LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

						if (attr != null) {
							for (int i = 0; i < len; i++) {
								String parameterName = attr.variableName(i + pos);
								String uriParamName = parameterName;
								Class<?> parameterClazz = parameterTypes[i];
								Object[] parameterAnnotations = allParameterAnnotations[i];

								if (parameterAnnotations != null && parameterAnnotations.length > 0) {
									for (Object parameterAnnotationObj : parameterAnnotations) {
										Class<? extends Annotation> annotationType = ((Annotation) parameterAnnotationObj).annotationType();

										if (annotationType == Validated.class) {
											Validated validatedAnnotation = (Validated) parameterAnnotationObj;
											if (validatedAnnotation.value() != null && validatedAnnotation.value().length > 0) {
												methodWrapper.getValidationGroups().put(parameterName, validatedAnnotation.value());
											} else {
												methodWrapper.getValidationGroups().put(parameterName, new Class[] { Default.class });
											}
										} else if (annotationType == RequestBody.class) {
											methodWrapper.setBodyParameterObjectName(parameterName);
											methodWrapper.setBodyParameterObjectType(parameterClazz);
										} else if (annotationType == RequestParam.class) {
											RequestParam requestParamAnnotation = (RequestParam) parameterAnnotationObj;
											if (StrUtil.isNotBlank(requestParamAnnotation.name())) {
												uriParamName = requestParamAnnotation.name();
											} else if (StrUtil.isNotBlank(requestParamAnnotation.value())) {
												uriParamName = requestParamAnnotation.value();
											}
										}
									}
								}

								switch (parameterClazz.getTypeName()) {
								case "java.lang.String":
								case "long":
								case "java.lang.Long":
								case "int":
								case "java.lang.Integer":
								case "short":
								case "java.lang.Short":
								case "byte":
								case "java.lang.Byte":
								case "boolean":
								case "java.lang.Boolean":
								case "float":
								case "java.lang.Float":
								case "double":
								case "java.lang.Double":
								case "java.math.BigDecimal":
								case "reactor.netty.http.server.HttpServerRequest":
								case "reactor.netty.http.server.HttpServerResponse":
									methodWrapper.getQueryParameters().put(uriParamName, new MethodParameter(parameterName, parameterClazz));
									break;
								default:
									methodWrapper.setQueryParameterObjectName(parameterName);
									methodWrapper.setQueryParameterObjectType(parameterClazz);
									methodWrapper.setQueryParameterObjectFields(getAllFieldsOfClass(parameterClazz));
									break;
								}
							}
						}

						REQUEST_METHODS.put(controllerClazz.getCanonicalName() + "." + method.getName(), methodWrapper);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
	}

	private BiFunction<String, Class<?>, ?> jsonConverter = (json, clazz) -> JSON.parseObject(json, clazz);

	@Override
	public void accept(HttpServerRoutes httpServerRoutes) {
		REQUEST_METHODS.forEach((methodName, methodWrapper) -> {
			BiFunction<? super HttpServerRequest, ? super HttpServerResponse, ? extends Publisher<Void>> requestHandler = (request, response) -> this
					.handleRequest(request, response, methodWrapper);

			for (HttpMethod httpMethod : methodWrapper.getRequestMapping().method()) {
				String[] paths = methodWrapper.getRequestMapping().path() != null ? methodWrapper.getRequestMapping().path()
						: methodWrapper.getRequestMapping().value();
				for (String path : paths) {
					switch (httpMethod) {

					case OPTIONS:
						// TODO
						break;
					case HEAD:
						// TODO
						break;
					case PATCH:
						// TODO
						break;
					case TRACE:
						// TODO
						break;
					case PUT:
						httpServerRoutes.put(path, requestHandler);
						break;
					case POST:
						httpServerRoutes.post(path, requestHandler);
						break;
					case DELETE:
						httpServerRoutes.delete(path, requestHandler);
						break;
					case GET:
					default:
						httpServerRoutes.get(path, requestHandler);
						break;
					}
				}
			}
		});
	}

	private Publisher<Void> handleRequest(HttpServerRequest request, HttpServerResponse response, MethodWrapper methodWrapper) {
		if (methodWrapper.getRequestMapping().headers() != null) {
			// TODO 不符合header要求的返回404
		}
		if (methodWrapper.getRequestMapping().consumes() != null) {
//			httpServerRequest.isMultipart()
			// TODO 不符合consumes要求的返回404
		}
		if (methodWrapper.getRequestMapping().produces() != null) {
			// TODO 不符合produces要求的返回404

			for (String produce : methodWrapper.getRequestMapping().produces()) {
				response.addHeader("Content-Type", produce);
			}
		}

		Object controller = Broker.getBean(methodWrapper.getMethod().getDeclaringClass());

		QueryStringDecoder query = new QueryStringDecoder(request.uri());
		Map<String, List<String>> queryParams = query.parameters();

		Map<String, Object> paramValues = new LinkedHashMap<>();
		if (CollUtil.isNotEmpty(methodWrapper.getQueryParameters())) {
			bindRawQueryParameters(request, response, methodWrapper, queryParams, paramValues);
		}
		if (StrUtil.isNotBlank(methodWrapper.getQueryParameterObjectName()) && methodWrapper.getQueryParameterObjectType() != null) {
			bindQueryParameterObject(request, response, methodWrapper, queryParams, paramValues);
		}

		if (request.method().name().equalsIgnoreCase("GET")) {
			return request.receive().then(Mono.defer(() -> {
				try {
					if (CollUtil.isNotEmpty(methodWrapper.getValidationGroups())) {
						for (Entry<String, Object> entry : paramValues.entrySet()) {
							if (methodWrapper.getValidationGroups().get(entry.getKey()) != null) {
								ValidationUtils.validate(entry.getValue(), methodWrapper.getValidationGroups().get(entry.getKey()));
							}
						}
					}

					Object ret = CollUtil.isNotEmpty(paramValues) ? methodWrapper.getMethod().invoke(controller, paramValues.values().toArray())
							: methodWrapper.getMethod().invoke(controller);

					if (methodWrapper.getRequestMapping().produces()[0].indexOf("application/json") > -1) {
						String result = JSON.toJSONString(Result.success(ret));
						return response.sendString(Mono.just(result)).then();
					} else {
						return Mono.empty();
					}
				} catch (ValidationException e1) {
					log.error("", e1);
					return response.sendString(Mono.just(JSON.toJSONString(Result.error(4, e1.getMessage())))).then();
				} catch (Exception e2) {
					log.error("", e2);
					return response.sendString(Mono.just(JSON.toJSONString(Result.error(5, "system error")))).then();
				}
			}));
		} else {
			return request.receive().asString(StandardCharsets.UTF_8).map(body -> jsonConverter.apply(body, methodWrapper.getBodyParameterObjectType()))
					.doOnNext(bodyParamObj -> {
						if (methodWrapper.getBodyParameterObjectName() != null) {
							paramValues.put(methodWrapper.getBodyParameterObjectName(), bodyParamObj);
						}

						try {
							boolean hasParameters = CollUtil.isNotEmpty(paramValues);
							if (hasParameters) {
								for (Entry<String, Object> entry : paramValues.entrySet()) {
									if (methodWrapper.getValidationGroups().get(entry.getKey()) != null) {
										ValidationUtils.validate(entry.getValue(), methodWrapper.getValidationGroups().get(entry.getKey()));
									}
								}
							}

							Object ret = hasParameters ? methodWrapper.getMethod().invoke(controller, paramValues.values().toArray())
									: methodWrapper.getMethod().invoke(controller);

							if (methodWrapper.getRequestMapping().produces()[0].indexOf("application/json") > -1) {
								String result = JSON.toJSONString(Result.success(ret));
								response.sendString(Mono.just(result)).then().subscribe();
							}
						} catch (ValidationException e1) {
							log.error("", e1);
							response.sendString(Mono.just(JSON.toJSONString(Result.error(4, e1.getMessage())))).then().subscribe();
						} catch (Exception e2) {
							log.error("", e2);
							response.sendString(Mono.just(JSON.toJSONString(Result.error(5, "system error")))).then().subscribe();
						}
					}).then();
		}
	}

	private String getQueryParameter(Map<String, List<String>> queryParams, String paramName) {
		String paramValue = null;
		if (CollUtil.isNotEmpty(queryParams) && CollUtil.isNotEmpty(queryParams.get(paramName))) {
			paramValue = queryParams.get(paramName).get(0);
		}

		return paramValue;
	}

	private void bindRawQueryParameters(HttpServerRequest request, HttpServerResponse response, MethodWrapper methodWrapper,
			Map<String, List<String>> queryParams, Map<String, Object> paramValues) {

		methodWrapper.getQueryParameters().forEach((uriParamName, methodParameter) -> {
			String paramValue = getQueryParameter(queryParams, uriParamName);

			switch (methodParameter.getType().getTypeName()) {
			case "java.lang.String":
				paramValues.put(methodParameter.getName(), paramValue);
				break;
			case "long":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Long.parseLong(paramValue));
				}
				break;
			case "java.lang.Long":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Long.valueOf(paramValue));
				}
				break;
			case "int":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Integer.parseInt(paramValue));
				}
				break;
			case "java.lang.Integer":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Integer.valueOf(paramValue));
				}
				break;
			case "short":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Short.parseShort(paramValue));
				}
				break;
			case "java.lang.Short":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Short.valueOf(paramValue));
				}
				break;
			case "byte":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Byte.parseByte(paramValue));
				}
				break;
			case "java.lang.Byte":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Byte.valueOf(paramValue));
				}
				break;
			case "boolean":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Boolean.parseBoolean(paramValue));
				}
				break;
			case "java.lang.Boolean":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Boolean.valueOf(paramValue));
				}
				break;
			case "float":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Float.parseFloat(paramValue));
				}
				break;
			case "java.lang.Float":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Float.valueOf(paramValue));
				}
				break;
			case "double":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Double.parseDouble(paramValue));
				}
				break;
			case "java.lang.Double":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), Double.valueOf(paramValue));
				}
				break;
			case "java.math.BigDecimal":
				if (paramValue != null) {
					paramValues.put(methodParameter.getName(), new BigDecimal(paramValue));
				}
				break;
			case "reactor.netty.http.server.HttpServerRequest":
				paramValues.put(methodParameter.getName(), request);
				break;
			case "reactor.netty.http.server.HttpServerResponse":
				paramValues.put(methodParameter.getName(), response);
				break;
			default:

				break;
			}
		});
	}

	private void bindQueryParameterObject(HttpServerRequest request, HttpServerResponse response, MethodWrapper methodWrapper,
			Map<String, List<String>> queryParams, Map<String, Object> paramValues) {
		// 参数自动绑定：
		// @RequestMapping(path = "/device/query")
		// public Page<Device> queryDevcie(QueryDeviceParams params) {...}
		try {
			Object queryParameterObject = methodWrapper.getQueryParameterObjectType().newInstance();
			Field[] fields = methodWrapper.getQueryParameterObjectFields();
			for (Field field : fields) {
				field.setAccessible(true);
				String paramValue = getQueryParameter(queryParams, field.getName());
				switch (field.getType().getTypeName()) {
				case "java.lang.String":
					field.set(queryParameterObject, paramValue);
					break;
				case "long":
					if (paramValue != null) {
						field.set(queryParameterObject, Long.parseLong(paramValue));
					}
					break;
				case "java.lang.Long":
					if (paramValue != null) {
						field.set(queryParameterObject, Long.valueOf(paramValue));
					}
					break;
				case "int":
					if (paramValue != null) {
						field.set(queryParameterObject, Integer.parseInt(paramValue));
					}
					break;
				case "java.lang.Integer":
					if (paramValue != null) {
						field.set(queryParameterObject, Integer.valueOf(paramValue));
					}
					break;
				case "short":
					if (paramValue != null) {
						field.set(queryParameterObject, Short.parseShort(paramValue));
					}
					break;
				case "java.lang.Short":
					if (paramValue != null) {
						field.set(queryParameterObject, Short.valueOf(paramValue));
					}
					break;
				case "byte":
					if (paramValue != null) {
						field.set(queryParameterObject, Byte.parseByte(paramValue));
					}
					break;
				case "java.lang.Byte":
					if (paramValue != null) {
						field.set(queryParameterObject, Byte.valueOf(paramValue));
					}
					break;
				case "boolean":
					if (paramValue != null) {
						field.set(queryParameterObject, Boolean.parseBoolean(paramValue));
					}
					break;
				case "java.lang.Boolean":
					if (paramValue != null) {
						field.set(queryParameterObject, Boolean.valueOf(paramValue));
					}
					break;
				case "float":
					if (paramValue != null) {
						field.set(queryParameterObject, Float.parseFloat(paramValue));
					}
					break;
				case "java.lang.Float":
					if (paramValue != null) {
						field.set(queryParameterObject, Float.valueOf(paramValue));
					}
					break;
				case "double":
					if (paramValue != null) {
						field.set(queryParameterObject, Double.parseDouble(paramValue));
					}
					break;
				case "java.lang.Double":
					if (paramValue != null) {
						field.set(queryParameterObject, Double.valueOf(paramValue));
					}
					break;
				case "java.math.BigDecimal":
					if (paramValue != null) {
						field.set(queryParameterObject, new BigDecimal(paramValue));
					}
					break;
				default:
					break;
				}
			}
			paramValues.put(methodWrapper.getQueryParameterObjectName(), queryParameterObject);

			methodWrapper.getQueryParameters().forEach((uriParamName, methodParameter) -> {
				switch (methodParameter.getType().getTypeName()) {
				case "reactor.netty.http.server.HttpServerRequest":
					paramValues.put(methodParameter.getName(), request);
					break;
				case "reactor.netty.http.server.HttpServerResponse":
					paramValues.put(methodParameter.getName(), response);
					break;
				default:
					break;
				}
			});
		} catch (Exception e) {
			log.error("", e);
		}
	}

}
