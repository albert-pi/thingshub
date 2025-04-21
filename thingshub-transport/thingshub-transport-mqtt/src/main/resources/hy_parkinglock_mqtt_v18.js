this.decode = function (bytes) {
	let jsonString = Array.from(bytes).map(byte => String.fromCharCode(byte)).join('');
	let rawPayload = JSON.parse(jsonString);
	
	let stdPayload = {};
	
	switch (rawPayload.method) {
		case 'startup':
			let params = {};
			
			params['iccid'] = rawPayload.content.ICCID;
			params['btName'] = rawPayload.content.btName;
			params['hvr'] = rawPayload.content.hvr;
			params['bvr'] = rawPayload.content.bvr;
			params['avr'] = rawPayload.content.avr;
			params['pvr'] = rawPayload.content.pvr;
			
			let latlng = rawPayload.content.GPS.split(',');
			params['lat'] = latlng[0];
			params['lng'] = latlng[1];
			
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.event.startup.post';
			stdPayload['params'] = params;			
			
			break;
		case 'status':
			let params = {};
			
	  	params['carState'] = rawPayload.content.carState;
	  	params['waterState'] = rawPayload.content.waterState;
	  	params['lockState'] = rawPayload.content.lockState;	  	
	  	params['lockAngle'] = rawPayload.content.lockAngle;	  	
	  	params['lockSource'] = rawPayload.content.lockSource;	  	
	  	params['motorWI'] = rawPayload.content.motorWI;	  	
	  	params['batV'] = rawPayload.content.batV;	  	
	  	params['batSOC'] = rawPayload.content.batSOC;	  	
	  	params['chargeState'] = rawPayload.content.chargeState;	  	
	  	params['vinV'] = rawPayload.content.vinV;	  	
	  	params['runTime'] = rawPayload.content.runTime;	  	
	  	params['btState'] = rawPayload.content.btState;
	  	params['rssi'] = rawPayload.content.rssi;
	  	params['parkNumber'] = rawPayload.content.pastdPayloadr;
	  	
	  	stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.property.status.post';
			stdPayload['params'] = params;	  	
	  	
	  	break;
		case 'reboot_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.reboot.call_reply';
			stdPayload['code'] = rawPayload.code;
			stdPayload['message'] = 'success';//TODO 根据协议规范中code值设置message 	
	  	
	  	break;
		case 'calibSensor_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.calibsensor.call_reply';
			stdPayload['code'] = rawPayload.code;
			stdPayload['message'] = 'success';//TODO	  	
	  	
	  	break;
		case 'config_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.config.call_reply';
			stdPayload['code'] = rawPayload.code;
			stdPayload['message'] = 'success';//TODO
	  	
	  	if(rawPayload.content){
	  		let theData = {};
	  		
	  		if(rawPayload.content.magnetTrigger){
	  			theData['magnetTrigger'] = rawPayload.content.magnetTrigger;
	  		}
	  		if(rawPayload.content.resistance_value){
	  			theData['resistanceValue'] = rawPayload.content.resistance_value;
	  		}
	  		if(rawPayload.content.resistance_time){
	  			theData['resistanceTime'] = rawPayload.content.resistance_time;
	  		}
	  		if(rawPayload.content.volume){
	  			theData['volume'] = rawPayload.content.volume;
	  		}
	  		if(rawPayload.content.btName){
	  			theData['btName'] = rawPayload.content.btName;
	  		}
	  		if(rawPayload.content.btPwd){
	  			theData['btPwd'] = rawPayload.content.btPwd;
	  		}
	  		if(rawPayload.content.close_critical_angle){
	  			theData['closeCriticalAngle'] = rawPayload.content.close_critical_angle;
	  		}
	  		if(rawPayload.content.open_critical_angle){
	  			theData['openCriticalAngle'] = rawPayload.content.open_critical_angle;
	  		}
	  		if(rawPayload.content.close_angle_max){
	  			theData['closeAngleMax'] = rawPayload.content.close_angle_max;
	  		}
	  		if(rawPayload.content.reback_angle){
	  			theData['rebackAngle'] = rawPayload.content.reback_angle;
	  		}
	  		if(rawPayload.content.radarTrigger1){
	  			theData['radarTrigger1'] = rawPayload.content.radarTrigger1;
	  		}
	  		if(rawPayload.content.radarTrigger2){
	  			theData['radarTrigger2'] = rawPayload.content.radarTrigger2;
	  		}
	  		if(rawPayload.content.ocp_time){
	  			theData['ocpTime'] = rawPayload.content.ocp_time;
	  		}
	  		if(rawPayload.content.light_en){
	  			theData['lightEn'] = rawPayload.content.light_en;
	  		}
	  		if(rawPayload.content.HBI_en){
	  			theData['HBIEn'] = rawPayload.content.HBI_en;
	  		}
	  		if(rawPayload.content.mq_host_name){
	  			theData['mqHostName'] = rawPayload.content.mq_host_name;
	  		}
	  		if(rawPayload.content.mq_port){
	  			theData['mqPort'] = rawPayload.content.mq_port;
	  		}
	  		if(rawPayload.content.mq_user){
	  			theData['mqUser'] = rawPayload.content.mq_user;
	  		}
	  		if(rawPayload.content.mq_passwd){
	  			theData['mqPassword'] = rawPayload.content.mq_passwd;
	  		}
	  		if(rawPayload.content.lock_mode){
	  			theData['lockMode'] = rawPayload.content.lock_mode;
	  		}
	  		
	  		stdPayload['data'] = theData;
	  	}
	  	
	  	break;
		case 'update_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.update.call_reply';
			stdPayload['code'] = rawPayload.code;
			stdPayload['message'] = 'success';//TODO 
	  	
	  	break;
		case 'openLock_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.openlock.call_reply';
			stdPayload['code'] = rawPayload.code;
			stdPayload['message'] = 'success';//TODO 			
	  	
	  	break;
		case 'closeLock_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.closelock.call_reply';
	  	stdPayload['code'] = rawPayload.code;
	  	stdPayload['message'] = 'success';//TODO 	  	
	  	
	  	break;
		case 'motorLearn_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.motorlearn.call_reply';
	  	stdPayload['code'] = rawPayload.code;
	  	stdPayload['message'] = 'success';//TODO
	  	
	  	if(rawPayload.content){
	  		let theData = {};
	  		
	  		if(rawPayload.content.resistance_current){
	  			theData['resistanceCurrent'] = rawPayload.content.resistance_current;
	  		}
	  		if(rawPayload.content.angle_ref){
	  			theData['angleRef'] = rawPayload.content.angle_ref;
	  		}
	  		if(rawPayload.content.time_max){
	  			theData['timeMax'] = rawPayload.content.time_max;
	  		}
	  		
	  		stdPayload['data'] = theData;
	  	}
	  	
	  	break;
		case 'factory_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.factory.call_reply';
	  	stdPayload['code'] = rawPayload.code;
	  	stdPayload['message'] = 'success';//TODO 	  	
	  	
	  	break;
		case 'tts_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.tts.call_reply';
	  	stdPayload['code'] = rawPayload.code;
	  	stdPayload['message'] = 'success';//TODO 	  	
	  	
	  	break;
		case 'closeLockPlan_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.closelockplan.call_reply';
	  	stdPayload['code'] = rawPayload.code;
	  	stdPayload['message'] = 'success';//TODO 	  	
	  	
	  	break;
		case 'setSysMode_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.setsysmode.call_reply';
	  	stdPayload['code'] = rawPayload.code;
	  	stdPayload['message'] = 'success';//TODO 	  	
	  	
	  	break;
		case 'device_info_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.deviceinfo.call_reply';
	  	stdPayload['code'] = rawPayload.code;
	  	stdPayload['message'] = 'success';//TODO 
	  	
			if(rawPayload.content){
				let theData = {};
				
	  		if(rawPayload.content.hvr){
	  			theData['hvr'] = rawPayload.content.hvr;
	  		}
	  		if(rawPayload.content.bvr){
	  			theData['bvr'] = rawPayload.content.bvr;
	  		}
	  		if(rawPayload.content.avr){
	  			theData['avr'] = rawPayload.content.avr;
	  		}
	  		if(rawPayload.content.pvr){
	  			theData['pvr'] = rawPayload.content.pvr;
	  		}
	  		if(rawPayload.content.vs_inf){
	  			theData['vsInf'] = rawPayload.content.vs_inf;
	  		}
	  		if(rawPayload.content.ICCID){
	  			theData['iccid'] = rawPayload.content.ICCID;
	  		}
	  		if(rawPayload.content.sim){
	  			theData['sim'] = rawPayload.content.sim;
	  		}
	  		if(rawPayload.content.OPER){
	  			theData['oper'] = rawPayload.content.OPER;
	  		}
	  		
	  		stdPayload['data'] = theData;
	  	}
	  	
	  	break;
		case 'selfCheck_ack':	  	
			stdPayload['id'] = rawPayload.uuid; 
			stdPayload['version'] = '1.0';
			stdPayload['method'] = 'thing.service.selfcheck.call_reply';
	  	stdPayload['code'] = rawPayload.code;
	  	stdPayload['message'] = 'success';//TODO 	  	
	  	
	  	break;
	  default:
	  	throw new Error('无效的消息类型');
	}	
	
	return stdPayload;
};

