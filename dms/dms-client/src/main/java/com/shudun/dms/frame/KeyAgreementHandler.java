package com.shudun.dms.frame;

import cn.com.shudun.util.CertTools;
import com.shudun.dms.channel.IChannel;
import com.shudun.dms.channel.JavaChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.ConnectionRequestMessage;
import com.shudun.dms.handshake.ConnectionResponseMessage;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.handshake.HsmInfo;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;

/**
 * 发起密钥协商请求
 */
@Slf4j
public class KeyAgreementHandler extends SimpleChannelInboundHandler<Message> {

    private HandShake handShake;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("[客户端]" + channel.localAddress() + " 发起密钥协商 " + LocalDateTime.now().format(dtf));

        Attribute<IChannel> attribute = channel.attr(GlobalVariable.CHANNEL_KEY);
        IChannel iChannel = attribute.get();
        if (iChannel == null) {
            iChannel = new JavaChannel(channel);
            attribute.set(iChannel);
        }
        handShake = ctx.channel().attr(GlobalVariable.CHANNEL_KEY).get().getHandShake();
        // 组装密钥协商基础数据
        handShake.initHandShake(builderHsmInfo());
        ConnectionRequestMessage connectionRequestMessage = new ConnectionRequestMessage(handShake);
        // 组装安全通道建立请求消息包
        ctx.channel().writeAndFlush(connectionRequestMessage.encode(null));

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        HeadInfo headInfo = msg.getHeadInfo();
        byte opType = headInfo.getOpType();
        if (opType == DmsConstants.MsgTypeEnum.RESPONSE.getCode()) {
            ConnectionResponseMessage connectionResponseMessage = new ConnectionResponseMessage(handShake);
            connectionResponseMessage.decode(msg);
            log.info("[客户端]" + ctx.channel().localAddress() + " 密钥协商成功 " + LocalDateTime.now().format(dtf));

        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private HsmInfo builderHsmInfo() {

        return HsmInfo.builder()
                .localSignPk(toPublicKey("MIICCjCCAbCgAwIBAgICAeYwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxNzAzMDQwNFoXDTI2MDkxNjAzMDQwNFowWDELMAkGA1UEBhMCQ04xDTALBgNVBAgMBG51bGwxDTALBgNVBAcMBG51bGwxDTALBgNVBAsMBG51bGwxDTALBgNVBAoMBG51bGwxDTALBgNVBAMMBG51bGwwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAQHKBX786881tHipUD3QdbD4RT78HyRitqzKexIgJp/9BISZpbrThJHGSgbbAPyABeCZYfUvXc99C3U0gb6hohAo3UwczAdBgNVHQ4EFgQUjNQXL85qwQwEQ9ECv4C2Txa0w/MwHwYDVR0jBBgwFoAUoCHsae37q0cwbjzmuMaBvy1CT1MwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCAoQwEwYDVR0lBAwwCgYIKwYBBQUHAwgwCgYIKoEcz1UBg3UDSAAwRQIhAJkhbCSXyV6ll8PYbr14RPIT8jVFiTpDS4g9lc0q367xAiBjSV80bKQ1WDGIkZv1sr/neZiOrRhfoVptL4dgshaqsw=="))
                .localSignSk(Base64.getDecoder().decode("MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIGXsumPOF1qs8VmZLzj77X2hf7iqmkE1xCnCi9EdFLJLoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAAQHKBX786881tHipUD3QdbD4RT78HyRitqzKexIgJp/9BISZpbrThJHGSgbbAPyABeCZYfUvXc99C3U0gb6hohA"))
                .localEncPk(toPublicKey("MIICADCCAaagAwIBAgICAecwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxNzAzMDQwNFoXDTI2MDkxNjAzMDQwNFowWDELMAkGA1UEBhMCQ04xDTALBgNVBAgMBG51bGwxDTALBgNVBAcMBG51bGwxDTALBgNVBAsMBG51bGwxDTALBgNVBAoMBG51bGwxDTALBgNVBAMMBG51bGwwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAQ5bioBSD2fqZL/tja42BTR0+SqEtpw8L1U7HVsD5WnpEyegJEYgJpZdBE6/oYvKZiBQl8UzqRpesJ+e0pLdyz9o2swaTAdBgNVHQ4EFgQUulreNDYH5jEyhRfRkCTRFRvSD7gwHwYDVR0jBBgwFoAUoCHsae37q0cwbjzmuMaBvy1CT1MwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBDAwCQYDVR0lBAIwADAKBggqgRzPVQGDdQNIADBFAiEAhf27BGwnnM64UJm0Ca3JWzzoOsxt0qv/3gFXd3Q0oLoCIEPhUHGcJjWeASwL7Y9RrIIgJJzYyQC/WSxL8qQZ6PM6"))
                .localEncSk(Base64.getDecoder().decode("MIICBQIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBDzCCAQsCAQEEIO8zMUdKLAssnNSNCxQIgfpbfg7Kovvr2plCfh93oKhGoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQE="))
                .localId(Arrays.copyOf("11111111111111111111111111111111".getBytes(), 32))
                .peerSignPk(toPublicKey("MIICEjCCAbmgAwIBAgICAeQwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxNzAyNTYyM1oXDTI2MDkxNjAyNTYyM1owYTELMAkGA1UEBhMCQ04xEDAOBgNVBAgMB2JlaWppbmcxEDAOBgNVBAcMB2JlaWppbmcxDzANBgNVBAsMBnNodWR1bjEPMA0GA1UECgwGc2h1ZHVuMQwwCgYDVQQDDANib3kwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAScCR0BgxlNURI7zt4582EyoyR0TPy6bTIl85OIMwmqlV9SV/nyC0eBM22OX/0kme8ISwIEz7UQaute3UpR/5Tko3UwczAdBgNVHQ4EFgQUAH7cVTXoqwqEQJ3QbDmUnk/zAAgwHwYDVR0jBBgwFoAUoCHsae37q0cwbjzmuMaBvy1CT1MwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCAoQwEwYDVR0lBAwwCgYIKwYBBQUHAwgwCgYIKoEcz1UBg3UDRwAwRAIgfRkki0Pwz2rLlY/dGU6/xmAaIs41Z4aq2Bn+o1YyhRoCIH3PQTpeT9KANTtaQxl1gfSClJhXKlChQl0CPgV64fa3"))
                .peerEncPk(toPublicKey("MIICCTCCAa+gAwIBAgICAeUwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxNzAyNTYyM1oXDTI2MDkxNjAyNTYyM1owYTELMAkGA1UEBhMCQ04xEDAOBgNVBAgMB2JlaWppbmcxEDAOBgNVBAcMB2JlaWppbmcxDzANBgNVBAsMBnNodWR1bjEPMA0GA1UECgwGc2h1ZHVuMQwwCgYDVQQDDANib3kwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAS6Qkvxx9aKu5507pzBXgMMKComeICBDhgKlJWd7zBMrFLgdsX6fVNqmrzPcScLK9FbAuUgakRuZqHd5Dlo5oO6o2swaTAdBgNVHQ4EFgQUF98/kz90JMzn96QdMgCfgj1zKq0wHwYDVR0jBBgwFoAUoCHsae37q0cwbjzmuMaBvy1CT1MwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBDAwCQYDVR0lBAIwADAKBggqgRzPVQGDdQNIADBFAiEAyJ7v19C7Sj7PZ077dAxONTewnaE3E8GukaHCAjpP4gcCIGH2eOEn5LqXRzbyWJkOMfGbqnwA0fAT57uwdaEblNAM"))
                .peerId(Arrays.copyOf("22222222222222222222222222222222".getBytes(), 32))
                .ip("127.0.0.1")
                .port(10197)
                .build();
    }

    private byte[] toPublicKey(String baseCert) {
        X509Certificate x509Certificate = null;
        try {
            x509Certificate = CertTools.generateCertificate(baseCert);
        } catch (Exception e) {
            log.error("证书处理异常", e);
            throw new RuntimeException("证书处理异常");
        }
        return x509Certificate.getPublicKey().getEncoded();
    }
}
