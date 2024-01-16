/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import java.io.File;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.LicenseUtils;
import org.dspace.content.ProxyLicenseUtils;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.core.service.LicenseService;
import org.dspace.services.ConfigurationService;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Submission license selection "add" PATCH operation
 *
 * To select a license.
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.server.url}/api/submission/workspaceitems/31599 -H "Content-Type:
 * application/json" -d '[{ "op": "add", "path": "/sections/license/selection", "value":"proxy"}]'
 * </code>
 *
 * Please note that according to the JSON Patch specification RFC6902 a
 * subsequent add operation on the "selection" path will have the effect to
 * replace the previous selected license with a new one.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class LicenseSelectionAddPatchOperation extends AddPatchOperation<String> {

    @Autowired
    ItemService itemService;

    @Autowired
    LicenseService licenseService;

    @Autowired
    ConfigurationService configurationService;

    @Override
    protected Class<String[]> getArrayClassForEvaluation() {
        return String[].class;
    }

    @Override
    protected Class<String> getClassForEvaluation() {
        return String.class;
    }

    @Override
    void add(Context context, HttpServletRequest currentRequest, InProgressSubmission source, String path, Object value)
        throws Exception {

        System.out.println("\n\n\n\n");
        System.out.println("add path: " + path);
        System.out.println("add selection: " + value);
        System.out.println("\n\n\n\n");

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Value is not a valid string");
        }

        String selection = (String) value;

        String[] licenseFilenames = licenseService.getLicenseFilenames();
        String[] permittedValues = new String[licenseFilenames.length];
        String licenseFilename = null;
        for (int i = 0; i < licenseFilenames.length; i++) {
            String filename = licenseFilenames[i];
            String[] parts = filename.split("\\.");
            String license = parts[0];

            permittedValues[i] = license;

            if (license.equalsIgnoreCase(selection)) {
                licenseFilename = filename;
            }
        }

        if (StringUtils.isBlank(licenseFilename)) {
            throw new IllegalArgumentException(
                String.format("Value is not a valid license selection (permitted values: %s)",
                    String.join(",", permittedValues)));
        }

        String licensePath = String.join(File.separator,
            configurationService.getProperty("dspace.dir"), "config", licenseFilename);

        Item item = source.getItem();
        EPerson submitter = context.getCurrentUser();

        String licenseText = selection.equalsIgnoreCase("default")
            ? LicenseUtils.getLicenseText(context.getCurrentLocale(), source.getCollection(), item, submitter)
            : licenseService.getLicenseText(licensePath);

        if (StringUtils.isNotBlank(licenseText)) {
            itemService.removeDSpaceLicense(context, item);

            ProxyLicenseUtils.addLicense(context, item, selection, licenseText);
        } else {
            throw new RuntimeException(String.format("Unable to find license file at %s", licenseFilename));
        }

    }

}
