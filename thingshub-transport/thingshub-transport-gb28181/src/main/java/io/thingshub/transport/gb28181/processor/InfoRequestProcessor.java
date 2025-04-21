package io.thingshub.transport.gb28181.processor;

import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.Processor;
import io.thingshub.transport.gb28181.message.InfoRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ACK请求处理器
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class InfoRequestProcessor extends Processor<BaseChannelContext, InfoRequest> {

	@Override
	public void process(@NonNull BaseChannelContext ctx, @NonNull InfoRequest request) {
		// TODO
	}

}
