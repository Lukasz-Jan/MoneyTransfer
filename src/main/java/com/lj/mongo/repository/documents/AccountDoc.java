package com.lj.mongo.repository.documents;

import com.lj.gen.json.mappings.transfer.CurrencyAmount;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transfers")
public class AccountDoc {

    @Id
    private String accountNumber;

    private List<CurrencyAmount> currencyAmounts;
}
