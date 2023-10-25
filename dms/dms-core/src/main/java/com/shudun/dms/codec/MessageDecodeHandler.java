package com.shudun.dms.codec;

import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.TailInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageDecodeHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {

        // 检查是否接收到了足够的数据以处理一个完整的消息
        if (buffer.readableBytes() < DmsConstants.HEAD_LENGTH) {
            log.warn("数据长度不足,等待更多数据......");
            return;
        }
        Message message = new Message();
        // 消息体
        byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), data);
        message.setData(data);

        // 消息头
        HeadInfo headInfo = new HeadInfo();
        byte[] headData = new byte[DmsConstants.HEAD_LENGTH];

        // 标记当前读取位置
        buffer.markReaderIndex();

        buffer.readBytes(headData);
        headInfo.decode(headData);
        message.setHeadInfo(headInfo);
        //读取消息长度，以确定消息的边界
        int pduLength = headInfo.getPduLength();

        if (pduLength > DmsConstants.MAX_PDU_LENGTH) {
            // 消息太大,可能是无效的消息,清空缓冲区
            log.warn("数据太大,已经超出" + DmsConstants.MAX_PDU_LENGTH + ",可能是无效的消息,清空缓冲区......");
            buffer.clear();
        }

        if (pduLength == 0) {
            out.add(message);
            return;
        }

        // 检查是否接收到了完整的消息
        if (buffer.readableBytes() < pduLength) {
            log.warn("数据长度不足,等待更多数据......");
            // 重置读取位置,等待更多数据
            buffer.resetReaderIndex();
            return;
        }

        // 读取完整的消息 PDU
        byte[] pduData = new byte[pduLength];
        buffer.readBytes(pduData);
        message.setPdu(pduData);

        // 消息尾
        byte secureModel = headInfo.getSecureModel();

        if ((secureModel & DmsConstants.SecureModelEnum.SDM_SECMODE_SIGN.getCode()) != 0) {
            if (buffer.readableBytes() < 4) {
                log.warn("数据长度不足,等待更多数据......");
                // 重置读取位置,等待更多数据
                buffer.resetReaderIndex();
                return;
            }
            int tailLength = buffer.readInt();
            if (buffer.readableBytes() < tailLength) {
                log.warn("数据长度不足,等待更多数据......");
                // 重置读取位置,等待更多数据
                buffer.resetReaderIndex();
                return;
            }
            byte[] signData = new byte[tailLength];
            buffer.readBytes(signData);
            TailInfo tailInfo = TailInfo.builder().length(tailLength).msg(signData).build();
            message.setTailInfo(tailInfo);
        }
        out.add(message);
    }

}
