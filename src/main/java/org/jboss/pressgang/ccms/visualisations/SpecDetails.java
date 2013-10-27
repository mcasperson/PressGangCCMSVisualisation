package org.jboss.pressgang.ccms.visualisations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to store the details of a content spec that we need to create the graphs
 */
public class SpecDetails {
    private String product;
    private String version;
    private String title;

    public SpecDetails() {

    }

    public SpecDetails(@Nullable final String product, @Nullable final String version, @Nullable final String title) {
        this.setProduct(product);
        this.setVersion(version);
        this.setTitle(title);
    }

    @Nullable
    public String getProduct() {
        return product;
    }

    @Nullable
    public String getFixedProduct() {
        if (product != null) {
            return product.replaceAll(" ", "_");
        }

        return null;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setProduct(@Nullable final String product) {
        this.product = product;
    }

    public void setVersion(@Nullable final String version) {
        this.version = version;
    }

    public void setTitle(@Nullable final String title) {
        this.title = title;
    }
}
