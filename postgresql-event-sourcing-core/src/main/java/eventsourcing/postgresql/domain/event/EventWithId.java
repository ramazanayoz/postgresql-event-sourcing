package eventsourcing.postgresql.domain.event;

import java.math.BigInteger;

public record EventWithId<T extends Event>(
        long id,
        BigInteger transactionId,
        T event
) {
}
