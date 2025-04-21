package io.thingshub.transport;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageRetryTimer extends HashedWheelTimer {

	public static class RetryTimerTask implements TimerTask {

		private final AtomicInteger count = new AtomicInteger();
		private final ChannelHandlerContext ctx;
		private Object msg;
		private final int times;
		private final int period;

		@Setter
		private Timeout timeout;

		public RetryTimerTask(ChannelHandlerContext ctx, Object msg, int times, int period) {
			this.ctx = ctx;
			this.msg = msg;
			this.times = times;
			this.period = period;
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			if (!timeout.isCancelled() && count.get() < times) {
				int retriedTimes = count.incrementAndGet();
				ctx.writeAndFlush(msg).addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							log.info("Server tried {} times to send the message successfully: {}", retriedTimes, msg);
							timeout.cancel();
						} else {
							log.error("Server failed to retry sending. retry times: {}, message: {}", retriedTimes, msg);
							if (future.cause() != null) {
								log.error("", future.cause());
							}
						}
					}

				});
				this.timeout = timeout.timer().newTimeout(this, period, TimeUnit.SECONDS);
			} else {
				msg = null;
			}
		}

		public void cancel() {
			Optional.ofNullable(timeout).ifPresent(tm -> {
				if (!tm.isExpired()) {
					tm.cancel();
				}
			});
		}
	};

	private final Map<String, Map<Integer, RetryTimerTask>> retryMap = new ConcurrentHashMap<>();

	public MessageRetryTimer() {
		super(20, TimeUnit.MILLISECONDS, 512);
	}

	public void doRetry(ChannelHandlerContext ctx, int packetId, Object msg, int times, int period) {
		// TODO 任务串行执行，任务执行时间过长可能会导致任务堆积？？？参考Kafka 时间轮？？？
		RetryTimerTask retryTimerTask = new RetryTimerTask(ctx, msg, times, period);
		retryTimerTask.setTimeout(this.newTimeout(retryTimerTask, period, TimeUnit.SECONDS));
		Map<Integer, RetryTimerTask> retryTaskMap = retryMap.computeIfAbsent(ctx.channel().id().asLongText(), channelId -> new ConcurrentHashMap<>());
		retryTaskMap.put(packetId, retryTimerTask);
	}

	public void cancel(String channelId, int packetId) {
		Optional.ofNullable(retryMap.get(channelId)).flatMap(taskMap -> Optional.ofNullable(taskMap.remove(packetId))).ifPresent(RetryTimerTask::cancel);
	}

	public void clean(String channelId) {
		Optional.ofNullable(retryMap.remove(channelId)).ifPresent(taskMap -> taskMap.values().forEach(RetryTimerTask::cancel));
	}

	@PreDestroy
	public void close() {
		this.stop();
	}

}
