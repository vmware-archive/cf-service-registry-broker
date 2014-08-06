package com.gopivotal.cf.srb.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class PlanMetadataCost {

    @Embedded
    private PlanMetadataCostAmount amount;

    @Column
    private String unit;

    public PlanMetadataCostAmount getAmount() {
        return amount;
    }

    public void setAmount(PlanMetadataCostAmount amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanMetadataCost that = (PlanMetadataCost) o;

        if (!amount.equals(that.amount)) return false;
        if (!unit.equals(that.unit)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = amount.hashCode();
        result = 31 * result + unit.hashCode();
        return result;
    }
}
