package com.msgeng.message;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.msgeng.engine.EngineManager;

public class MessageBus {
    private final MessageExecutor[] executors;
    
    private final ConcurrentLinkedQueue<Message> q1, q2;
    private volatile boolean using1;
    
    private final HashMap<String, Message> globals;

    private final ConcurrentLinkedQueue<Message> toRender;
    
    private final ConcurrentLinkedQueue<Message> toRequest;
    
    public MessageBus(int threadCount, EngineManager em) {
    	q1 = new ConcurrentLinkedQueue<>();
    	q2 = new ConcurrentLinkedQueue<>();
    	
    	globals = new HashMap<>();
    	
    	toRender = new ConcurrentLinkedQueue<>();
    	
    	toRequest = new ConcurrentLinkedQueue<>();
    	
        executors = new MessageExecutor[threadCount];
        for(int i = 0; i < threadCount; i++)
            executors[i] = new MessageExecutor(this, em, i);
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
    
    public synchronized void addRequestedGlobal(Message message) {
    	toRequest.add(message);
    }
    
    public synchronized void addMessage(Message message) {
    	// Will be handled by request engine
    	if(message.containsTag("global")) {
    		addRequestedGlobal(message);
    		message = Message.replaceTags(message, new String[] {"global"}, new String[] {"requested"});
    	}
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
    
    public Message getRequestedGlobal() {
    	return toRequest.poll();
    }
    
    public MessageExecutor[] getExecutors() {
        return executors;
    }
    
    public void overwriteGlobal(Message msg) {
    	globals.put(msg.getAlias(), msg);
    }
    
    public void deleteGlobal(Message msg) {
    	globals.remove(msg.getAlias());
    } 
    
    public Message getGlobal(String alias) {
    	return globals.get(alias);
    }
    
    @Override
    public String toString() {
        String list = "";
        list = getCurrentMessageList().stream().map((m) -> m.toString() + ";").reduce(list, String::concat);
        return list;
    } 
}
