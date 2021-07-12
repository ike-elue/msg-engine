package com.msgeng.core;

import com.msgeng.message.Message;
import com.msgeng.message.MessageBus;

public class MessageExecutor implements Runnable {

    private Message currentMessage;
    private double delta;
    private final MessageBus mb;
    private final int id;
    
    public MessageExecutor(MessageBus mb, int id) {
        this.mb = mb;
        delta = 0;
        this.id = id;
    }

    public void setMessage(Message message) {
        this.currentMessage = message;
    }

    public Message getMessage() {
        return currentMessage;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    @Override
    public void run() {
        currentMessage = mb.receiveMessage();
        if (currentMessage != null) {
            System.out.println(String.format("Running %s on thread-%s", currentMessage.getMessageTag(), id));
            System.out.println(currentMessage);
            if(currentMessage.containsTag("global")) {
            	// Will be handled by special engine in the future
            	//mb.addMessage(new Message(currentMessage, new String[] {"request"}));
            	
            	mb.addGlobalRequest(currentMessage);
            }
            
            
            // Temperary proof of concept
            if(currentMessage.getId() == 365) {
            	mb.addMessage(new Message(currentMessage));
            	System.out.println("Using global test varibale");
            	System.out.println(mb.getGlobal("test"));
            }
            
//            em.getEngines().forEach((e) -> {
//                e.updateWithCurrentMessage(currentMessage, delta);
//            });
//            if (!currentMessage.shouldDeactivate()) {
//                currentMessage.onRepeat();
//                mb.addMessage(currentMessage);
//            }
        }

        currentMessage = null;
        
        if (!mb.isEmpty()) {
            run();
        }
    }

}
