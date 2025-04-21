this.decode = function (bytes) {
	//解析第一个包获得设备的SN
	let result = {};
	
	if (bytes[0] == 0x68) {
		let deviceSnLen = IntOps.static.intFromBytes(bytes, 4, 1);
		let deviceSn = StringOps.static.strFromBytes(bytes, 5, deviceSnLen);
		result['deviceSn'] = deviceSn;
	}
	
	return result;
}
