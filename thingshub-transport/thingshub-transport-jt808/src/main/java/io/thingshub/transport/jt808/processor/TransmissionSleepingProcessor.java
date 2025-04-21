package io.thingshub.transport.jt808.processor;

import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.Processor;
import io.thingshub.transport.jt808.message.TransmissionSleepingMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransmissionSleepingProcessor extends Processor<BaseChannelContext, TransmissionSleepingMessage> {

	@Override
	public void process(@NonNull BaseChannelContext ctx, @NonNull TransmissionSleepingMessage msg) {
		log.info("JT808 client transmission sleeping data - {} | {} | {}", ctx.getClientAddr(), ctx.getClientId(), "transmission-sleeping");

		// TODO 发送Publish消息到MQTT（物模型中定义的采集量消息）

		// TODO 业务系统的处理：1.保存采集量信息；2.通过MQTT下发通用应答
	}

}
