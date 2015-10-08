package com.imprev.swf;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.PollForActivityTaskRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterActivityTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskFailedRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class ActivityWorker extends Thread {

    private static final Logger log = Logger.getLogger(ActivityWorker.class);

    public static final String REASON_INVALID_INPUT = "InvalidInput";

    public static void runFromEnv(Activity activity) {
        Log4JInit.init();
        AmazonSimpleWorkflowClient swf = SwfUtil.getAmazonSimpleWorkflowClient();
        ActivityWorker worker = ActivityWorker.start(swf, SwfUtil.getDomain(), activity);
        try {
            worker.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static ActivityWorker start(AmazonSimpleWorkflowClient swf, String domain, Activity activity) {
        ActivityWorker worker = new ActivityWorker(swf, domain, activity);
        worker.setName("ActivityWorker-" + activity.getActivityName());
        worker.start();
        return worker;
    }

    private AmazonSimpleWorkflowClient swf;
    private String domain;
    private Activity activity;
    private boolean running;

    public ActivityWorker(AmazonSimpleWorkflowClient swf, String domain, Activity activity) {
        this.swf = swf;
        this.domain = domain;
        this.activity = activity;
    }

    @Override
    public void run() {
        super.run();
        setRunning(true);

        SwfUtil.ensureActivityType(swf, new RegisterActivityTypeRequest()
                .withDomain(domain)
                .withName(activity.getActivityName())
                .withVersion("1.0"));

        while (isRunning()) {
            ActivityTask task = swf.pollForActivityTask(new PollForActivityTaskRequest()
                    .withDomain(domain)
                    .withTaskList(new TaskList().withName(activity.getActivityName())));

            log.debug("pollForActivityTask domain=" + domain + " name=" + activity.getActivityName() + " task=" + task);
            if (task != null && !Strings.isNullOrEmpty(task.getTaskToken())) {
                ActivityResult result = activity.execute(task.getInput());
                if (result.isSuccess()) {
                    log.debug("respondActivityTaskCompleted taskToken=" + task.getTaskToken() + " result=" + result.getSuccessResult());
                    swf.respondActivityTaskCompleted(new RespondActivityTaskCompletedRequest()
                            .withTaskToken(task.getTaskToken())
                            .withResult(result.getSuccessResult()));
                } else {
                    log.warn("respondActivityTaskFailed taskToken=" + task.getTaskToken() +
                            " reason=" + result.getErrorReason() +
                            " details=" + result.getErrorDetails());
                    swf.respondActivityTaskFailed(new RespondActivityTaskFailedRequest()
                            .withTaskToken(task.getTaskToken())
                            .withReason(result.getErrorReason())
                            .withDetails(result.getErrorDetails()));
                }
            }
        }
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public synchronized boolean isRunning() {
        return running;
    }

}
