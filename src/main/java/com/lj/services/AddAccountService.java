package com.lj.services;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import com.lj.repository.*;
import com.lj.entities.*;

@Service
public class AddAccountService {

    private static final Logger logger = LoggerFactory.getLogger(AddAccountService.class);
    private final AcctRepo accountRepo;
    private final File initDataFile;
    private final FileFetchService fileService;

    @Autowired
    public AddAccountService(AcctRepo accountRepo,
                             @Value("${initDataFile}") String initFileName,
                             FileFetchService fileService) throws IOException {

        this.fileService = fileService;
        this.initDataFile = this.fileService.fetchFile(initFileName);
        this.accountRepo = accountRepo;
    }

    @Transactional
    public void init() throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(initDataFile);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);

        Date creationDate = new Date();

        for (com.lj.gen.json.mappings.transfer.Account acc : transfer.getAccounts()) {

            logger.info("Adding account: " + acc.getAccountNumber());

            String accountNo = acc.getAccountNumber();
            List<CurrencyAmount> currencyAmounts = acc.getCurrencyAmounts();

            for (CurrencyAmount currencyInAccount : currencyAmounts) {
                BigDecimal amount = BigDecimal.valueOf(currencyInAccount.getAmount());
                saveAccountAgreementsAndTransactions(currencyInAccount.getCurrency(), amount, accountNo,
                        creationDate);
            }

        }
        streamWithJson.close();
    }

    private void saveAccountAgreementsAndTransactions(String currency, BigDecimal amount, String acctNo,
                                                      Date creationDate) {

        Optional<Account> accountOpt = accountRepo.findById(acctNo);

        Account account;
        if (!accountOpt.isPresent()) {
            account = Account.builder()
                    .acctId(acctNo)
                    .creDttm(creationDate)
                    .agreements(new HashSet<>())
                    .build();
        } else account = accountOpt.get();

        ServiceAgreement sa = addSa(creationDate, currency, account);
        addTransaction(sa, creationDate, amount);
        accountRepo.save(account);
        logger.info("Account " + account.getAcctId() + " income " + amount + " " + currency);

    }

private void addTransaction(ServiceAgreement sa, Date crDttm, BigDecimal amount) {

    Predicate<Transaction> isSameTransaction = tr -> (tr.getFreezeDttm()
            .equals(crDttm) && tr.getCurAmt()
            .equals(amount));
    Optional<Transaction> theSameTransactionOpt = sa
            .getTransactions()
            .stream()
            .filter(isSameTransaction)
            .findAny();

    if (!theSameTransactionOpt.isPresent()) {

        Transaction tr = Transaction.builder()
                .sa(sa)
                .freezeDttm(crDttm)
                .curAmt(amount)
                .build();

        sa.getTransactions().add(tr);
    }
}

private ServiceAgreement addSa(Date creationDate, String currency, Account acctDto) {

    Optional<ServiceAgreement> saOpt = acctDto
            .getAgreements().stream()
            .filter(sa -> (sa.getCurrencyCd().equals(currency)))
            .findFirst();

    if (!saOpt.isPresent()) {
        ServiceAgreement sa = ServiceAgreement.builder()
                .creDttm(creationDate)
                .currencyCd(currency)
                .account(acctDto)
                .transactions(new HashSet<>())
                .build();

        acctDto.getAgreements()
                .add(sa);
        return sa;
    } else {
        return saOpt.get();
    }
}
}