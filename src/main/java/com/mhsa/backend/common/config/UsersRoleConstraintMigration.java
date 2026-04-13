package com.mhsa.backend.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsersRoleConstraintMigration {

    private static final Logger log = LoggerFactory.getLogger(UsersRoleConstraintMigration.class);

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateUsersRoleConstraint() {
        Integer tableExists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public' AND table_name = 'users'
                """,
                Integer.class
        );

        if (tableExists == null || tableExists == 0) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
        jdbcTemplate.execute(
                """
                ALTER TABLE users
                ADD CONSTRAINT users_role_check
                CHECK (role IN (
                    'MANAGER', 'DEPENDENT', 'DOCTOR',
                    'PARENT', 'TEEN', 'THERAPIST', 'ADMIN'
                ))
                """
        );

        log.info("Ensured users_role_check supports legacy and current role values");
    }
}
