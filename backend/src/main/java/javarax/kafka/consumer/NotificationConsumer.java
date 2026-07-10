package javarax.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javarax.event.MoneyDepositedEvent;
import javarax.event.MoneyWithdrawnEvent;
import javarax.kafka.KafkaTopics;

/**
 * Імітує notification-сервіс: "надсилає" користувачу сповіщення про операції.
 * У реальній системі тут був би виклик email/SMS/push-провайдера;
 * в pet-проєкті достатньо показати сам механізм асинхронної обробки подій.
 *
 * Технічна деталь (важливо для співбесіди): один топік "transaction-events" містить
 * декілька типів подій (MoneyDepositedEvent, MoneyWithdrawnEvent, TransactionCreatedEvent).
 * Щоб один consumer міг обробляти кілька типів payload'ів з одного топіку, Spring Kafka
 * вимагає @KafkaListener на класі + @KafkaHandler на методах (а не кілька окремих
 * @KafkaListener-методів — інакше Kafka розподілить партиції між ними, і кожне
 * повідомлення дістанеться лише одному з методів, а не обом).
 */
@Component
@KafkaListener(topics = KafkaTopics.TRANSACTION_EVENTS, groupId = "notification-service")
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaHandler
    public void onMoneyDeposited(MoneyDepositedEvent event) {
        log.info("[Notification] Рахунку {} зараховано {} грн. Новий баланс: {}",
                event.accountId(), event.amount(), event.newBalance());
        // тут був би виклик EmailService / SmsService і т.д.
    }

    @KafkaHandler
    public void onMoneyWithdrawn(MoneyWithdrawnEvent event) {
        log.info("[Notification] З рахунку {} знято {} грн. Новий баланс: {}",
                event.accountId(), event.amount(), event.newBalance());
    }

    @KafkaHandler(isDefault = true)
    public void onOther(Object event) {
        log.debug("[Notification] Проігноровано подію типу {}", event.getClass().getSimpleName());
    }
}
