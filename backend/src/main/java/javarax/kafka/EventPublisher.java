package javarax.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class EventPublisher {

	private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	/**
	 * Усередині транзакції подія відправляється ЛИШЕ після успішного коміту: інакше при відкаті
	 * (недостатньо коштів, збій) споживачі отримали б подію про операцію, якої не було.
	 * Залишковий ризик: збій між комітом і відправкою втратить подію; повне рішення це transactional outbox.
	 */
	public void publish(String topic, String key, Object event) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					send(topic, key, event);
				}
			});
		} else {
			send(topic, key, event);
		}
	}

	private void send(String topic, String key, Object event) {
		try {
			kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
				if (ex != null) {
					log.error("Не вдалося опублікувати подію {} в топік {}: {}",
							event.getClass().getSimpleName(), topic, ex.getMessage());
				} else {
					log.debug("Подію {} опубліковано в {} (partition={}, offset={})",
							event.getClass().getSimpleName(), topic,
							result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
				}
			});
		} catch (RuntimeException e) {
			// Збій Kafka не повинен ламати вже зафіксовану банківську операцію
			log.error("Помилка відправки події {} в {}", event.getClass().getSimpleName(), topic, e);
		}
	}
}
