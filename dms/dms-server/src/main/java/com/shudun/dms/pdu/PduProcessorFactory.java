package com.shudun.dms.pdu;


import com.shudun.dms.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public final class PduProcessorFactory {

    /**
     * 收集系统中所有{@link PduProcessor} 接口的实现。 key为在spring容器中Bean的名字
     */
    @Autowired(required = false)
    private Map<String, PduProcessor> processorMap;

    private static final List<PduProcessor> processors = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        if (processorMap != null) {
            Set<Map.Entry<String, PduProcessor>> entrySet = processorMap.entrySet();
            for (Map.Entry<String, PduProcessor> entry : entrySet) {
                registerProcessor(entry.getValue());
            }

        }
    }

    public void registerProcessor(PduProcessor pduProcessor) {
        processors.add(pduProcessor);
    }

    /**
     * 在工厂类处理器中查找可以处理当前消息的处理器
     *
     * @param message
     * @return
     */
    public PduProcessor getProcessor(Message message) {
        PduProcessor processor_ = null;
        for (PduProcessor processor : processors) {
            if (processor.validate(message)) {
                processor_ = processor;
                break;
            }
        }
        return processor_;
    }

}
