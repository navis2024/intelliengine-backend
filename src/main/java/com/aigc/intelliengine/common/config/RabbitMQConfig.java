package com.aigc.intelliengine.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "intelliengine.direct";
    public static final String QUEUE_VIDEO_EXTRACT = "q.video.extract";
    public static final String QUEUE_VIDEO_DLQ = "q.video.extract.dlq";
    public static final String RK_VIDEO_EXTRACT = "video.extract";

    @Bean
    public DirectExchange intelliengineExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue videoExtractQueue() {
        return QueueBuilder.durable(QUEUE_VIDEO_EXTRACT)
                .deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(QUEUE_VIDEO_DLQ)
                .build();
    }

    @Bean
    public Queue videoExtractDlq() {
        return QueueBuilder.durable(QUEUE_VIDEO_DLQ).build();
    }

    @Bean
    public Binding videoExtractBinding() {
        return BindingBuilder.bind(videoExtractQueue())
                .to(intelliengineExchange()).with(RK_VIDEO_EXTRACT);
    }

    @Bean
    public Binding videoExtractDlqBinding() {
        return BindingBuilder.bind(videoExtractDlq())
                .to(intelliengineExchange()).with(QUEUE_VIDEO_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
