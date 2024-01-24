/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;

/**
 * TAMU Customization - Proxy license utilility for decoupling license selection from accepting licence
 */
public class ProxyLicenseUtils {
    private static final BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    private static final BitstreamFormatService bitstreamFormatService = ContentServiceFactory.getInstance()
                                                                                       .getBitstreamFormatService();
    private static final BundleService bundleService = ContentServiceFactory.getInstance().getBundleService();
    private static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    /**
     * Default constructor
     */
    private ProxyLicenseUtils() { }

    /**
     * Store a copy of the license as the user selected for the item.
     *
     * @param context        the dspace context
     * @param item           the item object of the license
     * @param selection      the license the user selected
     * @param licenseText    the license text
     * @throws SQLException       if database error
     * @throws IOException        if IO error
     * @throws AuthorizeException if authorization error
     */
    public static void addLicense(Context context, Item item, String selection, String licenseText)
        throws SQLException, IOException, AuthorizeException {

        // Store text as a bitstream
        byte[] licenseBytes = licenseText.getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(licenseBytes);

        List<Bundle> licenseBundles = itemService.getBundles(item, Constants.LICENSE_BUNDLE_NAME);
        Bundle licenseBundle = licenseBundles.size() > 0
            ? licenseBundles.get(0)
            : bundleService.create(context, item, Constants.LICENSE_BUNDLE_NAME);

        Bitstream bitstream = bitstreamService.create(context, licenseBundle, is);

        // Now set the format and name of the bitstream
        bitstream.setName(context, Constants.LICENSE_BITSTREAM_NAME);
        bitstream.setSource(context, "Written by org.dspace.content.ProxyLicenseUtils");

        // Find the License format
        BitstreamFormat bf = bitstreamFormatService.findByShortDescription(context,
                                                                    "License");
        bitstream.setFormat(bf);

        bitstreamService
            .setMetadataSingleValue(context, bitstream, "dcterms", "alternative", null, null, selection);

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
