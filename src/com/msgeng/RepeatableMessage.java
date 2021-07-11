package com.msgeng;

import com.msgeng.core.Message;

public class RepeatableMessage extends Message {

	public RepeatableMessage(int senderID) {
		super(senderID, "[Repeat]Test Msg: " + senderID);
	}

}	