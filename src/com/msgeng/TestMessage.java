package com.msgeng;

import com.msgeng.message.Message;

public class TestMessage extends Message {

	public TestMessage(int senderID) {
		super(senderID, "This is test", null);
	}
	
	public TestMessage(int senderID, String[] specialTags) {
		super(senderID, translateTags(specialTags) + "This is test", null);
	}

}
