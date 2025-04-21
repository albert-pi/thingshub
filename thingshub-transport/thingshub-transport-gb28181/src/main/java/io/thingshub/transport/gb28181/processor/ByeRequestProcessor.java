package io.thingshub.transport.gb28181.processor;

import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.Processor;
import io.thingshub.transport.gb28181.message.ByeRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * BYE请求处理器
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class ByeRequestProcessor extends Processor<BaseChannelContext, ByeRequest> {

	@Override
	public void process(@NonNull BaseChannelContext ctx, @NonNull ByeRequest request) {
		// TODO
	}

}
