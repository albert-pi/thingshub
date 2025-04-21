package io.thingshub.service;

import com.google.common.collect.Lists;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.sql.Condition;
import io.thingshub.domain.Session;
import io.thingshub.service.base.BaseService;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 客户端/设备会话管理服务
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class SessionService extends BaseService<Session> {

	enum SessionState {
		EXPIRED(-1), OFFLINE(0), ONLINE(1);

		@Getter
		@Accessors(fluent = true)
		private int state;

		SessionState(int state) {
			this.state = state;
		}
	}

	public void create(String clientId, int keepAlive, int sessionExpiryInterval) {
		Session session = new Session();
		session.setClientId(clientId);
		session.setKeepAlive(keepAlive);
		session.setSessionExpiryInterval(sessionExpiryInterval);
		session.setLastActiveTime(DateUtil.date());
		session.setStatus(SessionState.ONLINE.state());

		this.save(session);
	}

	public Session get(String clientId) {
		return this.getOne(Lists.newArrayList(new Condition("client_id", clientId)));
	}

	public void offline(String clientId) {
		Session session = this.getOne(Lists.newArrayList(new Condition("client_id", clientId)));
		if (session != null) {
			session.setStatus(SessionState.OFFLINE.state());

			this.updateById(session);
		}
	}

	public void expire(String clientId) {
		this.remove(Lists.newArrayList(new Condition("client_id", clientId)));
	}

}
