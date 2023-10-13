package com.shudun.dms.controller;

import com.google.common.collect.Maps;
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
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/rpc")
@Slf4j
public class RPCController {

    @Autowired
    private DmsRpcTemplate dmsRpcTemplate;

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
            Message message = dmsRpcTemplate.sync(deviceId, request,1 * 60, TimeUnit.SECONDS);
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
            Message message = dmsRpcTemplate.sync(deviceId, request,2 * 60, TimeUnit.SECONDS);
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
