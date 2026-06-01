package com.platform.routing.config;

import com.platform.common.filter.MdcMessagePostProcessor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitRoutingConfig {

    public static final String DELIVERY_EXCHANGE = "delivery.events.exchange";
    public static final String DELIVERY_MOBILE_QUEUE = "delivery.mobile.queue";
    public static final String DELIVERY_API_QUEUE = "delivery.api.queue";
    public static final String DELIVERY_DEFAULT_QUEUE = "delivery.default.queue";

    public static final String KEY_MOBILE = "delivery.mobile";
    public static final String KEY_API = "delivery.api";
    public static final String KEY_DEFAULT = "delivery.default";

    @Bean
    public ApplicationRunner decommissionLegacyQueue(AmqpAdmin amqpAdmin) {
        return args -> {
            amqpAdmin.deleteQueue("delivery.events.queue");
        };
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            MdcMessagePostProcessor mdcPostProcessor) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setBeforePublishPostProcessors(mdcPostProcessor);
        return template;
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE, true, false);
    }

    @Bean
    public Queue mobileQueue() {
        return QueueBuilder.durable(DELIVERY_MOBILE_QUEUE)
                .maxPriority(10)
                .build();
    }

    @Bean
    public Queue apiQueue() {
        return QueueBuilder.durable(DELIVERY_API_QUEUE)
                .maxPriority(10)
                .build();
    }

    @Bean
    public Queue defaultQueue() {
        return QueueBuilder.durable(DELIVERY_DEFAULT_QUEUE)
                .maxPriority(10)
                .build();
    }

    @Bean
    public Binding bindMobile(@Qualifier("mobileQueue") Queue mobileQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(mobileQueue).to(deliveryExchange).with(KEY_MOBILE);
    }

    @Bean
    public Binding bindApi(@Qualifier("apiQueue") Queue apiQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(apiQueue).to(deliveryExchange).with(KEY_API);
    }

    @Bean
    public Binding bindDefault(@Qualifier("defaultQueue") Queue defaultQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(defaultQueue).to(deliveryExchange).with(KEY_DEFAULT);
    }
}
