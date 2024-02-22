/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;
import org.dspace.app.rest.utils.ProxyLicenseUtils;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TAMU Customization - Customized Submission "add" PATCH operation
 *
 * To accept/reject the granted license:
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.server.url}/api/submission/workspaceitems/31599 -H "Content-Type:
 * application/json" -d '[{ "op": "add", "path": "/sections/license/granted", "value":"true"}]'
 * </code>
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class LicenseAddPatchOperation extends AddPatchOperation<String> {

    @Autowired
    ItemService itemService;

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

        Boolean grant = null;
        // we are friendly with the client and accept also a string representation for the boolean
        if (value instanceof String) {
            grant = BooleanUtils.toBooleanObject((String) value);
        } else {
            grant = (Boolean) value;
        }

        if (grant == null) {
            throw new IllegalArgumentException(
                "Value is not a valid boolean expression (permitted value: on/off, true/false and yes/no");
        }

        Item item = source.getItem();

        if (grant) {
            ProxyLicenseUtils.grantLicense(context, item);
        } else {
            ProxyLicenseUtils.revokeLicense(context, item);
        }
    }

}
