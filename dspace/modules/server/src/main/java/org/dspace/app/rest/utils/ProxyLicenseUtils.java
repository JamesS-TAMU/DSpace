/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.web.multipart.MultipartFile;

/**
 * TAMU Customization - Proxy license utilility for decoupling license selection from accepting licence
 */
public class ProxyLicenseUtils {
    private static final BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    private static final BitstreamFormatService bitstreamFormatService = ContentServiceFactory.getInstance()
                                                                                       .getBitstreamFormatService();
    private static final BundleService bundleService = ContentServiceFactory.getInstance().getBundleService();
    private static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    public static final String PROXY_LICENSE_NAME = "PERMISSION";

    /**
     * Default constructor
     */
    private ProxyLicenseUtils() { }

    /**
     * Store a copy of the proxy license as the user uploaded for the item.
     *
     * @param context        the dspace context
     * @param item           the item object of the license
     * @param file           the multipart file upload
     * @throws SQLException       if database error
     * @throws IOException        if IO error
     * @throws AuthorizeException if authorization error
     */
    public static void addProxyLicense(Context context, Item item, MultipartFile file)
        throws SQLException, IOException, AuthorizeException {

        InputStream is = new BufferedInputStream(file.getInputStream());

        List<Bundle> licenseBundles = itemService.getBundles(item, Constants.LICENSE_BUNDLE_NAME);
        Bundle licenseBundle = licenseBundles.size() > 0
            ? licenseBundles.get(0)
            : bundleService.create(context, item, Constants.LICENSE_BUNDLE_NAME);

        // remove existing proxy license bitstream
        for (Bitstream bitstream: licenseBundle.getBitstreams()) {
            if (bitstream.getName().startsWith(PROXY_LICENSE_NAME)) {
                bundleService.removeBitstream(context, licenseBundle, bitstream);
                break;
            }
        }

        Bitstream bitstream = bitstreamService.create(context, licenseBundle, is);

        String filename = Utils.getFileName(file);
        String[] parts = filename.split("\\.");
        String proxyLicenseName = parts.length == 1
            ? String.join(".", PROXY_LICENSE_NAME, "license")
            : String.join(".", PROXY_LICENSE_NAME, parts[parts.length - 1]);

        bitstream.setName(context, proxyLicenseName);

        bitstream.setSource(context, file.getOriginalFilename());
        bitstream.setDescription(context, "Proxy license");

        BitstreamFormat bf = bitstreamFormatService.guessFormat(context, bitstream);

        bitstream.setFormat(context, bf);

        bitstreamService.update(context, bitstream);
    }

    /**
     * Store a copy of the license as the user selected for the item.
     *
     * @param context        the dspace context
     * @param item           the item object of the license
     * @param selected       the license the user selected
     * @param licenseText    the license text
     * @throws SQLException       if database error
     * @throws IOException        if IO error
     * @throws AuthorizeException if authorization error
     */
    public static void addLicense(Context context, Item item, String selected, String licenseText)
        throws SQLException, IOException, AuthorizeException {

        // Store text as a bitstream
        byte[] licenseBytes = licenseText.getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(licenseBytes);

        List<Bundle> licenseBundles = itemService.getBundles(item, Constants.LICENSE_BUNDLE_NAME);
        Bundle licenseBundle = licenseBundles.size() > 0
            ? licenseBundles.get(0)
            : bundleService.create(context, item, Constants.LICENSE_BUNDLE_NAME);

        // remove existing license bitstream
        for (Bitstream bitstream: licenseBundle.getBitstreams()) {
            if (Constants.LICENSE_BITSTREAM_NAME.equals(bitstream.getName())) {
                bundleService.removeBitstream(context, licenseBundle, bitstream);
                break;
            }
        }

        // if proxy license not selected, remove proxy license
        if (!selected.equalsIgnoreCase("proxy")) {
            for (Bitstream bitstream: licenseBundle.getBitstreams()) {
                if (bitstream.getName().startsWith(PROXY_LICENSE_NAME)) {
                    bundleService.removeBitstream(context, licenseBundle, bitstream);
                    break;
                }
            }
        }

        Bitstream bitstream = bitstreamService.create(context, licenseBundle, is);

        // Now set the format and name of the bitstream
        bitstream.setName(context, Constants.LICENSE_BITSTREAM_NAME);
        bitstream.setSource(context, "Written by org.dspace.content.ProxyLicenseUtils");

        // Find the License format
        BitstreamFormat bf = bitstreamFormatService.findByShortDescription(context,
                                                                    "License");
        bitstream.setFormat(context, bf);

        bitstreamService
            .setMetadataSingleValue(context, bitstream, "dcterms", "alternative", null, null, selected);

        bitstreamService.update(context, bitstream);
    }

    /**
     * Grant license bitstream setting acceptance date, `dcterms.accessRights`.
     *
     * @param context        the dspace context
     * @param item           the item object of the license
     * @throws SQLException       if database error
     * @throws IOException        if IO error
     * @throws AuthorizeException if authorization error
     */
    public static void grantLicense(Context context, Item item)
        throws SQLException, IOException, AuthorizeException {

        Bitstream b = bitstreamService
            .getBitstreamByName(item, Constants.LICENSE_BUNDLE_NAME, Constants.LICENSE_BITSTREAM_NAME);

        bitstreamService.setMetadataSingleValue(context, b, "dcterms", "accessRights", null, null,
            DCDate.getCurrent().toString());

        bitstreamService.update(context, b);
    }

    /**
     * Revoke license bitstream unsetting the acceptance date, `dcterms.accessRights`.
     *
     * @param context        the dspace context
     * @param item           the item object of the license
     * @throws SQLException       if database error
     * @throws IOException        if IO error
     * @throws AuthorizeException if authorization error
     */
    public static void revokeLicense(Context context, Item item)
        throws SQLException, IOException, AuthorizeException {

        Bitstream b = bitstreamService
            .getBitstreamByName(item, Constants.LICENSE_BUNDLE_NAME, Constants.LICENSE_BITSTREAM_NAME);

        bitstreamService.clearMetadata(context, b, "dcterms", "accessRights", null, null);

        bitstreamService.update(context, b);
    }
}
