package com.lj.services;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.function.Predicate;
import javax.xml.bind.JAXBElement;
import com.lj.dto.TransactionDto;
import com.lj.entities.Account;
import com.lj.entities.Transaction;
import com.lj.repository.AcctRepo;
import com.lj.repository.SaRepo;
import com.lj.repository.TransactionRepo;
import com.lj.services.jsonutils.Utils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import com.lj.TransferApplication;
import com.lj.entities.ServiceAgreement;
import com.lj.gen.xsd.mappings.transfer.ActionType;
import com.lj.gen.xsd.mappings.transfer.OutcomeType;
import com.lj.gen.xsd.mappings.transfer.TransferRequestType;
import com.lj.services.request.MoneyRequestBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = TransferApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//@Rollback(false)
public class TransactionServiceIT {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceIT.class);

    private final File resourceFile;

    @Autowired
    private TransacionService requestSrv;
    @Autowired
    private TransactionRepo trRepo;
    @Autowired
    private AcctRepo acctRepo;
    @Autowired
    private SaRepo saRepo;

    private final Utils jsonHelper;

    @BeforeAll
    public void init() throws IOException {

    }

    @AfterAll
    public void afterAll() throws IOException {

    }

    @Transactional
    public void clearDatabase() {
        trRepo.deleteAll();
        acctRepo.deleteAll();
        saRepo.deleteAll();
    }

    public TransactionServiceIT(@Value("${initDataFile}") String initFile) throws IOException {

        String resourcePath = initFile;
        if(!initFile.contains("classpath:data")) {
            resourcePath = "classpath:data/" + initFile;
        }

        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource = resourceLoader.getResource(resourcePath);

        resourceFile = resource.getFile();

        if(!resourceFile.exists()) throw new RuntimeException();
        jsonHelper = new Utils();
    }

//    @Autowired
//    @Value("${fileWithAccountsInitPath}") String initializationAccountDataPath;

    /*
     * TransactionService::processRequest() operation is tested
     * Request for account 100056013005 is built of (type TransferRequestType).
     * Income for USD is processed (1027.88).
     * Operation result is checked (type OutcomeType) and balance.
     */
    @Test
    @Order(1)
    @Transactional
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
        System.out.println("TEST_1");
//        System.out.println("acctRepo.findAll().size(): " + acctRepo.findAll().size());
        clearDatabase();
//        System.out.println("acctRepo.findAll().size(): " + acctRepo.findAll().size());
    }

    /*
     * TransactionService::processRequest() operation is tested
     * Outcome Request (debit) is built for account 100056013005 of (type TransferRequestType).
     * Outcome-USD is processed for 777.55
     * Operation result is checked (type OutcomeType) and balance.
     */

    @Test
    @Order(2)
    @Transactional
    public void test_Outcome_Basic_Positive() throws IOException {

        System.out.println("TEST_2");
        System.out.println("acctRepo.findAll().size(): " + acctRepo.findAll().size());

        BigDecimal outcome = BigDecimal.valueOf(777.55);
        String inputAcctId = "100056013005";
        String currency = "USD";

        Predicate<TransactionDto> currencyEqualsL = el -> el.getSa().getCurrencyCd().equals(currency);

        Set<TransactionDto> jsonTxs = jsonHelper.getTransactionsFromJsonForAcoountId(resourceFile, inputAcctId);

        TransactionDto txJson = jsonTxs.stream()
                .filter(currencyEqualsL)
                .findAny()
                .orElse(null);

        BigDecimal initAmount = txJson.getCurAmt();

        JAXBElement<TransferRequestType> buildReqEl = new MoneyRequestBuilder.Builder().setAcctNo(inputAcctId)
                .setAction(ActionType.DEBIT).setCurrency(currency)
                .setQuantity(outcome).buildReqEl();

        TransferRequestType req = buildReqEl.getValue();

        Account account = acctRepo.fetchAccountWithTransactions(inputAcctId);

        Set<Transaction> txs = account.getAgreements()
                .stream()
                .filter(a -> a.getCurrencyCd().equals(currency))
                .map(a -> a.getTransactions())
                .findAny()
                .orElse(null);

        assertEquals(1, txs.size());

        BigDecimal balanceBefore = txs.iterator().next().getCurAmt();

        assertEquals(0, initAmount.setScale(2).compareTo(balanceBefore));

        System.out.println("Before: " + balanceBefore);

        OutcomeType outResult = requestSrv.processRequest(req);
        assertEquals(OutcomeType.ACCEPT, outResult);

        Account accountAfterExpense = acctRepo.fetchAccountWithTransactions(inputAcctId);

        BigDecimal afterExpenseBalance = accountAfterExpense.getAgreements()
                .stream()
                .filter(a -> a.getCurrencyCd().equals(currency))
                .flatMap(a -> a.getTransactions().stream())
                .map(tx -> tx.getCurAmt())
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal afterExpenseShouldBe = balanceBefore.subtract(outcome);
        assertEquals(0, afterExpenseShouldBe.compareTo(afterExpenseBalance));


//        System.out.println("acctRepo.findAll().size(): " + acctRepo.findAll().size());
        clearDatabase();
//        System.out.println("acctRepo.findAll().size(): " + acctRepo.findAll().size());
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
    @Order(3)
    @Transactional
    public void test_Outcome_Basic_Negative() throws IOException {

//        System.out.println("TEST 3");
//        System.out.println("acctRepo.findAll().size(): " + acctRepo.findAll().size());

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

        clearDatabase();
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