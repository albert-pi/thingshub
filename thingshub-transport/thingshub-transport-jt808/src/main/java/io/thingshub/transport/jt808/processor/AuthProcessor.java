package io.thingshub.transport.jt808.processor;

import io.thingshub.service.DeviceService;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.Processor;
import io.thingshub.transport.jt808.message.AuthMessage;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "io.thingshub.transport.processor")
public class AuthProcessor extends Processor<BaseChannelContext, AuthMessage> {

	@Inject
	private DeviceService deviceService;

	@Override
	public void process(@NonNull BaseChannelContext ctx, @NonNull AuthMessage msg) {

//		deviceService.auth(session.getClientId(), session.getClientId(), msg.getCode());

		// TODO 发送Publish消息到MQTT（物模型中定义的event消息）

		// TODO 业务系统的处理：1.对终端进行鉴权；2.回复通用应答

		// TODO 缓存连接，对绕过auth直接发送其它类型消息，应关闭非法连接

//		Context.topicManager().registerTopicSubscriptions(channel, subscriptions);
	}

}
