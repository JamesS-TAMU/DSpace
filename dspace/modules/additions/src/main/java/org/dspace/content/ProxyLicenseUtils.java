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
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.license.FormattableArgument;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public class ProxyLicenseUtils {
    private static final BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    private static final BitstreamFormatService bitstreamFormat = ContentServiceFactory.getInstance()
                                                                                       .getBitstreamFormatService();
    private static final CollectionService collectionService = ContentServiceFactory.getInstance()
                                                                                    .getCollectionService();
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
        ByteArrayInputStream bais = new ByteArrayInputStream(licenseBytes);
        Bitstream b = itemService.createSingleBitstream(context, bais, item, "LICENSE");

        // Now set the format and name of the bitstream
        b.setName(context, "license.txt");
        b.setSource(context, "Written by org.dspace.content.LicenseUtils");

        // Find the License format
        BitstreamFormat bf = bitstreamFormat.findByShortDescription(context,
                                                                    "License");
        b.setFormat(bf);

        bitstreamService.update(context, b);

        bitstreamService
            .setMetadataSingleValue(context, b, "dcterms", "license", null, null, selection);
    }
}
