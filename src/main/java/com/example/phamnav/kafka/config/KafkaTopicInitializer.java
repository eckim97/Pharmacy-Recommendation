package com.example.phamnav.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Configuration
@Slf4j
public class KafkaTopicInitializer {

    private final AdminClient adminClient;
    private final NewTopic pharmacyTopic;

    public KafkaTopicInitializer(AdminClient adminClient, NewTopic pharmacyTopic) {
        this.adminClient = adminClient;
        this.pharmacyTopic = pharmacyTopic;
    }

    @PostConstruct
    public void initializeKafkaTopic() {
        try {
            Set<String> existingTopics = adminClient.listTopics().names().get();
            if (!existingTopics.contains(pharmacyTopic.name())) {
                adminClient.createTopics(Collections.singleton(pharmacyTopic)).all().get();
                log.info("pharmacy-data 토픽이 성공적으로 생성되었습니다.");
            } else {
                log.info("pharmacy-data 토픽이 이미 존재합니다.");
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Kafka 토픽 초기화 중 오류 발생: ", e);
        } finally {
            adminClient.close();
        }
    }
}