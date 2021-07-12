package com.msgeng.message;

import com.msgeng.utils.DataObject;
import com.msgeng.utils.Utils;

public class Message {
	private final String messageTag;
    private final int senderID;
    private final int id;
    private final DataObject data;
    
    private Message(int senderID, String messageTag, DataObject data, int id, boolean exactMessageTag) {
        this.messageTag = exactMessageTag ? messageTag : String.format("[%s]%s", senderID, messageTag).toLowerCase();
        this.senderID = senderID;
        this.data = data;
        this.id = id;
    }
    
    public Message(int senderID, String messageTag, DataObject data) {
        this(senderID, messageTag, data, Utils.getId(), false);
    }

    public Message(Message m, DataObject data) {
        this(m.getSenderId(), m.getMessageTag(), data, m.getId(), true);
    }
    
    public Message(Message m) {
        this(m.getSenderId(), m.getMessageTag(), m.getData(), m.getId(), true);
    }
    
    public Message(Message m, String[] specialTags) {
        this(m.getSenderId(), translateTags(specialTags) + m.getMessageTag(), m.getData(), m.getId(), false);
    }

    public static String translateTags(String[] specialTags) {
    	String s = "[";
    	for(int i = 0 ; i < specialTags.length; i++) {
    		s += specialTags[i].toLowerCase() + "]["; 
    	}
    	return s.substring(0, s.length() - 1);
    }
    
    public final int getSenderId() {
        return senderID;
    }

    public final String getMessageTag() {
        return messageTag;
    }
    
    public boolean containsTag(String tag) {
    	return messageTag.contains(String.format("[%s]", tag.toLowerCase()));
    }
    
    public int getId() {
    	return id;
    }
    
    public DataObject getData() {
    	return data;
    }
    
    @Override
    public String toString() {
    	return String.format("Message \"%s\" carries \"%s\"", messageTag.replaceAll("\\[.*\\]", ""), data);
    }
}
