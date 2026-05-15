package com.lj.services;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.services.initial.InitialDatabaseImporter;
import com.lj.services.jsonutils.Utils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lj.entities.*;
import com.lj.repository.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InitialDatabaseImporterInitMethodTest {

    private static final String RESOURCE_LOADER_PATH = "classpath:data/initAmountsForTest.json";
    private final Utils jsonHelper = new Utils();
    private final AcctRepo acctRepoMock = mock(AcctRepo.class);
    private final Map<String, Set<ServiceAgreement>> checkingAccountsAndAgreementsMap = new HashMap<>();
    private final HashMap<CompositeId, Double> checkingAccountedMoneyMap = new HashMap<>();
    private final FileFetchService fileServiceSimulate = new FileFetchService();
    private InitialDatabaseImporter testedInstance;
    private final File initializationDataFile;

    public InitialDatabaseImporterInitMethodTest() throws IOException {
        initializationDataFile = fileServiceSimulate.fetchFile(RESOURCE_LOADER_PATH);
    }

    @BeforeAll
    public void init() throws IOException {
        testedInstance = new InitialDatabaseImporter(acctRepoMock, RESOURCE_LOADER_PATH, fileServiceSimulate);
    }

    @BeforeEach
    void setUp() {

        when(acctRepoMock.save(any(com.lj.entities.Account.class)))
                .thenAnswer(invocation -> {



                    com.lj.entities.Account savedAccount = (com.lj.entities.Account) invocation.getArguments()[0];

                    System.out.println("savedAccount: " + savedAccount.getAcctId());

                    for (ServiceAgreement agr : savedAccount.getAgreements()) {


                        CompositeId compositeId = new CompositeId(savedAccount.getAcctId(), agr.getCurrencyCd());
                        assertEquals(1, agr.getTransactions().size());
                        BigDecimal curAmt = agr.getTransactions().iterator().next().getCurAmt();
                        checkingAccountedMoneyMap.put(compositeId, curAmt.doubleValue());
                    }

                    checkingAccountsAndAgreementsMap.compute(savedAccount.getAcctId(), (acctId, set) -> {
                                if (Objects.isNull(set)) {
                                    set = new HashSet<>();
                                }
                                set.addAll(new ArrayList<>(savedAccount.getAgreements()));
                                return set;
                            }
                    );
                    return savedAccount;
                });
    }

    @AfterEach
    void tearDown() {
        reset(acctRepoMock);
        checkingAccountsAndAgreementsMap.clear();
    }


    @Test
    public void givenMoneyAmounsInFile_whenServerInitialized_thenAccounted() throws IOException {

        testedInstance.init();

        Set<com.lj.gen.json.mappings.transfer.Account> allTransferAccounts = jsonHelper.getAllJsonAccounts(initializationDataFile);

        for (com.lj.gen.json.mappings.transfer.Account transferAccount : allTransferAccounts) {
            for (CurrencyAmount transferCurrAmount : transferAccount.getCurrencyAmounts()) {

                Set<ServiceAgreement> serviceAgreements = checkingAccountsAndAgreementsMap.get(transferAccount.getAccountNumber());
                for (ServiceAgreement serviceAgreement : serviceAgreements) {

                    if (serviceAgreement.getCurrencyCd().equals(transferCurrAmount.getCurrency())) {

                        CompositeId compositeId = new CompositeId(transferAccount.getAccountNumber(), serviceAgreement.getCurrencyCd());
                        Double savedMoney = checkingAccountedMoneyMap.get(compositeId);
                        assertEquals(transferCurrAmount.getAmount(), savedMoney);
                    }
                }

            }
        }
    }


    @Test
    public void givenAccountsInFile_whenServerStarted_thenSaveToPersistCalled() throws JsonProcessingException, IOException {

        Set<com.lj.gen.json.mappings.transfer.Account> accountsInJson = jsonHelper.getAllJsonAccounts(initializationDataFile);
        assertNotNull(acctRepoMock);
        testedInstance.init();
        verify(acctRepoMock, times(accountsInJson.size()))
                .save(Mockito.any(Account.class));
    }

    @Test
    public void givenAccountsInFile_whenServerStarted_then_AccountsAndAgreements_Saved() throws IOException {

        testedInstance.init();

        Set<com.lj.gen.json.mappings.transfer.Account> allTransferAccounts = jsonHelper.getAllJsonAccounts(initializationDataFile);

        for (com.lj.gen.json.mappings.transfer.Account transferAccount : allTransferAccounts) {

            System.out.println("transferAccount.getAccountNumber(): " + transferAccount.getAccountNumber());

            Set<ServiceAgreement> persistedServiceAgreements = checkingAccountsAndAgreementsMap.get(transferAccount.getAccountNumber());
            assertEquals(persistedServiceAgreements.size(), transferAccount.getCurrencyAmounts().size());

            for (CurrencyAmount transferCurrency : transferAccount.getCurrencyAmounts()) {

                boolean isAgreeementInTransferAgreements = persistedServiceAgreements
                        .stream()
                        .anyMatch(agr -> agr.getCurrencyCd().equals(transferCurrency.getCurrency()));
                assertTrue(isAgreeementInTransferAgreements);
            }
        }
    }
}