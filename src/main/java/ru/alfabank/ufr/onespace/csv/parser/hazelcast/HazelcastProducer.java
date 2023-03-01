package ru.alfabank.ufr.onespace.csv.parser.hazelcast;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.bson.types.ObjectId;
import org.springframework.util.Assert;
import ru.alfabank.ufr.onespace.csv.parser.domain.FileSeparatorJobRequest;

import java.util.concurrent.ConcurrentHashMap;

public class HazelcastProducer {
    private static final String FILE_QUEUE_NAME = "fileSeparatorQueue";
    private final HazelcastInstance hzInstance;
    private ConcurrentHashMap<String, Object> cache;

    public HazelcastProducer(ConcurrentHashMap<String, Object> cache,
          HazelcastInstance hzInstance) {
        this.cache = cache;
        this.hzInstance = hzInstance;
    }

    public void produce(ObjectId filename) {
        FileSeparatorJobRequest params = (FileSeparatorJobRequest) cache.get(
              "params");
        Assert.notNull(params, "params are null");
        params.setFileField(filename);
        try {
            getFilenamesQueue().put(params);
        } catch (InterruptedException e) {
            //TODO refactor
            throw new RuntimeException(e);
        }
    }

    public IQueue<FileSeparatorJobRequest> getFilenamesQueue() {
        return hzInstance.getQueue(FILE_QUEUE_NAME);
    }
}
