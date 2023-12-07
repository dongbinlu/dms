package com.shudun.dms.constant;

public enum ErrorCodeEnum {

    SMR_OK(0x00000000, "操作成功"),

    SMR_BASE(0x03000000, "操作码基础值"),

    SMR_UNKNOWERR(ErrorCodeEnum.SMR_BASE.code + 0x00000001, "未知错误"),

    SMR_NOTSUPPORT(ErrorCodeEnum.SMR_BASE.code + 0x00000002, "不支持功能"),

    SMR_COMMFAIL(ErrorCodeEnum.SMR_BASE.code + 0x00000003, "通信超时"),

    SMR_VERIFY(ErrorCodeEnum.SMR_BASE.code + 0x00000004, "设备证书验证失败"),

    SMR_DEVNUM(ErrorCodeEnum.SMR_BASE.code + 0x00000005, "设备号非法"),

    SMR_NOSUCHDEV(ErrorCodeEnum.SMR_BASE.code + 0x00000006, "无此属性设备"),

    SMR_ERRVAL(ErrorCodeEnum.SMR_BASE.code + 0x00000007, "不支持此属性"),

    SMR_TRAPTYPE(ErrorCodeEnum.SMR_BASE.code + 0x00000008, "告警类型错误"),

    SMR_TRAPNUM(ErrorCodeEnum.SMR_BASE.code + 0x00000009, "告警号非法"),

    SMR_TOOBIG(ErrorCodeEnum.SMR_BASE.code + 0x0000000A, "包长度大于支持包长"),

    SMR_READONLY(ErrorCodeEnum.SMR_BASE.code + 0x0000000B, "试图修改只读属性"),

    SMR_NOROUTE(ErrorCodeEnum.SMR_BASE.code + 0x0000000C, "目标不可达"),

    SMR_SEND(ErrorCodeEnum.SMR_BASE.code + 0x00000010, "安全通道发送数据错误"),

    SMR_RECV(ErrorCodeEnum.SMR_BASE.code + 0x00000011, "安全通道接收数据错误"),

    SMR_ENCRYPT(ErrorCodeEnum.SMR_BASE.code + 0x0000000D, "安全通道加密失败"),

    SMR_DECRYPT(ErrorCodeEnum.SMR_BASE.code + 0x0000000D, "安全通道解密失败"),

    SMR_MESSAGE(ErrorCodeEnum.SMR_BASE.code + 0x0000000E, "消息错误"),

    SMR_NOINIT(ErrorCodeEnum.SMR_BASE.code + 0x0000000F, "未初始化接口"),

    SMR_INARGERR(ErrorCodeEnum.SMR_BASE.code + 0x0000001D, "输入参数错误"),

    SMR_OUTARGERR(ErrorCodeEnum.SMR_BASE.code + 0x0000001E, "输出参数错误"),


    SMR_LOGIN_BASE(ErrorCodeEnum.SMR_BASE.code + 0x00000100, "登录管理"),

    SMR_LOGIN_DEV_NO_ACCESS(ErrorCodeEnum.SMR_BASE.code + 0x00000101, "设备状态无用户登录权限"),

    SMR_LOGIN_PWD_LEN_ERR(ErrorCodeEnum.SMR_BASE.code + 0x00000102, "口令长度异常"),

    SMR_LOGIN_SDF_DEV_ERR(ErrorCodeEnum.SMR_BASE.code + 0x00000103, "关键密码部件异常"),

    SMR_LOGIN_SDF_AUTH_ERR(ErrorCodeEnum.SMR_BASE.code + 0x00000104, "角色身份鉴别失败"),

    SMR_LOGIN_MKEY_ERR(ErrorCodeEnum.SMR_BASE.code + 0x00000105, "管理密钥异常"),

    SMR_LOGIN_DEVKEY_ERR(ErrorCodeEnum.SMR_BASE.code + 0x00000106, "设备密钥异常"),

    SMR_KEY_MANAGE_BASE(ErrorCodeEnum.SMR_BASE.code + 0x00000200, "密钥管理"),

    SMR_SYSTEM_MANAGE_BASE(ErrorCodeEnum.SMR_BASE.code + 0x00000300, "系统管理"),

    SMR_PRIVILEGE_MANAGE_BASE(ErrorCodeEnum.SMR_BASE.code + 0x00000400, "权限管理"),

    SMR_AUDIT_MANAGE_BASE(ErrorCodeEnum.SMR_BASE.code + 0x00000500, "日志管理");

    private int code;

    private String msg;


    ErrorCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ErrorCodeEnum getByCode(int code) {
        ErrorCodeEnum[] errorCodes = ErrorCodeEnum.values();
        for (ErrorCodeEnum errorCode : errorCodes) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return null;
    }
    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
