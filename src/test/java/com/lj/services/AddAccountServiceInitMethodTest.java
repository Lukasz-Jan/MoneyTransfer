package com.lj.services;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.services.jsonutils.Utils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lj.entities.*;
import com.lj.repository.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddAccountServiceInitMethodTest {

    private final String INITIAL_DATA_PATH = "src/test/resources/data/initAmountsForTest.json";
    private final File initializationDataFile;
    private final Utils jsonHelper = new Utils();
    private final AcctRepo acctRepoMock = mock(AcctRepo.class);

    private final Map<String, List<ServiceAgreement>> checkingAccountsAndAgreementsMap = new HashMap<>();
    private final HashMap<CompositeId, Double> checkingAccountedMoneyMap = new HashMap<>();

    private AddAccountService testedInstance;

    public AddAccountServiceInitMethodTest() {
        this.initializationDataFile = new File(INITIAL_DATA_PATH);
    }

    @BeforeAll
    public void init() {
        testedInstance = new AddAccountService(acctRepoMock, "initAmountsForTest.json");
    }

    @BeforeEach
    void setUp() {

        when(acctRepoMock.save(any(com.lj.entities.Account.class)))
                .thenAnswer(invocation -> {

                    com.lj.entities.Account savedAccount = (com.lj.entities.Account) invocation.getArguments()[0];

                    for (ServiceAgreement agr : savedAccount.getAgreements()) {
                        CompositeId compositeId = new CompositeId(savedAccount.getAcctId(), agr.getCurrencyCd());
                        assertEquals(1, agr.getTransactions().size());
                        BigDecimal curAmt = agr.getTransactions().iterator().next().getCurAmt();
                        checkingAccountedMoneyMap.put(compositeId, curAmt.doubleValue());
                    }

                    checkingAccountsAndAgreementsMap.compute(savedAccount.getAcctId(), (acctId, list) -> {
                                if (Objects.isNull(list)) {
                                    list = new ArrayList<>();
                                }
                                list.addAll(new ArrayList<>(savedAccount.getAgreements()));
                                return list;
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
    public void test_givenMoneyAmounsInFile_whenServerInitialized_thenAccounted() throws IOException {

        testedInstance.init();

        Set<com.lj.gen.json.mappings.transfer.Account> allTransferAccounts = jsonHelper.getAllJsonAccounts(initializationDataFile);

        for (com.lj.gen.json.mappings.transfer.Account transferAccount : allTransferAccounts) {
            for (CurrencyAmount transferCurrAmount : transferAccount.getCurrencyAmounts()) {

                List<ServiceAgreement> serviceAgreements = checkingAccountsAndAgreementsMap.get(transferAccount.getAccountNumber());
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
    public void test_givenAccountsInFile_whenServerStarted_thenSaveToPersistCalled() throws JsonProcessingException, IOException {

        Set<CurrencyAmount> agreementsFromJson = jsonHelper.getAllAgreementsFromJson(initializationDataFile);
        assertNotNull(acctRepoMock);
        testedInstance.init();
        verify(acctRepoMock, times(agreementsFromJson.size()))
                .save(Mockito.any(Account.class));
    }

    @Test
    public void test_givenAccountsInFile_whenServerStarted_then_AccountsAndAgreements_Saved() throws IOException {

        testedInstance.init();

        Set<com.lj.gen.json.mappings.transfer.Account> allTransferAccounts = jsonHelper.getAllJsonAccounts(initializationDataFile);

        for (com.lj.gen.json.mappings.transfer.Account transferAccount : allTransferAccounts) {

            List<ServiceAgreement> persistedServiceAgreements = checkingAccountsAndAgreementsMap.get(transferAccount.getAccountNumber());
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