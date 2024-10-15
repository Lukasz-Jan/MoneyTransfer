package com.lj.services;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.lj.TransferApplication;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.lj.dto.AccountDto;
import com.lj.dto.TransactionDto;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.repository.*;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import com.lj.entity.*;
import com.lj.services.jsonutils.Utils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;

// starting:
//	mvn failsafe:integration-test
// executes one goal without executing its entire phase and the preceding phases

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = TransferApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AddAccountServiceIT implements TestCommons {

    @Autowired
    private SaRepo saRepo;
    @Autowired
    private AcctRepo acctRepo;

    private final File initializationDataFile;

    @Autowired
    public AddAccountServiceIT(@Value("${initDataFile}") String initFile) throws IOException {

        final String resourcePath = "classpath:data/" + initFile;

        final ResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource = resourceLoader.getResource(resourcePath);

        initializationDataFile = resource.getFile();
        assertEquals("initAmountsForTest.json", initializationDataFile.getName());
        initializationDataFile.getName();
        if(!initializationDataFile.exists()) throw new RuntimeException();
    }

    private Utils jsonHelper;

    @BeforeAll
    public void init() throws IOException {
        jsonHelper = new Utils();
    }

    /*
     * Check if account was added while initial data loading
     */
    @Test
    public void addAccountsTestIT() throws Throwable {

        assertNotNull(initializationDataFile);

        String acctId = "999142006678";

        com.lj.gen.json.mappings.transfer.Account accountJson = jsonHelper.getAccountFromJson(initializationDataFile, acctId);
        assertNotNull(accountJson);

        Account acc = acctRepo.findById(acctId).get();
        assertNotNull(acc);
        assertEquals(accountJson.getAccountNumber(), acc.getAcctId());

        List<CurrencyAmount> jsonAgreements = jsonHelper.getAllAgreementsFromJsonForAccountId(initializationDataFile, acctId);

        Set<ServiceAgreement> agreements = acc.getAgreements();
        assertEquals(jsonAgreements.size(), agreements.size());

        for(ServiceAgreement sa: agreements) {

            Predicate<String> sameAgreement = arg -> arg.equals(sa.getCurrencyCd());

            Optional<String> jsonSaOpt = jsonAgreements.stream().map(cA -> cA.getCurrency())
                    .filter(sameAgreement).findAny();
            assertEquals(true, jsonSaOpt.isPresent());
        }

        Optional<ServiceAgreement> saOpt = acc.getAgreements().stream().filter(sa -> sa.getCurrencyCd().equals(currencyPLN)).findAny();
        ServiceAgreement sa = saOpt.get();

        Executable exe = () -> sa.getTransactions().size();

        assertThrows(LazyInitializationException.class, exe);
    }

    /*
     * CurrencyAmount-s for currency are fetched from initial json file.
     * Check if there are corresponding currency-agreements in database.
     */
    @Test
    public void testForJoinFetchIT() throws IOException {

        Set<Account> accounts = acctRepo.findAll();
        Set<AccountDto> accountSet = jsonHelper.getAllAccountsByFile(initializationDataFile);
        assertEquals(accounts.size(), accountSet.size());

        List<ServiceAgreement> agreements = accounts.stream().flatMap(acc -> acc.getAgreements().stream()).collect(Collectors.toList());
        Set<CurrencyAmount> agreementsFromJson = jsonHelper.getAllAgreementsFromJson(initializationDataFile);

        assertEquals(agreements.size(), agreementsFromJson.size());

        for(ServiceAgreement agr: agreements) {

            String currencyCd = agr.getCurrencyCd();
            Optional<CurrencyAmount> currencyAmount = agreementsFromJson.stream().filter(jAgr -> jAgr.getCurrency().equals(currencyCd)).findAny();

            assertEquals(true, currencyAmount.isPresent());
        }
    }

    /*
     * Checking data for test account 100056013005
     */

    @Test
    public void accountFromAgreementIT() throws IOException {

        String acctId = "100056013005";
        Optional<Account> accountOpt = acctRepo.findById(acctId);
        assertNotNull(accountOpt.get());

        Set<ServiceAgreement> saSet = accountOpt.map(acc -> acc.getAgreements()).orElse(null);

        List<CurrencyAmount> jsonAgreements = jsonHelper.getAllAgreementsFromJsonForAccountId(initializationDataFile, acctId);

        assertEquals(saSet.size(), jsonAgreements.size());

        String saCurrencyCd = saSet.iterator().next().getCurrencyCd();

        String jsonCurrency = jsonAgreements.iterator().next().getCurrency();

        assertEquals(saCurrencyCd, jsonCurrency);

        Long saId = saSet.iterator().next().getSaId();

        Set<Transaction> txsSet = saRepo.findById(saId).map(saFound -> saFound.getTransactions()).orElse(null);

        Set<TransactionDto> txsSetJson = jsonHelper.getTransactionsFromJsonForAcoountId(initializationDataFile, acctId);

        assertEquals(txsSet.size(), txsSetJson.size());
        assertEquals(1, txsSetJson.size());

        //BigDecimal sumForSa = txsSet.stream().map(t -> t.getCurAmt()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saSum = txsSet.stream().map(Transaction::getCurAmt).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal jsonSum = txsSetJson.stream().map(TransactionDto::getCurAmt).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(jsonSum.setScale(2), saSum.setScale(2));
    }

    /*
     * Check for test account 999142006678
     * The same data in database and in file.
     */
    @Test
    public void fetchTransactionsByAccountIT() throws IOException {

        String acctId = "999142006678";

        Set<TransactionDto> txsDromJson = jsonHelper.getTransactionsFromJsonForAcoountId(initializationDataFile, acctId);

        Account acc = acctRepo.fetchAccountWithTransactions(acctId);
        List<Transaction> txs = acc.getAgreements().stream().flatMap(sa -> sa.getTransactions().stream()).collect(Collectors.toList());

        assertEquals(txsDromJson.size(), txs.size());

        List<TransactionDto> sekTxFromJson = txsDromJson.stream()
                .filter(txDto -> txDto.getSa().getCurrencyCd()
                        .equals(currencySEK)).collect(Collectors.toList());

        assertEquals(1, sekTxFromJson.size());
        List<Transaction> sekTxsListBase = acc.getAgreements().stream().
                filter(a -> a.getCurrencyCd().equals(currencySEK)).flatMap(strA -> strA.getTransactions().stream()).collect(Collectors.toList());

        assertEquals(1, sekTxsListBase.size());


        for(TransactionDto txJson: sekTxFromJson) {

            List<Transaction> txsFromBase = sekTxsListBase.stream()
                    .filter(tx -> tx.getSa().getCurrencyCd().equals(currencySEK))
                    .filter(tx -> (tx.getCurAmt().setScale(2).compareTo(txJson.getCurAmt()) == 0))
                    .collect(Collectors.toList());

            assertEquals(1, txsFromBase.size());

            Transaction txBase = txsFromBase.get(0);

            assertEquals(0, txJson.getCurAmt().compareTo(txBase.getCurAmt().setScale(2) )   );

            assertEquals(txJson.getSa().getCurrencyCd(), txBase.getSa().getCurrencyCd());
        }

        List<TransactionDto> plnTxFromJson = txsDromJson.stream()
                .filter(txDto -> txDto.getSa().getCurrencyCd().equals(currencyPLN))
                .collect(Collectors.toList());
        assertEquals(1, plnTxFromJson.size());

        List<Transaction> plnTxListBase = acc.getAgreements().stream().
                filter(a -> a.getCurrencyCd().equals(currencyPLN))
                .flatMap(strA -> strA.getTransactions().stream())
                .collect(Collectors.toList());
        assertEquals(1, plnTxListBase.size());

        for(TransactionDto txJson: plnTxFromJson) {

            List<Transaction> txsFromBase = plnTxListBase.stream()
                    .filter(tx -> tx.getSa().getCurrencyCd().equals(currencyPLN))
                    .filter(tx -> (tx.getCurAmt().setScale(2).compareTo(txJson.getCurAmt()) == 0))
                    .collect(Collectors.toList());

            assertEquals(1, txsFromBase.size());

            Transaction txBase = txsFromBase.get(0);

            assertEquals(0, txJson.getCurAmt().compareTo(txBase.getCurAmt().setScale(2) )   );

            assertEquals(txJson.getSa().getCurrencyCd(), txBase.getSa().getCurrencyCd());
        }

        List<TransactionDto> usdTxFromJson = txsDromJson.stream()
                .filter(txDto -> txDto.getSa().getCurrencyCd().equals(currencyUSD))
                .collect(Collectors.toList());
        assertEquals(1, usdTxFromJson.size());

        List<Transaction> usdTxListBase = acc.getAgreements().stream().
                filter(a -> a.getCurrencyCd().equals(currencyUSD))
                .flatMap(strA -> strA.getTransactions().stream())
                .collect(Collectors.toList());
        assertEquals(1, usdTxListBase.size());


        for(TransactionDto txJson: usdTxFromJson) {

            List<Transaction> txsFromBase = usdTxListBase.stream()
                    .filter(tx -> tx.getSa().getCurrencyCd().equals(currencyUSD))
                    .filter(tx -> (tx.getCurAmt().setScale(2).compareTo(txJson.getCurAmt()) == 0))
                    .collect(Collectors.toList());

            assertEquals(1, txsFromBase.size());

            Transaction txBase = txsFromBase.get(0);

            assertEquals(0, txJson.getCurAmt().compareTo(txBase.getCurAmt().setScale(2) )   );

            assertEquals(txJson.getSa().getCurrencyCd(), txBase.getSa().getCurrencyCd());
        }
    }

    private boolean equalFnc(String s1, String s2) {
        return s1.equals(s2);
    }

    @Test
    public void checkAllAccountsForRightDataIT() throws IOException {

        Set<com.lj.gen.json.mappings.transfer.Account> fileAccounts = jsonHelper.getAllJsonAccounts(initializationDataFile);
        Set<Account> accounts = acctRepo.findAll();
        assertEquals(fileAccounts.size(), accounts.size());

        for(com.lj.gen.json.mappings.transfer.Account fileAcc: fileAccounts) {

            Predicate<String> isInFile = accStr -> equalFnc(accStr, fileAcc.getAccountNumber());
            // check for one account
            long accCount = accounts.stream().map(Account::getAcctId).filter(isInFile).count();
            assertEquals(1, accCount);


            String accountId = accounts.stream()
                    .map(Account::getAcctId)
                    .filter(isInFile)
                    .findFirst().orElse(null);
            assertNotNull(accountId);

            Account account = acctRepo.fetchAccountWithTransactions(accountId);

            assertNotNull(account);

            // check for same no of currency agreements
            List<CurrencyAmount> fileCurrencies = fileAcc.getCurrencyAmounts();
            Set<ServiceAgreement> agreements = account.getAgreements();
            assertEquals(fileCurrencies.size(), agreements.size());

            // check for the same currencies

            //System.out.println("\naccountId: " + accountId);

            for(CurrencyAmount currAm: fileCurrencies) {

                String fileCurrency = currAm.getCurrency();


                Predicate<String> sameCurrency = c -> equalFnc(c, fileCurrency);

                long agreementsCount = agreements.stream()
                        .map(a -> a.getCurrencyCd()).
                        filter(sameCurrency).count();
                assertEquals(agreementsCount, 1);

                // compare amount for each currency on account

                Set<Transaction> txs = agreements.stream()
                        .filter(a -> a.getCurrencyCd().equals(fileCurrency))
                        .flatMap(a -> a.getTransactions().stream())
                        .collect(Collectors.toSet());

//                for (Transaction tx: txs) {
//                    System.out.println(tx.getFtId() + ":   " + tx.getSa().getSaId() + ":   " + tx.getCurAmt() );
//                }

                assertEquals(1, txs.size());
                Transaction tr = txs.iterator().next();


                BigDecimal curAmt = tr.getCurAmt().setScale(2);
                //System.out.println("curAmt: " + curAmt);
                BigDecimal fileAmount = BigDecimal.valueOf(currAm.getAmount()).setScale(2);
                //System.out.println("fileAmount: " + fileAmount);

                assertEquals(0, curAmt.compareTo(fileAmount));
            }
        }
    }

    /*
     * LazyInitializationException should be thrown here
     */

    @Test
    public void transactionsFromAccountViaAgreementIT() {

        Optional<Account> accountOpt = acctRepo.findById("100056013005");

        assertNotNull(accountOpt.get());

        Set<ServiceAgreement> sas = accountOpt.map(acc -> acc.getAgreements()).orElse(null);
        assertEquals(sas.size(), 1);

        ServiceAgreement sa = sas.iterator().next();

        assertEquals(sa.getCurrencyCd(), "USD");

        Executable exe = () -> sa.getTransactions().size();

        assertThrows(LazyInitializationException.class, exe);
    }
}