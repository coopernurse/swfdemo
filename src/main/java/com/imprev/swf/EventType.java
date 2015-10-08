package com.imprev.swf;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public enum EventType {

    WorkflowExecutionStarted,
    WorkflowExecutionCompleted,
    WorkflowExecutionFailed,
    WorkflowExecutionTimedOut,
    WorkflowExecutionCanceled,
    WorkflowExecutionTerminated,
    WorkflowExecutionContinuedAsNew,
    WorkflowExecutionCancelRequested,
    DecisionTaskScheduled,
    DecisionTaskStarted,
    DecisionTaskCompleted,
    DecisionTaskTimedOut,
    ActivityTaskScheduled,
    ScheduleActivityTaskFailed,
    ActivityTaskStarted,
    ActivityTaskCompleted,
    ActivityTaskFailed,
    ActivityTaskTimedOut,
    ActivityTaskCanceled,
    ActivityTaskCancelRequested,
    RequestCancelActivityTaskFailed,
    WorkflowExecutionSignaled,
    MarkerRecorded,
    TimerStarted,
    StartTimerFailed,
    TimerFired,
    TimerCanceled,
    CancelTimerFailed,
    StartChildWorkflowExecutionInitiated,
    StartChildWorkflowExecutionFailed,
    ChildWorkflowExecutionStarted,
    ChildWorkflowExecutionCompleted,
    ChildWorkflowExecutionFailed,
    ChildWorkflowExecutionTimedOut,
    ChildWorkflowExecutionCanceled,
    ChildWorkflowExecutionTerminated,
    SignalExternalWorkflowExecutionInitiated,
    ExternalWorkflowExecutionSignaled,
    SignalExternalWorkflowExecutionFailed,
    RequestCancelExternalWorkflowExecutionInitiated,
    ExternalWorkflowExecutionCancelRequested,
    RequestCancelExternalWorkflowExecutionFailed,
    LambdaFunctionScheduled,
    LambdaFunctionStarted,
    LambdaFunctionCompleted,
    LambdaFunctionFailed,
    LambdaFunctionTimedOut,
    ScheduleLambdaFunctionFailed,
    StartLambdaFunctionFailed

}
