package io.thingshub.transport;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.netty.channel.ChannelHandlerContext;
import lombok.NonNull;

public abstract class Processor<Ctx extends ChannelHandlerContext, M extends TransportMessage> {

	@SuppressWarnings("unchecked")
	public Class<M> getMessageClass() {
		Type[] types = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
		return (Class<M>) types[1];
	};

	public abstract void process(@NonNull Ctx ctx, @NonNull M message);

}
