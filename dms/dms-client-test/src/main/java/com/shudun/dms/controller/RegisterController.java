package com.shudun.dms.controller;

import cn.hutool.core.util.ByteUtil;
import cn.hutool.core.util.HexUtil;
import com.google.common.collect.Maps;
import com.shudun.dms.constant.AIDEnum;
import com.shudun.dms.message.Message;
import com.shudun.dms.rpc.DmsRpcTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/rpc")
@Slf4j
public class RegisterController {

    @Autowired(required = false)
    private DmsRpcTemplate dmsRpcTemplate;

    @GetMapping("/register")
    public String register() throws Exception {

        byte[] response;
        try (ByteArrayOutputStream baseBOS = new ByteArrayOutputStream(); DataOutputStream baseDOS = new DataOutputStream(baseBOS)) {
            {
                // 设备名称
                baseDOS.write(ByteUtil.longToBytes(AIDEnum.SDM_AID_Dev_Name.getCode(), ByteOrder.BIG_ENDIAN));
                byte[] value = "服务器密码机".getBytes();
                baseDOS.writeInt(value.length);
                baseDOS.write(value);
            }
            {
                // 地址
                baseDOS.write(ByteUtil.longToBytes(AIDEnum.SDM_AID_Dev_ID.getCode(), ByteOrder.BIG_ENDIAN));
                byte[] value = "11111111111111111111111111111111".getBytes();
                baseDOS.writeInt(value.length);
                baseDOS.write(value);
            }

            response = baseBOS.toByteArray();
            baseBOS.reset();

            baseDOS.writeByte((byte) 0x00);
            baseDOS.writeByte((byte) 0xB5);
            baseDOS.writeByte((byte) 0x00);
            baseDOS.writeLong(0x0200000900000000L);
            baseDOS.writeInt(response.length);
            baseDOS.write(response);

            Message message = dmsRpcTemplate.sync(baseBOS.toByteArray(), 10, TimeUnit.SECONDS);
            log.info("发送基本信息成功");
            System.out.println(HexUtil.encodeHex(message.getPdu()));
        } catch (Exception e) {
            log.error("channelConned,发送设备注册信息", e);
        }
        return "注册成功";
    }

    @GetMapping("/close")
    public String close(){
        dmsRpcTemplate.destroy();
        return "close";
    }

    @GetMapping("/init")
    public String init(){
        dmsRpcTemplate.init();
        return "init";
    }


    @GetMapping("/info/cert/{deviceId}")
    public String getInfo(@PathVariable("deviceId") String deviceId) throws Exception {

        HashMap<Long, byte[]> resultMap = Maps.newHashMap();

        // 设备名称
        long[] aids = {0x0100001200000000L};

        byte[] request;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            dataOutputStream.writeByte(0x00);
            dataOutputStream.writeByte(0xB0);
            for (long aid : aids) {
                dataOutputStream.writeLong(aid);
            }
            request = outputStream.toByteArray();
        }
        byte[] response = null;
        try {
            Message message = dmsRpcTemplate.sync(deviceId, request, 1 * 60, TimeUnit.SECONDS);
            response = message.getPdu();
            log.info("response:{}", response);
        } catch (Exception e) {
            System.out.println("================");
            e.printStackTrace();
        }

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(response))) {
            byte header = input.readByte();
            byte type = input.readByte();
            byte flag = input.readByte();
            // 正常
            if (flag == 0x00) {
                for (long aid : aids) {
                    long resAid = input.readLong();
                    int length = input.readInt();
                    byte[] value = new byte[length];
                    input.read(value);
                    resultMap.put(resAid, value);
                }
            } else {// 异常
                for (long aid : aids) {
                    long resAid = input.readLong();
                    int code = input.readInt();
                    log.error("GM0050 get,deviceId:{},aid:{},code:{},codeString:{}", deviceId, resAid, code, Integer.toHexString(code));
                }
                throw new RuntimeException("Wrong get error!");
            }
        }
        byte[] value = resultMap.get(0x0100001200000000L);
        log.info("result: " + value);
        return new String(value);
    }

    @GetMapping("/info/name/{deviceId}")
    public String getNameInfo(@PathVariable("deviceId") String deviceId) throws Exception {

        HashMap<Long, byte[]> resultMap = Maps.newHashMap();

        // 设备名称
        long[] aids = {0x0100000900000000L};

        byte[] request;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            dataOutputStream.writeByte(0x00);
            dataOutputStream.writeByte(0xB0);
            for (long aid : aids) {
                dataOutputStream.writeLong(aid);
            }
            request = outputStream.toByteArray();
        }
        byte[] response = null;
        try {
            Message message = dmsRpcTemplate.sync(deviceId, request, 2 * 60, TimeUnit.SECONDS);
            response = message.getPdu();
            log.info("response:{}", response);
        } catch (Exception e) {
            System.out.println("================");
            e.printStackTrace();
        }

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(response))) {
            byte header = input.readByte();
            byte type = input.readByte();
            byte flag = input.readByte();
            // 正常
            if (flag == 0x00) {
                for (long aid : aids) {
                    long resAid = input.readLong();
                    int length = input.readInt();
                    byte[] value = new byte[length];
                    input.read(value);
                    resultMap.put(resAid, value);
                }
            } else {// 异常
                for (long aid : aids) {
                    long resAid = input.readLong();
                    int code = input.readInt();
                    log.error("GM0050 get,deviceId:{},aid:{},code:{},codeString:{}", deviceId, resAid, code, Integer.toHexString(code));
                }
                throw new RuntimeException("Wrong get error!");
            }
        }
        byte[] value = resultMap.get(0x0100000900000000L);
        log.info("result: " + value);
        return new String(value);
    }

}
