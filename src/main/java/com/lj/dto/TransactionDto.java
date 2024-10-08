package com.lj.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class TransactionDto extends AbstractDto {

    private static final long serialVersionUID = 1L;

    private ServiceAgreementDto sa;

    private Long ftId;

    private BigDecimal curAmt;

    private Date freezeDttm;

    private Long version;
}
