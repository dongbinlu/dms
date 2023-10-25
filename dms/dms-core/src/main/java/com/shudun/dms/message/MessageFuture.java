package com.shudun.dms.message;

import cn.hutool.core.util.HexUtil;
import com.shudun.dms.constant.ErrorCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class MessageFuture {

    private Message message;

    private long errCode;

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
            try {
                String code = e.getCause().getMessage();
                ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.getByCode(Long.valueOf(code));
                String msg = errorCodeEnum.getMsg();
                log.error(msg + ",错误码:{}", HexUtil.toHex(errorCodeEnum.getCode()));
                throw new RuntimeException(msg);
            } catch (NumberFormatException numberFormatException) {
                throw new RuntimeException(ErrorCodeEnum.SMR_UNKNOWERR.getMsg());
            } catch (NullPointerException nullPointerException) {
                throw new RuntimeException(ErrorCodeEnum.SMR_UNKNOWERR.getMsg());
            }
        } catch (TimeoutException e) {
            log.error(ErrorCodeEnum.SMR_COMMFAIL.getMsg() + ",错误码:{},耗时:{} ms", HexUtil.toHex(ErrorCodeEnum.SMR_COMMFAIL.getCode()), (System.currentTimeMillis() - start));
            throw new TimeoutException(ErrorCodeEnum.SMR_COMMFAIL.getMsg());
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
