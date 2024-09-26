package com.lj.jms.response;



import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lj.gen.xsd.mappings.transfer.OutcomeType;
import com.lj.gen.xsd.mappings.transfer.TransferRequestType;
import com.lj.gen.xsd.mappings.transfer.ObjectFactory;
import com.lj.gen.xsd.mappings.transfer.TransferResponseType;

public class Response {

    public static class Builder {

        private final Logger logger = LoggerFactory.getLogger(Builder.class);

        private TransferRequestType requestType;
        private OutcomeType outcome;
        private ObjectFactory objFact = new ObjectFactory();

        public String buildResponseXmlAsString() {

            TransferResponseType responseType = createResponseType(requestType, outcome);
            JAXBElement<TransferResponseType> el = buildResponseElement(responseType);
            String xmlAsString = null;

            try (Writer writer = new StringWriter()) {

                JAXBContext ctx = JAXBContext.newInstance(TransferResponseType.class);
                Marshaller m = ctx.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                m.marshal(el, writer);
                xmlAsString = writer.toString();

            } catch (IOException | JAXBException e) {
                logger.error(e.toString());
            }
            return xmlAsString;
        }

        private JAXBElement<TransferResponseType> buildResponseElement(TransferResponseType responseType) {

            JAXBElement<TransferResponseType> el = objFact.createTransferResponse(responseType);
            return el;
        }

        private TransferResponseType createResponseType(TransferRequestType requestType, OutcomeType outcome) {

            TransferResponseType responseType = objFact.createTransferResponseType();
            responseType.setRequestId(requestType.getRequestId());
            responseType.setTargetAccountNumber(requestType.getTargetAccountNumber());
            responseType.setAction(requestType.getAction());
            responseType.setCurrency(requestType.getCurrency());
            responseType.setQuantity(requestType.getQuantity());
            responseType.setOutcome(outcome);
            return responseType;
        }

        public Builder setRequestType(TransferRequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public Builder setOutcome(OutcomeType outcome) {
            this.outcome = outcome;
            return this;
        }
    }

}