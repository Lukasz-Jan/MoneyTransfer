package com.lj.services;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.lj.TransferApplication;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import com.lj.repository.*;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import com.lj.entity.*;


// starting:
//	mvn failsafe:integration-test
// executes one goal without executing its entire phase and the preceding phases

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = TransferApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class AddAccountServiceIT {

    @Autowired
    private SaRepo saRepo;
    @Autowired
    private AcctRepo acctRepo;


    /*
     * Data is loaded from file initAmountsForTest.json whiele application starts
     * File path:
     * fileWithAccountsInitPath=src/test/resources/initAmountsForTest.json
     * file is set in application.properties in src\test\resources directory
     */
    @Test
    public void addAccountsTest() throws Throwable {

        System.out.println("addAccountaddAccountTest");
        Account acc = acctRepo.findById("999142006678").get();
        assertNotNull(acc);
        Set<ServiceAgreement> agreements = acc.getAgreements();

        assertEquals(3, agreements.size());

        Optional<ServiceAgreement> saOpt = acc.getAgreements().stream().filter(sa -> sa.getCurrencyCd().equals("PLN")).findAny();

        ServiceAgreement sa = saOpt.get();

        assertNotNull(sa);

        Executable exe = () -> sa.getTransactions().size();

        assertThrows(LazyInitializationException.class, exe);
    }

    /*
     * Integration test Starts app context Sarting app context initaializes accounts
     * Data from file initAmountsForTest.json Check is done if agreements were
     * fetched for all accounts
     *
     * one -> many account -> agreements
     *
     * No lazy initialization exception is thrown
     */
    @Test
    public void testForJoinFetchIT() {

        System.out.println("testForJoinFetchIT()");
        Iterable<Account> findAllIter = acctRepo.findAll();

        for (Account acc : findAllIter) {

            int size = acc.getAgreements().size();
            assertTrue(size > 0);
        }
    }

    /*
     * one account 100056013005 has One income with amount currency for sa is
     * checked one entrance is made in initialization file at startup for the same
     * currency total amount for sa is checked after initialization if amount was
     * the same should be considered as doubled
     */

    @Test
    public void accountFromAgreementIT() {

        System.out.println("accountFromAgreementIT()");

        // data in file example-transfer-systemTest.json
        Optional<Account> accountOpt = acctRepo.findById("100056013005");

        assertNotNull(accountOpt.get());

        Set<ServiceAgreement> sas = accountOpt.map(acc -> acc.getAgreements()).orElse(null);
        assertEquals(sas.size(), 1);

        ServiceAgreement sa = sas.iterator().next();

        assertEquals(sa.getCurrencyCd(), "USD");

        Long saId = sa.getSaId();

        Set<Transaction> saTransactions = saRepo.findById(saId).map(saFound -> saFound.getTransactions()).orElse(null);

        assertEquals(saTransactions.size(), 1);

        BigDecimal sumForSa = saTransactions.stream().map(t -> t.getCurAmt()).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("50025.00").setScale(2), sumForSa);

    }

    /*
     * LAZY INITIALIZATION EXC One to many relation does not access transactions via
     * sa too deep input data in file example-transfer-systemTest.json possible
     * solution: https://vladmihalcea.com/hibernate-facts-multi-level-fetching/
     *
     * Account 100056013005 is found check if there is only one sa "USD" according
     * to initAmountsForTest.json file.
     * Going from Account to Transaction in JPA is
     * not accessible so LazyInitializationException should be thrown
     */

    @Test
    public void transactionsFromAccountViaAgreementIT() {

        System.out.println("transactionsFromAccountViaAgreementIT()");

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