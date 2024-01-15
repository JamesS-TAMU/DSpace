/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * TAMU Customization - Proxy License REST resource.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class LicenseRest extends RestAddressableModel {

    public static final String NAME = "license";

    private final String name;

    private final String label;

    private final String text;

    private final boolean custom;

    private LicenseRest(String name, String label, String text, boolean custom) {
        this.name = name;
        this.label = label;
        this.text = text;
        this.custom = custom;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getText() {
        return text;
    }

    public boolean isCustom() {
        return custom;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    @JsonIgnore
    public String getCategory() {
        return null;
    }

    @Override
    @JsonIgnore
    public Class getController() {
        return null;
    }

    public static LicenseRest of(String name, String label, String text, boolean custom) {
        return new LicenseRest(name, label, text, custom);
    }

}
