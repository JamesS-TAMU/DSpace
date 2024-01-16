package org.dspace.app.rest.submit.step;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.step.DataLicense;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.rest.submit.factory.PatchOperationFactory;
import org.dspace.app.rest.submit.factory.impl.PatchOperation;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.Bitstream;
import org.dspace.content.InProgressSubmission;
import org.dspace.core.Constants;
import org.dspace.core.Context;

public class ProxyLicenseStep extends LicenseStep {

    public static final String LICENSE_STEP_SELECTION_OPERATION_ENTRY = "selection";

    private static final String DCTERMS_RIGHTSDATE = "dcterms.accessRights";
    private static final String DCTERMS_LICENSE = "dcterms.license";

    @Override
    public DataLicense getData(SubmissionService submissionService, InProgressSubmission obj,
            SubmissionStepConfig config)
        throws Exception {
        System.out.println("\n\n\n\n");
        System.out.println("obj: " + obj.getID());
        System.out.println("\n\n\n\n");

        DataLicense result = new DataLicense();

        Bitstream bitstream = bitstreamService
            .getBitstreamByName(obj.getItem(), Constants.LICENSE_BUNDLE_NAME, Constants.LICENSE_BITSTREAM_NAME);

        if (bitstream != null) {
            String selection = bitstreamService.getMetadata(bitstream, DCTERMS_LICENSE);
            result.setSelection(selection);

            String acceptanceDate = bitstreamService.getMetadata(bitstream, DCTERMS_RIGHTSDATE);

            if (StringUtils.isBlank(result.getAcceptanceDate())) {
                result.setGranted(false);
            } else {
                result.setAcceptanceDate(acceptanceDate);
                result.setGranted(true);
            }

            result.setUrl(
                configurationService.getProperty("dspace.server.url") + "/api/" + BitstreamRest.CATEGORY + "/" + English
                    .plural(BitstreamRest.NAME) + "/" + bitstream.getID() + "/content");
        }

        System.out.println("\n\n\n\n");
        System.out.println("result selection: " + result.getSelection());
        System.out.println("result granted: " + result.isGranted());
        System.out.println("result acceptance date: " + result.getAcceptanceDate());
        System.out.println("result URL: " + result.getUrl());
        System.out.println("\n\n\n\n");

        return result;
    }

    @Override
    public void doPatchProcessing(Context context, HttpServletRequest currentRequest, InProgressSubmission source,
            Operation op, SubmissionStepConfig stepConf) throws Exception {
        if (op.getPath().endsWith(LICENSE_STEP_SELECTION_OPERATION_ENTRY)) {
            PatchOperation<String> patchOperation = new PatchOperationFactory()
                .instanceOf(LICENSE_STEP_SELECTION_OPERATION_ENTRY, op.getOp());
            patchOperation.perform(context, currentRequest, source, op);
        } else {
            super.doPatchProcessing(context, currentRequest, source, op, stepConf);
        }
    }

}
