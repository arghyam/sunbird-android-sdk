package org.ekstep.genieresolvers.partner;

import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.ekstep.genieresolvers.BaseTask;
import org.ekstep.genieresolvers.util.Constants;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.PartnerData;
import org.ekstep.genieservices.commons.utils.GsonUtil;

/**
 * Created on 25/5/17.
 * shriharsh
 */

public class StartPartnerSessionTask extends BaseTask {
    private String appQualifier;
    private PartnerData partnerData;

    public StartPartnerSessionTask(Context context, String appQualifier, PartnerData partnerData) {
        super(context);
        this.appQualifier = appQualifier;
        this.partnerData = partnerData;
    }

    @Override
    protected String getLogTag() {
        return StartPartnerSessionTask.class.getSimpleName();
    }

    private Uri getUri() {
        String authority = String.format("content://%s.partner/startPartnerSession", appQualifier);
        return Uri.parse(authority);
    }

    @Override
    protected GenieResponse execute() {
        Cursor cursor = contentResolver.query(getUri(), null, GsonUtil.toJson(partnerData), null, "");
        GenieResponse response = getResponseFromCursor(cursor);

        if (response != null && response.getStatus()) {
            return getSuccessResponse(Constants.SUCCESSFUL);
        }

        return getErrorResponse(Constants.PROCESSING_ERROR, getErrorMessage(), "Unable to start the session!");
    }


    private GenieResponse getResponseFromCursor(Cursor cursor) {
        GenieResponse response = null;
        if (cursor != null && cursor.moveToFirst()) {
            String resultData = cursor.getString(0);
            response = GsonUtil.fromJson(resultData, GenieResponse.class);
            cursor.close();
        }

        return response;
    }


    @Override
    protected String getErrorMessage() {
        return "Error in starting the session!";
    }
}