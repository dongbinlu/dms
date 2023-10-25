package com.shudun.dms.controller;

import com.shudun.dms.message.Message;
import com.shudun.dms.rpc.RpcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/rpc/remoting/")
public class RpcRemotingController {


    @Autowired
    private RpcTemplate rpcTemplate;

    @GetMapping("/{deviceId}")
    public String test(@PathVariable("deviceId") String deviceId) {
        byte[] data = "hello client".getBytes();
        String result = "";
        try {
            Message message = rpcTemplate.sync(deviceId, data, 60, TimeUnit.SECONDS);
            byte[] pdu = message.getPdu();
            result = new String(pdu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}
