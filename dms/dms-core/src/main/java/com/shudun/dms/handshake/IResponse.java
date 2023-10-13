package com.shudun.dms.handshake;

import com.shudun.dms.message.Message;

public interface IResponse {

    byte[] decode(Message msg);

}
