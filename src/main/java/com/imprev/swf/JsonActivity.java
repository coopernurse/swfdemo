package com.imprev.swf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public abstract class JsonActivity<T> implements Activity {

    private static final Logger log = Logger.getLogger(JsonActivity.class);

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ActivityResult execute(String input) {
        Class<T> clazz = getTaskClass();
        T task;
        try {
            task = objectMapper.readValue(input, clazz);
        } catch (IOException e) {
            log.error("Unable to deserialize " + clazz.getName() + ". input=" + input, e);
            return ActivityResult.error(ActivityErrorReason.MalformedInput, "Unable to deserialize " +
                    clazz.getName() + " " + e.getMessage());
        }

        String validationError = validateTask(task);
        if (!Strings.isNullOrEmpty(validationError)) {
            return ActivityResult.error(ActivityErrorReason.ValidationError, validationError);
        }

        ActivityResult result;
        try {
            result = processTask(task);
            if (result == null) {
                String reason = getClass().getName() + " returned null ActivityResult from processTask";
                log.error(reason);
                result = ActivityResult.error(ActivityErrorReason.ServerError, reason);
            }
        }
        catch (Throwable t) {
            log.error(t.getMessage(), t);
            result = ActivityResult.error(ActivityErrorReason.ServerError, "Unknown error: " + t.getMessage());
        }
        return result;
    }

    @Override
    public String getActivityName() {
        // NOTE: if you rename a class it changes the default task list it will poll
        return getClass().getSimpleName();
    }

    public String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Class<T> getTaskClass();
    public abstract String validateTask(T task);
    public abstract ActivityResult processTask(T task);

}
