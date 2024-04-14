package com.example.eventsourcing.service.event;

import com.example.eventsourcing.config.KafkaTopicsConfig;
import com.example.eventsourcing.domain.AggregateType;
import com.example.eventsourcing.domain.OrderAggregate;
import com.example.eventsourcing.dto.OrderDto;
import com.example.eventsourcing.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import eventsourcing.postgresql.domain.Aggregate;
import eventsourcing.postgresql.domain.event.Event;
import eventsourcing.postgresql.domain.event.EventWithId;
import eventsourcing.postgresql.service.AggregateStore;
import eventsourcing.postgresql.service.event.AsyncEventHandler;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderIntegrationEventSender implements AsyncEventHandler {

    private final AggregateStore aggregateStore;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void handleEvent(EventWithId<Event> eventWithId) {
        Event event = eventWithId.event();
        Aggregate aggregate = aggregateStore.readAggregate(
                AggregateType.ORDER.toString(), event.getAggregateId(), event.getVersion());
        OrderDto orderDto = orderMapper.toDto(event, (OrderAggregate) aggregate);
        sendDataToKafka(orderDto);
    }

    @SneakyThrows
    private void sendDataToKafka(OrderDto orderDto) {
        log.info("Publishing integration event {}", orderDto);
        kafkaTemplate.send(
                KafkaTopicsConfig.TOPIC_ORDER_EVENTS,
                orderDto.orderId().toString(),
                objectMapper.writeValueAsString(orderDto)
        );
    }

    @Nonnull
    @Override
    public String getAggregateType() {
        return AggregateType.ORDER.toString();
    }
}
