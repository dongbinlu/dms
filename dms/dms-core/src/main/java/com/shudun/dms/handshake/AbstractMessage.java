package com.shudun.dms.handshake;

import com.shudun.dms.algorithm.AbstractMessageAlgorithm;
import com.shudun.dms.algorithm.GmECBAbstractMessageAlgorithm;
import com.shudun.dms.message.HeadInfo;

public abstract class AbstractMessage {

    private HeadInfo headInfo;

    protected HandShake handShake;

    protected AbstractMessageAlgorithm abstractMessageAlgorithm;

    public AbstractMessage(HandShake handShake) {
        this.handShake = handShake;
        initHeadInfo();
        abstractMessageAlgorithm = new GmECBAbstractMessageAlgorithm();
        this.handShake.setAbstractMessageAlgorithm(abstractMessageAlgorithm);
    }

    private void initHeadInfo() {
        headInfo = new HeadInfo();
        headInfo.setVersion((byte) 1);
        headInfo.setMsgId(handShake.getMsgId());
        headInfo.setSecureModel(secureModel());
        headInfo.setOpType(opType());
        headInfo.setDestId(handShake.getDestId());
        headInfo.setSourceId(handShake.getSourceId());
    }

    protected abstract byte secureModel();

    protected abstract byte opType();

    protected HeadInfo getHeadInfo() {
        return this.headInfo;
    }


}
