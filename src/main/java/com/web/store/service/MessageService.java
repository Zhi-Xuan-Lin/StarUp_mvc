package com.web.store.service;

import java.util.List;

import com.web.store.model.MessageBean;

public interface MessageService {
	
	int insertMessage(MessageBean mb);
	
	List<MessageBean> getReceivedMessages(Integer toId);
	
	List<MessageBean> getDeliveredMessages(Integer fromId);
	
	List<MessageBean> getUnreadMessages(Integer toId);
	
	int changeUnreadMessageToRead(Integer user_id);
}
