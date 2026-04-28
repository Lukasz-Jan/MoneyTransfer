package com.lj.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.entities.Account;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import com.lj.repository.AcctRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileAmountUpdaterTest {

    private static final Logger logger = LoggerFactory.getLogger(FileAmountUpdaterTest.class);
    private static final String RESOURCE_LOADER_PATH = "classpath:data/initDataForChangeAmountTest.json";

    private final FileFetchService fileServiceSimulate = new FileFetchService();
    private final FileAmountUpdater testedInstance = new FileAmountUpdater(RESOURCE_LOADER_PATH, fileServiceSimulate);

    @Mock
    private AcctRepo acctRepoMock;

    FileAmountUpdaterTest() throws IOException {
    }

    @BeforeAll
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenFilePath_instanceCreatedTest() {

        assertNotNull(testedInstance);
    }

    @Test
    public void havingFile_ChangeAmount_ForAccount_inFile() throws IOException {

        String testedAccountId = "999142006678";
        BigDecimal setNewAmountPLN = BigDecimal.valueOf(11000.88);
        BigDecimal setNewAmountSEK = BigDecimal.valueOf(2233.77);
        BigDecimal setNewAmountUSD = BigDecimal.valueOf(1000.77);

        testedInstance.changeAmountForAccountInInitDataFile(testedAccountId, "SEK", setNewAmountSEK);
        testedInstance.changeAmountForAccountInInitDataFile(testedAccountId, "PLN", setNewAmountPLN);
        testedInstance.changeAmountForAccountInInitDataFile(testedAccountId, "USD", setNewAmountUSD);

        TransfersystemSchema transferRoot = getTransfersystemRoot();

        Optional<CurrencyAmount> amountOptPLN = transferRoot.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(testedAccountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("PLN")).findAny();

        amountOptPLN.ifPresent(am -> assertEquals(setNewAmountPLN, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amountOptSEK = transferRoot.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(testedAccountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("SEK")).findAny();

        amountOptSEK.ifPresent(am -> assertEquals(setNewAmountSEK, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amountOptUSD = transferRoot
                .getAccounts()
                .stream()
                .filter(ac -> ac.getAccountNumber().equals(testedAccountId))
                .flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("USD"))
                .findAny();

        amountOptUSD.ifPresent(am -> assertEquals(setNewAmountUSD, BigDecimal.valueOf(am.getAmount())));

        verify(acctRepoMock, times(0)).findById(Mockito.anyString());
        verify(acctRepoMock, times(0)).save(Mockito.any(Account.class));
    }

    @Test
    public void havingDifferentAccounts_changeAmounts_InFile() throws IOException {

        String accountId = "999142006678";
        String currency = "PLN";
        BigDecimal setNewAmountPLN = BigDecimal.valueOf(555.66);
        testedInstance.changeAmountForAccountInInitDataFile(accountId, currency, setNewAmountPLN);

        currency = "SEK";
        BigDecimal setNewAmountSEK = BigDecimal.valueOf(00.00);
        testedInstance.changeAmountForAccountInInitDataFile(accountId, currency, setNewAmountSEK);

        currency = "USD";
        BigDecimal setNewAmountUSD = BigDecimal.valueOf(220.88);
        testedInstance.changeAmountForAccountInInitDataFile(accountId, currency, setNewAmountUSD);

        String acctId_111122224010 = "111122224010";
        currency = "USD";
        BigDecimal setNewAmountUSD_acc_4010 = BigDecimal.valueOf(200000.88);
        testedInstance.changeAmountForAccountInInitDataFile(acctId_111122224010, currency, setNewAmountUSD_acc_4010);

        String acctId_000056013005 = "000056013005";
        currency = "EUR";
        BigDecimal newAmountForAcct_3005_EUR = BigDecimal.valueOf(125000.88);
        testedInstance.changeAmountForAccountInInitDataFile(acctId_000056013005, currency, newAmountForAcct_3005_EUR);

        TransfersystemSchema transferRoot = getTransfersystemRoot();

        Optional<CurrencyAmount> amountOptPLN = transferRoot.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())

                .filter(curAmount -> curAmount.getCurrency().equals("PLN")).findAny();

        amountOptPLN.ifPresent(am -> assertEquals(setNewAmountPLN, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amountOptSEK = transferRoot.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("SEK")).findAny();

        amountOptSEK.ifPresent(am -> assertEquals(setNewAmountSEK, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amountOptUSD = transferRoot.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("USD")).findAny();
        amountOptUSD.ifPresent(am -> assertEquals(setNewAmountUSD, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amount_4010_Opt = transferRoot.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(acctId_111122224010)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("USD")).findAny();
        amount_4010_Opt.ifPresent(am -> assertEquals(setNewAmountUSD_acc_4010, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amount_3005_EUR_Opt = transferRoot
                .getAccounts()
                .stream()
                .filter(ac -> ac.getAccountNumber().equals(acctId_000056013005))
                .flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("EUR"))
                .findAny();
        amount_3005_EUR_Opt.ifPresent(am -> assertEquals(newAmountForAcct_3005_EUR, BigDecimal.valueOf(am.getAmount())));

        assertNotNull(acctRepoMock);
        verify(acctRepoMock, times(0)).findById(Mockito.anyString());
        verify(acctRepoMock, times(0)).save(Mockito.any(Account.class));
    }

    @Test
    public void having_accounts_when_change_In_wrong_currency_values_remain_the_same() {

        TransfersystemSchema transferRoot = getTransfersystemRoot();

        String testedAccountId = "000056013005";
        BigDecimal setNewAmountPLN = BigDecimal.valueOf(22.77);

        Double originalAmountForEUR = null;
        originalAmountForEUR = transferRoot
                .getAccounts()
                .stream().filter(acc -> acc.getAccountNumber().equals(testedAccountId))
                .flatMap(acc -> acc.getCurrencyAmounts().stream())
                .filter(c -> c.getCurrency().equals("EUR"))
                .findAny()
                .map(c -> c.getAmount())
                .orElse(null);

        assertNotNull(originalAmountForEUR, "original amount for account for euro should exist");

        // trying to set account with wrong currency
        String currency = "USD";
        testedInstance.changeAmountForAccountInInitDataFile(testedAccountId, currency, setNewAmountPLN);

        Optional<CurrencyAmount> amount_AfterChange_Opt = transferRoot
                .getAccounts()
                .stream()
                .filter(ac -> ac.getAccountNumber().equals(testedAccountId))
                .flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("EUR"))
                .findAny();

        BigDecimal amountBeforeChange = BigDecimal.valueOf(originalAmountForEUR);

        amount_AfterChange_Opt.ifPresent(am -> assertEquals(amountBeforeChange, BigDecimal.valueOf(am.getAmount())));
    }

    private TransfersystemSchema getTransfersystemRoot() {

        final ObjectMapper mapper = new ObjectMapper();
        TransfersystemSchema transfer = null;
        Resource resource = loadResource(RESOURCE_LOADER_PATH);

        try (final InputStream streamWithJson = resource.getInputStream()) {
            transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        } catch (IOException e1) {
            fail("Problems with resource while loading file  " + resource);
        }
        return transfer;
    }

    private Resource loadResource(String resourcePath) {
        Resource resource = new DefaultResourceLoader().getResource(resourcePath);

        if (!resource.exists() || !resource.isFile()) {
            logger.error("Resource " + resourcePath + " not loaded");
            throw new RuntimeException("resource not loaded");
        }
        return resource;
    }
}