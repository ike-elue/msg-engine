package com.msgeng.core;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageBus {
    private final MessageExecutor[] executors;
    
    private final ConcurrentLinkedQueue<Message> q1, q2;
    private volatile boolean using1;
    
    public MessageBus(int threadCount) {
    	q1 = new ConcurrentLinkedQueue<>();
    	q2 = new ConcurrentLinkedQueue<>();
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
    
    @Override
    public String toString() {
        String list = "";
        list = getCurrentMessageList().stream().map((m) -> m.toString() + ";").reduce(list, String::concat);
        return list;
    } 
}
