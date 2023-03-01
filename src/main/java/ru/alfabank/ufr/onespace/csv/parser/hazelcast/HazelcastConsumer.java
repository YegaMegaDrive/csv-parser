package ru.alfabank.ufr.onespace.csv.parser.hazelcast;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.alfabank.ufr.onespace.csv.parser.domain.FileSeparatorJobRequest;
import ru.alfabank.ufr.onespace.csv.parser.hazelcast.listener.HazelcastQueueListener;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;

@Component
public class HazelcastConsumer {
    private static final String FILE_QUEUE_NAME = "fileSeparatorQueue";
    private final HazelcastInstance hzInstance;

    private UUID listenerId;

    private ExecutionContext context;

    private Job mongoGridJob;

    private JobLauncher jobLauncher;

    @Autowired
    public HazelcastConsumer(
          @Qualifier("getHazelcast") HazelcastInstance hzInstance,
          @Qualifier("gridMongoExecutionContext") ExecutionContext context,
          @Qualifier("gridMongoJob") Job mongoGridJob,
          JobLauncher jobLauncher) {
        this.hzInstance = hzInstance;
        this.context = context;
        this.mongoGridJob = mongoGridJob;
        this.jobLauncher = jobLauncher;
    }

    @PostConstruct
    private void init() {
        addListener();
    }

    public void consume() throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException, JobParametersInvalidException,
          JobRestartException {
        FileSeparatorJobRequest jobParams = getFilenamesQueue().poll();
        context.put("params", jobParams);
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addDate("date", new Date());
        jobLauncher.run(mongoGridJob, builder.toJobParameters());
    }

    public IQueue<FileSeparatorJobRequest> getFilenamesQueue() {
        return hzInstance.getQueue(FILE_QUEUE_NAME);
    }

    public void removeListener() {
        getFilenamesQueue().removeItemListener(listenerId);
    }

    public void addListener() {
        listenerId = getFilenamesQueue().addItemListener(
              new HazelcastQueueListener(this), false);
    }
}
