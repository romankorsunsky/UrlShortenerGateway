package kors.roma.dev.messages;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

@Getter
public class Message {
    public static final int DFLT_MAX_RETRIES = 1;
    private final byte[] data;
    private int maxRetries;
    private final AtomicInteger retries = new AtomicInteger(0);

    public Message(byte[] data, int max_retries) throws Exception{
        if(data == null){
            throw new IllegalArgumentException();
        }
        this.data = data;
        this.maxRetries = max_retries;
    }
    
    public Message(byte[] data) throws Exception{
        if(data == null){
            throw new IllegalArgumentException();
        }
        this.data = data;
        this.maxRetries = DFLT_MAX_RETRIES;
    }

    public void incRetries(){
        this.retries.incrementAndGet();
    }
    public boolean shouldRetry(){
        return this.retries.get() < this.maxRetries;
    }
}
