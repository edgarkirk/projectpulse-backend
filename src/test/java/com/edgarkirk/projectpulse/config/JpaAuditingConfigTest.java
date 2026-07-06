package com.edgarkirk.projectpulse.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JpaAuditingConfigTest {

    @Test
    void should_exist() {
        assertThat(JpaAuditingConfig.class).isNotNull();
    }
}
