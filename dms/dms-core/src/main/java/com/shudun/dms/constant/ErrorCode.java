package com.shudun.dms.constant;

public enum ErrorCode {

    SMR_OK(0x00000000, "操作成功"),

    SMR_BASE(0x03000000, "操作码基础值"),

    SMR_UNKNOWERR(ErrorCode.SMR_BASE.code + 0x00000001, "未知错误"),

    SMR_NOTSUPPORT(ErrorCode.SMR_BASE.code + 0x00000002, "不支持功能"),

    SMR_COMMFAIL(ErrorCode.SMR_BASE.code + 0x00000003, "通信超时"),

    SMR_VERIFY(ErrorCode.SMR_BASE.code + 0x00000004, "设备证书验证失败"),

    SMR_DEVNUM(ErrorCode.SMR_BASE.code + 0x00000005, "设备号非法"),

    SMR_NOSUCHDEV(ErrorCode.SMR_BASE.code + 0x00000006, "无此属性设备"),

    SMR_ERRVAL(ErrorCode.SMR_BASE.code + 0x00000007, "不支持此属性"),

    SMR_TRAPTYPE(ErrorCode.SMR_BASE.code + 0x00000008, "告警类型错误"),

    SMR_TRAPNUM(ErrorCode.SMR_BASE.code + 0x00000009, "告警号非法"),

    SMR_TOOBIG(ErrorCode.SMR_BASE.code + 0x0000000A, "包长度大于支持包长"),

    SMR_READONLY(ErrorCode.SMR_BASE.code + 0x0000000B, "试图修改只读属性"),

    SMR_NOROUTE(ErrorCode.SMR_BASE.code + 0x0000000C, "目标不可达"),

    SMR_SEND(ErrorCode.SMR_BASE.code + 0x00000010, "安全通道发送数据错误"),

    SMR_RECV(ErrorCode.SMR_BASE.code + 0x00000011, "安全通道接收数据错误"),

    SMR_ENCRYPT(ErrorCode.SMR_BASE.code + 0x0000000D, "安全通道加密失败"),

    SMR_DECRYPT(ErrorCode.SMR_BASE.code + 0x0000000D, "安全通道解密失败"),

    SMR_MESSAGE(ErrorCode.SMR_BASE.code + 0x0000000E, "消息错误"),

    SMR_NOINIT(ErrorCode.SMR_BASE.code + 0x0000000F, "未初始化接口"),

    SMR_INARGERR(ErrorCode.SMR_BASE.code + 0x0000001D, "输入参数错误"),

    SMR_OUTARGERR(ErrorCode.SMR_BASE.code + 0x0000001E, "输出参数错误"),


    SMR_LOGIN_BASE(ErrorCode.SMR_BASE.code + 0x00000100, "登录管理"),

    SMR_LOGIN_DEV_NO_ACCESS(ErrorCode.SMR_BASE.code + 0x00000101, "设备状态无用户登录权限"),

    SMR_LOGIN_PWD_LEN_ERR(ErrorCode.SMR_BASE.code + 0x00000102, "口令长度异常"),

    SMR_LOGIN_SDF_DEV_ERR(ErrorCode.SMR_BASE.code + 0x00000103, "关键密码部件异常"),

    SMR_LOGIN_SDF_AUTH_ERR(ErrorCode.SMR_BASE.code + 0x00000104, "角色身份鉴别失败"),

    SMR_LOGIN_MKEY_ERR(ErrorCode.SMR_BASE.code + 0x00000105, "管理密钥异常"),

    SMR_LOGIN_DEVKEY_ERR(ErrorCode.SMR_BASE.code + 0x00000106, "设备密钥异常"),

    SMR_KEY_MANAGE_BASE(ErrorCode.SMR_BASE.code + 0x00000200, "密钥管理"),

    SMR_SYSTEM_MANAGE_BASE(ErrorCode.SMR_BASE.code + 0x00000300, "系统管理"),

    SMR_PRIVILEGE_MANAGE_BASE(ErrorCode.SMR_BASE.code + 0x00000400, "权限管理"),

    SMR_AUDIT_MANAGE_BASE(ErrorCode.SMR_BASE.code + 0x00000500, "日志管理");

    private long code;

    private String msg;


    ErrorCode(long code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public long getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public static ErrorCode getByCode(long code) {
        ErrorCode[] errorCodes = ErrorCode.values();
        for (ErrorCode errorCode : errorCodes) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return null;
    }
}
