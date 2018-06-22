package com.github.ruifengho.netty.handler;

import static com.github.ruifengho.DspConstants.ACTION_HEART;
import static com.github.ruifengho.DspConstants.ACTION_UPLOAD_CLIENT_MSG;
import static com.github.ruifengho.DspConstants.MSG_TYPE_CLIENT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.ruifengho.modal.DspAction;
import com.github.ruifengho.netty.NettyControlService;
import com.github.ruifengho.util.SocketUtils;
import com.github.ruifengho.utils.SocketManager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class DspClientConnectHandler extends ChannelInboundHandlerAdapter {

	private static String HEART_JSON = JSON.toJSONString(new DspAction(MSG_TYPE_CLIENT, ACTION_HEART, null, "K"));

	private static final Logger log = LoggerFactory.getLogger(DspClientConnectHandler.class);

	private NettyControlService nettyControlService;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		SocketManager.getInstance().setCtx(ctx);

		SocketUtils.sendMsg(ctx, HEART_JSON);

		// 上传模块信息
		SocketUtils.sendMsg(ctx,
				JSON.toJSONString(new DspAction(MSG_TYPE_CLIENT, ACTION_UPLOAD_CLIENT_MSG, null, "client msg")));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		SocketManager.getInstance().setConnected(false);
		nettyControlService.restart();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		String json = SocketUtils.getJson(msg);

		log.debug(json);

		nettyControlService.process(ctx, json);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {

			} else if (event.state() == IdleState.WRITER_IDLE) {
				// 多久没发
				SocketUtils.sendMsg(ctx, HEART_JSON);
			} else if (event.state() == IdleState.ALL_IDLE) {
				// 没发没收
			}
		}
	}

}
