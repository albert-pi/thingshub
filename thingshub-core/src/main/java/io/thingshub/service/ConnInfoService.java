package io.thingshub.service;

import com.google.common.collect.Lists;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.sql.Condition;
import io.thingshub.Broker;
import io.thingshub.domain.ConnInfo;
import io.thingshub.service.base.BaseService;

/**
 * <p>
 * 连接信息服务
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ConnInfoService extends BaseService<ConnInfo> {

	public void reg(String tenantId, String clientId, String channelId, String clientAddr, String transport) {
		ConnInfo connInfo = new ConnInfo();
		connInfo.setTenantId(tenantId);
		connInfo.setClientId(clientId);
		connInfo.setChannelId(channelId);
		connInfo.setClientAddr(clientAddr);
		connInfo.setConnectTime(DateUtil.date());
		connInfo.setTransport(transport);
		connInfo.setServerAddr(Broker.currentNode);

		this.save(connInfo);
	}

	public void unreg(String clientId) {
		this.remove(Lists.newArrayList(new Condition("client_id", clientId)));
	}

	public ConnInfo get(String clientId) {
		return this.getOne(Lists.newArrayList(new Condition("client_id", clientId)));
	}

}
