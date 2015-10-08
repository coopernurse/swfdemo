package com.imprev.swf;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.PollForDecisionTaskRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterDomainRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterWorkflowTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RespondDecisionTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class DeciderWorker extends Thread {

    private static final Logger log = Logger.getLogger(DeciderWorker.class);

    public static void runFromEnv(Decider decider) {
        Log4JInit.init();
        AmazonSimpleWorkflowClient swf = SwfUtil.getAmazonSimpleWorkflowClient();
        DeciderWorker worker = start(swf, SwfUtil.getDomain(), decider);
        try {
            worker.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static DeciderWorker start(AmazonSimpleWorkflowClient swf, String domain, Decider decider) {
        DeciderWorker worker = new DeciderWorker(swf, domain, decider);
        worker.setName("DeciderWorker-" + decider.getDeciderName());
        worker.start();
        return worker;
    }

    private AmazonSimpleWorkflowClient swf;
    private String domain;
    private Decider decider;
    private boolean running;

    public DeciderWorker(AmazonSimpleWorkflowClient swf, String domain, Decider decider) {
        this.swf = swf;
        this.domain = domain;
        this.decider = decider;
    }

    @Override
    public void run() {
        super.run();
        setRunning(true);

        SwfUtil.ensureDomain(swf, new RegisterDomainRequest()
                .withName(domain)
                .withWorkflowExecutionRetentionPeriodInDays("10"));

        SwfUtil.ensureWorkflowType(swf, new RegisterWorkflowTypeRequest()
                .withDomain(domain)
                .withName(decider.getDeciderName())
                .withDefaultExecutionStartToCloseTimeout(String.valueOf(decider.getDefaultExecutionStartToCloseTimeoutSeconds()))
                .withVersion("1.0"));

        while (isRunning()) {
            PollForDecisionTaskRequest pollReq = new PollForDecisionTaskRequest()
                    .withDomain(domain)
                    .withTaskList(new TaskList().withName(decider.getDeciderName()));
            DecisionTask task = swf.pollForDecisionTask(pollReq);

            log.debug("pollForDecisionTask domain=" + domain + " name=" + decider.getDeciderName() + " task=" + task);

            if (task != null && !Strings.isNullOrEmpty(task.getTaskToken())) {

                while (!Strings.isNullOrEmpty(task.getNextPageToken())) {
                    List<HistoryEvent> events = new ArrayList<>(task.getEvents());
                    pollReq.setNextPageToken(task.getNextPageToken());
                    task.setNextPageToken(null);

                    log.debug("loading next page. taskToken=" + task.getTaskToken() + " events=" + events.size());
                    DecisionTask task2 = swf.pollForDecisionTask(pollReq);

                    if (task2 != null && task2.getEvents() != null && !task2.getEvents().isEmpty()) {
                        events.addAll(task2.getEvents());
                        log.debug("added page of events. taskToken=" + task.getTaskToken() + " events=" + events.size());
                        task.setEvents(events);
                        task.setNextPageToken(task2.getNextPageToken());
                    }
                }

                List<Decision> decisions = decider.execute(task);

                if (decisions == null || decisions.isEmpty()) {
                    log.warn("Decider returned null/empty decisions list for task: " + task);
                }
                else {
                    log.debug("respondDecisionTaskCompleted taskToken=" + task.getTaskToken() + " decisions=" + decisions.size());
                    swf.respondDecisionTaskCompleted(new RespondDecisionTaskCompletedRequest()
                            .withTaskToken(task.getTaskToken())
                            .withDecisions(decisions));
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
