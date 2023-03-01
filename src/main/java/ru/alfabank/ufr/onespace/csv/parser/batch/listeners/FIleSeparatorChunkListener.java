package ru.alfabank.ufr.onespace.csv.parser.batch.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@StepScope
public class FIleSeparatorChunkListener implements ChunkListener {

    private final AtomicInteger counter = new AtomicInteger(1);

    private final ConcurrentHashMap<String,Object> cacheMap;

    @Autowired
    public FIleSeparatorChunkListener(
          ConcurrentHashMap<String, Object> cacheMap) {
        this.cacheMap = cacheMap;
    }

    @Override
    public void afterChunk(ChunkContext context) {
        cacheMap.remove("output");
        log.info("counter = {}", incrementAndGet());
        log.info("execution context map : {}", cacheMap);
    }

    @Override
    public void beforeChunk(ChunkContext context) {
        String filename = (String) cacheMap.get("filename");
        cacheMap.put("output", filename /*+ "-" + counter.get()*/);
        log.info("counter = {}", counter.get());
        log.info("cache map : {}", cacheMap);
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        log.error("Error after chunk");
    }

    public int incrementAndGet(){
        return counter.incrementAndGet();
    }
}
