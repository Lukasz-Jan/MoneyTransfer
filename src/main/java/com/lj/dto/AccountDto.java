package com.lj.dto;

import com.lj.gen.json.mappings.transfer.Account;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AccountDto extends AbstractDto {
    private static final long serialVersionUID = 1L;

    private String acctId;

    private Date creDttm;

    private Long version;

    public AbstractDto getObjectDto(Object ob) {

        Account acc = (Account) ob;

        AccountDto ret = new AccountDto();
        ret.setAcctId(acc.getAccountNumber());

        return null;
    }
}
