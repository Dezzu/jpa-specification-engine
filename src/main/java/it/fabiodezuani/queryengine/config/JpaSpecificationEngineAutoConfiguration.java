package it.fabiodezuani.queryengine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(JpaRepository.class)
@ComponentScan(basePackages = "it.fabiodezuani.queryengine")
public class JpaSpecificationEngineAutoConfiguration {
}
