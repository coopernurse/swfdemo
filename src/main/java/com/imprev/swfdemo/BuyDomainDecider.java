package com.imprev.swfdemo;

import com.amazonaws.services.simpleworkflow.model.ActivityTaskCompletedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.ActivityTaskFailedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.ActivityType;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.ScheduleActivityTaskDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.imprev.swf.ActivityErrorReason;
import com.imprev.swf.Decider;
import com.imprev.swf.DeciderWorker;
import com.imprev.swf.DecisionStep;
import com.imprev.swf.EventType;
import com.imprev.swf.FsmDecider;
import com.imprev.swf.SwfUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.imprev.swf.SwfUtil.findLastActivityTaskFailed;
import static com.imprev.swf.SwfUtil.getWorkflowStartInput;
import static com.imprev.swf.SwfUtil.readJson;
import static com.imprev.swf.SwfUtil.writeJson;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class BuyDomainDecider extends FsmDecider {

    private static final Logger log = Logger.getLogger(BuyDomainDecider.class);

    public static void main(String argv[]) throws Exception {
        DeciderWorker.runFromEnv(new BuyDomainDecider());
    }

    //
    // Every state in our workflow, mapped to a function to run when in that state
    // which tells SWF what to do next.
    //
    public enum State implements DecisionStep {
        // When the workflow is 'started', try to charge the credit card
        Started(scheduleChargeCreditCard),

        ChargeCreditCardScheduled(null),
        ChargeCreditCardFailed(chargeCreditCardFailed),
        CreditCardCharged(scheduleRegisterDomain),

        RegisterDomainScheduled(null),
        RegisterDomainFailed(scheduleRegisterDomain),
        DomainRegistered(scheduleSendEmailConfirmation),

        EmailConfirmationScheduled(null),
        EmailConfirmationFailed(scheduleSendEmailConfirmation),

        Completed(SwfUtil.workflowCompleted),
        Failed(SwfUtil.workflowFailed);

        private DecisionStep step;
        State(DecisionStep step) {
            this.step = step;
        }

        @Override
        public Decision nextDecision(List<HistoryEvent> events) {
            return (step == null) ? null : step.nextDecision(events);
        }
    }

    @Override
    public StateMachine<DecisionStep, EventType> newFSM() {
        StateMachineConfig<DecisionStep, EventType> fsmConfig = new StateMachineConfig<>();

        fsmConfig.configure(State.Started)
                .permit(EventType.ActivityTaskScheduled, State.ChargeCreditCardScheduled)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);


        fsmConfig.configure(State.ChargeCreditCardScheduled)
                .permit(EventType.ActivityTaskCompleted, State.CreditCardCharged)
                .permit(EventType.ActivityTaskFailed, State.ChargeCreditCardFailed)
                .permit(EventType.ActivityTaskTimedOut, State.ChargeCreditCardFailed)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);

        fsmConfig.configure(State.ChargeCreditCardFailed)
                .permit(EventType.ActivityTaskScheduled, State.ChargeCreditCardScheduled)
                .permit(EventType.WorkflowExecutionTerminated, State.Failed)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);

        fsmConfig.configure(State.CreditCardCharged)
                .permit(EventType.ActivityTaskScheduled, State.RegisterDomainScheduled)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);


        fsmConfig.configure(State.RegisterDomainScheduled)
                .permit(EventType.ActivityTaskCompleted, State.DomainRegistered)
                .permit(EventType.ActivityTaskFailed, State.RegisterDomainFailed)
                .permit(EventType.ActivityTaskTimedOut, State.RegisterDomainFailed)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);

        fsmConfig.configure(State.RegisterDomainFailed)
                .permit(EventType.ActivityTaskScheduled, State.RegisterDomainScheduled)
                .permit(EventType.WorkflowExecutionTerminated, State.Failed)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);

        fsmConfig.configure(State.DomainRegistered)
                .permit(EventType.ActivityTaskScheduled, State.EmailConfirmationScheduled)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);


        fsmConfig.configure(State.EmailConfirmationScheduled)
                .permit(EventType.ActivityTaskCompleted, State.Completed)
                .permit(EventType.ActivityTaskFailed, State.EmailConfirmationFailed)
                .permit(EventType.ActivityTaskTimedOut, State.EmailConfirmationFailed)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);

        fsmConfig.configure(State.EmailConfirmationFailed)
                .permit(EventType.ActivityTaskScheduled, State.EmailConfirmationScheduled)
                .permit(EventType.WorkflowExecutionTerminated, State.Failed)
                .permit(EventType.ScheduleActivityTaskFailed, State.Failed);


        return new StateMachine<>(State.Started, fsmConfig);
    }

    static DecisionStep scheduleChargeCreditCard = (events) -> new Decision()
                .withDecisionType(DecisionType.ScheduleActivityTask)
                .withScheduleActivityTaskDecisionAttributes(new ScheduleActivityTaskDecisionAttributes()
                        .withActivityType(new ActivityType().withName("ChargeCreditCardActivity").withVersion("1.0"))
                        .withActivityId(UUID.randomUUID().toString())
                        .withStartToCloseTimeout("300")
                        .withScheduleToCloseTimeout("600")
                        .withScheduleToStartTimeout("600")
                        .withHeartbeatTimeout("30")
                        .withTaskList(new TaskList().withName("ChargeCreditCardActivity"))
                        .withInput(writeJson(new ChargeCreditCardTask(getWorkflowStartInput(events, BuyDomainRequest.class).getCreditCard()))));

    static DecisionStep scheduleRegisterDomain = (events) -> new Decision()
            .withDecisionType(DecisionType.ScheduleActivityTask)
            .withScheduleActivityTaskDecisionAttributes(new ScheduleActivityTaskDecisionAttributes()
                    .withActivityType(new ActivityType().withName("RegisterDomainActivity").withVersion("1.0"))
                    .withActivityId(UUID.randomUUID().toString())
                    .withStartToCloseTimeout("300")
                    .withScheduleToCloseTimeout("600")
                    .withScheduleToStartTimeout("600")
                    .withHeartbeatTimeout("30")
                    .withTaskList(new TaskList().withName("RegisterDomainActivity"))
                    .withInput(writeJson(new RegisterDomainTask(getWorkflowStartInput(events, BuyDomainRequest.class).getDomainName()))));

    static DecisionStep scheduleSendEmailConfirmation = (events) -> {
        BuyDomainRequest req = getWorkflowStartInput(events, BuyDomainRequest.class);
        return new Decision()
                .withDecisionType(DecisionType.ScheduleActivityTask)
                .withScheduleActivityTaskDecisionAttributes(new ScheduleActivityTaskDecisionAttributes()
                        .withActivityType(new ActivityType().withName("SendEmailActivity").withVersion("1.0"))
                        .withActivityId(UUID.randomUUID().toString())
                        .withStartToCloseTimeout("300")
                        .withScheduleToCloseTimeout("600")
                        .withScheduleToStartTimeout("600")
                        .withHeartbeatTimeout("30")
                        .withTaskList(new TaskList().withName("SendEmailActivity"))
                        .withInput(writeJson(new SendEmailTask(
                                req.getCustomerEmail(),
                                "sender@example.com",
                                "Your domain purchase is complete",
                                "You have purchased: " + req.getDomainName()
                        ))));
    };

    static DecisionStep chargeCreditCardFailed = (events) -> {
        HistoryEvent event = findLastActivityTaskFailed(events);
        if (event == null || event.getActivityTaskFailedEventAttributes() == null) {
            // assume a timeout - reschedule
            return scheduleChargeCreditCard.nextDecision(events);
        }

        ActivityTaskFailedEventAttributes lastFailed = event.getActivityTaskFailedEventAttributes();
        boolean retry = false;
        switch (ActivityErrorReason.valueOf(lastFailed.getReason())) {
            // Retry if there's a server error - may be transient
            case ServerError:
                retry = true;
                break;

            // If it's a custom error, parse the details and check type
            case CustomError:
                ChargeCreditCardError err = readJson(lastFailed.getDetails(), ChargeCreditCardError.class);
                switch (err.getReason()) {
                    // Retry - gateway may come back up
                    case GatewayDown:
                        retry = true;
                }
                break;
        }

        if (retry) {
            return scheduleChargeCreditCard.nextDecision(events);
        }
        else {
            return SwfUtil.workflowFailed.nextDecision(events);
        }
    };

}
