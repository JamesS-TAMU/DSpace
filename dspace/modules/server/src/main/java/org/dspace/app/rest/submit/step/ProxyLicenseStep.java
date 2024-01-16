package org.dspace.app.rest.submit.step;

import javax.servlet.http.HttpServletRequest;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.step.DataLicense;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.InProgressSubmission;
import org.dspace.core.Context;

public class ProxyLicenseStep extends LicenseStep {

    @Override
    public DataLicense getData(SubmissionService submissionService, InProgressSubmission obj,
            SubmissionStepConfig config)
        throws Exception {
        System.out.println("\n\n\n\n");
        System.out.println("obj: " + obj.getID());
        System.out.println("\n\n\n\n");
        DataLicense result = super.getData(submissionService, obj, config);

        System.out.println("\n\n\n\n");
        System.out.println("result name: " + result.getName());
        System.out.println("result granted: " + result.isGranted());
        System.out.println("result acceptance date: " + result.getAcceptanceDate());
        System.out.println("result URL: " + result.getUrl());
        System.out.println("\n\n\n\n");

        result.setName("default");

        return result;
    }

    @Override
    public void doPatchProcessing(Context context, HttpServletRequest currentRequest, InProgressSubmission source,
            Operation op, SubmissionStepConfig stepConf) throws Exception {
        System.out.println("\n\n\n\n");
        System.out.println("source: " + source.getID());
        System.out.println("\top: " + op.getOp());
        System.out.println("\top path: " + op.getPath());
        System.out.println("\top value: " + op.getValue());
        System.out.println("\n\n\n\n");
        super.doPatchProcessing(context, currentRequest, source, op, stepConf);
    }

}
