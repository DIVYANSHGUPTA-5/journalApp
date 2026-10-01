package net.engineeringdigest.journalApp.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Chooses the transaction manager used by the {@code @Transactional} methods.
 * <p>
 * MongoDB multi-document transactions only work on a replica set (or a sharded cluster via mongos).
 * A plain local {@code mongod} is a standalone server, where every transactional call would fail with
 * "Transaction numbers are only allowed on a replica set member or mongos". So:
 * <ul>
 *   <li>replica set / mongos -> the regular {@link MongoTransactionManager} (real transactions, unchanged behaviour);</li>
 *   <li>standalone mongod    -> a no-op manager: the methods still work, just without atomicity.</li>
 * </ul>
 */
public final class MongoTransactionSupport {

    private static final Logger log = LoggerFactory.getLogger(MongoTransactionSupport.class);

    private MongoTransactionSupport() {
    }

    public static PlatformTransactionManager transactionManagerFor(MongoDatabaseFactory dbFactory) {
        try {
            Document hello = dbFactory.getMongoDatabase().runCommand(new Document("hello", 1));
            String replicaSet = hello.getString("setName");

            if (replicaSet != null || "isdbgrid".equals(hello.getString("msg"))) {
                log.info("MongoDB transactions: ENABLED ({})",
                        replicaSet != null ? "replica set " + replicaSet : "sharded cluster");
                return new MongoTransactionManager(dbFactory);
            }

            log.warn("MongoDB transactions: UNAVAILABLE - the server is a standalone mongod, not a replica set. "
                    + "@Transactional methods run WITHOUT a transaction (no atomicity). "
                    + "To get real transactions, run mongod as a single-node replica set (see README.md).");
            return new NoOpTransactionManager();

        } catch (Exception e) {
            // Could not probe the server: keep the original behaviour (real MongoDB transactions).
            log.warn("Could not determine whether MongoDB supports transactions ({}); using MongoTransactionManager.",
                    e.toString());
            return new MongoTransactionManager(dbFactory);
        }
    }

    /** Runs the transactional method without any transaction (standalone MongoDB only). */
    private static final class NoOpTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            // nothing to begin
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            // nothing to commit
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            // nothing to roll back
        }

        @Override
        protected void doSetRollbackOnly(DefaultTransactionStatus status) {
            // nothing to mark
        }
    }
}
