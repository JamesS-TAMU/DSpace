package org.dspace.app.rest.submit.step;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.atteo.evo.inflector.English;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.ErrorRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.step.DataLicense;
import org.dspace.app.rest.repository.WorkspaceItemRestRepository;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.rest.submit.UploadableStep;
import org.dspace.app.rest.submit.factory.PatchOperationFactory;
import org.dspace.app.rest.submit.factory.impl.PatchOperation;
import org.dspace.app.rest.utils.Utils;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.web.multipart.MultipartFile;

public class ProxyLicenseStep extends LicenseStep implements UploadableStep {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ProxyLicenseStep.class);

    private static final String LICENSE_STEP_SELECTED_OPERATION_ENTRY = "selected";

    private static final String DCTERMS_RIGHTSDATE = "dcterms.accessRights";
    private static final String DCTERMS_ALTERNATIVE = "dcterms.alternative";

    @Override
    public boolean isExclusiveMatchingStepId() {
        return true;
    }

    @Override
    public DataLicense getData(SubmissionService submissionService, InProgressSubmission obj,
            SubmissionStepConfig config)
        throws Exception {

        DataLicense result = new DataLicense();

        Bitstream bitstream = bitstreamService
            .getBitstreamByName(obj.getItem(), Constants.LICENSE_BUNDLE_NAME, Constants.LICENSE_BITSTREAM_NAME);

        if (bitstream != null) {
            String selected = bitstreamService.getMetadata(bitstream, DCTERMS_ALTERNATIVE);
            result.setSelected(selected);

            String acceptanceDate = bitstreamService.getMetadata(bitstream, DCTERMS_RIGHTSDATE);

            if (StringUtils.isNotBlank(acceptanceDate)) {
                result.setAcceptanceDate(acceptanceDate);
                result.setGranted(true);
            } else {
                result.setGranted(false);
            }

            result.setUrl(
                configurationService.getProperty("dspace.server.url") + "/api/" + BitstreamRest.CATEGORY + "/" + English
                    .plural(BitstreamRest.NAME) + "/" + bitstream.getID() + "/content");
        }

        System.out.println("\nProxyLicenseStep.getData\n");
        System.out.println("\tselected: " + result.getSelected());
        System.out.println("\tacceptance date: " + result.getAcceptanceDate());
        System.out.println("\tgranted: " + result.isGranted());
        System.out.println("\turl: " + result.getUrl());
        System.out.println("\n");

        return result;
    }

    @Override
    public void doPatchProcessing(Context context, HttpServletRequest currentRequest, InProgressSubmission source,
            Operation op, SubmissionStepConfig stepConf) throws Exception {
        if (op.getPath().endsWith(LICENSE_STEP_SELECTED_OPERATION_ENTRY)) {
            PatchOperation<String> patchOperation = new PatchOperationFactory()
                .instanceOf(LICENSE_STEP_SELECTED_OPERATION_ENTRY, op.getOp());
            patchOperation.perform(context, currentRequest, source, op);
        } else {
            super.doPatchProcessing(context, currentRequest, source, op, stepConf);
        }
    }

    @Override
    public ErrorRest upload(Context context, SubmissionService submissionService, SubmissionStepConfig stepConfig,
                            InProgressSubmission wsi, MultipartFile file) {
        Item item = wsi.getItem();

        System.out.println("\nProxyLicenseStep.upload\n");

        List<Bundle> licenseBundles = null;
        Bundle licenseBundle = null;
        Bitstream proxyBitstream = null;

        try (InputStream in = new BufferedInputStream(file.getInputStream())) {

            licenseBundles = itemService.getBundles(item, Constants.LICENSE_BUNDLE_NAME);

            System.out.println("\t\tremove existing proxy license");
            if (licenseBundles.size() > 0) {
                licenseBundle = licenseBundles.get(0);
                for (Bitstream bitstream: licenseBundle.getBitstreams()) {
                    if (bitstream.getName().startsWith("PERMISSION")) {
                        System.out.println("\n\nremove bitstream: " + bitstream.getName() + "\n\n");
                        bundleService.removeBitstream(context, licenseBundle, bitstream);
                        break;
                    }
                }
            } else {
                System.out.println("\t\tcreate license bundle");
                licenseBundle = bundleService.create(context, item, Constants.LICENSE_BUNDLE_NAME);
            }

            proxyBitstream = bitstreamService.create(context, licenseBundle, in);

            String filename = Utils.getFileName(file);
            System.out.println("\t\tfilename: " + filename);
            String[] parts = filename.split("\\.");
            String permissionLicenseName = parts.length == 1
                ? "PERMISSION.license"
                : String.join(".", "PERMISSION", parts[parts.length - 1]);

            System.out.println("\t\tset proxy bitstream name: " + permissionLicenseName);
            proxyBitstream.setName(context, permissionLicenseName);

            System.out.println("\t\tset proxy bitstream source: " + file.getOriginalFilename());
            proxyBitstream.setSource(context, file.getOriginalFilename());
            System.out.println("\t\tset proxy bitstream description: Proxy license");
            proxyBitstream.setDescription(context, "Proxy license");

            BitstreamFormat bf = bitstreamFormatService.guessFormat(context, proxyBitstream);

            System.out.println("\t\tset proxy bitstream format:" + bf.getShortDescription());
            proxyBitstream.setFormat(context, bf);

            System.out.println("\t\tupdate bitstream service");
            bitstreamService.update(context, proxyBitstream);
            System.out.println("\t\tupdate item service");
            itemService.update(context, item);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorRest result = new ErrorRest();
            result.setMessage(e.getMessage());
            if (licenseBundles != null && licenseBundles.size() > 0) {
                result.getPaths().add(
                    "/" + WorkspaceItemRestRepository.OPERATION_PATH_SECTIONS + "/" + stepConfig.getId() + "/files/" +
                    licenseBundles.get(0).getBitstreams().size());
            } else {
                result.getPaths()
                    .add("/" + WorkspaceItemRestRepository.OPERATION_PATH_SECTIONS + "/" + stepConfig.getId());
            }
            return result;
        }

        return null;
    }

}
