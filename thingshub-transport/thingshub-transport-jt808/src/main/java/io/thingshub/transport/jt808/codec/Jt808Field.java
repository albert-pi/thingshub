package io.thingshub.transport.jt808.codec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Jt808Field {

	int order();

	Jt808DataType dataType();

	int length() default 0;

}