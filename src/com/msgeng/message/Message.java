package com.msgeng.message;

import com.msgeng.core.Core;
import com.msgeng.utils.DataObject;

public class Message {
	private final String messageTag;
	private final String alias;
    private final int senderID;
    private final DataObject data;
    private final long timestamp;
    
    private Message(int senderID, String messageTag, DataObject data, boolean exactMessageTag, String alias) {
        this.messageTag = exactMessageTag ? messageTag : String.format("[%s]%s", senderID, messageTag).toLowerCase();
        this.senderID = senderID;
        this.data = data;
        this.alias = alias;
        this.timestamp = System.nanoTime() / (long) Core.MILLISECOND;
    }
    
    public Message(int senderID, String messageTag, DataObject data) {
        this(senderID, messageTag, data, false, null);
    }
    
    public Message(int senderID, String messageTag, DataObject data, String alias) {
        this(senderID, messageTag, data, false, alias);
    }

    public Message(Message m, DataObject data) {
        this(m.getSenderId(), m.getMessageTag(), data, true, m.getAlias());
    }
    
    public Message(Message m) {
        this(m.getSenderId(), m.getMessageTag(), m.getData(), true, m.getAlias());
    }
    
    public Message(Message m, String msgTag) {
        this(m.getSenderId(), msgTag, m.getData(), true, m.getAlias());
    }
    
    public Message(Message m, String[] specialTags, String alias) {
        this(m.getSenderId(), translateTags(specialTags) + m.getMessageTag(), m.getData(), false, alias);
    }

    public static String translateTags(String[] specialTags) {
    	String s = "[";
    	for(int i = 0 ; i < specialTags.length; i++) {
    		s += specialTags[i].toLowerCase() + "]["; 
    	}
    	return s.substring(0, s.length() - 1);
    }
    
    public static Message replaceTags(Message msg, String[] tagsOld, String[] tagsNew) {
    	if(tagsOld.length != tagsNew.length) return msg; // Should prolly return an error
    	String msgTag = msg.getMessageTag();
    	for(int i = 0; i < tagsOld.length; i++) {
    		msgTag = msgTag.replace(String.format("[%s]", tagsOld[i]), String.format("[%s]", tagsNew[i]));
    	}
    	return new Message(msg, msgTag);
    } 
    
    public String getAlias() {
    	return alias;
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
    
    public DataObject getData() {
    	return data;
    }
    
    public long getTimestamp() {
    	return timestamp;
    }
    
    public long timeFromGameInit() {
    	return timestamp - Core.INIT_TIME;
    }
    
    @Override
    public String toString() {
    	return String.format("Message \"%s\" carries \"%s\", made %s ms in", messageTag.replaceAll("\\[.*\\]", ""), data, timeFromGameInit());
    }
}
