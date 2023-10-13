package com.shudun.dms.rpc;

import com.shudun.dms.message.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageFuture {

    private Message message;

    private int errCode;

    private long timeout;

    private long start = System.currentTimeMillis();

    private transient CompletableFuture<Message> origin = new CompletableFuture();

    public boolean isTimeout() {
        return System.currentTimeMillis() - start > timeout;
    }

    public Message get(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        Message result = null;
        try {
            result = origin.get(timeout, unit);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new TimeoutException("cost " + (System.currentTimeMillis() - start) + " ms");
        }
        return result;
    }

    public void setResultMessage(Message message) {
        if (this.errCode == 0) {
            this.origin.complete(message);
        } else {
            this.origin.completeExceptionally(new RuntimeException(errCode + ""));
        }
    }

}
