package javarax.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Тонка обгортка над KafkaTemplate для публікації доменних подій.
 *
 * Ключ повідомлення (partition key) — завжди accountId/userId у вигляді String.
 * Це гарантує, що всі події одного рахунку потраплять в одну partition
 * і будуть оброблені consumer'ом СТРОГО в тому порядку, в якому відбулись
 * (наприклад: спочатку MoneyDepositedEvent, потім TransactionCreatedEvent для того ж рахунку).
 *
 * NB (для співбесіди): це "dual write" — ми пишемо в Postgres і в Kafka окремими операціями,
 * тому теоретично можлива ситуація, коли транзакція в БД закомітилась, а подія в Kafka
 * не надіслалась (напр. Kafka тимчасово недоступна). У продакшн-системах цю проблему
 * вирішують патерном Transactional Outbox (подія спершу пишеться в ту ж БД-транзакцію
 * в окрему outbox-таблицю, а окремий процес/CDC вже гарантовано доставляє її в Kafka)./
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Не вдалося опублікувати подію {} в топік {}: {}",
                                event.getClass().getSimpleName(), topic, ex.getMessage());
                    } else {
                        log.debug("Подію {} опубліковано в {} (partition={}, offset={})",
                                event.getClass().getSimpleName(),
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
