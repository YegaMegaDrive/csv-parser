package ru.alfabank.ufr.onespace.csv.parser.hazelcast.listener;

import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import lombok.extern.slf4j.Slf4j;
import ru.alfabank.ufr.onespace.csv.parser.hazelcast.HazelcastConsumer;
import ru.alfabank.ufr.onespace.csv.parser.domain.FileSeparatorJobRequest;

@Slf4j
public class HazelcastQueueListener implements ItemListener<FileSeparatorJobRequest> {
    private HazelcastConsumer hzConsumer;

    public HazelcastQueueListener(HazelcastConsumer hzService) {
        this.hzConsumer = hzService;
    }

    @Override
    public void itemAdded(ItemEvent<FileSeparatorJobRequest> item) {
        log.info("get Item from hazel: {}", item.getItem());
        try {
            hzConsumer.consume();
        }catch (Exception e){
            log.error("Error while mongoGridJob:{}",e.getMessage());
        }

    }

    @Override
    public void itemRemoved(ItemEvent<FileSeparatorJobRequest> item) {

    }

}
