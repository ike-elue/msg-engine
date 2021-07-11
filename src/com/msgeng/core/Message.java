package com.msgeng.core;

public abstract class Message {
	private final String messageTag;
    private final int senderID;
    
    public Message(int senderID, String messageTag) {
        this.messageTag = messageTag;
        this.senderID = senderID;
    }

    public Message(Message m) {
        this(m.getSenderId(), m.getMessageTag());
    }

    public final int getSenderId() {
        return senderID;
    }

    public final String getMessageTag() {
        return messageTag;
    }
    
    public boolean containsTag(String tag) {
    	return messageTag.contains(String.format("[%s]", tag));
    }
    
    @Override
    public String toString() {
    	return String.format("Message \"%s\" carries \"%s\"", messageTag.replaceAll("\\[.*\\]", ""), null);
    }
}
