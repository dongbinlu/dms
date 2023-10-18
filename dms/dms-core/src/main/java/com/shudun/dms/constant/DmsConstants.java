package com.shudun.dms.constant;

import cn.hutool.crypto.digest.SM3;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Base64;

public interface DmsConstants {

    // 当前消息版本
    byte MSG_VERSION = 1;

    // 最大PDU长度 10MB
    int MAX_PDU_LENGTH = 1024 * 1204 * 10;

    // 消息头长度
    int HEAD_LENGTH = 81;


    enum PDUGroupEnum {

        DEV_MANAGEMENT((byte) 0x00, " 设备管理 GM/T 0050 6.2"),
        SYM_KEY_MANAGEMENT((byte) 0xC0, " 对称密钥管理 GM/T 0051 7.1"),
        VPN_COMPLIANCE((byte) 0xC5, " VPN设备监察 GM/T 0052 7.1"),
        DEV_REMOTE_MONITOR((byte) 0xC1, " 密码设备远程监控 GM/T 0053 6.1"),
        DEV_COMPLIANCE((byte) 0xC4, " 密码设备合规性检验 GM/T 0053 6.2"),
        MSG_SEND_UP((byte) 0xD0, " 扩展 消息上送");

        private byte code;

        private String msg;

        PDUGroupEnum(byte code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        public byte getCode() {
            return this.code;
        }

        public String getMsg() {
            return this.msg;
        }

    }

    /**
     * 安全模式
     */
    enum SecureModelEnum {

        SDM_SECMODE_NOT((byte) 0x00, "不设置安全模式"),

        SDM_SECMODE_NOCHECK((byte) 0x00, "不需要验证"),

        SDM_SECMODE_CHECK((byte) (1 << 0), "需要验证"),

        SDM_SECMODE_NOSIGN(SecureModelEnum.SDM_SECMODE_NOCHECK.code, "未签名"),

        SDM_SECMODE_SIGN(SecureModelEnum.SDM_SECMODE_CHECK.code, "签名"),

        SDM_SECMODE_NOHMAC(SecureModelEnum.SDM_SECMODE_NOCHECK.code, "未做HMAC"),

        SDM_SECMODE_HMAC(SecureModelEnum.SDM_SECMODE_CHECK.code, "HMAC"),

        SDM_SECMODE_NOENC((byte) 0x00, "未加密"),

        SDM_SECMODE_ENC((byte) (1 << 1), "已加密"),

        SDM_SECMODE_NORET((byte) 0x00, "不需要回复"),

        SDM_SECMODE_RET((byte) (1 << 2), "需要回复");

        private byte code;

        private String msg;

        SecureModelEnum(byte code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        public byte getCode() {
            return this.code;
        }

        public String getMsg() {
            return this.msg;
        }

    }


    /**
     * 消息类型
     */
    enum MsgTypeEnum {
        CONNECT((byte) 0xA1, "安全通道建立请求消息类型"),

        RESPONSE((byte) 0xA2, "安全通道建立响应消息类型"),

        DATA((byte) 0xA3, "安全通道数据发送消息类型"),

        RESET((byte) 0xA4, "安全通道通知重启消息类型"),

        GET_LOCAL_ID((byte) 0xF0, " 获取本地设备ID"),

        GET_DEVID_LIST((byte) 0xF1, " 获取管理设备列表"),

        GET_DEV_INFO((byte) 0xF2, " 获取管理设备信息"),

        GET_DEV_CERT((byte) 0xF3, " 获取管理设备证书"),

        GET_TRAP_COUNT((byte) 0xF4, " 获取告警信息数量"),

        GET_TRAP_INFO((byte) 0xF5, " 获取告警信息"),

        SET_TRAP_INFO((byte) 0xF6, " 设置告警信息"),

        IS_DEV_CONNECTED((byte) 0xF7, " 检查是否存在安全通道"),

        ADD_DEVICE((byte) 0xFA, " 新增设备信息"),

        UPDATE_DEVCERT((byte) 0xFC, " 更新设备证书"),

        DEL_DEVICE((byte) 0xFD, " 删除设备信息"),

        LOCAL_ERROR((byte) 0xFE, " 本地错误码");

        private byte code;

        private String msg;

        MsgTypeEnum(byte code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        public byte getCode() {
            return this.code;
        }

        public String getMsg() {
            return this.msg;
        }
    }


    public static void main(String[] args) throws Exception {

        byte[] bytes = FileUtils.readFileToByteArray(new File("C:\\Users\\v_boy\\Desktop\\1.txt"));

//        SM3Digest sm3Digest = new SM3Digest();
//        sm3Digest.
//        sm3Digest.doFinal(bytes)
        SM3 sm3 = SM3.create();
        byte[] digest = sm3.digest(bytes);
        System.out.println(Base64.getEncoder().encodeToString(digest));


    }


}
