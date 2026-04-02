package com.lj.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.*;

/**
 * The persistent class for the CI_SA database table.
 *
 */

@Entity
@Table(name="CI_SA")
@NamedQuery(name="ServiceAgreement.findAll", query="SELECT s FROM ServiceAgreement s")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAgreement implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="SA_ID", nullable=false, length=10)
    private Long saId;

    @Temporal(TemporalType.DATE)
    @Column(name="CRE_DTTM")
    private Date creDttm;

    @Column(name="CURRENCY_CD")
    private String currencyCd;

    @Column(name="SA_STATUS")
    private String saStatus;

    @Version
    @Column(name="VERSION")
    private Long version;

    @ManyToOne
    @JoinColumn(name="ACCT_ID", nullable = false)
    private Account account;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="sa", fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<Transaction>();

    public Long getSaId() {
        return this.saId;
    }

    public Date getCreDttm() {
        return this.creDttm;
    }

    public String getCurrencyCd() {
        return this.currencyCd;
    }

    public String getSaStatus() {
        return this.saStatus;
    }

    public Long getVersion() {
        return this.version;
    }

}