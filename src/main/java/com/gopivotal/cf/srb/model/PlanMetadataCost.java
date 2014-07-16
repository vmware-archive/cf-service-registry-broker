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
}
