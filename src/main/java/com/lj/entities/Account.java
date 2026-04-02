package com.lj.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ServiceAgreement> agreements = new HashSet<>();

    @Id
    @Column(name = "ACCT_ID", nullable = false, length = 12)
    private String acctId;

    @Temporal(TemporalType.DATE)
    @Column(name = "CRE_DTTM")
    private Date creDttm;

    @Version
    @Column(name = "VERSION")
    private Long version;

    public Account(String acctId, Date creDttm) {
        this.acctId = acctId;
        this.creDttm = creDttm;
    }
}