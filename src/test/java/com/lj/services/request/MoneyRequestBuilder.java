package com.lj.services.request;

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import com.lj.entity.*;
import com.lj.repository.*;
import com.lj.gen.xsd.mappings.transfer.ActionType;
import com.lj.gen.xsd.mappings.transfer.TransferRequestType;
import com.lj.gen.xsd.mappings.transfer.ObjectFactory;


public class MoneyRequestBuilder {

    public static class Builder {

        private String currency;
        private String acctNo;
        private ActionType action;
        private BigDecimal quantity;
        private ObjectFactory objFact = new ObjectFactory();

        public JAXBElement<TransferRequestType> buildReqEl() {

            System.out.println("buildReqEl from test package");

            TransferRequestType request = objFact.createTransferRequestType();
            JAXBElement<TransferRequestType> el = objFact.createTransferRequest(request);
            request.setRequestId(generateUUID());
            request.setTargetAccountNumber(acctNo);
            request.setAction(action);
            request.setCurrency(this.currency);
            request.setQuantity(this.quantity);
            return el;
        }

        public String buildRequestXmlAsString() {

            JAXBElement<TransferRequestType> el = buildReqEl();

            try {
                JAXBContext ctx = JAXBContext.newInstance(TransferRequestType.class);
                Marshaller m = ctx.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                //m.marshal(el, new File("req.xml"));
                Writer writer = new StringWriter();
                m.marshal(el, writer);
                return writer.toString();

            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Builder setAcctNo(String acctNo) {
            this.acctNo = acctNo;
            return this;
        }

        public Builder setAction(ActionType action) {
            this.action = action;
            return this;
        }

        public Builder  setQuantity(BigDecimal quantity) {
            this.quantity = quantity.setScale(2, RoundingMode.HALF_UP);
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        private String generateUUID() {

            return UUID.randomUUID().toString();
        }
    }


}