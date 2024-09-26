package com.lj.services;


import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import javax.xml.bind.JAXBElement;

import com.lj.repository.AcctRepo;
import com.lj.repository.TransactionRepo;
import com.lj.service.TransacionService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import com.lj.TransferApplication;
import com.lj.entity.ServiceAgreement;
import com.lj.gen.xsd.mappings.transfer.ActionType;
import com.lj.gen.xsd.mappings.transfer.OutcomeType;
import com.lj.gen.xsd.mappings.transfer.TransferRequestType;

import com.lj.services.request.MoneyRequestBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = TransferApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)

public class TransactionServiceIT {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceIT.class);

    @Autowired
    private TransacionService requestSrv;
    @Autowired
    private TransactionRepo trRepo;
    @Autowired
    private AcctRepo acctRepo;

    private static final String TEMP_FILE_FOR_TEST = "src/test/resources/tempDataForTest.json";

    @Autowired
    @Value("${fileWithAccountsInitPath}") String initializationAccountDataPath;

    /*
     * TransactionService::processRequest() operation is tested
     * Request for account 100056013005 is built of (type TransferRequestType).
     * Income for USD is processed (1027.88).
     * Operation result is checked (type OutcomeType) and balance.
     */
    @Test
    public void testIncomeBasicOne() {

        BigDecimal income = BigDecimal.valueOf(1027.88);

        String account = "100056013005";
        String currency = "USD";
        JAXBElement<TransferRequestType> buildReqEl = new MoneyRequestBuilder.Builder().setAcctNo(account).setAction(ActionType.CREDIT).setCurrency(currency).setQuantity(income).buildReqEl();

        TransferRequestType req = buildReqEl.getValue();

        ServiceAgreement saFound = acctRepo.findById(req.getTargetAccountNumber()).map(acct -> {

            ServiceAgreement saInner = acct.getAgreements().stream().filter(sa -> sa.getCurrencyCd().equals(currency)).findFirst().map(sa -> sa).orElse(null);
            return saInner;
        }).orElse(null);


        BigDecimal beforeIncomeBalance = trRepo.findBySaId(saFound.getSaId()).stream().map(tr -> tr.getCurAmt()).reduce(BigDecimal.ZERO, BigDecimal::add);

        OutcomeType outResult = requestSrv.processRequest(req);

        assertEquals(OutcomeType.ACCEPT, outResult);

        BigDecimal afterIncomeBalance = trRepo.findBySaId(saFound.getSaId()).stream().map(tr -> tr.getCurAmt()).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal afterIncomeShouldBe = beforeIncomeBalance.add(income);

        assertEquals(afterIncomeBalance, afterIncomeShouldBe);


    }

    /*
     * TransactionService::processRequest() operation is tested
     * Outcome Request (debit) is built for account 100056013005 of (type TransferRequestType).
     * Outcome-USD is processed for 777.55
     * Operation result is checked (type OutcomeType) and balance.
     */
    @Test
    public void test_Outcome_Basic_Positive() {

        BigDecimal outcome = BigDecimal.valueOf(777.55);

        String account = "100056013005";
        String currency = "USD";
        JAXBElement<TransferRequestType> buildReqEl = new MoneyRequestBuilder.Builder().setAcctNo(account).setAction(ActionType.DEBIT).setCurrency(currency).setQuantity(outcome).buildReqEl();

        TransferRequestType req = buildReqEl.getValue();

        ServiceAgreement saFound = acctRepo.findById(req.getTargetAccountNumber()).map(acct -> {

            ServiceAgreement saInner = acct.getAgreements().stream().filter(sa -> sa.getCurrencyCd().equals(currency)).findFirst().map(sa -> sa).orElse(null);
            return saInner;
        }).orElse(null);


        BigDecimal beforeOutcomeBalance = trRepo.findBySaId(saFound.getSaId()).stream().map(tr -> tr.getCurAmt()).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(BigDecimal.valueOf(50025.00).setScale(2), beforeOutcomeBalance);

        System.out.println("Before: " + beforeOutcomeBalance);

        OutcomeType outResult = requestSrv.processRequest(req);
        assertEquals(OutcomeType.ACCEPT, outResult);

        BigDecimal afterOutcomeBalance = trRepo.findBySaId(saFound.getSaId()).stream().map(tr -> tr.getCurAmt()).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal afterIncomeShouldBe = beforeOutcomeBalance.subtract(outcome);

        assertEquals(afterIncomeShouldBe, afterOutcomeBalance);
    }

    // Here no funds on account

    /*
     * Account are initialized basing on file example-transfer-systemTest.json.
     * TransactionService::processRequest() operation is tested
     * Outcome Request (debit) is built for account 100056013005.
     * Outcome-USD is processed for 54055.55 USD.
     * Available funds are only 50025.00 USD;
     * Operation result is checked (type OutcomeType) and balance.
     */

    @Test
    public void test_Outcome_Basic_Negative() throws IOException {

        BigDecimal income = BigDecimal.valueOf(54055.55);

        String account = "100056013005";
        String currency = "USD";
        JAXBElement<TransferRequestType> buildReqEl = new MoneyRequestBuilder.Builder().setAcctNo(account).setAction(ActionType.DEBIT).setCurrency(currency).setQuantity(income).buildReqEl();

        TransferRequestType req = buildReqEl.getValue();

        ServiceAgreement saFound = acctRepo.findById(req.getTargetAccountNumber()).map(acct -> {

            ServiceAgreement saInner = acct.getAgreements().stream().filter(sa -> sa.getCurrencyCd().equals(currency)).findFirst().map(sa -> sa).orElse(null);
            return saInner;
        }).orElse(null);


        BigDecimal beforeOutcomeBalance = trRepo.findBySaId(saFound.getSaId()).stream().map(tr -> tr.getCurAmt()).reduce(BigDecimal.ZERO, BigDecimal::add);

        OutcomeType outResult = requestSrv.processRequest(req);
        assertEquals(OutcomeType.REJECT, outResult);

        BigDecimal afterOutcomeBalance = trRepo.findBySaId(saFound.getSaId()).stream().map(tr -> tr.getCurAmt()).reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("After: " + afterOutcomeBalance);

        BigDecimal afterOutcomeShouldBe = beforeOutcomeBalance;

        assertEquals(afterOutcomeShouldBe, afterOutcomeBalance);

    }

    @BeforeAll
    public void init() throws IOException {

        copyFileWithAccountToTemporaryFile(initializationAccountDataPath, TEMP_FILE_FOR_TEST);
    }

    @AfterAll
    public void afterAll() throws IOException {

        copyTempFileWithAccountBackToOrigin( TEMP_FILE_FOR_TEST, initializationAccountDataPath);

    }

    private void copyFileWithAccountToTemporaryFile(String fromPathStr, String toPathStr) throws IOException {

        Path fromPath = Paths.get(fromPathStr);
        Path toPath = Paths.get(toPathStr);
        Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyTempFileWithAccountBackToOrigin(String fromPathStr, String toPathStr) throws IOException {

        Path fromPath = Paths.get(fromPathStr);
        Path toPath = Paths.get(toPathStr);
        Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
    }
}