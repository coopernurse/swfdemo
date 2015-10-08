package com.imprev.swfdemo;

import com.google.common.base.Strings;
import com.imprev.swf.ActivityErrorReason;
import com.imprev.swf.ActivityResult;
import com.imprev.swf.ActivityWorker;
import com.imprev.swf.JsonActivity;
import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class RegisterDomainActivity extends JsonActivity<RegisterDomainTask> {

    private static final Logger log = Logger.getLogger(RegisterDomainActivity.class);

    public static void main(String argv[]) throws Exception {
        ActivityWorker.runFromEnv(new RegisterDomainActivity());
    }

    @Override
    public Class<RegisterDomainTask> getTaskClass() {
        return RegisterDomainTask.class;
    }

    @Override
    public String validateTask(RegisterDomainTask task) {
        if (Strings.isNullOrEmpty(task.getDomainName())) {
            return "domainName cannot be empty";
        }

        return null;
    }

    @Override
    public ActivityResult processTask(RegisterDomainTask task) {
        // dnsimple fails about 50% of the time
        if (new Random().nextFloat() <= .5) {
            String error = "Timeout to api.dnsimple.com";
            log.error(error);
            return ActivityResult.error(ActivityErrorReason.ServerError, error);
        }

        return ActivityResult.success("OK");
    }
}
