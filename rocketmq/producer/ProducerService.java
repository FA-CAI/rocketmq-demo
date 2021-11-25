package com.cowain.base.modules.visitor.rocketmq.producer;


import com.cowain.base.modules.visitor.dto.VisitorMessageCenterDto;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Service
public class ProducerService {
    @Value("${rocketmq.producer.groupName}")
    private String groupName;
    @Value("${rocketmq.producer.producerGroup}")
    private String producerGroup;
    @Value("${rocketmq.producer.namesrvAddr}")
    private String namesrvAddr;
    @Value("${rocketmq.topics.topic.name}")
    private String topics;
    @Value("${rocketmq.topics.topic.tags}")
    private String tags;
    @Value("${rocketmq.producer.retryTimesWhenSendFailed}")
    private Integer retryTimesWhenSendFailed;
    @Value("${rocketmq.producer.sendMsgTimeout}")
    private Integer sendMsgTimeout;

    @Autowired
    private DefaultMQProducer producer;

    public static final Logger LOGGER = LoggerFactory.getLogger(ProducerService.class);

    /**
     * 项目启动时运行
     */
    @PostConstruct
    public void initProducer() {
        //肯定不为空，永远执行不到的代码
        // if (null == producer) {
        //     producer = new DefaultMQProducer(producerGroup);
        //     producer.setNamesrvAddr(namesrvAddr);
        //     producer.setRetryTimesWhenSendFailed(retryTimesWhenSendFailed);
        // }
        try {
            producer.start();
            LOGGER.info(String.format("producer start 正常! groupName:[%s],namesrvAddr:[%s]", this.groupName, this.namesrvAddr));
        } catch (MQClientException e) {
            LOGGER.error("producer start 异常，groupName: {},namesrvAddr: {}", groupName, namesrvAddr);
            e.printStackTrace() ;

        }
    }



    /**
     *
     * 发送“访客报错提醒消息”方法
     * @param centerDto
     * @return
     */
    public String send(VisitorMessageCenterDto centerDto) {

        //搞复杂了
        ByteArrayOutputStream inputStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(inputStream);
            stream.writeObject(centerDto);
        } catch (IOException e) {
             e.printStackTrace();
        }
        byte[] msg = inputStream.toByteArray();

        SendResult sendResult = null;
        try {
            Message message = new Message(topics, tags, msg);
            sendResult = producer.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("rocketMq生产者-发送结果:{}",sendResult);
        return "{\"MsgId\":\"" + sendResult.getMsgId() + "\"}";
    }

    /**
     * 在开发中我们如果要在关闭spring容器后释放一些资源,通常的做法有如下几种:
     * 1.在方法上加上@PreDestroy注解
     *
     * 似乎并没有起作用
     */
    @PreDestroy
    public void shutDownProducer() {
          LOGGER.info("producerService正在被容器删除");
        if (producer != null) {
            producer.shutdown();
        }
    }
}
