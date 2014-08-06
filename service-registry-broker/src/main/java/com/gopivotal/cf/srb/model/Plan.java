package com.gopivotal.cf.srb.model;

import javax.persistence.*;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean free;

    @Embedded
    private PlanMetadata metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public PlanMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PlanMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Plan plan = (Plan) o;

        if (free != plan.free) return false;
        if (!description.equals(plan.description)) return false;
        if (!metadata.equals(plan.metadata)) return false;
        if (!name.equals(plan.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + (free ? 1 : 0);
        result = 31 * result + metadata.hashCode();
        return result;
    }
}
