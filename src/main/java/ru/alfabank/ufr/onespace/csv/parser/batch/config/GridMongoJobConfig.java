package ru.alfabank.ufr.onespace.csv.parser.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.alfabank.ufr.onespace.csv.parser.domain.FileSeparatorJobRequest;
import ru.alfabank.ufr.onespace.csv.parser.entity.TestEntity;
import ru.alfabank.ufr.onespace.csv.parser.batch.listeners.GridMongoStepListener;
import ru.alfabank.ufr.onespace.csv.parser.utils.Utils;

import java.io.InputStream;

@Configuration
@EnableAsync
@Slf4j
public class GridMongoJobConfig {

    @Value("${chunk.size}")
    private Integer chunkSize;

    @Bean
    public Job gridMongoJob(JobBuilderFactory jobBuilderFactory)
          throws Exception {
        return jobBuilderFactory.get("gridMongoJob")
              .incrementer(new RunIdIncrementer())
              .flow(gridMongoStep(null, null))
              .end()
              .build();
    }

    @Bean
    public Step gridMongoStep(StepBuilderFactory stepBuilderFactory,
          GridMongoStepListener listener) {
        return stepBuilderFactory.get("gridMongoStep")
              .<TestEntity, TestEntity>chunk(chunkSize/100)
              .reader(mongoGridItemReader())
              .processor(gridMongoItemProcessor())
              .writer(mongoGridWriter(null))
              .taskExecutor(new SimpleAsyncTaskExecutor())
              .listener(listener)
              .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<TestEntity, TestEntity> gridMongoItemProcessor() {
        FileSeparatorJobRequest params =
              (FileSeparatorJobRequest) gridMongoExecutionContext().get("params");
        return (transaction) -> {
            transaction.setStatus(params.getStatus());
            transaction.setName(params.getName());
            transaction.setEndDate(params.getEndDate());
            transaction.setStartDate(params.getStartDate());
            transaction.setActionDate(params.getActionDate());
            transaction.setFileId(params.getFileField());
            return transaction;
        };
    }

    @Bean
    @StepScope
    public MongoItemWriter<TestEntity> mongoGridWriter(MongoTemplate mongoTemplate) {
        return new MongoItemWriterBuilder<TestEntity>().template(mongoTemplate).collection("notifications")
              .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<TestEntity> mongoGridItemReader() {
        InputStream stream = (InputStream) gridMongoExecutionContext().get(
              "fileBytes");
        String encoding = gridMongoExecutionContext().getString("encoding");
        return new FlatFileItemReaderBuilder<TestEntity>()
              .name("mongoGridItemReader")
              .resource(new InputStreamResource(stream))
              .delimited()
              .delimiter(";")
              .names("pinEq", "text")
              .encoding(encoding)
              .targetType(TestEntity.class)
              .build();
    }

    @Bean
    ExecutionContext gridMongoExecutionContext() {
        return new ExecutionContext();
    }

}
