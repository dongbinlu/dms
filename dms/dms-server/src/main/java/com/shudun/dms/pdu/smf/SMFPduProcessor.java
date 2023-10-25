package com.shudun.dms.pdu.smf;

import cn.hutool.core.util.ByteUtil;
import com.shudun.dms.channel.IChannel;
import com.shudun.dms.channel.JavaChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.constant.ErrorCodeEnum;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.pdu.PduProcessor;
import com.shudun.dms.rpc.ConntionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * SMF处理器
 */
@Component
@Slf4j
public class SMFPduProcessor implements PduProcessor {

    @Autowired
    private ConntionManager conntionManager;

    @Override
    public boolean validate(Message message) {
        return Arrays.equals(message.getHeadInfo().getSourceId(), Arrays.copyOf("00000000000000000000000000000000".getBytes(), 32));
    }

    /**
     * 注意：
     * 需要判断是接收SMF给设备发送
     * 还是设备响应SMF
     *
     * @param message:
     * @param iChannel:注意iChannel是SMF接口的channel，并不是设备的channel
     * @throws Exception
     */
    @Override
    public void handler(Message message, IChannel iChannel) throws Exception {
        HeadInfo headInfo = message.getHeadInfo();
        byte[] destId = headInfo.getDestId();
        // 1、判断目的连接是否正常
        JavaChannel javaChannel = (JavaChannel) conntionManager.getIChannelByKey(new String(destId).trim());
        if (javaChannel == null || !javaChannel.getChannel().isActive()) {
            log.warn("目标不可达:{}", new String(destId).trim());
            iChannel.oneway(new String(headInfo.getDestId()).trim(),
                    new String(headInfo.getSourceId()).trim(),
                    ByteUtil.longToBytes(ErrorCodeEnum.SMR_NOROUTE.getCode(), ByteOrder.BIG_ENDIAN),
                    headInfo.getMsgId(),
                    DmsConstants.MsgTypeEnum.LOCAL_ERROR.getCode(),
                    DmsConstants.SecureModelEnum.SDM_SECMODE_NOENC.getCode(),
                    DmsConstants.SecureModelEnum.SDM_SECMODE_NOSIGN.getCode());
            return;
        }
        // 2、判断是否需要回复
        if ((headInfo.getSecureModel() & DmsConstants.SecureModelEnum.SDM_SECMODE_RET.getCode()) != 0) {
            // 需要回复
            Message msg = javaChannel.sync(message.getPdu());
            //响应SMF
            iChannel.oneway(new String(headInfo.getDestId()).trim(),
                    new String(headInfo.getSourceId()).trim(),
                    msg.getPdu(),
                    headInfo.getMsgId(),
                    msg.getHeadInfo().getOpType(),
                    DmsConstants.SecureModelEnum.SDM_SECMODE_NOENC.getCode(),
                    DmsConstants.SecureModelEnum.SDM_SECMODE_NOSIGN.getCode());
        } else {
            //不需要回复
            javaChannel.oneway(message.getPdu());
        }
    }

}
