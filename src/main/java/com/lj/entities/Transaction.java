package com.lj.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * The persistent class for the CI_FT database table.
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name="CI_FT")
@NamedQuery(name="Transaction.findAll", query="SELECT t FROM Transaction t")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "SA_IDD", nullable = false)
    private ServiceAgreement sa;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="FT_ID", nullable=false, length=12)
    private Long ftId;

    @Column(name="CUR_AMT")
    private BigDecimal curAmt;

    @Temporal(TemporalType.DATE)
    @Column(name="FREEZE_DTTM")
    private Date freezeDttm;

    @Version
    @Column(name="VERSION")
    private Long version;
}