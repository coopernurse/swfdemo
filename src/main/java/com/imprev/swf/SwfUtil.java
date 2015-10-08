package com.imprev.swf;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.ActivityTaskCompletedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.ActivityTaskFailedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.ActivityTypeInfo;
import com.amazonaws.services.simpleworkflow.model.ActivityTypeInfos;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.DomainInfo;
import com.amazonaws.services.simpleworkflow.model.DomainInfos;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.ListActivityTypesRequest;
import com.amazonaws.services.simpleworkflow.model.ListDomainsRequest;
import com.amazonaws.services.simpleworkflow.model.ListWorkflowTypesRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterActivityTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterDomainRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterWorkflowTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RegistrationStatus;
import com.amazonaws.services.simpleworkflow.model.WorkflowTypeInfo;
import com.amazonaws.services.simpleworkflow.model.WorkflowTypeInfos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.imprev.swfdemo.BuyDomainRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class SwfUtil {

    private static final Logger log = Logger.getLogger(SwfUtil.class);

    public static AmazonSimpleWorkflowClient getAmazonSimpleWorkflowClient() {
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        List<String> errors = new ArrayList<>();
        if (Strings.isNullOrEmpty(accessKey)) {
            errors.add("AWS_ACCESS_KEY_ID env var is not set");
        }
        if (Strings.isNullOrEmpty(secretKey)) {
            errors.add("AWS_SECRET_ACCESS_KEY env var is not set");
        }
        if (!errors.isEmpty()) {
            for (String err : errors) {
                System.err.println(err);
            }
            System.exit(1);
        }

        String region = System.getenv("AWS_REGION");
        if (Strings.isNullOrEmpty(region)) {
            region = Regions.US_WEST_2.name();
        }

        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        AmazonSimpleWorkflowClient swf = new AmazonSimpleWorkflowClient(creds);
        swf.setRegion(Region.getRegion(Regions.valueOf(region)));

        log.info("Creating AmazonSimpleWorkflowClient region=" + region + " accessKey=" + accessKey);

        return swf;
    }

    public static String getDomain() {
        String domain = System.getenv("SWF_DOMAIN");
        if (Strings.isNullOrEmpty(domain)) {
            return "imprev-test";
        }
        return domain;
    }

    public static void ensureWorkflowType(AmazonSimpleWorkflowClient swf, RegisterWorkflowTypeRequest req) {
        WorkflowTypeInfos infos = swf.listWorkflowTypes(new ListWorkflowTypesRequest()
                .withDomain(req.getDomain())
                .withRegistrationStatus(RegistrationStatus.REGISTERED));
        for (WorkflowTypeInfo info : infos.getTypeInfos()) {
            if (info.getWorkflowType().getName().equalsIgnoreCase(req.getName())) {
                return;
            }
        }

        log.info("Creating wokflowType.  domain=" + req.getDomain() + " name=" + req.getName());
        swf.registerWorkflowType(req);
    }

    public static void ensureDomain(AmazonSimpleWorkflowClient swf, RegisterDomainRequest req) {
        DomainInfos resp = swf.listDomains(new ListDomainsRequest()
                .withRegistrationStatus(RegistrationStatus.REGISTERED));
        for (DomainInfo di : resp.getDomainInfos()) {
            if (di.getName().equals(req.getName())) {
                return;
            }
        }

        log.info("Creating domain.  domain=" + req.getName() + " retentionDays=" + req.getWorkflowExecutionRetentionPeriodInDays());
        swf.registerDomain(req);
    }

    public static void ensureActivityType(AmazonSimpleWorkflowClient swf, RegisterActivityTypeRequest req) {
        ActivityTypeInfos infos = swf.listActivityTypes(new ListActivityTypesRequest()
                .withDomain(req.getDomain())
                .withRegistrationStatus(RegistrationStatus.REGISTERED));
        for (ActivityTypeInfo info : infos.getTypeInfos()) {
            if (info.getActivityType().getName().equals(req.getName())) {
                return;
            }
        }

        log.info("Creating activityType.  domain=" + req.getDomain() + " name=" + req.getName());
        swf.registerActivityType(req);
    }

    public static <T> T getWorkflowStartInput(List<HistoryEvent> events, Class<T> clazz) {
        for (HistoryEvent history : events) {
            if ("WorkflowExecutionStarted".equals(history.getEventType())) {
                String json = history.getWorkflowExecutionStartedEventAttributes().getInput();
                try {
                    return new ObjectMapper().readValue(json, clazz);
                } catch (IOException e) {
                    throw new RuntimeException("Could not deserialize input: " + json + " - " + e.getMessage());
                }
            }
        }

        throw new RuntimeException("Could not find WorkflowExecutionStarted input");
    }

    public static String writeJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readJson(String json, Class<T> clazz) {
        try {
            return new ObjectMapper().readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HistoryEvent findLastActivityTaskFailed(List<HistoryEvent> events) {
        List<HistoryEvent> keep = filterHistoryEventsByType(events, EventType.ActivityTaskFailed);
        return (keep.isEmpty()) ? null : keep.get(keep.size()-1);
    }

    public static List<HistoryEvent> filterHistoryEventsByType(List<HistoryEvent> events, EventType type) {
        List<HistoryEvent> keep = new ArrayList<>();
        String typeStr = type.name();
        for (HistoryEvent event : events) {
            if (typeStr.equals(event.getEventType())) {
                keep.add(event);
            }
        }
        return keep;
    }

    public static DecisionStep workflowCompleted = (events) -> new Decision()
            .withDecisionType(DecisionType.CompleteWorkflowExecution)
            .withCompleteWorkflowExecutionDecisionAttributes(new CompleteWorkflowExecutionDecisionAttributes()
                    .withResult("OK"));

    public static DecisionStep workflowFailed = (events) -> new Decision()
            .withDecisionType(DecisionType.FailWorkflowExecution)
            .withFailWorkflowExecutionDecisionAttributes(new FailWorkflowExecutionDecisionAttributes()
                    .withReason("WorkflowFailed")
                    .withDetails("Not sure of details"));


}
