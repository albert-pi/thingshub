function parsePayload(msgType, payloadBytes) {
	let payloadObj = {};
	let params = {};
	
	payloadObj["version"] = "1.0";	
	
	switch (msgType) {
		case 0x00:
			payloadObj["method"] = "thing.service.register";// 客户端调用server端
			
			break;
		case 0x01:
			params["equipType"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
			params["power"] = IntOps.static.intFromBytes(payloadBytes, 1, 4);
			params["connectors"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
			params["policyId"] = IntOps.static.intFromBytes(payloadBytes, 6, 4);
			params["policyVer"] = IntOps.static.intFromBytes(payloadBytes, 10, 4);
			params["operatorId"] = IntOps.static.intFromBytes(payloadBytes, 14, 4);	    
			params["secret"] = BcdOps.static.bcd2Str(payloadBytes, 18, 3);
			params["softwareVer"] = StringOps.static.strFromBytes(payloadBytes, 21, 16);
			params["hardwareVer"] = StringOps.static.strFromBytes(payloadBytes, 37, 16);
			params["protocolVer"] = StringOps.static.strFromBytes(payloadBytes, 53, 8);
			
			payloadObj["params"] = params;
			payloadObj["method"] = "thing.service.auth";
			
			break;
		case 0x82:
			params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 0, 16);	
			params["result"] = IntOps.static.intFromBytes(payloadBytes, 16, 1);	
			
			payloadObj["params"] = params;
			payloadObj["method"] = "thing.service.call.reply";
			
			break;
		case 0x83:
			params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 0, 16);	
			params["result"] = IntOps.static.intFromBytes(payloadBytes, 16, 1);	
			
			payloadObj["params"] = params;
			payloadObj["method"] = "thing.service.call.reply";
			
			break;
	  case 0x04:
	  	params["connectorNo"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["bootingMethod"] = IntOps.static.intFromBytes(payloadBytes, 1, 1);
	  	params["chargingMode"] = IntOps.static.intFromBytes(payloadBytes, 2, 1);
	  	params["chargingData"] = IntOps.static.intFromBytes(payloadBytes, 3, 4);
	  	params["uid"] = StringOps.static.strFromBytes(payloadBytes, 7, 32);
	  	params["password"] = StringOps.static.strFromBytes(payloadBytes, 39, 32);				
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 71, 16);			
	  	params["validFlag"] = IntOps.static.intFromBytes(payloadBytes, 87, 1);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.request";
	  	
	  	break;
	  case 0x05:
	  	params["connectorNo"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["uid"] = StringOps.static.strFromBytes(payloadBytes, 1, 16); // 16 or 32?
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 17, 16);	
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.request";
	  	
	  	break;
	  case 0x86:
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 0, 16);	
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 16, 1);	
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.call.reply";
			
			break;
	  case 0x87:
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 0, 16);	
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 16, 1);	
			
	  	payloadObj["params"] = params;
			payloadObj["method"] = "thing.service.call.reply";
			
			break;
	  case 0x08:
	  	params["connectorNo"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 1, 16);	
	  	params["bootingMethod"] = IntOps.static.intFromBytes(payloadBytes, 17, 1);
	  	params["uid"] = StringOps.static.strFromBytes(payloadBytes, 18, 16); // 16 or 32?
	  	params["vin"] = StringOps.static.strFromBytes(payloadBytes, 34, 17);
	  	params["socStart"] = IntOps.static.intFromBytes(payloadBytes, 51, 1);
	  	params["socEnd"] = IntOps.static.intFromBytes(payloadBytes, 52, 1);
	  	params["stopReason"] = IntOps.static.intFromBytes(payloadBytes, 53, 1);
	  	params["start"] = BcdOps.static.bcd2Str(payloadBytes, 54, 6);
	  	params["end"] = BcdOps.static.bcd2Str(payloadBytes, 60, 6);
	  	params["precision"] = IntOps.static.intFromBytes(payloadBytes, 66, 1);
	  	params["elecMeterStart"] = IntOps.static.intFromBytes(payloadBytes, 67, 4);
	  	params["elecMeterEnd"] = IntOps.static.intFromBytes(payloadBytes, 71, 4);
	  	params["power"] = IntOps.static.intFromBytes(payloadBytes, 75, 4);
	  	params["peakPower"] = IntOps.static.intFromBytes(payloadBytes, 79, 4);
	  	params["highPower"] = IntOps.static.intFromBytes(payloadBytes, 83, 4);
	  	params["normalPower"] = IntOps.static.intFromBytes(payloadBytes, 87, 4);
	  	params["lowPower"] = IntOps.static.intFromBytes(payloadBytes, 91, 4);
	  	params["chargingAmount"] = IntOps.static.intFromBytes(payloadBytes, 95, 4);
	  	params["serviceAmount"] = IntOps.static.intFromBytes(payloadBytes, 99, 4);
	  	params["bookingAmount"] = IntOps.static.intFromBytes(payloadBytes, 103, 4);
	  	params["parkingAmount"] = IntOps.static.intFromBytes(payloadBytes, 107, 4);
	  	params["details"] = [];// TODO start, end, power ...48个时间段
	  	params["policyId"] = IntOps.static.intFromBytes(payloadBytes, 111 + 48*4, 4);
	  	params["policyVer"] = IntOps.static.intFromBytes(payloadBytes, 111 + 49*4, 4);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.property.post";
			
	    break;
	  case 0x09:
	  	params["connectorNo"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 1, 16);	
	  	params["bootingMethod"] = IntOps.static.intFromBytes(payloadBytes, 17, 1);
	  	params["uid"] = StringOps.static.strFromBytes(payloadBytes, 18, 16);	// 16 or 32?
	  	params["vin"] = StringOps.static.strFromBytes(payloadBytes, 34, 17);
	  	params["soc"] = StringOps.static.strFromBytes(payloadBytes, 51, 1);
	  	params["start"] = BcdOps.static.bcd2Str(payloadBytes, 52, 6);
	  	params["end"] = BcdOps.static.bcd2Str(payloadBytes, 58, 6);
	  	params["precision"] = IntOps.static.intFromBytes(payloadBytes, 64, 1);
	  	params["power"] = IntOps.static.intFromBytes(payloadBytes, 65, 4);
	  	params["peakPower"] = IntOps.static.intFromBytes(payloadBytes, 69, 4);
	  	params["highPower"] = IntOps.static.intFromBytes(payloadBytes, 73, 4);
	  	params["normalPower"] = IntOps.static.intFromBytes(payloadBytes, 77, 4);
	  	params["lowPower"] = IntOps.static.intFromBytes(payloadBytes, 81, 4);
	  	params["chargingAmount"] = IntOps.static.intFromBytes(payloadBytes, 85, 4);
	  	params["serviceAmount"] = IntOps.static.intFromBytes(payloadBytes, 89, 4);
	  	params["bookingAmount"] = IntOps.static.intFromBytes(payloadBytes, 93, 4);
	  	params["parkingAmount"] = IntOps.static.intFromBytes(payloadBytes, 97, 4);
	  	params["equipTemperature"] = IntOps.static.intFromBytes(payloadBytes, 101, 4);
	  	params["connectorTemperature"] = IntOps.static.intFromBytes(payloadBytes, 105, 4);
	  	params["inputVoltage"] = IntOps.static.intFromBytes(payloadBytes, 109, 4);
	  	params["inputCurrent"] = IntOps.static.intFromBytes(payloadBytes, 113, 4);
	  	params["outputVoltage"] = IntOps.static.intFromBytes(payloadBytes, 117, 4);
	  	params["outputCurrent"] = IntOps.static.intFromBytes(payloadBytes, 121, 4);
	  	params["requireVoltage"] = IntOps.static.intFromBytes(payloadBytes, 125, 4);
	  	params["requireCurrent"] = IntOps.static.intFromBytes(payloadBytes, 129, 4);
	  	params["voltageA"] = IntOps.static.intFromBytes(payloadBytes, 133, 4);
	  	params["voltageB"] = IntOps.static.intFromBytes(payloadBytes, 137, 4);
	  	params["voltageC"] = IntOps.static.intFromBytes(payloadBytes, 141, 4);
	  	params["currentA"] = IntOps.static.intFromBytes(payloadBytes, 145, 4);
	  	params["currentB"] = IntOps.static.intFromBytes(payloadBytes, 149, 4);
	  	params["currentC"] = IntOps.static.intFromBytes(payloadBytes, 153, 4);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.property.post";
			
	  	break;
	  case 0x0A:
	  	params["connectorNo"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 1, 16);
	  	params["bmsVer"] = IntOps.static.intFromBytes(payloadBytes, 17, 3);
	  	params["bmsType"] = IntOps.static.intFromBytes(payloadBytes, 20, 1);	
	  	params["power"] = IntOps.static.intFromBytes(payloadBytes, 21, 4);	
	  	params["current"] = IntOps.static.intFromBytes(payloadBytes, 25, 4);	
	  	params["voltage"] = IntOps.static.intFromBytes(payloadBytes, 29, 4);	
	  	params["manufacturerId"] = IntOps.static.intFromBytes(payloadBytes, 33, 4);
	  	params["productionDate"] = BcdOps.static.bcd2Str(payloadBytes,37, 3);
	  	params["chargingTimes"] = IntOps.static.intFromBytes(payloadBytes, 40, 4);
	  	params["maxCurrent"] = IntOps.static.intFromBytes(payloadBytes, 44, 4);
	  	params["maxVoltage"] = IntOps.static.intFromBytes(payloadBytes, 48, 4);
	  	params["maxTemperature"] = IntOps.static.intFromBytes(payloadBytes, 52, 4);
