package com.msgeng.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.msgeng.message.Message;
import com.msgeng.message.MessageBus;

public class ThreadPool {

    private final ExecutorService executor;
    private final List<Future> futures;
    private final MessageBus mb;

    public ThreadPool(MessageBus mb) {
        executor = Executors.newFixedThreadPool(mb.getExecutors().length);
        futures = new ArrayList<>();
        this.mb = mb;
    }

    /**
     * Runs the run() method in each message executor
     */
    private synchronized void execute(double delta) {
        if (!mb.isEmpty()) {
            for(MessageExecutor m : mb.getExecutors()) {
                m.setDelta(delta);
                Future f = executor.submit(m);
                futures.add(f);
            }
            
            futures.stream().forEach((f) -> {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException ex) {
                    //Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

//        try {
//            wait();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        futures.clear();
    }

    /**
     * Method where you are only supposed to act on gathered data and/or own
     * functions based on delta
     *
     * @param delta
     */
    public void update(double delta) {
        execute(delta);
        if(mb.isEmpty())
            mb.flipMessages();
    }

    // Might be temperary
    public void updateRequests() {
    	//System.out.println("Current Globals:");
        //System.out.println(mb.getGlobals());
        
    	Message msg = mb.tempRequest.poll();
    	while(msg != null) {
    		mb.overwriteGlobal(msg);
    		msg = mb.tempRequest.poll();
    	}
    }
    
    public void dispose() {

        try {
            System.out.println("Attempting to shutdown executor...");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                System.err.println("Cancel non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("Executor shutdown finished!");
        }
    }
}
