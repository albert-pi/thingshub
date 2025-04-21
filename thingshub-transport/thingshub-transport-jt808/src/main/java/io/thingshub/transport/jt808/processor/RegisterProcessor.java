package io.thingshub.transport.jt808.processor;

import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.Processor;
import io.thingshub.transport.jt808.message.RegisterMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterProcessor extends Processor<BaseChannelContext, RegisterMessage> {

	@Override
	public void process(@NonNull BaseChannelContext ctx, @NonNull RegisterMessage msg) {
		log.info("JT808 client register");

		// TODO 业务系统的处理：1.如果终端未注册，保存终端信息；2.重设密码（鉴权码）
//		String subcribeTopic = "/test/response";// TODO 订阅Topic
//		Context.topicManager().registerTopicSubscription(channel, new TopicSubscription(subcribeTopic, MqttQoS.EXACTLY_ONCE, channel));
//
//		JSONObject dataObj = JSONObject.of();
//		dataObj.put("provinceId", msg.getProvinceId());
//		dataObj.put("cityId", msg.getCityId());
//		dataObj.put("manufacturerId", msg.getManufacturerId());
//		dataObj.put("model", msg.getModel());
//		dataObj.put("terminalId", msg.getTerminalId());
//		dataObj.put("plateColor", msg.getPlateColor());
//		dataObj.put("licensePlate", msg.getLicensePlate());
//
//		JSONObject payloadObj = JSONObject.of();
//		payloadObj.put("reqId", msg.getPacketId());
//		payloadObj.put("serviceId", msg.getEventId());
//		payloadObj.put("deviceId", msg.getClientId());
//		payloadObj.put("timestamp", DateUtil.current());
//		payloadObj.put("data", dataObj);
//
//		String pubTopic = "/test/request";// TODO 发布Topic
//
//		ClusterEvent clusterEvent = ClusterEvent.builder() //
//				.topic(pubTopic) //
//				.qos(MqttQoS.EXACTLY_ONCE) //
//				.retain(false) //
//				.payload(payloadObj) //
//				.msgTimestamp(DateUtil.current()) //
//				.srcChannelId(channel.getId()) //
//				.clientId(channel.getClientId()) //
//				.build();
//		Context.clusterManager().broadcast(pubTopic, clusterEvent);
//
//		Context.meterManager().getMetricRegistry().getMetricCounter(CounterType.PUBLISH_EVENT).increment();

		// TODO 下发查询终端参数消息
		// TODO 在处理查询终端参数应答消息重设空闲处理逻辑
//		channel.getConnection().onReadIdle((long) connMsg.getKeepAlive() * 1000 << 1,
//				() -> log.warn("JT808 client heartbeat timeout - {} | {} | {}", clientAddr, clientId, LogEvent.HEARTBEAT_TIMEOUT.name()));
	}

}
