package com.lj.jms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import com.lj.service.TransacionService;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import com.lj.gen.xsd.mappings.transfer.OutcomeType;
import com.lj.gen.xsd.mappings.transfer.TransferRequestType;
import com.lj.jms.response.ResponseService;

@Component
public class MessageListenerAdapter implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageListenerAdapter.class);

    private final Schema requestResponseSchema;

    private final TransacionService trSrv;

    private final ResponseService respSrv;

    @Autowired
    public MessageListenerAdapter(TransacionService trSrv, ResponseService respSrv, @Value("${messageXsd}") String requestResponseXsdPath) throws InstantiationException {

        this.trSrv = trSrv;
        this.respSrv = respSrv;

        String resourceFileName = fetchFileNameFromPath(requestResponseXsdPath);
        logger.info("Init xsd resource name: " + resourceFileName);

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String resourcePath = "classpath:" + resourceFileName;
        logger.info("resourcePath: " + resourcePath);
        Resource resource = resourceLoader.getResource(resourcePath);

        if(resource.exists()) {

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            StreamSource source = null;
            Schema requestResponseSchemaTemp = null;

            if (!resource.isFile()) {

                logger.info("Xsd Resource is fetched from jar : " + resourceFileName);

                try (InputStream inputStream = resource.getInputStream()) {
                    source = new StreamSource(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    requestResponseSchemaTemp = schemaFactory.newSchema(source);
                } catch (IOException | SAXException e2) {
                    logger.error("IO exception while xsd reading for resource stream: " + resourcePath);
                }
            } else {

                logger.info("Resource is fetched from file system : " + resourceFileName);

                try (InputStream inputStream = Files.newInputStream(Paths.get(requestResponseXsdPath))) {
                    source = new StreamSource(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    requestResponseSchemaTemp = schemaFactory.newSchema(source);
                } catch (IOException | SAXException e2) {
                    logger.error("IO exception while xsd reading for " + requestResponseXsdPath);
                }
            }
            requestResponseSchema = requestResponseSchemaTemp;
        }
        else {
            throw new InstantiationException("MessageListenerAdapter file not created");
        }
    }

    private String fetchFileNameFromPath(String requestResponseXsdPath) {

        String[] split = requestResponseXsdPath.split("/");
        int retIdx = split.length - 1;
        String ret = split[retIdx];
        return ret;
    }

    @JmsListener(destination = "${srcQueue}")
    @Override
    public void onMessage(Message mss) {

        if (mss instanceof TextMessage) {

            TextMessage textXmlMsg = (TextMessage) mss;
            TransferRequestType requestInstance = handleMessage(textXmlMsg);

            if (requestInstance != null) {

                logger.info("Request " + requestInstance.getRequestId() + " being processed");
                OutcomeType outcome = trSrv.processRequest(requestInstance);
                respSrv.sendResponseMessage(requestInstance, outcome);
            } else
                logger.info("errors occured for request");
        }
    }

    private TransferRequestType handleMessage(TextMessage jmsTxtMsg) {

        TransferRequestType requestInstance = null;
        JAXBElement<TransferRequestType> jaxEl = null;
        InputStream inputStream = null;
        XMLStreamReader reader = null;

        try {
            String requestXmlStr = jmsTxtMsg.getText();
            inputStream = new ByteArrayInputStream(requestXmlStr.getBytes());

            JAXBContext ctx = JAXBContext.newInstance(TransferRequestType.class);
            Unmarshaller u = ctx.createUnmarshaller();

            if(requestResponseSchema != null) {
                u.setSchema(requestResponseSchema);
            }

            final XMLInputFactory xif = XMLInputFactory.newInstance();

            reader = xif.createXMLStreamReader(inputStream);
            jaxEl = u.unmarshal(reader, TransferRequestType.class);
            requestInstance = jaxEl.getValue();

        } catch (JMSException | JAXBException | XMLStreamException e) {

            logger.info("Parsing Exception caught, incoming message not processed");
            logger.error(e.toString());
        } finally {

            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException | XMLStreamException e) {
                logger.error(e.toString());
                requestInstance = null;
            }
        }
        return requestInstance;
    }
}