package com.github.ruifengho.netty.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.ruifengho.DspConstants;
import com.github.ruifengho.config.ConfigReader;
import com.github.ruifengho.modal.DspAction;
import com.github.ruifengho.netty.service.ActionService;

import io.netty.channel.ChannelHandlerContext;

@Service(DspConstants.ACTION_HEART)
public class ActionHeartServiceImpl implements ActionService {

	@Autowired
	private ConfigReader configReader;

	@Override
	public String execute(ChannelHandlerContext ctx, DspAction action) {
		return String.valueOf(configReader.getTransactionNettyDelaytime());
	}

}
