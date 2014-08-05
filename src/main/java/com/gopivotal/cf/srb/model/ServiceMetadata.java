package com.gopivotal.cf.srb.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ServiceMetadata {

    @Column(length = 11000)
    private String imageUrl;

    @Column
    private String displayName;

    @Column
    private String longDescription;

    @Column
    private String providerDisplayName;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getProviderDisplayName() {
        return providerDisplayName;
    }

    public void setProviderDisplayName(String providerDisplayName) {
        this.providerDisplayName = providerDisplayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceMetadata that = (ServiceMetadata) o;

        if (!displayName.equals(that.displayName)) return false;
        if (!imageUrl.equals(that.imageUrl)) return false;
        if (!longDescription.equals(that.longDescription)) return false;
        if (!providerDisplayName.equals(that.providerDisplayName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = imageUrl.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + longDescription.hashCode();
        result = 31 * result + providerDisplayName.hashCode();
        return result;
    }
}
