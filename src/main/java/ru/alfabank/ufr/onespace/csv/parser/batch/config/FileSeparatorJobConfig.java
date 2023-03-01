package ru.alfabank.ufr.onespace.csv.parser.batch.config;


import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.alfabank.ufr.onespace.csv.parser.batch.listeners.FIleSeparatorChunkListener;
import ru.alfabank.ufr.onespace.csv.parser.batch.writers.GridFsItemWriter;
import ru.alfabank.ufr.onespace.csv.parser.hazelcast.HazelcastProducer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class FileSeparatorJobConfig {
    @Value("${chunk.size}")
    private Integer chunkSize;

    @Bean
    public Job fileSeparatorJob(JobBuilderFactory jobBuilderFactory)
          throws Exception {
        return jobBuilderFactory.get("fileSeparatorJob")
              .incrementer(new RunIdIncrementer())
              .flow(fileSeparatorStep(null, null))
              .end()
              .build();
    }

    @Bean
    public Step fileSeparatorStep(StepBuilderFactory stepBuilderFactory,
          FIleSeparatorChunkListener listener) throws Exception {
        return stepBuilderFactory.get("fileSeparatorStep")
              .<String, String>chunk(chunkSize)
              .reader(synchronizedReader())
              .processor(fileSeparatorItemProcessor())
              .writer(fileSeparatorItemWriter(null, null))
              .throttleLimit(1)
              .taskExecutor(fileSeparatorTaskExecutor())
              .listener(listener)
              .build();
    }

    @Bean
    @StepScope
    public SynchronizedItemStreamReader<String> synchronizedReader()
          throws Exception {
        SynchronizedItemStreamReader<String> reader =
              new SynchronizedItemStreamReader<>();
        reader.setDelegate(fileSeparatorItemReader(null));
        reader.afterPropertiesSet();
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> fileSeparatorItemReader(
          ConcurrentHashMap<String, Object> cache) {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();
        reader.setName("fileSeparatorItemReader");
        reader.setResource(
              new FileSystemResource(cache.get("filename") + ".csv"));
        reader.setLineMapper(new PassThroughLineMapper());
        String encoding = (String) cache.get("encoding");
        reader.setEncoding(encoding);
        log.info("initialize reader with encoding - {}", encoding);
        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<String, String> fileSeparatorItemProcessor() {
        return (transaction) -> transaction;
    }

    @Bean
    @StepScope
    public GridFsItemWriter fileSeparatorItemWriter(GridFsTemplate template,
          ConcurrentHashMap<String, Object> cache) {
        GridFsItemWriter writer = new GridFsItemWriter();
        writer.setTemplate(template);
        writer.setCache(cache);
        writer.setDelegate(producer(null, null));
        return writer;
    }

    @Bean
    @StepScope
    public HazelcastProducer producer(
          @Qualifier("getHazelcast") HazelcastInstance hzInstance,
          ConcurrentHashMap<String, Object> cache) {
        return new HazelcastProducer(cache, hzInstance);
    }

    @Bean
    public TaskExecutor fileSeparatorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(32);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(64);
        executor.setThreadNamePrefix("fileSeparator");
        executor.setRejectedExecutionHandler(
              new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
