/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import java.io.File;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.LicenseUtils;
import org.dspace.content.ProxyLicenseUtils;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.service.LicenseService;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TAMU Customization - Submission license selected "add" PATCH operation
 *
 * To select a license:
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.server.url}/api/submission/workspaceitems/31599 -H "Content-Type:
 * application/json" -d '[{ "op": "add", "path": "/sections/license/selected", "value":"proxy"}]'
 * </code>
 *
 * Please note that according to the JSON Patch specification RFC6902 a
 * subsequent add operation on the "selected" path will have the effect to
 * replace the previous selected license with a new one.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class LicenseSelectedAddPatchOperation extends AddPatchOperation<String> {

    @Autowired
    ItemService itemService;

    @Autowired
    BundleService bundleService;

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

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Value is not a valid string");
        }

        System.out.println("\nLicenseSelectedAddPatchOperation\n");
        System.out.println("\tpath: " + path);
        System.out.println("\tvalue: " + value);

        String selected = (String) value;

        String[] licenseFilenames = licenseService.getLicenseFilenames();
        String[] permittedValues = new String[licenseFilenames.length];
        String licenseFilename = null;
        for (int i = 0; i < licenseFilenames.length; i++) {
            String filename = licenseFilenames[i];
            String[] parts = filename.split("\\.");
            String license = parts[0];

            permittedValues[i] = license;

            if (license.equalsIgnoreCase(selected)) {
                licenseFilename = filename;
            }
        }

        System.out.println("\t\tlicense filename: " + licenseFilename);
        System.out.println("\t\tpermitted values: " + String.join(",", permittedValues));

        if (StringUtils.isBlank(licenseFilename)) {
            throw new IllegalArgumentException(
                String.format("Value is not a valid license (permitted values: %s)",
                    String.join(",", permittedValues)));
        }

        String licensePath = String.join(File.separator,
            configurationService.getProperty("dspace.dir"), "config", licenseFilename);

        System.out.println("\t\tlicense path: " + licensePath);
        Item item = source.getItem();
        EPerson submitter = context.getCurrentUser();

        String licenseText = selected.equalsIgnoreCase("default")
            ? LicenseUtils.getLicenseText(context.getCurrentLocale(), source.getCollection(), item, submitter)
            : licenseService.getLicenseText(licensePath);

        System.out.println("\t\tlicense text: " + licenseText);
        if (StringUtils.isNotBlank(licenseText)) {
            if (selected.equalsIgnoreCase("proxy")) {
                List<Bundle> licenseBundles = itemService.getBundles(item, Constants.LICENSE_BUNDLE_NAME);
                if (licenseBundles.size() > 0) {
                    Bundle licenseBundle = licenseBundles.get(0);
                    for (Bitstream bitstream: licenseBundle.getBitstreams()) {
                        if (Constants.LICENSE_BITSTREAM_NAME.equals(bitstream.getName())) {
                            System.out.println("\n\nremove bitstream: " + bitstream.getName() + "\n\n");
                            bundleService.removeBitstream(context, licenseBundle, bitstream);
                            break;
                        }
                    }
                }
            } else {
                System.out.println("\t\tremove dspace license");
                itemService.removeDSpaceLicense(context, item);
            }

            System.out.println("\t\tadd license");
            ProxyLicenseUtils.addLicense(context, item, selected, licenseText);
            System.out.println("\n");
        } else {
            throw new RuntimeException(String.format("Unable to find license file at %s", licenseFilename));
        }

    }

}
