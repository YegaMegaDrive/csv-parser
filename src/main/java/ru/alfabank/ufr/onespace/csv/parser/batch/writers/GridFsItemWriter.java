package ru.alfabank.ufr.onespace.csv.parser.batch.writers;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import ru.alfabank.ufr.onespace.csv.parser.hazelcast.HazelcastProducer;
import ru.alfabank.ufr.onespace.csv.parser.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class GridFsItemWriter implements ItemWriter<String>, InitializingBean {

    private static final String CONTENT_TYPE = "text/csv";
    private final Object bufferKey;
    private GridFsOperations template;
    private ConcurrentHashMap<String, Object> cache;
    private HazelcastProducer delegate;
    private boolean delete = false;

    public GridFsItemWriter() {
        super();
        this.bufferKey = new Object();
    }

    /**
     * Indicates if the items being passed to the writer are to be saved or
     * removed from the data store.  If set to false (default), the items will
     * be saved.  If set to true, the items will be removed.
     *
     * @param delete removal indicator
     **/
    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setDelegate(HazelcastProducer delegate) {
        this.delegate = delegate;
    }

    /**
     * Set the {@link ExecutionContext} to be used get filename from context.
     *
     * @param cache the context implementation to be used.
     */
    public void setCache(ConcurrentHashMap<String, Object> cache) {
        this.cache = cache;
    }

    /**
     * Get the {@link GridFsOperations} to be used to save items to be written.
     * This can be called by a subclass if necessary.
     *
     * @return template the template implementation to be used.
     */
    protected GridFsOperations getTemplate() {
        return template;
    }

    /**
     * Set the {@link GridFsOperations} to be used to save items to be written.
     *
     * @param template the template implementation to be used.
     */
    public void setTemplate(GridFsOperations template) {
        this.template = template;
    }

    /**
     * If a transaction is active, buffer items to be written just before
     * commit. Otherwise write items using the provided template.
     *
     * @see org.springframework.batch.item.ItemWriter#write(List)
     */
    @Override
    public void write(List<? extends String> items) throws Exception {
        if (!transactionActive()) {
            doWrite(items);
            return;
        }

        List<String> bufferedItems = getCurrentBuffer();
        bufferedItems.addAll(items);
    }

    /**
     * Performs the actual write to the store via the template. This can be
     * overridden by a subclass if necessary.
     *
     * @param items the list of items to be persisted.
     */
    protected void doWrite(List<? extends String> items) {
        if (!CollectionUtils.isEmpty(items)) {
            if (this.delete) {
                delete(items);
            } else {
                saveOrUpdate(items);
            }
        }
    }

    private void delete(List<? extends String> items) {
        if (cache.containsKey("output") && !((String) cache.get(
              "output")).isEmpty()) {
            Query query = new Query().addCriteria(
                  GridFsCriteria.whereFilename().is(cache.get("output")));
            template.delete(query);
        }
    }

    private void saveOrUpdate(List<? extends String> items) {
        String filename = prepareFileName();
        if (filename != null) {
            String fullString = Utils.convertListToString(items.stream()
                  .map(Objects::toString)
                  .collect(Collectors.toList()));
            String encoding = (String) cache.get("encoding");
            ObjectId id = template.store(
                  Utils.convertStringToInputStream(fullString, encoding), filename,
                  CONTENT_TYPE, prepareMetadata(encoding));
            delegate.produce(id);
        }
    }

    private boolean transactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @SuppressWarnings("unchecked")
    private List<String> getCurrentBuffer() {
        if (!TransactionSynchronizationManager.hasResource(bufferKey)) {
            TransactionSynchronizationManager.bindResource(bufferKey,
                  new ArrayList<String>());

            TransactionSynchronizationManager.registerSynchronization(
                  new TransactionSynchronization() {
                      @Override
                      public void beforeCommit(boolean readOnly) {
                          List<String> items =
                                (List<String>) TransactionSynchronizationManager.getResource(
                                      bufferKey);

                          if (!CollectionUtils.isEmpty(items)) {
                              if (!readOnly) {
                                  doWrite(items);

                              }
                          }
                      }

                      @Override
                      public void afterCompletion(int status) {
                          if (TransactionSynchronizationManager.hasResource(
                                bufferKey)) {
                              TransactionSynchronizationManager.unbindResource(
                                    bufferKey);
                          }

                      }
                  });
        }

        return (List<String>) TransactionSynchronizationManager.getResource(
              bufferKey);
    }

    @Override
    public void afterPropertiesSet() {
        Assert.state(template != null,
              "A GridFsOperations implementation is " + "required.");
    }

    private String prepareFileName() {
        String name = (String) cache.get(/*"output"*/"filename");
        if (name != null) {
            return name + ".csv";
        }
        return null;

    }

    private DBObject prepareMetadata(String encoding){
        DBObject metaData = new BasicDBObject();
        metaData.put("encoding", encoding);
        return metaData;
    }
}
