package com.lj.jms.response;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.lj.gen.xsd.mappings.transfer.OutcomeType;
import com.lj.gen.xsd.mappings.transfer.TransferRequestType;

@Component
public class ResponseService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);

    private final String respQueName;

    @Autowired
    JmsTemplate jmsTemplate;

    public ResponseService(@Value("${responseQueue}") String respQueName) {
        this.respQueName = respQueName;
    }

    public void sendResponseMessage(TransferRequestType requestType, OutcomeType outcome) {

        String responseXmlAsString = new Response.Builder().setRequestType(requestType).setOutcome(outcome)
                .buildResponseXmlAsString();

        if (responseXmlAsString != null) {
            jmsTemplate.convertAndSend(respQueName, responseXmlAsString);
            logger.info("Response sent");
            logger.debug(responseXmlAsString);
        }
        else logger.info("Message to response queue not sent");
    }
}