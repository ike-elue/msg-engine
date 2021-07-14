package com.msgeng.message;

import com.msgeng.engine.EngineManager;

public class MessageExecutor implements Runnable {

    private Message currentMessage;
    private double delta;
    private final MessageBus mb;
    private final EngineManager em;
    private final int id;
    
    public MessageExecutor(MessageBus mb, EngineManager em, int id) {
        this.mb = mb;
        delta = 0;
        this.em = em;
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
            System.out.println(String.format("Running \"%s\" on thread-%s", currentMessage.getMessageTag(), id));
            System.out.println(currentMessage);
            if(currentMessage.containsTag("global")) {
            	// Will be special engine later
            	mb.addMessage(currentMessage);
            }
            
            em.getEngines().forEach((e) -> {
                e.updateWithCurrentMessage(currentMessage, delta);
            });
        }

        currentMessage = null;
        
        if (!mb.isEmpty()) {
            run();
        }
    }

}
