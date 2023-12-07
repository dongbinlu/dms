package com.shudun.dms.pdu;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.constant.AIDEnum;
import com.shudun.dms.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteOrder;

@Component
@Slf4j
public class DevInfoPduProcessor implements PduProcessor {
    @Override
    public boolean validate(Message message) {
        boolean flag = false;
        byte[] pudData = message.getPdu();

        try (ByteArrayInputStream binput = new ByteArrayInputStream(pudData); DataInputStream dinput = new DataInputStream(binput)) {
            byte header = dinput.readByte();
            byte type = dinput.readByte();
            if (header == (byte) 0x00 && type == (byte) 0xB0) {
                flag = true;
            }
        } catch (Exception e) {
            // do nothing
        }
        return flag;
    }

    @Override
    public void handler(Message message, IChannel iChannel) throws Exception {
        log.info("devinfo handler start......");
        byte[] response;
        try (ByteArrayOutputStream baseBOS = new ByteArrayOutputStream(); DataOutputStream baseDOS = new DataOutputStream(baseBOS);) {

            baseDOS.writeByte((byte) 0x00);
            baseDOS.writeByte((byte) 0xB2);
            baseDOS.writeByte((byte) 0x00);

            {
                baseDOS.write(cn.hutool.core.util.ByteUtil.longToBytes(AIDEnum.SDM_AID_Dev_Name.getCode(), ByteOrder.BIG_ENDIAN));
                byte[] value = "服务器密码机".getBytes();
                baseDOS.writeInt(value.length);
                baseDOS.write(value);
            }

            {
                baseDOS.write(cn.hutool.core.util.ByteUtil.longToBytes(AIDEnum.SDM_AID_Dev_ID.getCode(), ByteOrder.BIG_ENDIAN));
                byte[] value = "11111111111111111111111111111111".getBytes();
                baseDOS.writeInt(value.length);
                baseDOS.write(value);
            }

            response = baseBOS.toByteArray();
        }

        iChannel.oneway(response);

    }
}
