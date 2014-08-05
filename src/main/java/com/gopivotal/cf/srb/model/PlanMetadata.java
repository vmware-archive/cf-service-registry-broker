package com.gopivotal.cf.srb.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Embeddable
public class PlanMetadata {

    @ElementCollection
    @CollectionTable(name = "plan_metadata_bullets", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "bullet")
    private List<String> bullets = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "plan_metadata_costs", joinColumns = @JoinColumn(name = "plan_id"))
    private Set<PlanMetadataCost> costs = new HashSet<>();

    public List<String> getBullets() {
        return bullets;
    }

    public void setBullets(List<String> bullets) {
        this.bullets = bullets;
    }

    public Set<PlanMetadataCost> getCosts() {
        return costs;
    }

    public void setCosts(Set<PlanMetadataCost> costs) {
        this.costs = costs;
    }

    public void addCost(PlanMetadataCost cost) {
        this.costs.add(cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanMetadata that = (PlanMetadata) o;

        if (!bullets.equals(that.bullets)) return false;
        if (!costs.equals(that.costs)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bullets.hashCode();
        result = 31 * result + costs.hashCode();
        return result;
    }
}
