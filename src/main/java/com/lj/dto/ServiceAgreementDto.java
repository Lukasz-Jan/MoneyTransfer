package com.lj.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class ServiceAgreementDto extends AbstractDto {

    private static final long serialVersionUID = 1L;

    private Long saId;

    private Date creDttm;

    private String currencyCd;

    private String saStatus;

    private Long version;

    private Set<TransactionDto> transactions;
}
