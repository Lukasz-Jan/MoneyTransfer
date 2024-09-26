package com.lj;


/*
 * All program is written and owned by Lukasz Janowski
 * email: lukasz_jan@vp.pl
 */

import java.io.IOException;
import javax.annotation.PostConstruct;
import com.lj.service.AddAccountService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;


@SpringBootApplication
@EnableJpaRepositories(enableDefaultTransactions = false)
public class TransferApplication {

    private static final Logger log = LoggerFactory.getLogger(TransferApplication.class);

    private final String brokerUrl;
    private final AddAccountService accountSetUp;

    @PostConstruct
    private void init() throws JsonProcessingException, IOException {
        accountSetUp.init();
    }

    @Autowired
    //public TransferApplication(@Value("${spring.activemq.broker-url}") String brokerUrl, AddAccountService accountSetUp) {
    public TransferApplication(@Value("${spring.activemq.broker-url}") String brokerUrl, AddAccountService accountSetUp) {

        this.brokerUrl = brokerUrl;
        this.accountSetUp = accountSetUp;
        log.info("brokerUrl: " + brokerUrl);
    }


    public static void main(String[] args) throws InterruptedException {

        System.out.println("args.length: " + args.length);

        ConfigurableApplicationContext appCtx = SpringApplication.run(TransferApplication.class, args);
    }


    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        return activeMQConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {

        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(activeMQConnectionFactory());
        return jmsTemplate;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory());
        return factory;
    }
}