package com.github.ruifengho.netty.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;

import io.netty.channel.ChannelHandlerContext;

public class ChannelManager {

	private static Queue<ChannelHandlerContext> channelList = new ConcurrentLinkedQueue<>();

	private static Map<String, List<ChannelHandlerContext>> map = new ConcurrentHashMap<>();

	private static class ChannelManagerHolder {
		static ChannelManager HOLDER = new ChannelManager();
	}

	private ChannelManager() {

	}

	public boolean hasGroup(String groupId) {
		return map.containsKey(groupId);
	}

	public void clearNoActiveChannel() {
		Iterator<ChannelHandlerContext> iterator = channelList.iterator();
		while (iterator.hasNext()) {
			ChannelHandlerContext context = iterator.next();
			if (context.isRemoved()) {
				iterator.remove();
			}
		}

	}

	public void group(String groupId, ChannelHandlerContext ctx) {
		if (StringUtils.isNotBlank(groupId)) {
			if (!map.containsKey(groupId)) {
				map.put(groupId, new ArrayList<ChannelHandlerContext>());
			}
			if (!map.get(groupId).contains(ctx)) {
				map.get(groupId).add(ctx);
			}
		}
	}

	public List<ChannelHandlerContext> getGroup(String groupId) {
		if (StringUtils.isNotBlank(groupId)) {
			List<ChannelHandlerContext> list = map.get(groupId);
			Iterator<ChannelHandlerContext> iterator = list.iterator();
			while (iterator.hasNext()) {
				ChannelHandlerContext context = iterator.next();
				if (context.isRemoved()) {
					iterator.remove();
				}
			}
			return list;
		}
		return null;
	}

	public static ChannelManager getInstance() {
		return ChannelManagerHolder.HOLDER;
	}

	public void remove(ChannelHandlerContext channel) {
		channelList.remove(channel);
	}

	public ChannelHandlerContext put(ChannelHandlerContext channel) {
		try {
			channelList.add(channel);
		} catch (IllegalStateException e) {
			channelList.remove();
			put(channel);
		}
		return channel;
	}

}