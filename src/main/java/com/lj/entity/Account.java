package com.lj.entity;


import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * The persistent class for the CI_ACCT database table.
 *
 */
@Entity
@Table(name = "CI_ACCT")
@NamedQuery(name = "Account.findAll", query = "SELECT a FROM Account a")
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

//----------------------------------

    @OneToMany(mappedBy = "account", cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ServiceAgreement> agreements = new HashSet<>();

    public Set<ServiceAgreement> getAgreements() {
        return agreements;
    };

    @Id
    @Column(name="ACCT_ID", nullable=false, length=12)
    private String acctId;

    @Temporal(TemporalType.DATE)
    @Column(name = "CRE_DTTM")
    private Date creDttm;

    @Version
    @Column(name = "VERSION")
    private Long version;

    public Account() {
    }

    public Account(String acctId, Date creDttm) {
        this.acctId = acctId;
        this.creDttm = creDttm;
    }

    public String getAcctId() {
        return this.acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }

    public Date getCreDttm() {
        return this.creDttm;
    }

    public void setCreDttm(Date creDttm) {
        this.creDttm = creDttm;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public static class Builder {

        private String acctId;
        private Date creDttm;

        public Account build() {

            return new Account(acctId, creDttm);
        }

        public Builder setAcctId(String acctId) {
            this.acctId = acctId;
            return this;
        }

        public Builder setCreDttm(Date creDttm) {
            this.creDttm = creDttm;
            return this;
        }

    }

}