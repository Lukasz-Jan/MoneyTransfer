package com.lj.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the CI_SA database table.
 *
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="CI_SA")
@NamedQuery(name="ServiceAgreement.findAll", query="SELECT s FROM ServiceAgreement s")

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

//    public Set<Transaction> getTransactions() { return transactions; }
//
//
//    public void setTransactions(Set<Transaction> transactions) { this.transactions = transactions; }
//
//    public void setAccount(Account account) {
//        this.account = account;
//    }
//
//    public Account getAccount() {
//        return account;
//    }
//
//
//    public ServiceAgreement() {
//    }

    private ServiceAgreement(Date creDttm, String currencyCd, Account acct, String saStatus) {

        this.creDttm = creDttm;
        this.currencyCd = currencyCd;
        this.account = acct;
        this.saStatus = saStatus;
    }

    public Long getSaId() {
        return this.saId;
    }

    public void setSaId(Long saId) {
        this.saId = saId;
    }

    public Date getCreDttm() {
        return this.creDttm;
    }

    public void setCreDttm(Date creDttm) {
        this.creDttm = creDttm;
    }

    public String getCurrencyCd() {
        return this.currencyCd;
    }

    public void setCurrencyCd(String currencyCd) {
        this.currencyCd = currencyCd;
    }

    public String getSaStatus() {
        return this.saStatus;
    }

    public void setSaStatus(String saStatus) {
        this.saStatus = saStatus;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public static class Builder {

        private Date creDttm;
        private String currencyCd;
        private Account account;
        private String saStatus;

        public ServiceAgreement build() {
            return new  ServiceAgreement(creDttm, currencyCd, account, saStatus);
        }

        public Builder setAccount(Account account) {
            this.account = account;
            return this;
        }

        public Builder setCreDttm(Date creDttm) {
            this.creDttm = creDttm;
            return this;
        }

        public Builder setCurrencyCd(String currencyCd) {
            this.currencyCd = currencyCd;
            return this;
        }

        public Builder setSaStatus(String saStatus) {
            this.saStatus = saStatus;
            return this;
        }
    }
}