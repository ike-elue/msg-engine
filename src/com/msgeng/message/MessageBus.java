package com.msgeng.message;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageBus {
    private final MessageExecutor[] executors;
    
    private final ConcurrentLinkedQueue<Message> q1, q2;
    private volatile boolean using1;
    
    private final HashMap<Integer, Message> globals;

    private final ConcurrentLinkedQueue<Message> toRender;
    
    /**
     * TEMPORARY
     */
    public final ConcurrentLinkedQueue<Message> tempRequest;
    
    public MessageBus(int threadCount) {
    	q1 = new ConcurrentLinkedQueue<>();
    	q2 = new ConcurrentLinkedQueue<>();
    	
    	globals = new HashMap<>();
    	
    	toRender = new ConcurrentLinkedQueue<>();
    	
    	tempRequest = new ConcurrentLinkedQueue<>();
    	
        executors = new MessageExecutor[threadCount];
        for(int i = 0; i < threadCount; i++)
            executors[i] = new MessageExecutor(this, i);
        using1 = true;
    }
    
    public synchronized boolean isEmpty() {
        return getCurrentMessageList().isEmpty();
    }
    
    public void flipMessages() {
        using1 = !using1;
    }
    
    public synchronized Message receiveMessage() {
        return getCurrentMessageList().poll();
    }
    
    public synchronized void addRenderMsg(Message msg) {
        toRender.add(msg);
    }
    
    public Message getRenderMsg() {
    	return toRender.poll();
    }
    
    public synchronized void addMessage(Message message) {
       getFutureMessageList().add(message);
    }
    
    public ConcurrentLinkedQueue<Message> getFutureMessageList() {
        if(!using1)
            return q1;
        return q2;
    }
    
    public ConcurrentLinkedQueue<Message> getCurrentMessageList() {
        if(using1)
            return q1;
        return q2;
    }
    
    public MessageExecutor[] getExecutors() {
        return executors;
    }
    
    public void overwriteGlobal(Message msg) {
    	globals.put(msg.getId(), msg);
    }
    
    public void deleteGlobal(Message msg) {
    	globals.remove(msg.getId());
    } 
    
    /**
     * TEMPORARY
     */
    public void addGlobalRequest(Message msg) {
    	tempRequest.add(msg);
    }
    
    /**
     * TEMPORARY
     */
    public HashMap<Integer, Message> getGlobals() {
    	return globals;
    } 
    
    // Really simple way of getting gloabl
    public Message getGlobal(String keyword) {
    	Set<Integer> keys = globals.keySet();
    	for(Integer key : keys) {
    		if(globals.get(key).getMessageTag().contains(keyword)) {
    			return globals.get(key); 
    		}
    	}
    	return null;
    }
    
    @Override
    public String toString() {
        String list = "";
        list = getCurrentMessageList().stream().map((m) -> m.toString() + ";").reduce(list, String::concat);
        return list;
    } 
}
