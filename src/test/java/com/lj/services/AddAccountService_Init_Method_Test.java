package com.lj.services;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import com.lj.service.AddAccountService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddAccountService_Init_Method_Test {


    @Mock
    private AcctRepo acctRepoMock;

    @BeforeAll
    public void init() throws IOException {

        MockitoAnnotations.initMocks(this);
    }

    @InjectMocks
    private AddAccountService testedInstance 	= new AddAccountService(acctRepoMock, "src/test/resources/initAmountsForTest.json");

    @Test
    public void initMethodTest() throws JsonProcessingException, IOException {

        int findByIdCalls = 0;

        assertNotNull(acctRepoMock);

        testedInstance.init();
        verify(acctRepoMock, times(findByIdCalls + 7)).findById(Mockito.anyString());

        verify(acctRepoMock, times(7)).save(Mockito.any(Account.class));

        String fakeId = "111122224010";
        prepareFakeAccountForId(fakeId);
        Optional<Account> accOpt = acctRepoMock.findById(fakeId);

        assertEquals(accOpt.get().getAcctId(), fakeId);
    }

    private int simpleTestForAccount(int findByIdCalls) {

        findByIdCalls = 0;
        assertEquals(findByIdCalls, 0);
        return findByIdCalls;
    }

    private void testForSpecificAccountId(int findByIdCalls) {

        String accountId_2 = "111122224010";
        Account acc_2 = new Account.Builder().setAcctId(accountId_2).setCreDttm(new Date()).build();
        when(acctRepoMock.findById(eq(accountId_2))).thenReturn(Optional.ofNullable(acc_2));
    }

    private void prepareFakeAccountForId(String accountId) {

        Account acc_2 = new Account.Builder().setAcctId(accountId).setCreDttm(new Date()).build();
        when(acctRepoMock.findById(eq(accountId))).thenReturn(Optional.ofNullable(acc_2));
    }

    @Test
    public void changeAmountForAccountTest() {

    }
}