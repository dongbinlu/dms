package com.shudun.dms.constant;

public enum AIDEnum {

    SDM_AID_Dev_Vendor(0x0100000100000000L, "厂商名称"),

    SDM_AID_Dev_Prodct(0x0100000200000000L, "设备型号"),

    SDM_AID_Dev_Serial(0x0100000300000000L, "设备序列号"),

    SDM_AID_Dev_Version(0x0100000400000000L, "系统版本号"),

    SDM_AID_Dev_StandardVersion(0x0100000500000000L, "管理协议版本号"),

    SDM_AID_Dev_Descr(0x0100000600000000L, "设备描述"),

    SDM_AID_Dev_TimeTicks(0x0100000700000000L, "已运行时间"),

    SDM_AID_Dev_MgrContact(0x0100000800000000L, "联系方式"),

    SDM_AID_Dev_Name(0x0100000900000000L, "设备名称"),

    SDM_AID_Dev_Location(0x0100000A00000000L, "设备位置"),

    SDM_AID_Dev_AsymAlgAbility(0x0100000B00000000L, "系统支持的非对称算法"),

    SDM_AID_Dev_SymAlgAbility(0x0100000C00000000L, "系统支持的对称算法"),

    SDM_AID_Dev_HashAlgAbility(0x0100000D00000000L, "系统支持的杂凑算法"),

    SDM_AID_Dev_MgmtID(0x0100000E00000001L, "管理组对象ID"),

    SDM_AID_Dev_MgmtDecription(0x0100000E00000002L, "管理组对象描述"),

    SDM_AID_Dev_MgmtTimeTicks(0x0100000E00000003L, "上次该行赋值时间"),

    SDM_AID_Dev_MgmtCtrlStats(0x0100000E00000004L, "控制添加和删除的状态变量"),

    SDM_AID_Dev_ID(0x0100000F00000000L, "设备唯一标识"),

    SDM_AID_Dev_CertApplication(0x0100001100000000L, "证书申请"),

    SDM_AID_Dev_MgmtCenterCert(0x0100001200000000L, "总中心证书"),

    SDM_AID_Dev_MgmtCert(0x0100001300000000L, "父节点证书"),

    SDM_AID_Dev_Cert(0x0100001400000000L, "被管设备证书"),

    SDM_AID_Inf_Cont(0x0200000100000000L, "接口数目"),

    SDM_AID_Inf_Name(0x0200000200000001L, "接口名称"),

    SDM_AID_Inf_Descr(0x0200000200000002L, "接口描述"),

    SDM_AID_Inf_Type(0x0200000200000003L, "接口类型"),

    SDM_AID_Inf_Address(0x0200000200000004L, "物理地址"),

    SDM_AID_Inf_MT(0x0200000200000005L, "最大包长"),

    SDM_AID_Inf_Speed(0x0200000200000006L, "接口速率"),

    SDM_AID_Inf_Packet(0x0200000200000007L, "收到总包数"),

    SDM_AID_Inf_OtPacket(0x0200000200000008L, "发送总包数"),

    SDM_AID_Inf_ErrPacket(0x0200000200000009L, "出错包数"),

    SDM_AID_Inf_pDownTrapEnable(0x020000020000000AL, "是否允许接口发送trap包"),

    SDM_AID_Inf_LastChange(0x020000020000000BL, "接口被修改时间"),

    SDM_AID_Inf_CtrlStats(0x020000020000000CL, "控制接口添加和删除的状态变量"),

    SDM_AID__Mgr_InPacket(0x0300000100000000L, "收到的管理包数"),

    SDM_AID__Mgr_OtPacket(0x0300000200000000L, "发送的管理包数"),

    SDM_AID__Mgr_ErrPacket(0x0300000300000000L, "错误的管理包数"),

    SDM_AID__Mgr_WarningPacket(0x0300000400000000L, "发送的告警包数"),

    SDM_AID__Mgr_TrapOid(0x0300000500000001L, "设置阈值的对象"),

    SDM_AID__Mgr_TrapLVale(0x0300000500000002L, "低于多少告警"),

    SDM_AID__Mgr_TrapRVale(0x0300000500000003L, "高于多少告警"),

    SDM_AID__Mgr_TrapSetTime(0x0300000500000004L, "上次设置时间"),

    SDM_AID__Mgr_TrapMapCtrlStats(0x0300000500000005L, "告警行添加删除控制变量");

    private long code;

    private String msg;


    AIDEnum(long code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static AIDEnum getByAID(long aid) {
        AIDEnum[] aidEnums = AIDEnum.values();
        for (AIDEnum aidEnum : aidEnums) {
            if (aidEnum.code == aid) {
                return aidEnum;
            }
        }
        return null;
    }
    public long getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

}