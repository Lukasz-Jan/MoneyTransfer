package com.lj.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


/**
 * The persistent class for the CI_FT database table.
 *
 */
@Entity
@Table(name="CI_FT")
@NamedQuery(name="Transaction.findAll", query="SELECT t FROM Transaction t")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "SA_IDD", nullable = false)
    private ServiceAgreement sa;
    public ServiceAgreement getSa() {
        return sa;
    }

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



    private Transaction(BigDecimal curAmt, Date freezeDttm, ServiceAgreement sa) {
        this.curAmt = curAmt;
        this.freezeDttm = freezeDttm;
        this.sa = sa;
    }

    public Transaction() {
    }

    public Long getFtId() {
        return this.ftId;
    }

    public void setFtId(Long ftId) {
        this.ftId = ftId;
    }

    public BigDecimal getCurAmt() {
        return this.curAmt;
    }

    public void setCurAmt(BigDecimal curAmt) {
        this.curAmt = curAmt;
    }

    public Date getFreezeDttm() {
        return this.freezeDttm;
    }

    public void setFreezeDttm(Date freezeDttm) {
        this.freezeDttm = freezeDttm;
    }



    public void setSa(ServiceAgreement sa) {
        this.sa = sa;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public static class Builder {

        private BigDecimal curAmt;
        private Date freezeDttm;
        private ServiceAgreement saId;

        public Transaction build() {
            return new Transaction(curAmt, freezeDttm, saId);
        }

        public Builder setCurAmt(BigDecimal curAmt) {
            this.curAmt = curAmt;
            return this;
        }
        public Builder setFreezeDttm(Date freezeDttm) {
            this.freezeDttm = freezeDttm;
            return this;
        }
        public Builder setSa(ServiceAgreement saId) {
            this.saId = saId;
            return this;
        }
    }


}