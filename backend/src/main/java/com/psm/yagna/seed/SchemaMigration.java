package com.psm.yagna.seed;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate's ddl-auto=update never relaxes existing constraints, so changes that remove a constraint from an
 * already created database are applied here. Every step is idempotent and runs before the data seeder.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);

    private final JdbcTemplate jdbc;

    public SchemaMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Haribhakts are identified by name + mandal now; the old BAPS ID column is kept but is no longer required.
        Integer requiredBapsId = jdbc.queryForObject("""
                select count(*) from information_schema.columns
                where lower(table_name) = 'haribhakt' and lower(column_name) = 'baps_id'
                  and is_nullable = 'NO' and table_schema = current_schema()
                """, Integer.class);
        if (requiredBapsId != null && requiredBapsId > 0) {
            jdbc.execute("alter table haribhakt alter column baps_id drop not null");
            log.info("Migration: haribhakt.baps_id is now optional");
        }

        // A haribhakt may book several vanchan or shravan slots on the same date.
        for (String[] constraint : new String[][] {
                {"vanchan_booking", "uk_vanchan_haribhakt_date"}, {"shravan_booking", "uk_shravan_haribhakt_date"}}) {
            List<String> existing = jdbc.queryForList("""
                    select constraint_name from information_schema.table_constraints
                    where lower(table_name) = ? and lower(constraint_name) = ? and table_schema = current_schema()
                    """, String.class, constraint[0], constraint[1]);
            if (!existing.isEmpty()) {
                jdbc.execute("alter table " + constraint[0] + " drop constraint " + existing.get(0));
                log.info("Migration: dropped {} from {}", constraint[1], constraint[0]);
            }
        }
    }
}
