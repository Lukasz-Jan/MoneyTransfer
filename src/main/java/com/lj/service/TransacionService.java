package com.lj.service;


import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import com.lj.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.lj.repository.*;
import com.lj.gen.xsd.mappings.transfer.ActionType;
import com.lj.gen.xsd.mappings.transfer.OutcomeType;
import com.lj.gen.xsd.mappings.transfer.TransferRequestType;

@Component
public class TransacionService {

    private static final Logger logger = LoggerFactory.getLogger(TransacionService.class);

    @Autowired
    private AcctRepo acctRepo;

    @Autowired
    private TransactionRepo trRepo;

    @Autowired
    AddAccountService jsonSrv;

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public OutcomeType processRequest(TransferRequestType req) {

        ServiceAgreement sa = findSa(req);

        Supplier<OutcomeType> sup = () -> notFound(req.getRequestId());

        OutcomeType out = Optional.ofNullable(sa).map(saF -> prosessCreditDebit(sa, req)).orElseGet(sup);

        return out;
    }

    private ServiceAgreement findSa(TransferRequestType req) {

        String saCurrency = req.getCurrency().trim();
        String accountNumber = req.getTargetAccountNumber();

        ServiceAgreement saFound = acctRepo.findById(accountNumber).map(acct -> {

            ServiceAgreement saInner = acct.getAgreements().stream().filter(sa -> sa.getCurrencyCd().equals(saCurrency))
                    .findFirst().map(sa -> sa).orElse(null);
            return saInner;
        }).orElse(null);

        return saFound;
    }


    private OutcomeType prosessCreditDebit(ServiceAgreement sa, TransferRequestType req) {

        OutcomeType outType;

        if (req.getAction().equals(ActionType.CREDIT)) {
            processIncome(sa, req);
            return OutcomeType.ACCEPT;
        } else {
            outType = processOutcome(sa, req);
            return outType;
        }
    }


    private void processIncome(ServiceAgreement sa, TransferRequestType req) {

        Date currentDate = new Date();
        Transaction incomeTx = new Transaction.Builder().setSa(sa).setFreezeDttm(currentDate)
                .setCurAmt(req.getQuantity()).build();
        sa.getTransactions().add(incomeTx);

        logger.info("Account " + sa.getAccount().getAcctId() + " income of: " + req.getQuantity());
        BigDecimal countBalance = countBalance(sa);
        changeJsonFile(sa.getAccount().getAcctId(), sa.getCurrencyCd(), countBalance);
    }


    private OutcomeType processOutcome(ServiceAgreement sa, TransferRequestType req) {

        Date currentDate = new Date();
        BigDecimal balance = trRepo.findBySaId(sa.getSaId()).stream().map(tr -> tr.getCurAmt()).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        BigDecimal amountArg = req.getQuantity();

        if (amountArg.compareTo(BigDecimal.ZERO) > 0) {

            amountArg = amountArg.negate();
        }

        if (balance.compareTo(amountArg.abs()) >= 0) {

            Transaction outcomeTx = new Transaction.Builder().setSa(sa).setFreezeDttm(currentDate).setCurAmt(amountArg)
                    .build();
            sa.getTransactions().add(outcomeTx);
            logger.info("Request " + req.getRequestId() + " outcome of: " + amountArg);

            BigDecimal countBalance = countBalance(sa);
            changeJsonFile(sa.getAccount().getAcctId(), sa.getCurrencyCd(), countBalance);

            return OutcomeType.ACCEPT;
        } else {
            logger.info("Request " + req.getRequestId() + " outcome of: " + amountArg + " no funds");
            return OutcomeType.REJECT;
        }
    }

    private OutcomeType notFound(String reqId) {
        logger.info("Account not found for request " + reqId);
        return OutcomeType.REJECT;
    }

    private void changeJsonFile(String acct, String currency, BigDecimal newAmount) {

        jsonSrv.changeAmountForAccount(acct, currency, newAmount);
    }

    private BigDecimal countBalance(ServiceAgreement sa) {

        BigDecimal balance = trRepo.findBySaId(sa.getSaId()).stream().map(tr -> tr.getCurAmt()).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        logger.info("Account " + sa.getAccount().getAcctId() + " balance: " + balance.toString() + " "
                + sa.getCurrencyCd());
        return balance;
    }
}