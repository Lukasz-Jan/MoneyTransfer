package com.lj.services;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import com.lj.service.AddAccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.entity.*;
import com.lj.repository.*;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddAccountServiceChangeAmountForAccountTest {

    private final String testedFileWithDataString = "src/test/resources/initDataForChangeAmountTest.json";
    private final String tempFileForTest = "src/test/resources/tempDataForChangeAmountTest.json";

    @Mock
    private AcctRepo acctRepoMock;

    private AddAccountService testedInstance = new AddAccountService(acctRepoMock, testedFileWithDataString);

    @BeforeAll
    public void init() throws IOException {

        MockitoAnnotations.initMocks(this);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        copyFileWithAccountToTemporaryFile(testedFileWithDataString, tempFileForTest);

    }

    @AfterEach
    public void afterEach() throws IOException {
        copyTempFileWithAccountBackToOrigin(tempFileForTest, testedFileWithDataString);
    }



    @Test
    public void changeAmountForAccountInFile_Account_IncomeForEachCurrency() {

        String accountId = "999142006678";
        BigDecimal setNewAmountPLN = BigDecimal.valueOf(11000.88);
        BigDecimal setNewAmountSEK = BigDecimal.valueOf(2233.77);
        BigDecimal setNewAmountUSD = BigDecimal.valueOf(1000.77);

        testedInstance.changeAmountForAccount(accountId, "SEK", setNewAmountSEK);
        testedInstance.changeAmountForAccount(accountId, "PLN", setNewAmountPLN);
        testedInstance.changeAmountForAccount(accountId, "USD", setNewAmountUSD);

        final ObjectMapper mapper = new ObjectMapper();
        File fileWithAccountsDataAfterChange = new File(testedFileWithDataString);
        TransfersystemSchema transfer = null;

        try (final InputStream streamWithJson = Files.newInputStream(fileWithAccountsDataAfterChange.toPath())) {

            transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        } catch (IOException e1) {
            fail("Problems with file " + testedFileWithDataString);
        }

        Optional<CurrencyAmount> amountOptPLN = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("PLN")).findAny();

        amountOptPLN.ifPresent(am -> assertEquals(setNewAmountPLN, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amountOptSEK = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("SEK")).findAny();

        amountOptSEK.ifPresent(am -> assertEquals(setNewAmountSEK, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amountOptUSD = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("USD")).findAny();

        amountOptUSD.ifPresent(am -> assertEquals(setNewAmountUSD, BigDecimal.valueOf(am.getAmount())));

        verify(acctRepoMock, times(0)).findById(Mockito.anyString());
        verify(acctRepoMock, times(0)).save(Mockito.any(Account.class));
    }

    /*
     * tested function: AddAccountService::changeAmountForAccount()
     * After each change of amount for account data is updated in file src/test/resources/initDataForChangeAmountTest.json
     * Accounts in file  are updated during test for specified currencies.
     * After all updates file is read and is checked if new values are set.
     */

    @Test
    public void changeAmountForAccountInFile_differentAccounts() {

        assertNotNull(acctRepoMock);

        String accountId = "999142006678";
        String currency = "PLN";
        BigDecimal setNewAmountPLN = BigDecimal.valueOf(555.66);

        testedInstance.changeAmountForAccount(accountId, currency, setNewAmountPLN);

        currency = "SEK";
        BigDecimal setNewAmountSEK = BigDecimal.valueOf(00.00);
        testedInstance.changeAmountForAccount(accountId, currency, setNewAmountSEK);

        currency = "USD";
        BigDecimal setNewAmountUSD = BigDecimal.valueOf(220.88);
        testedInstance.changeAmountForAccount(accountId, currency, setNewAmountUSD);

        String acctId_111122224010 = "111122224010";
        currency = "USD";
        BigDecimal setNewAmountUSD_acc_4010 = BigDecimal.valueOf(200000.88);
        testedInstance.changeAmountForAccount(acctId_111122224010, currency, setNewAmountUSD_acc_4010);

        String acctId_000056013005 = "000056013005";
        currency = "EUR";
        BigDecimal newAmountForAcct_3005_EUR = BigDecimal.valueOf(125000.88);
        testedInstance.changeAmountForAccount(acctId_000056013005, currency, newAmountForAcct_3005_EUR);

        final ObjectMapper mapper = new ObjectMapper();
        File fileWithAccountsDataAfterChange = new File(testedFileWithDataString);
        TransfersystemSchema transfer = null;

        try (final InputStream streamWithJson = Files.newInputStream(fileWithAccountsDataAfterChange.toPath())) {

            transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        } catch (IOException e1) {
            fail("Problems with file " + testedFileWithDataString);
        }


        Optional<CurrencyAmount> amountOptPLN = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("PLN")).findAny();
        amountOptPLN.ifPresent(am -> assertEquals(setNewAmountPLN, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amountOptSEK = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("SEK")).findAny();
        amountOptSEK.ifPresent(am -> assertEquals(setNewAmountSEK, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amountOptUSD = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("USD")).findAny();
        amountOptUSD.ifPresent(am -> assertEquals(setNewAmountUSD, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amount_4010_Opt = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(acctId_111122224010)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("USD")).findAny();
        amount_4010_Opt.ifPresent(am -> assertEquals(setNewAmountUSD_acc_4010, BigDecimal.valueOf(am.getAmount())));

        Optional<CurrencyAmount> amount_3005_EUR_Opt = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(acctId_000056013005)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("EUR")).findAny();
        amount_3005_EUR_Opt.ifPresent(am -> assertEquals(newAmountForAcct_3005_EUR, BigDecimal.valueOf(am.getAmount())));

        verify(acctRepoMock, times(0)).findById(Mockito.anyString());
        verify(acctRepoMock, times(0)).save(Mockito.any(Account.class));
    }

    @Test
    public void function_changeAmountForAccount_AccountOK_CURRENCY_NOT_OK() {

        String accountId = "000056013005";
        BigDecimal setNewAmountPLN = BigDecimal.valueOf(22.77);

        final ObjectMapper mapper = new ObjectMapper();
        File fileWithAccountsDataAfterChange = new File(testedFileWithDataString);
        TransfersystemSchema transfer = null;

        Double originalAmountForEUR = null;

        try (final InputStream streamWithJson = new FileInputStream(fileWithAccountsDataAfterChange)) {

            transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
            originalAmountForEUR = transfer.getAccounts().stream().filter(acc -> acc.getAccountNumber().equals(accountId))
                    .flatMap(acc -> acc.getCurrencyAmounts().stream()).filter(c -> c.getCurrency().equals("EUR")).findAny().map(c -> c.getAmount()).orElse(null);

            assertNotNull(originalAmountForEUR, "original amount for account for euro should exist");
        } catch (IOException e1) {
            fail("Problems with file " + testedFileWithDataString);
        }

        // trying to set account with wrong currency
        String currency = "USD";
        testedInstance.changeAmountForAccount(accountId, currency, setNewAmountPLN);


        try (final InputStream streamWithJson = new FileInputStream(fileWithAccountsDataAfterChange)) {
            transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        } catch (IOException e1) {
            fail("Problems with file " + testedFileWithDataString);
        }

        Optional<CurrencyAmount> amount_AfterChange_Opt = transfer.getAccounts().stream().filter(ac -> ac.getAccountNumber().equals(accountId)).flatMap(ac -> ac.getCurrencyAmounts().stream())
                .filter(curAmount -> curAmount.getCurrency().equals("EUR")).findAny();

        BigDecimal amountBeforeChange = BigDecimal.valueOf(originalAmountForEUR);

        amount_AfterChange_Opt.ifPresent(am -> assertEquals(amountBeforeChange, BigDecimal.valueOf(am.getAmount())));

        verify(acctRepoMock, times(0)).findById(Mockito.anyString());
        verify(acctRepoMock, times(0)).save(Mockito.any(Account.class));
    }





    private void copyFileWithAccountToTemporaryFile(String fromPathStr, String toPathStr) throws IOException {

        Path fromPath = Paths.get(fromPathStr);
        System.out.println("fromPath: " + fromPath);
        assertNotNull(fromPath);

        if(!Files.exists(Paths.get(fromPath.toString()))) {
            fail("File not present " + fromPath.toString());
        }

        Path toPath = Paths.get(toPathStr);

        if(!Files.exists(Paths.get(toPath.toString()))) {
            fail("File not present " + toPath.toString());
        }
        assertNotNull(toPath);
        Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyTempFileWithAccountBackToOrigin(String fromPathStr, String toPathStr) throws IOException {

        Path fromPath = Paths.get(fromPathStr);
        if(!Files.exists(Paths.get(fromPath.toString()))) {
            fail("File not present " + fromPath.toString());
        }
        Path toPath = Paths.get(toPathStr);
        if(!Files.exists(Paths.get(toPath.toString()))) {
            fail("File not present " + toPath.toString());
        }
        Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
    }
}