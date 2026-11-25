package enterprise.sharma.prateek.inventory.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import enterprise.sharma.prateek.inventory.entity.OutboxEvent;
import jakarta.transaction.Transactional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

	@Query(value = """
			SELECT *
			FROM OUTBOX_EVENTS
			WHERE STATUS = 'PENDING'
			  AND (
			      NEXT_RETRY_AT IS NULL
			      OR NEXT_RETRY_AT <= CURRENT_TIMESTAMP
			  )
			ORDER BY CREATED_AT
			""", nativeQuery = true)
	List<OutboxEvent> findEligiblePendingEvents(Pageable pageable);

	@Modifying
	@Transactional
	@Query(value = """
			UPDATE OUTBOX_EVENTS
			SET STATUS = 'PROCESSING',
			    CLAIMED_AT = CURRENT_TIMESTAMP
			WHERE ID = :eventId
			  AND STATUS = 'PENDING'
			""", nativeQuery = true)
	int claimEvent(@Param("eventId") UUID eventId);

	@Modifying
	@Transactional
	@Query(value = """
			UPDATE OUTBOX_EVENTS
			SET STATUS = 'PUBLISHED',
			    PUBLISHED_AT = CURRENT_TIMESTAMP,
			    CLAIMED_AT = NULL,
			    ERROR_MESSAGE = NULL
			WHERE ID = :eventId
			  AND STATUS = 'PROCESSING'
			""", nativeQuery = true)
	int markPublished(@Param("eventId") UUID eventId);

	@Modifying
	@Transactional
	@Query(value = """
			UPDATE OUTBOX_EVENTS
			SET RETRY_COUNT = RETRY_COUNT + 1,
			    ERROR_MESSAGE = :error,
			    CLAIMED_AT = NULL,
			    STATUS = CASE
			        WHEN RETRY_COUNT + 1 >= 10 THEN 'FAILED'
			        ELSE 'PENDING'
			    END,
			    NEXT_RETRY_AT = CASE
			        WHEN RETRY_COUNT + 1 >= 10 THEN NULL
			        ELSE DATE_ADD(
			            CURRENT_TIMESTAMP,
			            INTERVAL LEAST(300, POW(2, RETRY_COUNT + 1)) SECOND
			        )
			    END
			WHERE ID = :eventId
			  AND STATUS = 'PROCESSING'
			""", nativeQuery = true)
	int markFailed(@Param("eventId") UUID eventId, @Param("error") String error);

	@Modifying
	@Transactional
	@Query(value = """
			UPDATE OUTBOX_EVENTS
			SET STATUS = 'PENDING',
			    CLAIMED_AT = NULL,
			    ERROR_MESSAGE = 'Recovered stale event'
			WHERE STATUS = 'PROCESSING'
			  AND CLAIMED_AT <
			      DATE_SUB(CURRENT_TIMESTAMP, INTERVAL :minutes MINUTE)
			""", nativeQuery = true)
	int recoverStaleEvents(@Param("minutes") int minutes);

}
