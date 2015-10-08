package com.imprev.swfdemo;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.Run;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imprev.swf.Log4JInit;
import com.imprev.swf.SwfUtil;
import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class StartWorkflow {

    private static final Logger log = Logger.getLogger(StartWorkflow.class);

    public static void main(String argv[]) throws Exception {
        Log4JInit.init();
        AmazonSimpleWorkflowClient swf = SwfUtil.getAmazonSimpleWorkflowClient();

        String workflowName = "BuyDomainDecider";
        String workflowId = UUID.randomUUID().toString();

        BuyDomainRequest req = new BuyDomainRequest("4111-1111-1111-1111", "bestrealestate2.com", "james@bitmechanic.com");

        Run run = swf.startWorkflowExecution(new StartWorkflowExecutionRequest()
                .withDomain(SwfUtil.getDomain())
                .withWorkflowId(workflowId)
                .withTaskList(new TaskList().withName(workflowName))
                .withTaskStartToCloseTimeout("3600")
                .withChildPolicy(ChildPolicy.TERMINATE)
                .withWorkflowType(new WorkflowType().withName(workflowName).withVersion("1.0"))
                .withInput(new ObjectMapper().writeValueAsString(req)));
        log.info("Started workflow.  workflowName=" + workflowName + " workflowId=" + workflowId +
                " runId=" + run.getRunId());
    }

}
