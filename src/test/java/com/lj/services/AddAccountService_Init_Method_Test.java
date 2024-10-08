package com.lj.services;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import com.lj.dto.AccountDto;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.service.AddAccountService;
import com.lj.services.jsonutils.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lj.entity.*;
import com.lj.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddAccountService_Init_Method_Test {

    private final String initDataPAtahStr = "src/test/resources/initAmountsForTest.json";

    private final File initializationDataFile;

    private Utils jsonHelper;


    public AddAccountService_Init_Method_Test() {

        //System.out.println("\ninitializationAccountDataPathStr: " + initializationAccountDataPathStr);

        //this.initializationAccountDataPathStr = initializationAccountDataPathStr;

        this.initializationDataFile = new File(initDataPAtahStr);
    }

    private AcctRepo acctRepoMock = mock(AcctRepo.class);

    @BeforeAll
    public void init() throws IOException {
        //MockitoAnnotations.initMocks(this);
        jsonHelper = new Utils();
    }

    //@InjectMocks
    private AddAccountService testedInstance = new AddAccountService(acctRepoMock, "src/test/resources/initAmountsForTest.json");

    @Test
    public void initMethodTest() throws JsonProcessingException, IOException {

        Set<CurrencyAmount> agreementsFromJson = jsonHelper.getAllAgreementsFromJson(initializationDataFile);

        int agreementsSize = agreementsFromJson.size();

        assertNotNull(acctRepoMock);

        testedInstance.init();
        verify(acctRepoMock, times(agreementsSize)).findById(Mockito.anyString());

        verify(acctRepoMock, times(agreementsSize)).save(Mockito.any(Account.class));

        String fakeId = "111122224010";
        prepareFakeAccountForId(fakeId);
        Optional<Account> accOpt = acctRepoMock.findById(fakeId);

        assertEquals(accOpt.get().getAcctId(), fakeId);
    }

    @Test
    public void forSpecificAccountIdTest() throws IOException {

        reset(acctRepoMock);

        Set<CurrencyAmount> agreementsFromJson = jsonHelper.getAllAgreementsFromJson(initializationDataFile);

        int agreementsSize = agreementsFromJson.size();


        String accountId_2 = "111122224010";

        Account acc_2 = new Account.Builder().setAcctId(accountId_2).setCreDttm(new Date()).build();

        when(acctRepoMock.findById(eq(accountId_2))).thenReturn(Optional.ofNullable(acc_2));

        testedInstance.init();

        verify(acctRepoMock, times(agreementsSize)).save(Mockito.any(Account.class));
    }


    private void prepareFakeAccountForId(String accountId) {

        Account acc_2 = new Account.Builder().setAcctId(accountId).setCreDttm(new Date()).build();
        when(acctRepoMock.findById(eq(accountId))).thenReturn(Optional.ofNullable(acc_2));
    }

//    @Test
//    public void changeAmountForAccountTest() {
//
//    }
}