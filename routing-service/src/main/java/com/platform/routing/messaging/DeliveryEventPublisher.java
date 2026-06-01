package com.platform.routing.messaging;

import com.platform.common.model.EventEnvelope;
import com.platform.routing.config.RabbitRoutingConfig;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DeliveryEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    public DeliveryEventPublisher(RabbitTemplate rabbitTemplate, OpenTelemetry openTelemetry) {
        this.rabbitTemplate = rabbitTemplate;
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer(DeliveryEventPublisher.class.getName());
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(mapper));
    }

    private static final TextMapSetter<MessageProperties> setter = (carrier, key, value) -> {
        if (carrier != null) {
            carrier.setHeader(key, value);
        }
    };

    public void publish(EventEnvelope eventEnvelope, String routingKey, int priority) {
        Span span = tracer.spanBuilder("queue-publish")
                .setAttribute("eventId", eventEnvelope.getEventId().toString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            rabbitTemplate.convertAndSend(
                    RabbitRoutingConfig.DELIVERY_EXCHANGE,
                    routingKey,
                    eventEnvelope,
                    message -> {
                        message.getMessageProperties().setPriority(priority);

                        // Inject trace context into message headers
                        openTelemetry.getPropagators()
                                .getTextMapPropagator()
                                .inject(Context.current(), message.getMessageProperties(), setter);

                        return message;
                    });
        } catch (Exception ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR);
            throw ex;
        } finally {
            span.end();
        }
    }
}
