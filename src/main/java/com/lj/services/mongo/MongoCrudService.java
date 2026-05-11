package com.lj.services.mongo;

import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.gen.xsd.mappings.transfer.ActionType;
import com.lj.gen.xsd.mappings.transfer.TransferRequestType;
import com.lj.mongo.repository.AccountRepo;
import com.lj.mongo.repository.documents.AccountDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class MongoCrudService {

    private static final Logger logger = LoggerFactory.getLogger(MongoCrudService.class);

    private final AccountRepo accountRepo;

    @Autowired
    public MongoCrudService(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    public void processRequest(TransferRequestType req) {

        Optional<AccountDoc> targetAccountOpt = accountRepo.findById(req.getTargetAccountNumber());

        targetAccountOpt.ifPresentOrElse(targetAccount ->
                {
                    for (CurrencyAmount currencyAmountInBase : targetAccount.getCurrencyAmounts()) {

                        if (req.getCurrency().equals(currencyAmountInBase.getCurrency())) {

                            BigDecimal oldValue = BigDecimal.valueOf(currencyAmountInBase.getAmount());
                            BigDecimal newValue;

                            if (req.getAction() == ActionType.CREDIT) {
                                newValue = oldValue.add(req.getQuantity());
                            } else {
                                newValue = oldValue.subtract(req.getQuantity());
                                if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                                    logger.info("account " + targetAccount.getAccountNumber() + ": not enough funds");
                                    return;
                                }
                            }
                            currencyAmountInBase.setAmount(newValue.doubleValue());
                            accountRepo.save(targetAccount);
                            logger.info("New value for account " + targetAccount.getAccountNumber() + ": " + newValue);
                        }
                    }
                },
                () -> logger.info("Account " + req.getTargetAccountNumber() + " not found")
        );
    }
}
