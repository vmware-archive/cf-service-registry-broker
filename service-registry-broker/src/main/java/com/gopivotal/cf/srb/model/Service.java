package com.gopivotal.cf.srb.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "services")
public class Service {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean bindable;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "service_id")
    private Set<Plan> plans = new HashSet<>();

    @Embedded
    private ServiceMetadata metadata;

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

    public boolean isBindable() {
        return bindable;
    }

    public void setBindable(boolean bindable) {
        this.bindable = bindable;
    }

    public Set<Plan> getPlans() {
        return plans;
    }

    public void setPlans(Set<Plan> plans) {
        this.plans = plans;
    }

    public void addPlan(Plan plan) {
        this.plans.add(plan);
    }

    public ServiceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ServiceMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        if (bindable != service.bindable) return false;
        if (!description.equals(service.description)) return false;
        if (!metadata.equals(service.metadata)) return false;
        if (!name.equals(service.name)) return false;
        if (!plans.equals(service.plans)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + (bindable ? 1 : 0);
        result = 31 * result + plans.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", bindable=" + bindable +
                ", plans=" + plans +
                ", metadata=" + metadata +
                '}';
    }
}
