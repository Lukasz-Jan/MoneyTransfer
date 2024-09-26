package com.lj.service;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import com.lj.repository.*;
import com.lj.entity.*;


@Service
public class AddAccountService {

    private static final Logger logger = LoggerFactory.getLogger(AddAccountService.class);
    private AcctRepo accountRepo;

    private final File fileWithAccountsData;

    @Autowired
    public AddAccountService(AcctRepo accountRepo,
                             @Value("${fileWithAccountsInitPath}") String initializationDataFilePath) {

        this.accountRepo = accountRepo;
        this.fileWithAccountsData = new File(initializationDataFilePath);
    }

    @Transactional
    public void init() throws JsonProcessingException, IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(fileWithAccountsData);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        List<com.lj.gen.json.mappings.transfer.Account> accounts = transfer.getAccounts();

        Date creationDate = new Date();

        for (com.lj.gen.json.mappings.transfer.Account acc : accounts) {

            logger.info("Adding account: " + acc.getAccountNumber());

            String accountNo = acc.getAccountNumber();
            List<CurrencyAmount> currencyAmounts = acc.getCurrencyAmounts();

            for (CurrencyAmount currencyInAccount : currencyAmounts) {
                BigDecimal amount = BigDecimal.valueOf(currencyInAccount.getAmount());
                saveAccountAndAgreementsAndTransactions_Cascade(currencyInAccount.getCurrency(), amount, accountNo,
                        creationDate);
            }

        }
        streamWithJson.close();
    }


    private void saveAccountAndAgreementsAndTransactions_Cascade(String currency, BigDecimal amount, String acctNo,
                                                                 Date creationDate) {

        Optional<Account> accountOpt = accountRepo.findById(acctNo);

        if (!accountOpt.isPresent()) {
            accountOpt = Optional.of(new Account.Builder().setAcctId(acctNo).setCreDttm(creationDate).build());
        }

        Account account = accountOpt.get();

        ServiceAgreement sa = addSa(creationDate, currency, account);
        addTransaction(sa, creationDate, amount);
        accountRepo.save(accountOpt.get());
        logger.info("Account " + account.getAcctId() + " income " + amount + " " + currency);
    }

    private void addTransaction(ServiceAgreement sa, Date crDttm, BigDecimal amount) {

        Predicate<Transaction> isSameTransaction = tr -> (tr.getFreezeDttm().equals(crDttm)
                && tr.getCurAmt().equals(amount));
        Optional<Transaction> theSameTransactionOpt = sa.getTransactions().stream().filter(isSameTransaction).findAny();

        if (!theSameTransactionOpt.isPresent()) {

            Transaction tr = new Transaction.Builder().setFreezeDttm(crDttm).setCurAmt(amount).setSa(sa).build();
            sa.getTransactions().add(tr);
        }
    }

    private ServiceAgreement addSa(Date creationDate, String currency, Account acctDto) {

        Optional<ServiceAgreement> saOpt = acctDto.getAgreements().stream()
                .filter(sa -> (sa.getCurrencyCd().equals(currency))).findFirst();

        if (!saOpt.isPresent()) {
            ServiceAgreement sa = new ServiceAgreement.Builder().setCreDttm(creationDate).setCurrencyCd(currency)
                    .setAccount(acctDto).build();
            acctDto.getAgreements().add(sa);
            return sa;
        } else {
            return saOpt.get();
        }
    }

    public void changeAmountForAccount(String searchAccount, String searchCurr, BigDecimal newAmount) {

        final ObjectMapper mapper = new ObjectMapper();
        TransfersystemSchema transfer = null;

        try (final InputStream streamWithJson = new FileInputStream(fileWithAccountsData)) {

            transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);

            transfer.getAccounts().stream().filter(acc -> acc.getAccountNumber().contentEquals(searchAccount)).findAny()
                    .map(acc -> {

                        for (CurrencyAmount saForCurrency : acc.getCurrencyAmounts()) {

                            String saCurrency = saForCurrency.getCurrency();
                            if (saCurrency.equals(searchCurr)) {
                                saForCurrency.setAmount(newAmount.doubleValue());
                                logger.info("Account updated in file " + acc.getAccountNumber() + " " + saCurrency);
                                break;
                            }
                        }
                        return true;
                    });

            mapper.writeValue(fileWithAccountsData, transfer);

        } catch (IOException e1) {
            logger.error("Reading Json exception while updating account file");
            return;
        }
        System.out.println("After try");

    }
}