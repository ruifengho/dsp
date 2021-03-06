package com.github.ruifengho.netty.service.impl;

import org.springframework.stereotype.Service;

import com.github.ruifengho.DspConstants;
import com.github.ruifengho.modal.DspAction;
import com.github.ruifengho.netty.service.ActionService;
import com.github.ruifengho.tx.TxManagerPool;

import io.netty.channel.ChannelHandlerContext;

@Service(DspConstants.ACTION_UPLOAD_CLIENT_MSG)
public class ActionUploadClientInfoServiceImpl implements ActionService {

	@Override
	public String execute(ChannelHandlerContext ctx, DspAction action) {
		TxManagerPool.setState(action.getGroupId(), action.getState());
		return String.format("[%s] upload success", ctx.channel().remoteAddress().toString());
	}

}
