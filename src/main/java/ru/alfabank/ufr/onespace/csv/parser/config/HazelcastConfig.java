package ru.alfabank.ufr.onespace.csv.parser.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class HazelcastConfig {

    @Bean
    public HazelcastInstance getHazelcast(){
        log.info("Connecting to Hazelcast on");
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("hello-world");
        clientConfig.getNetworkConfig()
              .setSmartRouting(false)
              .setAddresses(List.of("localhost:5701"/*,"localhost:5702"*/));
        return HazelcastClient.newHazelcastClient(clientConfig);
    }


}
