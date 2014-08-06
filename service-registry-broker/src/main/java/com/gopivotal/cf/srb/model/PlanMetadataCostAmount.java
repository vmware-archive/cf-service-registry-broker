package com.gopivotal.cf.srb.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class PlanMetadataCostAmount {

    @Column
    BigDecimal usd;

    public BigDecimal getUsd() {
        return usd;
    }

    public void setUsd(BigDecimal usd) {
        this.usd = usd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanMetadataCostAmount amount = (PlanMetadataCostAmount) o;

        if (!usd.equals(amount.usd)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return usd.hashCode();
    }
}
