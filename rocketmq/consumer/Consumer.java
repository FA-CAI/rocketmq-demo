package com.cowain.base.modules.visitor.rocketmq.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 自产自销，我吐了
 */
@Component
public class Consumer {
    @Value("${rocketmq.consumer.consumerGroup}")
    private String consumerGroup;
    @Value("${rocketmq.consumer.namesrvAddr}")
    private String namesrvAddr;
    @Value("${rocketmq.topics.topic.name}")
    private String topicName;
    @Value("${rocketmq.topics.topic.tags}")
    private String tags;

    public static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    private DefaultMQPushConsumer consumer;
    @Autowired
    private MQConsumeMsgListenerConcurrently mQConsumeMsgListenerConcurrently;

    @PostConstruct
    public void defaultMQPushConsumer() {
        // consumerGroup: visitorConsumer 没有用到默认设置  @Autowired
        // DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        // nameServer 192.168.188.26:9876
        // consumer.setNamesrvAddr(namesrvAddr);
        try {
            // topicName  visitorTopic ,  tags  visitorTag
            // consumer.subscribe(topicName, tags);
            // offset设置：一个新的订阅组第一次启动从队列的最前位置开始消费。后续再启动接着上次消费的进度开始消费
            // consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            //集群消费时允许失败重试(默认:3次)
            consumer.registerMessageListener(mQConsumeMsgListenerConcurrently);
            consumer.start();
            // System.out.println("[Consumer 已启动]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
