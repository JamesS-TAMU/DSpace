/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model.step;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * TAMU Customization - Customized DTO to expose the section license during in progress submission.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class DataLicense implements SectionData {

    @JsonProperty(access = Access.READ_ONLY)
    private String url;

    @JsonProperty(access = Access.READ_ONLY)
    private String acceptanceDate;

    private boolean granted = false;

    // TAMU Cusomtization - selected license
    private String selected;

    // TAMU Cusomtization - proxy license
    @JsonUnwrapped
    private List<UploadBitstreamRest> files;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(String acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public List<UploadBitstreamRest> getFiles() {
        return files;
    }

    public void setFiles(List<UploadBitstreamRest> files) {
        this.files = files;
    }

}
