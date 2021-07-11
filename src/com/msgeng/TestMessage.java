package com.msgeng;

import com.msgeng.core.Message;

public class TestMessage extends Message {

	public TestMessage(int senderID) {
		super(senderID, "Test Msg: " + senderID);
	}

}	
