this.decode = function (bytes) {	
	let stdPayload = {};
	
	switch (msgType) {
		case 0x01:
			let params = {};
			
			params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
			params["equipType"] = IntOps.static.intFromBytes(payloadBytes, 1, 4);
			params["connectors"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
			params["protocolVer"] = IntOps.static.intFromBytes(payloadBytes, 6, 4);
			params["appVer"] = IntOps.static.intFromBytes(payloadBytes, 10, 4);
			params["linkMode"] = IntOps.static.intFromBytes(payloadBytes, 14, 4);	    
			params["sim"] = BcdOps.static.bcd2Str(payloadBytes, 18, 3);
			params["telecom"] = StringOps.static.strFromBytes(payloadBytes, 21, 16);
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.auth.request";
			stdPayload["params"] = params;			
			
			break;
		case 0x03:
			params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
			params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
			params["state"] = IntOps.static.intFromBytes(payloadBytes, 6, 4);
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.event.heartbeat.post";
			stdPayload["params"] = params;	
			
			break;
		case 0x05:
			params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
			params["billingStrategyId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.checkStrategy.request";
			stdPayload["params"] = params;	
			
			break;
	  case 0x09:
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.getStrategy.request";
			stdPayload["params"] = params;	
			
	  	break;
	  case 0x13:
	  	params["transactionId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["status"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["resetFlag"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["plugFlag"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["voltage"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["current"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["wireTemp"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["wireCode"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["soc"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["maxBatteryTemp"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["chargingDuration"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["remaining"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["power"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["wastedPower"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["amount"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["faultCode"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.property.chargingMetrics.post";
			stdPayload["params"] = params;
			
			break;
	  case 0x15:
	  	params["transactionId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.event.handshake.post";
			stdPayload["params"] = params;
			
			break;
	  case 0x17:
	  	params["transactionId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.event.initializing.post";
			stdPayload["params"] = params;
			
	    break;
	  case 0x19:
	  	params["transactionId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.event.endCharging.post";
			stdPayload["params"] = params;
	  				
	  	break;
	  case 0x1B:
	  	params["transactionId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.event.bmserror.post";
			stdPayload["params"] = params;
	  	
	  	break;
	  case 0x1D:	
	  	params["transactionId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.event.bmsinterrupted.post";
			stdPayload["params"] = params;
	  	
			break;
	  case 0x21:
	  	params["transactionId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.event.motorinterrupted.post";
			stdPayload["params"] = params;
	  	
	  	break;
	  case 0x23:	  	
	  	
	  	break;
	  case 0x25:
	  	params["transactionId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	params["connectorId"] = IntOps.static.intFromBytes(payloadBytes, 5, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.propery.bmsMetrics.post";
			stdPayload["params"] = params;
	  	
			break;
	  case 0x31:	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.startCharging.request";
			stdPayload["params"] = params;	
	  	
			break;
	  case 0x33:
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.startCharging.call_reply";
			stdPayload["params"] = params;	
	  	
			break;
	  case 0x35:	  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.shutdown.call_reply";
			stdPayload["params"] = params;
			
	  	break;
	  case 0x3B:	  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.property.transactions.post";
			stdPayload["params"] = params;
			
			break;
	  case 0x41:
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.updateBalance.call_reply";
			stdPayload["params"] = params;
	  	
			break;
	  case 0x43:	  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.syncCards.call_reply";
			stdPayload["params"] = params;
			
	  	break;
	  case 0x45:	  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.eraseCards.call_reply";
			stdPayload["params"] = params;
			
	  	break;
	  case 0x47: 	  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.queryCards.call_reply";
			stdPayload["params"] = params;
			
			break;
	  case 0x51:
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.property.workParametes.set_reply";
			stdPayload["params"] = params;
	  	
			break;
	  case 0x55: 	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.syncClock.call_reply";
			stdPayload["params"] = params;
			
	  	break;
	  case 0x57:  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.setStrategy.call_reply";
			stdPayload["params"] = params;
	  	
	  	break;
	  case 0x61:  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.property.parklockState.post";
			stdPayload["params"] = params;
	  	
	  	break;
	  case 0x63:  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.controlParklock.call_reply";
			stdPayload["params"] = params;
	  	
	  	break;
	  case 0x91:  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.reboot.call_reply";
			stdPayload["params"] = params;
	  	
	  	break;
	  case 0x93:  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.upgrade.call_reply";
			stdPayload["params"] = params;
	  	
	  	break;
	  case 0xA1:  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.launchMultiCharging.request";
			stdPayload["params"] = params;
	  	
	  	break;
	  case 0xA3:  	
	  	params["equipId"] = IntOps.static.intFromBytes(payloadBytes, 0, 1);
	  	//TODO ...
			
			stdPayload["id"] = ""; 
			stdPayload["version"] = "1.0";
			stdPayload["method"] = "thing.service.startMultiCharging.call_reply";
			stdPayload["params"] = params;
	  	
	  	break;
	  default:
	  	throw new Error("无效的消息类型");
	}
	
	return stdPayload;
};

this.encode = function (msg) {
	//print("msg===================" + msg);	
		
	let msgBytes = [];
	msgBytes.push(0x68);
	
	let theId = msg.getIntValue("id")
	let theMethod = msg.getString("method");
	let dataJSON = msg.getJSONObject("data");
	
	switch (theMethod) {
		case "thing.service.auth.request_reply":
			msgBytes.push(IntOps.static.intToByte(12));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
			msgBytes.push(0x02);
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(IntOps.static.intToByte(dataJSON.getIntValue("result")));
			
			break;
		case "thing.event.heartbeat.post_reply":
			msgBytes.push(IntOps.static.intToByte(13));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
			msgBytes.push(0x04);
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("connctorId")));
			msgBytes.push(0x00);
			
			break;
	  case "thing.service.checkStrategy.request_reply":
	  	msgBytes.push(IntOps.static.intToByte(14));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x06);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("billingStrategyId")));
			msgBytes.push(IntOps.static.intToByte(dataJSON.getIntValue("result")));
	  	
	  	break;
	  case "thing.service.getStrategy.request_reply":
	  	msgBytes.push(IntOps.static.intToByte(93));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x0A);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("billingStrategyId")));
			
			msgBytes.push(...IntOps.static.intTo4Bytes(dataObj.getIntValue("peakElecPrice")));
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("peakServicePrice")));			
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("highElecPrice")));
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("highServicePrice")));
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("normalElecPrice")));
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("normalServicePrice")));
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("lowElecPrice")));		
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("lowServicePrice")));
			msgBytes.push(IntOps.static.intToByte(dataJSON.getIntValue("wasterRatio")));
			
			let priceDetails = dataJSON.getJSONArray("priceDetails")
			for(let i = 0;i<priceDetails.size();i++){
				msgBytes.push(IntOps.static.intToByte(priceDetails.getJSONObject(i).getIntValue("priceType")));
			}
	  	
	  	break;
	  case "thing.service.getChargingMetric.call":
	  	msgBytes.push(IntOps.static.intToByte(12));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x12);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("connctorId")));	  	
	  	
	    break;
	  case "thing.service.startCharging.request_reply":
	  	msgBytes.push(IntOps.static.intToByte(42));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x32);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("transactionId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("connctorId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("cardCode")));
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("balance")));
			msgBytes.push(IntOps.static.intTo4Byte(dataJSON.getIntValue("result")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("reason")));	  	
	  	
	  	break;
	  case "thing.service.startCharging.call":
	  	msgBytes.push(IntOps.static.intToByte(48));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x34);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("transactionId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("connctorId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("cardCode")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("cardNo")));
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("balance")));
	  	
	  	break;
	  case "thing.service.shutdown.call":
	  	msgBytes.push(IntOps.static.intToByte(12));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x36);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("connctorId")));
	  	
	  	break;
	  case "thing.property.transactions.post_reply":
	  	msgBytes.push(IntOps.static.intToByte(21));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x40);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("transactionId")));
			msgBytes.push(IntOps.static.intToByte(dataJSON.getIntValue("result")));
	  	
	  	break;
	  case "thing.service.updateBalance.call":
	  	msgBytes.push(IntOps.static.intToByte(24));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x42);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("cardNo")));
			msgBytes.push(...IntOps.static.intTo4Bytes(dataJSON.getIntValue("balance")));
	  	
	  	break;
	  case "thing.service.syncCards.call":
	  	let cards = dataJSON.getJSONArray("cards");
	  	
	  	msgBytes.push(IntOps.static.intToByte(12 + cards.size() * 2 * 8));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x44);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
	  	msgBytes.push(IntOps.static.intToByte(cards.size()));
	  	
			for(let i = 0;i<cards.size();i+1){
				msgBytes.push(...BcdOps.static.strToBcd(cards.getJSONObject(i).getString("cardCode")));
				msgBytes.push(...LongOps.static.longTo8Bytes(cards.getJSONObject(i).getLongValue("cardNo")));
			}
	  	
	  	break;
	  case "thing.service.eraseCards.call":
	  	let cards = dataJSON.getJSONArray("cards");
	  	
	  	msgBytes.push(IntOps.static.intToByte(12 + cards.size() * 2 * 8));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x46);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
	  	msgBytes.push(IntOps.static.intToByte(cards.size()));
	  	
			for(let i = 0;i<cards.size();i+1){
				msgBytes.push(...BcdOps.static.strToBcd(cards.getJSONObject(i).getString("cardCode")));
				msgBytes.push(...LongOps.static.longTo8Bytes(cards.getJSONObject(i).getLongValue("cardNo")));
			}
	  	
	  	break;
	  case "thing.service.queryCards.call":
	  	let cards = dataJSON.getJSONArray("cards");
	  	
	  	msgBytes.push(IntOps.static.intToByte(12 + cards.size() * 8));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x48);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
	  	msgBytes.push(IntOps.static.intToByte(cards.size()));
	  	
			for(let i = 0;i<cards.size();i+1){
				msgBytes.push(...LongOps.static.longTo8Bytes(cards.getJSONObject(i).getLongValue("cardNo")));
			}
	  	
	  	break;
	  case "thing.property.workParameters.set":
	  	msgBytes.push(IntOps.static.intToByte(13));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x52);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(IntOps.static.intToByte(dataJSON.getIntValue("status")));
			msgBytes.push(IntOps.static.intToByte(dataJSON.getIntValue("maxPower")));
	  	
	  	break;
	  case "thing.service.syncClock.call":
	  	msgBytes.push(IntOps.static.intToByte(18));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x56);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(IntOps.static.intToByte(dataJSON.getIntValue("time")));//TODO 7个字节表示时间？？？
	  	
	  	break;
	  case "thing.service.setStrategy.call":
	  	msgBytes.push(0x58);
	  	
	  	break;
	  case "thing.service.controlParklock.call":
	  	msgBytes.push(IntOps.static.intToByte(17));
			msgBytes.push(...IntOps.static.intTo2Bytes(theId));
			msgBytes.push(0x00);
	  	msgBytes.push(0x62);
	  	msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("equipId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("connctorId")));
			msgBytes.push(...BcdOps.static.strToBcd(dataJSON.getString("cmd")));
			msgBytes.push(0x00);
			msgBytes.push(0x00);
			msgBytes.push(0x00);
			msgBytes.push(0x00);
	  	
	  	break;
	  case "thing.service.reboot.call":
	  	msgBytes.push(0x92);
	  	
	  	break;
	  case "thing.service.upgrade.call":
	  	msgBytes.push(0x94);
	  	
	  	break;
	  case "thing.service.launchMultiCharging.request_reply":
	  	msgBytes.push(0xA2);
	  	
	  	break;
	  case "thing.service.startMultiCharging.call":
	  	msgBytes.push(0xA4);
	  	
	  	break;
	  default:
	  	throw new Error("无效的消息类型");
	}
	
	//TODO CRC16校验
	
	return msgBytes;
};
