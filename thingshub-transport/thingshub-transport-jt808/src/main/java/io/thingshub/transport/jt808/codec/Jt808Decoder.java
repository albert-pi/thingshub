package io.thingshub.transport.jt808.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.util.HexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.thingshub.transport.codec.MessagePayload;
import io.thingshub.transport.codec.MessageType;
import io.thingshub.transport.jt808.Jt808Exception;
import io.thingshub.transport.jt808.codec.Jt808LocationPayload.Jt808LocationPayloadBuilder;
import io.thingshub.transport.jt808.codec.LocationAdditionalAlarm.LocationAdditionalAlarmBuilder;
import io.thingshub.transport.jt808.codec.LocationAdditionalBasic.LocationAdditionalBasicBuilder;
import io.thingshub.utils.BcdOps;
import io.thingshub.utils.ByteOps;
import io.thingshub.utils.IntOps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class Jt808Decoder extends ChannelInboundHandlerAdapter {

	public static final Jt808Decoder INSTANCE = new Jt808Decoder();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ByteBuf) {
			final ByteBuf buf = (ByteBuf) msg;
			try {
				if (buf.readableBytes() <= 0) {
					return;
				}

				final byte[] unescapedBytes = new byte[buf.readableBytes()];
				buf.readBytes(unescapedBytes);

				if (unescapedBytes[0] != Jt808Message.DELIMITER) {
					ctx.fireChannelRead(msg);
					return;
				}

				log.debug(">>>>>>>>>>>>>>> raw msg: {}", HexUtil.encodeHexStr(unescapedBytes));
				final byte[] escapedBytes = escapeInboundMessage(unescapedBytes, 0, unescapedBytes.length);
				log.debug(">>>>>>>>>>>>>>> escaped msg: {}", HexUtil.encodeHexStr(escapedBytes));

				final byte[] messageBytes = ByteOps.slice(escapedBytes, 1, escapedBytes.length - 2);
				final Jt808Header header = decodeHeader(messageBytes);

				final int checkSumInPkg = IntOps.intFromBytes(escapedBytes, escapedBytes.length - 3, 1);
				validateCheckSum(messageBytes, header.getMessageType(), checkSumInPkg);

				final int payloadStartIndex = header.getPayloadProps().isSubPackage() ? 16 : 12;
				if (messageBytes.length > payloadStartIndex) {
					final byte[] payloadBytes = ByteOps.slice(messageBytes, payloadStartIndex, messageBytes.length);
					final MessagePayload payload = decodePayload(payloadBytes, header.getMessageType());

					final Jt808Message message = new Jt808Message(header, payload);
					ctx.fireChannelRead(message);
				} else {
					final Jt808Message message = new Jt808Message(header, null);
					ctx.fireChannelRead(message);
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				buf.release();
			}
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	private static void validateCheckSum(byte[] bytes, MessageType messageType, int checkSumInPkg) {
		final int calculatedCheckSum = Jt808Message.calculateCheckSum(bytes, 0, bytes.length - 1);
		if (checkSumInPkg != calculatedCheckSum) {
			log.warn("校验码不一致。message type: {}, expected checksum: {}, calculated checksum: {}", messageType.getName(), checkSumInPkg, calculatedCheckSum);
		}
	}

	private static byte[] escapeInboundMessage(byte[] bytes, int start, int end) throws Jt808Exception {
		if (start < 0 || end > bytes.length) {
			throw new Jt808Exception("byte index out of bounds(start=" + start + ",end=" + end + ",bytes length=" + bytes.length + ")");
		}
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			int i = 0;
			for (; i < start; i++) {
				outputStream.write(bytes[i]);
			}
			for (; i < end - 1; i++) {
				if (bytes[i] == 0x7d && bytes[i + 1] == 0x01) {
					outputStream.write(0x7d);
					i++;
				} else if (bytes[i] == 0x7d && bytes[i + 1] == 0x02) {
					outputStream.write(0x7e);
					i++;
				} else {
					outputStream.write(bytes[i]);
				}
			}
			for (; i < bytes.length; i++) {
				outputStream.write(bytes[i]);
			}
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new Jt808Exception(e);
		}
	}

	private static Jt808Header decodeHeader(final byte[] bytes) {
		int messageId = IntOps.intFromBytes(bytes, 0, 2);
		MessageType msgType = Jt808MessageType.valueOf(messageId);

		final int bodyPropVal = IntOps.intFromBytes(bytes, 2, 2);
		int payloadLength = bodyPropVal & 0x3ff;
		int encryptionType = (bodyPropVal & 0x1c00) >> 10;
		boolean hasSubPackage = ((bodyPropVal & 0x2000) >> 13) == 1;
		int reversed = (bodyPropVal & 0xc000) >> 14;
		Jt808Header.PayloadProps payloadProps = new Jt808Header.PayloadProps(payloadLength, encryptionType, hasSubPackage, reversed);

		String mobile = BcdOps.bcd2Str(bytes, 4, 6);
		int messageSeq = IntOps.intFromBytes(bytes, 10, 2);

		return new Jt808Header(msgType, payloadProps, mobile, messageSeq);
	}

	private static MessagePayload decodePayload(final byte[] payloadBytes, MessageType msgType) {
		Jt808MessageType theMsgType = (Jt808MessageType) msgType;
		switch (theMsgType) {
		case TERMINAL_GENERIC_REPLY:
			return decodeGenericReplyPayload(payloadBytes);

		case HEARTBEAT:
			return null;

		case REGISTER:
			return decodeRegisterPayload(payloadBytes);

		case UNREGISTER:
			return null;

		case AUTH:
			return decodeAuthPayload(payloadBytes);

		case LOCATION:
			return decodeLocationPayload(payloadBytes);

		case CAN:
			return decodeCanPayload(payloadBytes);

		case TRANSMISSION:
			return decodeTransmissionPayload(payloadBytes);

		case VERSION:
			return decodeVersionPayload(payloadBytes);

		case SETTING_QRY_REPLY:
			return decodeSettingQryReplyPayload(payloadBytes);

		case LOCATION_QRY_REPLY:
			return decodeLocationQryReplyPayload(payloadBytes);

		case TEXT_REPLY:
			return decodeTextReplyPayload(payloadBytes);

		case CTRL_TERMINAL_REPLY:
			return decodeCtrlTerminalReplyPayload(payloadBytes);

		case UPGRADE_RESULT:
			return decodeUpgradeResultPayload(payloadBytes);

		default:
			return null;
		}
	}

	private static Jt808GenericReplyPayload decodeGenericReplyPayload(final byte[] payloadBytes) {
		int messageSeq = IntOps.intFromBytes(payloadBytes, 0, 2);

		int messageId = IntOps.intFromBytes(payloadBytes, 2, 2);
		int result = IntOps.intFromBytes(payloadBytes, 4, 1);

		return Jt808GenericReplyPayload.builder().messageSeq(messageSeq).messageId(messageId).result(result).build();
	}

	private static Jt808RegisterPayload decodeRegisterPayload(final byte[] payloadBytes) {
		int provinceId = IntOps.intFromBytes(payloadBytes, 0, 2);
		int cityId = IntOps.intFromBytes(payloadBytes, 2, 2);
		String manufacturerId = new String(ByteOps.slice(payloadBytes, 4, 5), Jt808Message.JT808_STRING_ENCODING);
		String model = new String(ByteOps.slice(payloadBytes, 9, 20), Jt808Message.JT808_STRING_ENCODING);
		String terminalId = new String(ByteOps.slice(payloadBytes, 29, 7), Jt808Message.JT808_STRING_ENCODING);
		int plateColor = IntOps.intFromBytes(payloadBytes, 36, 1);
		String licensePlate = new String(ByteOps.slice(payloadBytes, 37, payloadBytes.length - 37), Jt808Message.JT808_STRING_ENCODING);

		return Jt808RegisterPayload.builder().provinceId(provinceId).cityId(cityId).manufacturerId(manufacturerId.trim()).model(model.trim())
				.terminalId(terminalId.trim()).plateColor(plateColor).licensePlate(licensePlate.trim()).build();
	}

	private static Jt808AuthPayload decodeAuthPayload(final byte[] payloadBytes) {
		String code = new String(payloadBytes, Jt808Message.JT808_STRING_ENCODING);

		return Jt808AuthPayload.builder().code(code).build();
	}

	private static Jt808LocationPayload decodeLocationPayload(final byte[] payloadBytes) {
		int alarm = IntOps.intFromBytes(payloadBytes, 0, 4);
		int status = IntOps.intFromBytes(payloadBytes, 4, 4);
		int lat = IntOps.intFromBytes(payloadBytes, 8, 4);
		int lng = IntOps.intFromBytes(payloadBytes, 12, 4);
		int elevation = IntOps.intFromBytes(payloadBytes, 16, 2);
		int speed = IntOps.intFromBytes(payloadBytes, 18, 2);
		int angle = IntOps.intFromBytes(payloadBytes, 20, 2);
		String time = BcdOps.bcd2Str(payloadBytes, 22, 6);

		Jt808LocationPayloadBuilder builder = Jt808LocationPayload.builder().alarm(alarm).status(status).lat(lat).lng(lng).elevation(elevation).speed(speed)
				.angle(angle).time(time);
		int byteIndex = 28;
		while (payloadBytes.length > byteIndex) {
			byteIndex = parseAdditionalInfo(payloadBytes, builder, byteIndex);
		}

		return builder.build();
	}

	private static int parseAdditionalInfo(final byte[] payloadBytes, Jt808LocationPayloadBuilder builder, int byteIndex) {
		int additionalId = IntOps.intFromBytes(payloadBytes, byteIndex, 1);
		int additionalLen = IntOps.intFromBytes(payloadBytes, byteIndex + 1, 1);

		switch (additionalId) {
		case 0xFA:
			int alarmId = IntOps.intFromBytes(payloadBytes, byteIndex + 2, 2);

			LocationAdditionalAlarmBuilder additionalAlarmBuilder = LocationAdditionalAlarm.builder();
			additionalAlarmBuilder.alarmId(alarmId);

			switch (alarmId) {
			case 0x0001: // 点火上报
			case 0x0002: // 熄火上报
			case 0x0003: // 设防上报
			case 0x0004: // 撤防上报
			case 0x0005: // 车门打开
			case 0x0006: // 车门关闭
			case 0x0007: // 系统启动
			case 0x0101: // 拖车报警
			case 0x0102: // 定位过长报警
			case 0x0103: // 终端拔出报警(主电掉电)
			case 0x0104: // 终端插入报警（主电上电）
			case 0x0105: // 低电压报警
				break;
			case 0x0106: // 怠速过长报警
				int idlingStatus = IntOps.intFromBytes(payloadBytes, byteIndex + 5, 1);
				additionalAlarmBuilder.status(idlingStatus);
				if (idlingStatus == 0) {
					int durationInIdling = IntOps.intFromBytes(payloadBytes, byteIndex + 6, 2);
					int fuelInIdling = IntOps.intFromBytes(payloadBytes, byteIndex + 8, 2);
					int maxIdlingRotatingSpeed = IntOps.intFromBytes(payloadBytes, byteIndex + 10, 2);
					int minIdlingRotatingSpeed = IntOps.intFromBytes(payloadBytes, byteIndex + 12, 2);

					additionalAlarmBuilder.duration(durationInIdling).fuelInIdling(fuelInIdling).maxIdlingRotatingSpeed(maxIdlingRotatingSpeed)
							.minIdlingRotatingSpeed(minIdlingRotatingSpeed);
				}
				break;
			case 0x0107: // 超速报警
				int overSpeedStatus = IntOps.intFromBytes(payloadBytes, byteIndex + 5, 1);
				additionalAlarmBuilder.status(overSpeedStatus);
				if (overSpeedStatus == 0) {
					int durationInOverSpeed = IntOps.intFromBytes(payloadBytes, byteIndex + 6, 2);
					int maxSpeed = IntOps.intFromBytes(payloadBytes, byteIndex + 8, 2);
					int avgSpeed = IntOps.intFromBytes(payloadBytes, byteIndex + 10, 2);
					int mileageInOverSpeed = IntOps.intFromBytes(payloadBytes, byteIndex + 12, 2);

					additionalAlarmBuilder.duration(durationInOverSpeed).maxSpeed(maxSpeed).avgSpeed(avgSpeed).mileageInOverSpeed(mileageInOverSpeed);
				}
				break;
			case 0x0108: // 疲劳驾驶报警
				int tirednessStatus = IntOps.intFromBytes(payloadBytes, byteIndex + 5, 1);
				additionalAlarmBuilder.status(tirednessStatus);
				if (tirednessStatus == 0) {
					int durationInTired = IntOps.intFromBytes(payloadBytes, byteIndex + 6, 2);
					additionalAlarmBuilder.duration(durationInTired);
				}
				break;
			case 0x0109: // 水温报警
				int waterTempStatus = IntOps.intFromBytes(payloadBytes, byteIndex + 5, 1);
				additionalAlarmBuilder.status(waterTempStatus);
				if (waterTempStatus == 0) {
					int durationInHighWaterTemp = IntOps.intFromBytes(payloadBytes, byteIndex + 6, 4);
					int maxWaterTemp = IntOps.intFromBytes(payloadBytes, byteIndex + 10, 2);
					int avgWaterTemp = IntOps.intFromBytes(payloadBytes, byteIndex + 12, 2);

					additionalAlarmBuilder.duration(durationInHighWaterTemp).maxWaterTemp(maxWaterTemp).avgWaterTemp(avgWaterTemp);
				}
				break;
			case 0x010A: // 高速空档滑行报警
			case 0x010B: // 油耗不支持报警
			case 0x010C: // OBD不支持报警
			case 0x010D: // 低水温高转速
			case 0x010E: // 总线不睡眠报警
			case 0x010F: // 非法开门
			case 0x0110: // 非法点火
			case 0x0111: // 急加速报警
			case 0x0112: // 急减速报警
			case 0x0113: // 急拐弯报警
			case 0x0114: // 碰撞报警
			case 0x0115: // 异常振动报警
			case 0x0201: // GPS模块故障报警
			case 0x0202: // FLASH故障报警
			case 0x0203: // CAN模块故障报警
			case 0x0204: // 3D传感器故障报警
			case 0x0205: // RTC模块故障报警
			case 0x0206: // 温度传感器故障报警
			case 0x0301: // 设防玻璃未关提醒
			case 0x0302: // 锁车未成功提醒
			case 0x0303: // 超时未设防提醒
			case 0x0401: // 急刹车
			case 0x0402: // 紧急刹车
			case 0x0403: // 超转速
			case 0x0404: // PTO怠速
			case 0x0405: // OBD头（4-5）拔出
			case 0x0406: // OBD头(4-5)插入
			case 0x0407: // 主机拆除报警（探针）
			case 0x0408: // 主机盒被打开报警（光传感器）
			case 0x0409: // 新能源充电状态变化
			case 0x040A: // 电池低电量报警
				break;
			default:
				break;
			}

			builder.additionalAlarm(additionalAlarmBuilder.build());
			break;
		case 0xEA:
			int mileage = IntOps.intFromBytes(payloadBytes, byteIndex + 6, 4);
			int fuel = IntOps.intFromBytes(payloadBytes, byteIndex + 14, 4);
			int duration = IntOps.intFromBytes(payloadBytes, byteIndex + 21, 4);
			int durationInStalling = IntOps.intFromBytes(payloadBytes, byteIndex + 28, 4);
			int durationInIdling = IntOps.intFromBytes(payloadBytes, byteIndex + 35, 4);
			int accelerationMetrics = IntOps.intFromBytes(payloadBytes, byteIndex + 42, 2);
			int accelerationMetricInterval = IntOps.intFromBytes(payloadBytes, byteIndex + 44, 2);

			List<Integer> avgAccelerations = new ArrayList<>();
			for (int i = 1; i <= accelerationMetrics; i++) {
				avgAccelerations.add(IntOps.intFromBytes(payloadBytes, byteIndex + 44 + i * 2, 2));
			}
			int maxAcceleration = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 2, 2);

			int voltage = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 7, 2);
			int batteryVoltage = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 12, 1);
			int csq = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 16, 1);
			int vehicleType = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 20, 2);
			int obdProtocol = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 25, 1);
			int drivingId = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 29, 2);
			int gpsNum = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 34, 1);
			int gpsPrecision = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 38, 2);
			int gpsAntenna = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 43, 1);
			int devicePulled = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 47, 1);
			int totalMileage = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 51, 4);
			int ignitionType = IntOps.intFromBytes(payloadBytes, byteIndex + 44 + accelerationMetrics * 2 + 58, 2);

			LocationAdditionalBasicBuilder additionalBasicBuilder = LocationAdditionalBasic.builder();
			additionalBasicBuilder.mileage(mileage).fuel(fuel) //
					.duration(duration).durationInStalling(durationInStalling).durationInIdling(durationInIdling) //
					.accelerationMetrics(accelerationMetrics).accelerationMetricInterval(accelerationMetricInterval) //
					.avgAccelerations(avgAccelerations).maxAcceleration(maxAcceleration) //
					.voltage(voltage).batteryVoltage(batteryVoltage) //
					.csq(csq).vehicleType(vehicleType).obdProtocol(obdProtocol) //
					.drivingId(drivingId).gpsNum(gpsNum).gpsPrecision(gpsPrecision).gpsAntenna(gpsAntenna) //
					.devicePulled(devicePulled).totalMileage(totalMileage).ignitionType(ignitionType);

			builder.additionalBasic(additionalBasicBuilder.build());
			break;
		default:
			break;
		}

		return byteIndex + 2 + additionalLen;
	}

	private static Jt808CanPayload decodeCanPayload(final byte[] payloadBytes) {
		// TODO CAN总线数据

		return null;
	}

	private static Jt808TransmissionPayload decodeTransmissionPayload(final byte[] payloadBytes) {
		int b = IntOps.intFromBytes(payloadBytes, 0, 1);
		Jt808TransmissionType transmissionType = Jt808TransmissionType.valueOf(b);

		switch (transmissionType) {
		case DRIVING_DATA:
			String tStartTime = BcdOps.bcd2Str(payloadBytes, 4, 6);
			String tEndTime = BcdOps.bcd2Str(payloadBytes, 13, 6);
			int tStartLat = IntOps.intFromBytes(payloadBytes, 22, 4);
			int tStartLng = IntOps.intFromBytes(payloadBytes, 29, 4);
			int tEndLat = IntOps.intFromBytes(payloadBytes, 36, 4);
			int tEndLng = IntOps.intFromBytes(payloadBytes, 43, 4);
			int tDrivingId = IntOps.intFromBytes(payloadBytes, 50, 2);
			int tMileageType = IntOps.intFromBytes(payloadBytes, 55, 1);
			int tMileage = IntOps.intFromBytes(payloadBytes, 59, 4);
			int tFuel = IntOps.intFromBytes(payloadBytes, 66, 4);
			int tDuration = IntOps.intFromBytes(payloadBytes, 73, 4);
			int tDurationInOverspeed = IntOps.intFromBytes(payloadBytes, 80, 2);
			int tTimesInOverspeed = IntOps.intFromBytes(payloadBytes, 85, 2);
			int tAvgSpeed = IntOps.intFromBytes(payloadBytes, 90, 1);
			int tMaxSpeed = IntOps.intFromBytes(payloadBytes, 94, 1);
			int tDurationInIdling = IntOps.intFromBytes(payloadBytes, 98, 4);
			int tSupportFootbraking = IntOps.intFromBytes(payloadBytes, 105, 1);
			int tFootbrakingTimes = IntOps.intFromBytes(payloadBytes, 109, 2);
			int tRapidSpeedupTimes = IntOps.intFromBytes(payloadBytes, 114, 4);
			int tRapidSlowdownTimes = IntOps.intFromBytes(payloadBytes, 121, 4);
			int tRapidTurningTimes = IntOps.intFromBytes(payloadBytes, 128, 4);
			int tMileage4Speed20 = IntOps.intFromBytes(payloadBytes, 135, 4);
			int tMileage4Speed2040 = IntOps.intFromBytes(payloadBytes, 142, 4);
			int tMileage4Speed4060 = IntOps.intFromBytes(payloadBytes, 149, 4);
			int tMileage4Speed6080 = IntOps.intFromBytes(payloadBytes, 156, 4);
			int tMileage4Speed80100 = IntOps.intFromBytes(payloadBytes, 163, 4);
			int tMileage4Speed100120 = IntOps.intFromBytes(payloadBytes, 170, 4);
			int tMileage4Speed120 = IntOps.intFromBytes(payloadBytes, 177, 4);
			int tFuleInIdling = IntOps.intFromBytes(payloadBytes, 184, 4);

			return Jt808TransmissionPayload.builder().type(b) //
					.startTime(tStartTime).endTime(tEndTime) //
					.startLat(tStartLat).startLng(tStartLng).endLat(tEndLat).endLng(tEndLng) //
					.drivingId(tDrivingId).mileageType(tMileageType).mileage(tMileage).fuel(tFuel).duration(tDuration) //
					.durationInOverspeed(tDurationInOverspeed).timesInOverspeed(tTimesInOverspeed).avgSpeed(tAvgSpeed).maxSpeed(tMaxSpeed) //
					.durationInIdling(tDurationInIdling).supportFootbraking(tSupportFootbraking).footbrakingTimes(tFootbrakingTimes) //
					.rapidSpeedupTimes(tRapidSpeedupTimes).rapidSlowdownTimes(tRapidSlowdownTimes).rapidTurningTimes(tRapidTurningTimes) //
					.mileage4Speed20(tMileage4Speed20).mileage4Speed2040(tMileage4Speed2040).mileage4Speed4060(tMileage4Speed4060) //
					.mileage4Speed6080(tMileage4Speed6080).mileage4Speed80100(tMileage4Speed80100).mileage4Speed100120(tMileage4Speed100120) //
					.mileage4Speed120(tMileage4Speed120).fuleInIdling(tFuleInIdling).build();

		case FAULT_DATA:
			String faultTime = BcdOps.bcd2Str(payloadBytes, 1, 6);
			int faultLat = IntOps.intFromBytes(payloadBytes, 7, 4);
			int faultLng = IntOps.intFromBytes(payloadBytes, 11, 4);
			int faultCount = IntOps.intFromBytes(payloadBytes, 15, 1);

			List<Integer> faultIds = null;
			if (faultCount > 0) {
				faultIds = new ArrayList<>();

				int byteIndex = 16;
				for (int i = 1; i <= faultCount; i++) {
					faultIds.add(IntOps.intFromBytes(payloadBytes, byteIndex, 4));
					byteIndex += 4;
				}
			}

			return Jt808TransmissionPayload.builder().type(b).faultTime(faultTime).faultLat(faultLat).faultLng(faultLng).faultCount(faultCount)
					.faultIds(faultIds).build();

		case SLEEPING_DATA:
			String sleepingTime = BcdOps.bcd2Str(payloadBytes, 1, 6);
			return Jt808TransmissionPayload.builder().type(b).sleepingTime(sleepingTime).build();

		case WAKENING_DATA:
			String wakeningTime = BcdOps.bcd2Str(payloadBytes, 1, 6);
			int wakeningType = IntOps.intFromBytes(payloadBytes, 7, 1);
			int voltage = IntOps.intFromBytes(payloadBytes, 8, 2);
			int acceleration = IntOps.intFromBytes(payloadBytes, 10, 2);

			return Jt808TransmissionPayload.builder().type(b).wakeningTime(wakeningTime).wakeningType(wakeningType).voltage(voltage).acceleration(acceleration)
					.build();

		default:
			return null;
		}
	}

	private static Jt808VersionPayload decodeVersionPayload(final byte[] payloadBytes) {
		String versionName = new String(ByteOps.slice(payloadBytes, 0, 14), Jt808Message.JT808_STRING_ENCODING);
		String versionDate = new String(ByteOps.slice(payloadBytes, 14, 10), Jt808Message.JT808_STRING_ENCODING);
		String cpuId = new String(ByteOps.slice(payloadBytes, 24, 12), Jt808Message.JT808_STRING_ENCODING);
		String gsmModel = new String(ByteOps.slice(payloadBytes, 36, 15), Jt808Message.JT808_STRING_ENCODING);
		String imei = new String(ByteOps.slice(payloadBytes, 51, 15), Jt808Message.JT808_STRING_ENCODING);
		String imsi = new String(ByteOps.slice(payloadBytes, 66, 15), Jt808Message.JT808_STRING_ENCODING);
		String iccid = new String(ByteOps.slice(payloadBytes, 81, 20), Jt808Message.JT808_STRING_ENCODING);
		Integer vehicleType = IntOps.intFromBytes(payloadBytes, 101, 2);
		String vin = new String(ByteOps.slice(payloadBytes, 103, 17), Jt808Message.JT808_STRING_ENCODING);
		Integer mileage = IntOps.intFromBytes(payloadBytes, 120, 4);
		Integer fuel = IntOps.intFromBytes(payloadBytes, 124, 4);

		return Jt808VersionPayload.builder().versionName(versionName).versionDate(versionDate).cpuId(cpuId).gsmModel(gsmModel).imei(imei).imsi(imsi)
				.iccid(iccid).vehicleType(vehicleType).vin(vin).mileage(mileage).fuel(fuel).build();
	}

	private static Jt808SettingQryReplyPayload decodeSettingQryReplyPayload(final byte[] payloadBytes) {
		// TODO 查询终端参数应答信息

		return null;
	}

	private static Jt808LocationQryReplyPayload decodeLocationQryReplyPayload(final byte[] payloadBytes) {
		// TODO 查询终端位置应答信息

		return null;
	}

	private static Jt808TextReplyPayload decodeTextReplyPayload(final byte[] payloadBytes) {
		// TODO 终端上传文本信息

		return null;
	}

	private static Jt808CtrlTerminalReplyPayload decodeCtrlTerminalReplyPayload(final byte[] payloadBytes) {
		// TODO 控制终端命令应答信息

		return null;
	}

	private static Jt808UpgradeResultPayload decodeUpgradeResultPayload(final byte[] payloadBytes) {
		// TODO 终端上传文本信息

		return null;
	}

}
