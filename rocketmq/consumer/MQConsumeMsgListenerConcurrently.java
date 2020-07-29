package com.cowain.base.modules.visitor.rocketmq.consumer;


import com.cowain.base.modules.visitor.dto.VisitorMessageCenterDto;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

@Component
public class MQConsumeMsgListenerConcurrently implements MessageListenerConcurrently {
    private static final Logger logger = LoggerFactory.getLogger(MQConsumeMsgListenerConcurrently.class);


    /**
     * 默认msgs里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息<br/>
     * 不要抛异常，如果没有return CONSUME_SUCCESS ，consumer会重新消费该消息，直到return CONSUME_SUCCESS
     */
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        logger.info("---chenwei开始消费-----");

        if (CollectionUtils.isEmpty(msgs)) {
            logger.info("接受到的消息为空，不处理，直接返回成功");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        //默认只消费一条，List<MessageExt>是假象
        MessageExt messageExt = msgs.get(0);

        //查表，解决幂等问题
        // int count =  messageCenterService.findMsgId(messageExt.getMsgId());
        int count = 0;
        if (count != 0) {
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }

        //将消息转换为对象
        VisitorMessageCenterDto entity = null;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(messageExt.getBody());
            ObjectInputStream stream = new ObjectInputStream(inputStream);
            entity = (VisitorMessageCenterDto) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (messageExt.getTags().equals("visitorErrorTag")) {
            int reconsume = messageExt.getReconsumeTimes();
            if (reconsume == 3) {//消息已经重试了3次，如果不需要再次消费，则返回成功
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }

            //把消息发送给保安
            System.out.println("保安【uzi】接收到报错信息！"+entity);


            // 如果没有return success ，consumer会重新消费该消息，直到return success
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

        }

        // 如果没有return success ，consumer会重新消费该消息，直到return success
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
