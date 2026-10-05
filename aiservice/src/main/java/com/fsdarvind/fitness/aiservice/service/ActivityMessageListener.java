package com.fsdarvind.fitness.aiservice.service;

import com.fsdarvind.fitness.aiservice.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityMessageListener {
    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity){
        log.info("Received activity for processing: {}",activity.getId());
        log.info(String.valueOf(activity));
    }
}
