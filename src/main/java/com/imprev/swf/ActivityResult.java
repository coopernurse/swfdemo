package com.imprev.swf;

import com.google.common.base.Strings;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class ActivityResult {

    public static ActivityResult success(String successResult) {
        ActivityResult result = new ActivityResult();
        result.successResult = successResult;
        return result;
    }

    public static ActivityResult error(ActivityErrorReason errorReason, String errorDetails) {
        ActivityResult result = new ActivityResult();
        result.errorReason = errorReason.name();
        result.errorDetails = errorDetails;
        return result;
    }

    ///////////////////////////////////////

    private String successResult;

    private String errorReason;
    private String errorDetails;

    private ActivityResult() {}

    public boolean isSuccess() {
        return !Strings.isNullOrEmpty(successResult);
    }

    public String getSuccessResult() {
        return successResult;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityResult that = (ActivityResult) o;

        if (successResult != null ? !successResult.equals(that.successResult) : that.successResult != null)
            return false;
        if (errorReason != null ? !errorReason.equals(that.errorReason) : that.errorReason != null) return false;
        return !(errorDetails != null ? !errorDetails.equals(that.errorDetails) : that.errorDetails != null);

    }

    @Override
    public int hashCode() {
        int result = successResult != null ? successResult.hashCode() : 0;
        result = 31 * result + (errorReason != null ? errorReason.hashCode() : 0);
        result = 31 * result + (errorDetails != null ? errorDetails.hashCode() : 0);
        return result;
    }
}
