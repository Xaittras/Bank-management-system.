package javarax.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javarax.event.MoneyDepositedEvent;
import javarax.event.MoneyWithdrawnEvent;
import javarax.event.TransactionCreatedEvent;
import javarax.kafka.KafkaTopics;


@Component
@KafkaListener(topics = KafkaTopics.TRANSACTION_EVENTS, groupId = "audit-service")
public class TransactionAuditConsumer {

	private static final Logger log = LoggerFactory.getLogger(TransactionAuditConsumer.class);

	@KafkaHandler
	public void onTransactionCreated(TransactionCreatedEvent event) {
		log.info("[Audit] Транзакція #{}: рахунок={}, тип={}, сума={}, час={}",
				event.transactionId(), event.accountId(), event.type(), event.amount(), event.occurredAt());
	}

	@KafkaHandler
	public void ignoreDeposit(MoneyDepositedEvent event) {

	}

	@KafkaHandler
	public void ignoreWithdraw(MoneyWithdrawnEvent event) {

	}
}
