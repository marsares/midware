package com.alibaba.middleware.race.mom.async;

import com.alibaba.middleware.race.mom.model.ConsumeResult;
import com.alibaba.middleware.race.mom.model.Message;

public interface MessageListener {
	/**
	 * 
	 * @param message
	 * @return
	 */
	ConsumeResult onMessage(Message message);
}