// params["batteryMaxVoltage"] = IntOps.static.intFromBytes(payloadBytes, 56, 4); //???
	  	params["batteryMaxVoltage"] = IntOps.static.intFromBytes(payloadBytes, 56, 4);
	  	params["batteryMinVoltage"] = IntOps.static.intFromBytes(payloadBytes, 60, 4);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.property.post";
	  	
	  	break;
	  case 0x8B:
	  	params["policyId"] = IntOps.static.intFromBytes(payloadBytes, 0, 4);
	  	params["policyVer"] = IntOps.static.intFromBytes(payloadBytes, 4, 4);
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 8, 1);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.call.reply";
	  	
			break;
	  case 0x0C:
	  	params["connectors"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["states"] = [];
	  	for (var i = 1; i <= params["connectors"]; i++) {
	  		var _state = IntOps.static.intFromBytes(payloadBytes, i, 1);
	  		params["states"].push(_state);
	  	}
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.property.post";
	  	
	  	break;
	  case 0x0D:
	  	params["connectorNo"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["alarmCode"] = BcdOps.static.bcd2Str(payloadBytes, 1, 2);
	  	params["alarmTime"] = BcdOps.static.bcd2Str(payloadBytes,3, 6);
	  	params["state"] = IntOps.static.intFromBytes(payloadBytes, 9, 1);
	  	params["val"] = IntOps.static.intFromBytes(payloadBytes, 10, 4);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.event.post";
	  	
	  	break;
	  case 0x8E:
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.call.reply";
	  	
			break;
	  case 0x9A:
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.call.reply";
	  	
			break;
	  case 0x9B:
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.call.reply";
	  	
			break;
	  case 0x1C:
	  	params["connectorNo"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.request";
	  	
	  	break;
	  case 0x9D:
	  	params["connectorNo"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 1, 16);	
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 17, 1);	
			
	  	payloadObj["params"] = params;
			payloadObj["method"] = "thing.service.call.reply";
			
			break;
	  case 0x9E:
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);	
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.call.reply";
	  	
			break;
	  case 0x1F:
	  	params["segment"] = IntOps.static.intFromBytes(payloadBytes, 0, 2);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.request";
	  	
	  	break;
	  case 0x20:
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["softwareVer"] = StringOps.static.strFromBytes(payloadBytes, 1, 16);
	  	params["protocolVer"] = StringOps.static.strFromBytes(payloadBytes, 17, 8);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.event.post";
	  	
	  	break;
	  case 0xA1: 
	  	params["ctrlTypeId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 1, 1);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.call.reply";
	  	
			break;
	  case 0xA2:
	  	params["result"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.call.reply";
	  	
			break;
	  case 0x24: 	
	  	params["coordinate"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["lng"] = IntOps.static.intFromBytes(payloadBytes, 1, 4);
	  	params["lat"] = IntOps.static.intFromBytes(payloadBytes, 5, 4);
	  	params["network"] = IntOps.static.intFromBytes(payloadBytes, 9, 1);
	  	params["operator"] = StringOps.static.strFromBytes(payloadBytes, 10, 8);
	  	params["rssi"] = IntOps.static.intFromBytes(payloadBytes, 18, 1);
	  	params["iccid"] = StringOps.static.strFromBytes(payloadBytes, 19, 32);
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.property.post";
	  	
	  	break;
	  case 0x26:  	
	  	params["orderId"] = StringOps.static.strFromBytes(payloadBytes, 0, 16);	
	  	params["bootingMethod"] = IntOps.static.intFromBytes(payloadBytes, 16, 1);
	  	params["uid"] = StringOps.static.strFromBytes(payloadBytes, 17, 32);	// 16 or 32？
	  	
	  	payloadObj["params"] = params;
	  	payloadObj["method"] = "thing.service.request";
	  	
	  	break;
	  default:
	  	throw new Error("无效的消息类型");
	}
	
	return payloadObj;
}

function getMsgName(msgType) {
	let msgName ="";
	
	switch (msgType) {
		case 0x00:
			msgName = "request_secret";
			break;
		case 0x01:
			msgName = "auth";
			break;
		case 0x82:
			msgName = "booking_reply";
			break;
		case 0x83:
			msgName = "cancel_booking_reply";
			break;
	  case 0x04:
	  	msgName = "start";
	  	break;
	  case 0x05:
	  	msgName = "end";
	  	break;
	  case 0x86:
	  	msgName = "start_reply";
			break;
	  case 0x87:
	  	msgName = "end_reply";
			break;
	  case 0x08:
	  	msgName = "charging_details";
	    break;
	  case 0x09:
	  	msgName = "charging_metric";
	  	break;
	  case 0x0A:
	  	msgName = "bms_details";	  	
	  	break;
	  case 0x8B:
	  	msgName = "set_policy_reply";
			break;
	  case 0x0C:
	  	msgName = "heartbeat";
	  	break;
	  case 0x0D:
	  	msgName = "alarm";
	  	break;
	  case 0x8E:
	  	msgName = "correct_time_reply";
			break;
	  case 0x9A:
	  	msgName = "whitelist_reply";
			break;
	  case 0x9B:
	  	msgName = "blacklist_reply";
			break;
	  case 0x1C:
	  	msgName = "qrcode";
	  	break;
	  case 0x9D:
	  	msgName = "adjust_power_reply";
			break;
	  case 0x9E:
	  	msgName = "ota_reply";
			break;
	  case 0x1F:
	  	msgName = "ota_package";
	  	break;
	  case 0x20:
	  	msgName = "ota_result";
	  	break;
	  case 0xA1:
	  	msgName = "ctrl_reply";
			break;
	  case 0xA2:
	  	msgName = "set_domain_reply";
			break;
	  case 0x24:
	  	msgName = "device_state";
	  	break;
	  case 0x26:
	  	msgName = "query_balance";
	  	break;
	  default:
	  	throw new Error("无效的消息类型");
	}
	
	return msgName;
}

this.decode = function (bytes) {	
	if (bytes[0] != 0x68) {
		return ;
	}
	
	let msgType = IntOps.static.intFromBytes(bytes, 1, 1);
	let packetId = IntOps.static.intFromBytes(bytes, 2, 2);
	let deviceSnLen = IntOps.static.intFromBytes(bytes, 4, 1);
	let deviceSn = StringOps.static.strFromBytes(bytes, 5, deviceSnLen);	
	let encryptType = IntOps.static.intFromBytes(bytes, 5 + deviceSnLen, 1);
	let payloadLen = IntOps.static.intFromBytes(bytes, 6 + deviceSnLen, 2);
	
	let theHeader = {};
	theHeader["clientId"] = deviceSn;
	theHeader["packetId"] = packetId;
	theHeader["msgType"] = msgType;
	theHeader["msgName"] = getMsgName(msgType);
	theHeader["version"] = "1.0";
	
	let props = {};
	props["encryptType"] = encryptType;
	props["isSubpackage"] = false;
	props["totalPackages"] = 1;
	props["packageSeq"] = 1;
	theHeader["props"] = props;
	
	let payloadBytes = ByteOps.static.slice(bytes, 8 + deviceSnLen, bytes.length - (8 + deviceSnLen) - 2);
	let thePayload = parsePayload(msgType, payloadBytes);
	thePayload["id"] = packetId + "";	

	let crcBytes = ByteOps.static.slice(bytes, bytes.length - 2);	
	// TODO CRC校验
	
	let msgObj = {};
	msgObj["header"] = theHeader;	
	msgObj["payload"] = thePayload;
	
	return msgObj;
};

this.encode = function (msg) {
	print("msg===================" + msg);	
		
	let payloadBytes = [];	
	let msgType = msg.getHeader().getMsgType();
	let resultCode = msg.getPayload().getIntValue("code");
	let dataObj = msg.getPayload().getJSONObject("data");
	
	switch (msgType) {
		case 0x00:
			msgType = 0x80;
			payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("len")));			
			payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("secret")));
			
			break;
		case 0x01:
			msgType = 0x81;
			payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("result")));
			
			break;
	  case 0x02:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("connectorNo")));
	  	payloadBytes.push(...IntOps.static.intTo2Bytes(dataObj.getIntValue("duration")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("bookingAmount")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));
	  	
	  	break;
	  case 0x03:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("connectorNo")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));
	  	
	  	break;
	  case 0x84:
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("uid")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("balance")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));	  		  	
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("result")));
	  	payloadBytes.push(...BcdOps.static.strToBcd(dataObj.getString("stopCode")));
	  	
	    break;
	  case 0x85:
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));	  		  	
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("result")));
	  	
	  	break;
	  case 0x06:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("connectorNo")));	  	
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("policyType")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("policyData")));
	  	payloadBytes.push(...BcdOps.static.strToBcd(dataObj.getString("stopCode")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("balance")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("maxPower")));
	  	
	  	break;
	  case 0x07:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("connectorNo")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));
	  	
	  	break;
	  case 0x88:
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));	  	
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("result")));
	  	
	  	break;
	  case 0x8A:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("connectorNo")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));
	  	
	  	break;
	  case 0x0B:
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("policyId")));
	  	payloadBytes.push(...pIntOps.static.intTo4Bytes(dataObj.getIntValue("policyVer")));
	  	payloadBytes.push(...BcdOps.static.strToBcd(dataObj.getString("start")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("bookingPrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("parkingPrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("peakElecPrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("highElecPrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("normalElecPrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("lowElecPrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("peakServicePrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("highServicePrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("normalServicePrice")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("lowServicePrice")));
	  	
	  	for(let i = 0;i<msg["spans"].length;i++){
	  		payloadBytes.push(IntOps.static.intToByte(dataObj.getJSONArray().get(i)));
	  	}
	  	
	  	break;
	  case 0x8C:
	  	payloadBytes.push(...BcdOps.static.strToBcd(dataObj.getString("time")));
	  	
	  	break;
	  case 0x8D:
	  	break;
	  case 0x0E:
	  	payloadBytes.push(...BcdOps.static.strToBcd(dataObj.getString("time")));
	  	
	  	break;
	  case 0x1A:
	  	// TODO
	  	
	  	break;
	  case 0x1B:
	  	// TODO
	  	
	  	break;
	  case 0x9C:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("connectorNo")));
	  	payloadBytes.push(...IntOps.static.intTo2Bytes(dataObj.getIntValue("conentLen")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("content")));
	  	
	  	break;
	  case 0x1D:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("connectorNo")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("maxPower")));
	  	
	  	break;
	  case 0x1E:
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("softwareVer")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("protocolVer")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("fileMd5")));
	  	payloadBytes.push(...IntOps.static.intTo2Bytes(dataObj.getIntValue("fileUrlLen")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("fileUrl")));	  	
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("userNameLen")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("userName")));	  	
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("passwordLen")));
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("password")));
	  	
	  	break;
	  case 0x9F:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("result")));
	  	
	  	break;
	  case 0xA0:
	  	break;
	  case 0x21:
	  	payloadBytes.push(IntOps.static.intToByte(dataObj.getIntValue("ctrlTypeId")));
	  	payloadBytes.push(...[0x00, 0x00, 0x00, 0x00]); // reserved bytes
	  	
	  	break;
	  case 0x22:
	  	let ips = dataObj.getString("host").split(".");
	  	for(let i = 0;i<ips.length;i++){
	  		payloadBytes.push(IntOps.static.intToByte(ips[i]));
	  	}
	  	
	  	payloadBytes.push(...IntOps.static.intTo2Bytes(dataObj.getIntValue("port")));
	  	
	  	break;
	  case 0xA4:
	  	break;
	  case 0x25:	  	
	  	break;
	  case 0xA6:
	  	payloadBytes.push(...StringOps.static.strToBytes(dataObj.getString("orderId")));
	  	payloadBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("balance")));
	  	
	  	break;
	  default:
	  	throw new Error("无效的消息类型");
	}
	
	let msgBytes = [];
	msgBytes.push(0x68);
	msgBytes.push(IntOps.static.intToByte(msgType));
	msgBytes.push(...IntOps.static.intTo2Bytes(msg.getHeader().getPacketId()));
	
	let deviceSnBytes = StringOps.static.strToBytes(msg.getHeader().getDeviceSn())
	msgBytes.push(IntOps.static.intToByte(deviceSnBytes.length));
	msgBytes.push(...deviceSnBytes);
	
	msgBytes.push(IntOps.static.intToByte(msg.getHeader().getEncryptType()));
	msgBytes.push(IntOps.static.intTo2Bytes(payloadBytes.length));
	if(payloadBytes.length > 0){
		msgBytes.push(...payloadBytes);
	}
	
	return msgBytes;
};
