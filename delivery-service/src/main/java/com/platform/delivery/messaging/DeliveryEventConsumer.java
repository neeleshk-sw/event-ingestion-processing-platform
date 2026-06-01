package com.platform.delivery.messaging;

import com.platform.common.model.EventEnvelope;
import com.platform.delivery.config.RabbitDeliveryConfig;
import com.platform.delivery.service.EventDeliveryService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final EventDeliveryService eventDeliveryService;
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    public DeliveryEventConsumer(EventDeliveryService eventDeliveryService,
            OpenTelemetry openTelemetry) {
        this.eventDeliveryService = eventDeliveryService;
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer(DeliveryEventConsumer.class.getName());
    }

    private static final TextMapGetter<Message> getter = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Message carrier) {
            return carrier.getMessageProperties().getHeaders().keySet();
        }

        @Override
        public String get(Message carrier, String key) {
            if (carrier == null || carrier.getMessageProperties().getHeaders().get(key) == null) {
                return null;
            }
            return carrier.getMessageProperties().getHeaders().get(key).toString();
        }
    };

    @RabbitListener(queues = {
            RabbitDeliveryConfig.DELIVERY_MOBILE_QUEUE,
            RabbitDeliveryConfig.DELIVERY_API_QUEUE,
            RabbitDeliveryConfig.DELIVERY_DEFAULT_QUEUE
    })
    public void consume(EventEnvelope eventEnvelope, Message message) {
        Context extractedContext = openTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), message, getter);

        Span span = tracer.spanBuilder("queue-consume")
                .setParent(extractedContext)
                .setAttribute("eventId", eventEnvelope.getEventId().toString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            logger.info(
                    "Delivery event consumed from queue, routingKey={}",
                    message.getMessageProperties().getReceivedRoutingKey());

            eventDeliveryService.deliver(eventEnvelope);
        } catch (Exception ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR);
            throw ex;
        } finally {
            span.end();
        }
    }
}