this.lastWill = function (bytes) {
	let jsonString = Array.from(bytes).map(byte => String.fromCharCode(byte)).join('');
	let theLastWillPayload = JSON.parse(jsonString);
	
	let params = {}, stdPayload = {};
	
	params['iccid'] = theLastWillPayload.ICCID;
	params['btMac'] = theLastWillPayload.btMAC;
	params['online'] = theLastWillPayload.online;
	params['sim'] = theLastWillPayload.sim;
	params['oper'] = theLastWillPayload.OPER;
	
	stdPayload['id'] = ''; 
	stdPayload['version'] = '1.0';
	stdPayload['method'] = 'thing.event.lastwill.post';
	stdPayload['params'] = params;
	
	return stdPayload;
};

this.encode = function (payload) {	
	let result = {};
	
	switch (payload.method) {
		case 'thing.event.startup.post_reply':
			
			break;
		case 'thing.property.status.post_reply':
	  	
	  	break;
		case 'thing.service.reboot.call':
	  	
	  	break;
		case 'thing.service.calibsensor.call':
	  	
	  	break;
		case 'thing.service.config.call':
	  	
	  	break;
		case 'thing.service.update.call':
	  	
	  	break;
		case 'thing.service.openlock.call':
	  	
	  	break;
		case 'thing.service.closelock.call':
	  	
	  	break;
		case 'thing.service.motorlearn.call':
	  	
	  	break;
		case 'thing.service.factory.call':
	  	
	  	break;
		case 'thing.service.tts.call':
	  	
	  	break;
		case 'thing.service.closelockplan.call':
	  	
	  	break;
		case 'thing.property.sysmode.set':
	  	
	  	break;
		case 'thing.service.deviceinfo.call':	
	  	
	  	break;
		case 'thing.service.selfcheck.call':
	  	
	  	break;
	  default:
	  	throw new Error('无效的消息类型');
	}
	
	return result;
};
