package com.lj.mongo.repository.documents;

import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "transfers")
public class AccountDoc {

    @Id
    private String accountNumber;

    private List<CurrencyAmount> currencyAmounts;

    public AccountDoc(String accountNumber, List<CurrencyAmount> currencyAmounts) {
        super();
        this.accountNumber = accountNumber;
        this.currencyAmounts = currencyAmounts;
    }

    public List<CurrencyAmount> getCurrencyAmounts() {
        return currencyAmounts;
    }

    public void setCurrencyAmounts(List<CurrencyAmount> currencyAmounts) {
        this.currencyAmounts = currencyAmounts;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
