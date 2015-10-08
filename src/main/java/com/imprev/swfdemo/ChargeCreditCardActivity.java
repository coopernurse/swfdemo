package com.imprev.swfdemo;

import com.google.common.base.Strings;
import com.imprev.swf.ActivityErrorReason;
import com.imprev.swf.ActivityResult;
import com.imprev.swf.ActivityWorker;
import com.imprev.swf.JsonActivity;
import org.apache.log4j.Logger;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class ChargeCreditCardActivity extends JsonActivity<ChargeCreditCardTask> {

    private static final Logger log = Logger.getLogger(ChargeCreditCardActivity.class);

    public static void main(String argv[]) throws Exception {
        ActivityWorker.runFromEnv(new ChargeCreditCardActivity());
    }

    @Override
    public Class<ChargeCreditCardTask> getTaskClass() {
        return ChargeCreditCardTask.class;
    }

    @Override
    public String validateTask(ChargeCreditCardTask task) {
        if (Strings.isNullOrEmpty(task.getCardNumber())) {
            return "cardNumber cannot be empty";
        }

        return null;
    }

    @Override
    public ActivityResult processTask(ChargeCreditCardTask task) {
        // our card processor only accepts VISA..
        if (!task.getCardNumber().startsWith("4")) {
            String reason = "Card declined";
            ChargeCreditCardError err = new ChargeCreditCardError(ChargeCreditCardError.Reason.Declined, reason);
            return ActivityResult.error(ActivityErrorReason.CustomError, writeJson(err));
        }

        return ActivityResult.success("OK");
    }
}
