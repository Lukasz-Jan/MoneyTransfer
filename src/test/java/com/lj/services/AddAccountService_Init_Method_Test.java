package com.lj.services;

import java.io.*;
import java.util.*;

import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.services.jsonutils.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lj.entity.*;
import com.lj.repository.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddAccountService_Init_Method_Test {

    private final String initDataPath = "src/test/resources/data/initAmountsForTest.json";

    private final File initializationDataFile;

    private final Utils jsonHelper = new Utils();

    private final AcctRepo acctRepoMock = mock(AcctRepo.class);
    private final TransactionRepo transactionsRepoMock = mock(TransactionRepo.class);

    private AddAccountService testedInstance;

    private final HashMap<CompositeId, Double> checkingAccountedMoneyMap = new HashMap<>();

    public AddAccountService_Init_Method_Test() {
        this.initializationDataFile = new File(initDataPath);
    }

    @BeforeAll
    public void init() {
        testedInstance = new AddAccountService(acctRepoMock, "initAmountsForTest.json");

        when(transactionsRepoMock.save(any(Transaction.class)))
                .thenAnswer(invocation -> {

                            Transaction transactionSaved = (Transaction) invocation.getArguments()[0];
                            String currencyCode = transactionSaved.getSa().getCurrencyCd();
                            String accountId = transactionSaved.getSa().getAccount().getAcctId();
                            CompositeId compositeId = new CompositeId(accountId, currencyCode);
                            checkingAccountedMoneyMap.put(compositeId, transactionSaved.getCurAmt().doubleValue());
                            return transactionSaved;
                        }
                );
    }

    @Test
    public void test_givenMoneyAmounsInFile_whenServerInitialized_thenAccounted() throws IOException {

        testedInstance.init();
        Set<com.lj.gen.json.mappings.transfer.Account> allTransferAccounts = jsonHelper.getAllJsonAccounts(initializationDataFile);

        for (com.lj.gen.json.mappings.transfer.Account transferAccount : allTransferAccounts) {
            for (CurrencyAmount transferCurrAmount : transferAccount.getCurrencyAmounts()) {

                CompositeId transferCompositeId = new CompositeId(transferAccount.getAccountNumber(), transferCurrAmount.getCurrency());
                if (checkingAccountedMoneyMap.containsKey(transferCompositeId)) {
                    assertEquals(transferCurrAmount.getAmount(), checkingAccountedMoneyMap.get(transferCompositeId));
                }
            }
        }
    }

    @Test
    public void test_givenAccountsInFile_whenServerStarted_thenSaveToPersistCalled() throws JsonProcessingException, IOException {

        reset(acctRepoMock);
        Set<CurrencyAmount> agreementsFromJson = jsonHelper.getAllAgreementsFromJson(initializationDataFile);
        assertNotNull(acctRepoMock);
        testedInstance.init();
        verify(acctRepoMock, times(agreementsFromJson.size()))
                .save(Mockito.any(Account.class));
    }

    @Test
    public void test_givenAccountsInFile_whenServerStarted_thenAccountsSaved_AllIds_Verification() {

    }
}