package com.lj.services.initial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.entities.Account;
import com.lj.entities.ServiceAgreement;
import com.lj.entities.Transaction;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import com.lj.repository.AcctRepo;
import com.lj.services.FileFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataImporter {

    private static final Logger logger = LoggerFactory.getLogger(DataImporter.class);
    private final AcctRepo accountRepo;
    private final File initDataFile;
    private final FileFetchService fileService;
    private GenericApplicationContext context;

    @Autowired
    public DataImporter(AcctRepo accountRepo,
                        @Value("${initDataFile}") String initFileName,
                        FileFetchService fileService,
                        GenericApplicationContext context) throws IOException {
        this.fileService = fileService;
        this.initDataFile = this.fileService.fetchFile(initFileName);
        this.accountRepo = accountRepo;
        this.context = context;
    }

    public void init() throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(initDataFile);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        Date creationDate = new Date();


        for (com.lj.gen.json.mappings.transfer.Account acc : transfer.getAccounts()) {

            accountRepo.findById(acc.getAccountNumber()).ifPresentOrElse(

                    account -> logger.info("Account " + account.getAcctId() + " already persisted "),
                    () -> {

                        logger.info("Adding account: " + acc.getAccountNumber());
                        Account account = Account.builder()
                                .acctId(acc.getAccountNumber())
                                .creDttm(creationDate)
                                .agreements(new HashSet<>())
                                .build();

                        for (CurrencyAmount currencyAmount : acc.getCurrencyAmounts()) {
                            attachAgreementAndTransaction(currencyAmount.getCurrency(),
                                    BigDecimal.valueOf(currencyAmount.getAmount()),
                                    creationDate, account);
                        }

                        try {
                            context.getBean(DataImporter.class).save(account);
                            logger.info("Added account: " + acc.getAccountNumber());
                        } catch (DataIntegrityViolationException e) {
                            logger.info("ConstraintViolationException, account save " + account.getAcctId());
                        }
                    }
            );
        }
        streamWithJson.close();
    }


    @Transactional
    public void save(Account account) {
        accountRepo.save(account);
    }

    private void attachAgreementAndTransaction(String currency, BigDecimal amount,
                                               Date creationDate, Account account) {

        ServiceAgreement sa = addSa(creationDate, currency, account);
        addTransaction(sa, creationDate, amount);
        logger.info("Attaching income " + amount + " " + currency);
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