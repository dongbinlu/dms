package com.shudun.dms.handshake;

import com.shudun.dms.message.Message;

public interface IRequest {

    Message encode(byte[] data);


}
