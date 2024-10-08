package com.lj.services.jsonutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.dto.AccountDto;
import com.lj.dto.ServiceAgreementDto;
import com.lj.dto.TransactionDto;
import com.lj.gen.json.mappings.transfer.Account;
import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;


public class Utils {

    public Set<AccountDto> getAllAccountsByFile(File file) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(file);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);

        Set<Account> accounts = new HashSet<>(transfer.getAccounts());

        Set<AccountDto> ret = new HashSet<>();
        for(Account acc: accounts) {

            AccountDto accDto = new AccountDto();
            accDto.getObjectDto(acc);
            ret.add(accDto);
        }
        return ret;
    }

    public Set<Account> getAllJsonAccounts(File file) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(file);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);

        Set<Account> accounts = new HashSet<>(transfer.getAccounts());


        return accounts;
    }

    public Set<CurrencyAmount> getAllAgreementsFromJson(File file) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(file);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        Set<Account> accounts = new HashSet<>(transfer.getAccounts());

        Set<CurrencyAmount> ret = new HashSet<>();

        for(Account acc: accounts) {
            List<CurrencyAmount> currencyAmounts = acc.getCurrencyAmounts();
            for(CurrencyAmount am: currencyAmounts) {
                ret.add(am);
            }
        }
        return ret;
    }

    public List<CurrencyAmount> getAllAgreementsFromJsonForAccountId(File file, String accountId) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(file);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        Set<Account> accounts = new HashSet<>(transfer.getAccounts());

        Optional<Account> accOpt = accounts.stream().filter(a -> a.getAccountNumber().equals(accountId)).findAny();

        List<CurrencyAmount> ret = accOpt.map(a -> a.getCurrencyAmounts()).orElse(null);

        return ret;
    }

    public Account getAccountFromJson(File file, String acctId) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(file);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        List<Account> accounts = transfer.getAccounts();

        Optional<Account> accOpt = accounts.stream().filter(acc -> acc.getAccountNumber().equals(acctId)).findAny();

        return accOpt.orElse(null);
    }

    public Set<TransactionDto> getTransactionsFromJsonForAcoountId(File file, String acctId) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        final InputStream streamWithJson = new FileInputStream(file);

        TransfersystemSchema transfer = mapper.readValue(streamWithJson, TransfersystemSchema.class);
        List<Account> accounts = transfer.getAccounts();

        Set<TransactionDto> txs = new HashSet<>();

        for (com.lj.gen.json.mappings.transfer.Account acc : accounts) {

            String accountNo = acc.getAccountNumber();

            if(accountNo.equals(acctId)) {

                List<CurrencyAmount> currencyAmounts = acc.getCurrencyAmounts();

                for (CurrencyAmount currencyInAccount : currencyAmounts) {

                    BigDecimal amount = BigDecimal.valueOf(currencyInAccount.getAmount());

                    ServiceAgreementDto saDto = new ServiceAgreementDto();
                    saDto.setCurrencyCd(currencyInAccount.getCurrency());

                    TransactionDto txDto = new TransactionDto();
                    txDto.setSa(saDto);
                    txDto.setCurAmt(amount);
                    txs.add(txDto);
                }
            }
        }
        streamWithJson.close();

        return txs;
    }

}
