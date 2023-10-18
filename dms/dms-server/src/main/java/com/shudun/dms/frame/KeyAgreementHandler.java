package com.shudun.dms.frame;

import cn.com.shudun.cert.AbstractCertMaker;
import cn.com.shudun.cert.gmhelper.SM2Util;
import cn.com.shudun.cert.gmhelper.cert.SM2X509CertMakerImpl;
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
import com.shudun.dms.rpc.ConntionManager;
import io.netty.channel.*;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;

/**
 *
 */
@Slf4j
public class KeyAgreementHandler extends SimpleChannelInboundHandler<Message> {

    private ConntionManager conntionManager;

    private HandShake handShake;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

    public KeyAgreementHandler(ConntionManager conntionManager) {
        this.conntionManager = conntionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        log.info("[客户端]" + channel.remoteAddress() + " 上线了 " + LocalDateTime.now().format(dtf));

        Attribute<IChannel> attribute = channel.attr(GlobalVariable.CHANNEL_KEY);
        IChannel iChannel = attribute.get();
        if (iChannel == null) {
            iChannel = new JavaChannel(channel);
            attribute.set(iChannel);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        HeadInfo headInfo = msg.getHeadInfo();
        byte opType = headInfo.getOpType();
        if (opType == DmsConstants.MsgTypeEnum.CONNECT.getCode()) {
            log.info("[服务端收到客户端]" + ctx.channel().remoteAddress() + " 密钥协商 " + LocalDateTime.now().format(dtf));

            handShake = ctx.channel().attr(GlobalVariable.CHANNEL_KEY).get().getHandShake();
            // 组装密钥协商基础数据-- 基于对端HandShake构建本端HandShake
            handShake.initHandShake(builderHsmInfo(headInfo));

            ConnectionRequestMessage connectionRequestMessage = new ConnectionRequestMessage(handShake);
            connectionRequestMessage.decode(msg);

            ConnectionResponseMessage connectionResponseMessage = new ConnectionResponseMessage(handShake);
            ChannelFuture future = ctx.channel().writeAndFlush(connectionResponseMessage.encode(msg));
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        // 发送成功后，纳入连接管理中。
                        conntionManager.addChannel(new String(msg.getHeadInfo().getSourceId()).trim(), ctx.channel().attr(GlobalVariable.CHANNEL_KEY).get());
                    }
                }
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 根据目的ID和源ID构建
     * 当目的ID和本端ID不匹配时－说明消息包中ID有问题
     *
     * @return
     */
    private HsmInfo builderHsmInfo(HeadInfo headInfo) {

        byte[] destId = headInfo.getDestId();
        if (!Arrays.equals(Arrays.copyOf("22222222222222222222222222222222".getBytes(), 32), destId)) {
            throw new RuntimeException("目的ID不匹配");
        }

        return HsmInfo.builder()
                .localSignPk(toPublicKey("MIICEjCCAbmgAwIBAgICAeQwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxNzAyNTYyM1oXDTI2MDkxNjAyNTYyM1owYTELMAkGA1UEBhMCQ04xEDAOBgNVBAgMB2JlaWppbmcxEDAOBgNVBAcMB2JlaWppbmcxDzANBgNVBAsMBnNodWR1bjEPMA0GA1UECgwGc2h1ZHVuMQwwCgYDVQQDDANib3kwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAScCR0BgxlNURI7zt4582EyoyR0TPy6bTIl85OIMwmqlV9SV/nyC0eBM22OX/0kme8ISwIEz7UQaute3UpR/5Tko3UwczAdBgNVHQ4EFgQUAH7cVTXoqwqEQJ3QbDmUnk/zAAgwHwYDVR0jBBgwFoAUoCHsae37q0cwbjzmuMaBvy1CT1MwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCAoQwEwYDVR0lBAwwCgYIKwYBBQUHAwgwCgYIKoEcz1UBg3UDRwAwRAIgfRkki0Pwz2rLlY/dGU6/xmAaIs41Z4aq2Bn+o1YyhRoCIH3PQTpeT9KANTtaQxl1gfSClJhXKlChQl0CPgV64fa3"))
                .localSignSk(Base64.getDecoder().decode("MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEILDSMfOyJoBMO6oV7ONlqM2Z/W91WXGJ6EHpYS9zkNenoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAAScCR0BgxlNURI7zt4582EyoyR0TPy6bTIl85OIMwmqlV9SV/nyC0eBM22OX/0kme8ISwIEz7UQaute3UpR/5Tk"))
                .localEncPk(toPublicKey("MIICCTCCAa+gAwIBAgICAeUwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxNzAyNTYyM1oXDTI2MDkxNjAyNTYyM1owYTELMAkGA1UEBhMCQ04xEDAOBgNVBAgMB2JlaWppbmcxEDAOBgNVBAcMB2JlaWppbmcxDzANBgNVBAsMBnNodWR1bjEPMA0GA1UECgwGc2h1ZHVuMQwwCgYDVQQDDANib3kwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAS6Qkvxx9aKu5507pzBXgMMKComeICBDhgKlJWd7zBMrFLgdsX6fVNqmrzPcScLK9FbAuUgakRuZqHd5Dlo5oO6o2swaTAdBgNVHQ4EFgQUF98/kz90JMzn96QdMgCfgj1zKq0wHwYDVR0jBBgwFoAUoCHsae37q0cwbjzmuMaBvy1CT1MwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBDAwCQYDVR0lBAIwADAKBggqgRzPVQGDdQNIADBFAiEAyJ7v19C7Sj7PZ077dAxONTewnaE3E8GukaHCAjpP4gcCIGH2eOEn5LqXRzbyWJkOMfGbqnwA0fAT57uwdaEblNAM"))
                .localEncSk(Base64.getDecoder().decode("MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIOHKPW40f5g/52pjfEC8QoFAGNvbS2VdUZYUlWCZK+W8oIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAAS6Qkvxx9aKu5507pzBXgMMKComeICBDhgKlJWd7zBMrFLgdsX6fVNqmrzPcScLK9FbAuUgakRuZqHd5Dlo5oO6"))
                .localId(Arrays.copyOf("22222222222222222222222222222222".getBytes(), 32))
                .peerSignPk(toPublicKey("MIICCjCCAbCgAwIBAgICAeYwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxNzAzMDQwNFoXDTI2MDkxNjAzMDQwNFowWDELMAkGA1UEBhMCQ04xDTALBgNVBAgMBG51bGwxDTALBgNVBAcMBG51bGwxDTALBgNVBAsMBG51bGwxDTALBgNVBAoMBG51bGwxDTALBgNVBAMMBG51bGwwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAQHKBX786881tHipUD3QdbD4RT78HyRitqzKexIgJp/9BISZpbrThJHGSgbbAPyABeCZYfUvXc99C3U0gb6hohAo3UwczAdBgNVHQ4EFgQUjNQXL85qwQwEQ9ECv4C2Txa0w/MwHwYDVR0jBBgwFoAUoCHsae37q0cwbjzmuMaBvy1CT1MwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCAoQwEwYDVR0lBAwwCgYIKwYBBQUHAwgwCgYIKoEcz1UBg3UDSAAwRQIhAJkhbCSXyV6ll8PYbr14RPIT8jVFiTpDS4g9lc0q367xAiBjSV80bKQ1WDGIkZv1sr/neZiOrRhfoVptL4dgshaqsw=="))
                .peerEncPk(toPublicKey("MIICADCCAaagAwIBAgICAecwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxNzAzMDQwNFoXDTI2MDkxNjAzMDQwNFowWDELMAkGA1UEBhMCQ04xDTALBgNVBAgMBG51bGwxDTALBgNVBAcMBG51bGwxDTALBgNVBAsMBG51bGwxDTALBgNVBAoMBG51bGwxDTALBgNVBAMMBG51bGwwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAQ5bioBSD2fqZL/tja42BTR0+SqEtpw8L1U7HVsD5WnpEyegJEYgJpZdBE6/oYvKZiBQl8UzqRpesJ+e0pLdyz9o2swaTAdBgNVHQ4EFgQUulreNDYH5jEyhRfRkCTRFRvSD7gwHwYDVR0jBBgwFoAUoCHsae37q0cwbjzmuMaBvy1CT1MwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBDAwCQYDVR0lBAIwADAKBggqgRzPVQGDdQNIADBFAiEAhf27BGwnnM64UJm0Ca3JWzzoOsxt0qv/3gFXd3Q0oLoCIEPhUHGcJjWeASwL7Y9RrIIgJJzYyQC/WSxL8qQZ6PM6"))
                .peerId(Arrays.copyOf("11111111111111111111111111111111".getBytes(), 32)).ip("127.0.0.1").port(10197).build();
    }

    public static void main(String[] args) throws Exception {
        String encPkString = "MIIB5DCCAYqgAwIBAgICAIQwCgYIKoEcz1UBg3UwTTEcMBoGA1UEAwwTU00yIEludGVybWVkaWF0ZSBDQTEPMA0GA1UECwwGU0hVRFVOMQ8wDQYDVQQKDAZTSFVEVU4xCzAJBgNVBAYTAkNOMB4XDTIzMDkxODA0MDc0N1oXDTI2MDkxNzA0MDc0N1owPDELMAkGA1UEBhMCQ04xDTALBgNVBAoMBDg5NTYxDTALBgNVBAsMBHRlc3QxDzANBgNVBAMMBnNodWR1bjBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABDNWnku7464HEJJkCuUsv0ludKKobVkUI03BpYR5eicerAZSeNdM3ko0F5sTo0XIeGE9Vwl2esUuPN7L8BeH98yjazBpMB0GA1UdDgQWBBSgz/samfkZGQ+gIBr5zJ2hLHZgQDAfBgNVHSMEGDAWgBSgIexp7furRzBuPOa4xoG/LUJPUzAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIEMDAJBgNVHSUEAjAAMAoGCCqBHM9VAYN1A0gAMEUCIHEU/e1qF/MqJFYgMdciy1/ELuWz+6NJetDbpBzTqlFyAiEAnzQ9AV+7QxGXFmyMTkd0VWPI9CwPpDAyihE8DDePU7Q=";

        String encSkString = "MIICBQIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBDzCCAQsCAQEEIPfMXVtY43RBjIfx1fBZRIOaugHJjKGHWkrYtBPZf7m4oIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQE=";

        byte[] data = "hello".getBytes();

        byte[] encoded = CertTools.generateCertificate(encPkString).getPublicKey().getEncoded();
        AbstractCertMaker abstractCertMaker = new SM2X509CertMakerImpl();
        PublicKey publicKey = abstractCertMaker.byteConvertPublickey(encoded);
        byte[] enc = SM2Util.encryptAsn1((BCECPublicKey) publicKey, data);


        byte[] decode = Base64.getDecoder().decode(encSkString);
        PrivateKey privateKey = abstractCertMaker.byteConvertPrivatekey(decode);

        byte[] bytes = SM2Util.decryptAsn1((BCECPrivateKey) privateKey, enc);
        System.out.println(new String(bytes));


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
