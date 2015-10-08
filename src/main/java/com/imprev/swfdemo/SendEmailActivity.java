package com.imprev.swfdemo;

import com.google.common.base.Strings;
import com.imprev.swf.ActivityResult;
import com.imprev.swf.ActivityWorker;
import com.imprev.swf.JsonActivity;
import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class SendEmailActivity extends JsonActivity<SendEmailTask> {

    private static final Logger log = Logger.getLogger(SendEmailActivity.class);

    public static void main(String argv[]) throws Exception {
        ActivityWorker.runFromEnv(new SendEmailActivity());
    }

    @Override
    public Class<SendEmailTask> getTaskClass() {
        return SendEmailTask.class;
    }

    @Override
    public String validateTask(SendEmailTask task) {
        if (Strings.isNullOrEmpty(task.getRecipEmail())) {
            return "recipEmail cannot be empty";
        }
        if (Strings.isNullOrEmpty(task.getSenderEmail())) {
            return "senderEmail cannot be empty";
        }
        if (Strings.isNullOrEmpty(task.getSubject())) {
            return "subject cannot be empty";
        }
        if (Strings.isNullOrEmpty(task.getBody())) {
            return "body cannot be empty";
        }

        return null;
    }

    @Override
    public ActivityResult processTask(SendEmailTask task) {
        String bmsMailingId = UUID.randomUUID().toString();
        return ActivityResult.success(writeJson(new SendEmailSuccess(bmsMailingId)));
    }

}
