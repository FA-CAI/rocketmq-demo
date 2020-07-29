package com.cowain.base.modules.visitor.rocketmq.consumer;

import com.alibaba.druid.util.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class MQConsumerConfiguration {
    public static final Logger LOGGER = LoggerFactory.getLogger(MQConsumerConfiguration.class);
    @Value("${rocketmq.consumer.namesrvAddr}")
    private String namesrvAddr;
    @Value("${rocketmq.consumer.groupName}")
    private String groupName;
    @Value("${rocketmq.consumer.consumeThreadMin}")
    private int consumeThreadMin;
    @Value("${rocketmq.consumer.consumeThreadMax}")
    private int consumeThreadMax;
    @Value("${rocketmq.topics.topic.name}")
    private String topicName;
    @Value("${rocketmq.consumer.consumeMessageBatchMaxSize}")
    private int consumeMessageBatchMaxSize;

    @Autowired
    private MQConsumeMsgListenerConcurrently mqMessageListenerProcessor;

    @Bean
    public DefaultMQPushConsumer getRocketMQConsumer() throws MQClientException {
        if (StringUtils.isEmpty(groupName)) {
            throw new MQClientException(10001, "custom groupName is null");
        }
        if (StringUtils.isEmpty(namesrvAddr)) {
            throw new MQClientException(10002, "custom namesrvAddr is null");
        }
        if (StringUtils.isEmpty(topicName)) {
            throw new MQClientException(10003, "custom topicName is null");
        }
        // consumerGroup  ?? 默认设置？
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.setConsumeThreadMin(consumeThreadMin);
        consumer.setConsumeThreadMax(consumeThreadMax);
        consumer.registerMessageListener(mqMessageListenerProcessor);
        // offset设置：一个新的订阅组第一次启动从队列的最前位置开始消费。后续再启动接着上次消费的进度开始消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        //消费模式：集群（默认）
        consumer.setMessageModel(MessageModel.CLUSTERING);
        //批量消费。默认是1
        //consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
        //默认了————“访客报错信息”专用mq
        // 设置该消费者订阅的主题和tag，如果是订阅该主题下的所有tag，则tag使用*；如果需要指定订阅该主题下的某些tag，则使用||分割，例如tag1||tag2||tag3
        consumer.subscribe(topicName, "*");
        try {
            consumer.start();
            LOGGER.info("consumer  start 正常， groupName:{},topics:{},namesrvAddr:{}", groupName, topicName, namesrvAddr);
        } catch (MQClientException e) {
            LOGGER.error("consumer start 异常，groupName:{},topics:{},namesrvAddr:{}", groupName, topicName, namesrvAddr, e);
            e.printStackTrace();
        }
        return consumer;
    }

}
