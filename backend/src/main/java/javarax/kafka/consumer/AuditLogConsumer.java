package javarax.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javarax.event.AccountCreatedEvent;
import javarax.event.UserCreatedEvent;
import javarax.kafka.KafkaTopics;

/**
 * Імітує audit-сервіс для подій "user-events" та "account-events".
 * Обробку "transaction-events" винесено в окремий клас TransactionAuditConsumer,
 * бо цей топік містить кілька різних типів payload'ів (див. коментар там).
 */
@Component
public class AuditLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditLogConsumer.class);

    @KafkaListener(topics = KafkaTopics.USER_EVENTS, groupId = "audit-service")
    public void onUserCreated(UserCreatedEvent event) {
        log.info("[Audit] Новий користувач: id={}, email={}, createdAt={}",
                event.userId(), event.email(), event.createdAt());
    }

    @KafkaListener(topics = KafkaTopics.ACCOUNT_EVENTS, groupId = "audit-service")
    public void onAccountCreated(AccountCreatedEvent event) {
        log.info("[Audit] Новий рахунок: id={}, userId={}, accountNumber={}",
                event.accountId(), event.userId(), event.accountNumber());
    }
}
