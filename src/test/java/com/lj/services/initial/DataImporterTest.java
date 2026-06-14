package com.lj.services.initial;

import com.lj.entities.Account;
import com.lj.entities.ServiceAgreement;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.repository.AcctRepo;
import com.lj.services.CompositeId;
import com.lj.services.FileFetchService;
import com.lj.services.jsonutils.Utils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataImporterTest {

    private static final String RESOURCE_LOADER_PATH = "classpath:data/initAmountsForTest.json";
    private final Utils jsonHelper = new Utils();
    private final AcctRepo acctRepoMock = mock(AcctRepo.class);
    private final Map<String, Set<ServiceAgreement>> checkingAccountsAndAgreementsMap = new HashMap<>();
    private final HashMap<CompositeId, Double> checkingAccountedMoneyMap = new HashMap<>();
    private final FileFetchService fileServiceSimulate = new FileFetchService();
    private DataImporter testedInstance;
    private final File initializationDataFile;
    private final GenericApplicationContext contextMock = mock(GenericApplicationContext.class);

    public DataImporterTest() throws IOException {
        initializationDataFile = fileServiceSimulate.fetchFile(RESOURCE_LOADER_PATH);
    }

    @BeforeAll
    void init() throws IOException {
        testedInstance = new DataImporter(acctRepoMock, RESOURCE_LOADER_PATH, fileServiceSimulate, contextMock);
        when(contextMock.getBean(DataImporter.class)).thenReturn(testedInstance);
    }

    @BeforeEach
    void setUp() {

        when(acctRepoMock.save(any(Account.class)))
                .thenAnswer(invocation -> {

                    Account savedAccount = (Account) invocation.getArguments()[0];

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
    void givenMoneyAmounsInFile_whenServerInitialized_thenAccounted() throws IOException {

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
    void givenAccountsInFile_whenServerStarted_thenSaveToPersistCalled() throws IOException {

        Set<com.lj.gen.json.mappings.transfer.Account> accountsInJson = jsonHelper.getAllJsonAccounts(initializationDataFile);
        assertNotNull(acctRepoMock);
        testedInstance.init();
        verify(acctRepoMock, times(accountsInJson.size()))
                .save(Mockito.any(Account.class));
    }

    @Test
    void givenAccountsInFile_whenServerStarted_then_AccountsAndAgreements_Saved() throws IOException {

        testedInstance.init();

        Set<com.lj.gen.json.mappings.transfer.Account> allTransferAccounts = jsonHelper.getAllJsonAccounts(initializationDataFile);

        for (com.lj.gen.json.mappings.transfer.Account transferAccount : allTransferAccounts) {



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