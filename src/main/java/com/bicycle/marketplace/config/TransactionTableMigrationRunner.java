package com.bicycle.marketplace.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionTableMigrationRunner {

    private final JdbcTemplate jdbcTemplate;

    public TransactionTableMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String[] tableNames = { "\"transaction\"", "transaction" };
        String[] columns = { "event_id", "listing_id", "seller_id", "deposit_id", "reservation_id" };
        int done = 0;
        for (String table : tableNames) {
            for (String col : columns) {
                String sql = "ALTER TABLE " + table + " ALTER COLUMN " + col + " DROP NOT NULL";
                try {
                    jdbcTemplate.execute(sql);
                    log.info("Transaction table: {} nullable ok", col);
                    done++;
                } catch (Exception e) {
                    log.warn("Transaction table migration: {} -> {}", sql, e.getMessage());
                }
            }
            if (done > 0) break;
        }
    }
}
