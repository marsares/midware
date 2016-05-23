package com.alibaba.middleware.race.mom.async;

import com.alibaba.middleware.race.mom.model.SendResult;

public interface SendCallback {
	void onResult(SendResult result);
}
