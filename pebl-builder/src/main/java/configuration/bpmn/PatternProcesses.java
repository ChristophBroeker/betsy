package configuration.bpmn;

import java.util.Arrays;
import java.util.List;

import betsy.bpmn.model.BPMNTestCaseBuilder;
import pebl.benchmark.feature.Feature;
import pebl.benchmark.feature.FeatureSet;
import pebl.benchmark.test.Test;

/**
 * Class defining the processes and tests for the basic Workflow Control-Flow Patterns (WCP) 1-20
 */
public class PatternProcesses {

    public static final FeatureSet WCP01 = new FeatureSet(Groups.CFPATTERNS, "WCP01_Sequence");
    public static final FeatureSet WCP02 = new FeatureSet(Groups.CFPATTERNS, "WCP02_ParallelSplit");
    public static final FeatureSet WCP03 = new FeatureSet(Groups.CFPATTERNS, "WCP03_Synchronization");
    public static final FeatureSet WCP04 = new FeatureSet(Groups.CFPATTERNS, "WCP04_ExclusiveChoice");
    public static final FeatureSet WCP05 = new FeatureSet(Groups.CFPATTERNS, "WCP05_SimpleMerge");
    public static final FeatureSet WCP06 = new FeatureSet(Groups.CFPATTERNS, "WCP06_MultiChoice");
    public static final FeatureSet WCP07 = new FeatureSet(Groups.CFPATTERNS, "WCP07_StructuredSynchronizingMerge");
    public static final FeatureSet WCP08 = new FeatureSet(Groups.CFPATTERNS, "WCP08_MultiMerge");
    public static final FeatureSet WCP09 = new FeatureSet(Groups.CFPATTERNS, "WCP09_Structured_Discriminator");
    public static final FeatureSet WCP10 = new FeatureSet(Groups.CFPATTERNS, "WCP10_ArbitraryCycles");
    public static final FeatureSet WCP11 = new FeatureSet(Groups.CFPATTERNS, "WCP11_ImplicitTermination");
    public static final FeatureSet WCP12 = new FeatureSet(Groups.CFPATTERNS, "WCP12_MultipleInstancesWithoutSynchronization");
    public static final FeatureSet WCP13 = new FeatureSet(Groups.CFPATTERNS, "WCP13_MultipleInstancesWithAPrioriDesignTimeKnowledge");
    public static final FeatureSet WCP14 = new FeatureSet(Groups.CFPATTERNS, "WCP14_MultipleInstancesWithAPrioriRuntimeKnowledge");
    public static final FeatureSet WCP16 = new FeatureSet(Groups.CFPATTERNS, "WCP16_DeferredChoice");
    public static final FeatureSet WCP17 = new FeatureSet(Groups.CFPATTERNS, "WCP17_InterleavedParallelRouting");
    public static final FeatureSet WCP19 = new FeatureSet(Groups.CFPATTERNS, "WCP19_CancelTask");
    public static final FeatureSet WCP20 = new FeatureSet(Groups.CFPATTERNS, "WCP20_CancelCase");

    public static final Test WCP01_SEQUENCE = BPMNProcessBuilder.buildPatternProcess(
            "Test Process for WCP01 Sequence: Containing a Start Event, two ScriptTasks (for logging purposes) and an "
                    + "EndEvent. All connected by only basic SequenceFlows."
                    + "Test passed successfully if the trace confirms the execution of 'Task1'.",
            new Feature(WCP01, "WCP01_Sequence", ""),
            new BPMNTestCaseBuilder().assertTask1()
    );

    public static final Test WCP02_PARALLEL_SPLIT = BPMNProcessBuilder.buildPatternProcess(
            "WCP02 ParallelSplit: Checking the ability to create two parallel branches by a ParallelGateway followed by "
                    + "a ScriptTask in each branch."
                    + "Test passed successfully if both Tasks are executed.",
            new Feature(WCP02, "WCP02_ParallelSplit", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask2()
    );

    public static final Test WCP03_SYNCHRONIZATION = BPMNProcessBuilder.buildPatternProcess(
            "WCP03 Synchronization: Checking the ability to synchronize two parallel branches. The ScriptTask after the "
                    + "merging ParallelGateway has to be executed only once.",
            new Feature(WCP03, "WCP03_Synchronization", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask2().assertTask3()
    );

    public static final Test WCP04_EXCLUSIVE_CHOICE = BPMNProcessBuilder.buildPatternProcess(
            "WCP04 Exclusive Choice: Checking the ability to create exclusive branches based on an input. "
                    + "If the input contains 'a' it should execute task1, if it contains 'b' it should execute task2, "
                    + "in any other cases, the default task (task3) should be executed."
                    + "Special case: If the input contains 'a' and 'b' only the first branch must be activated (task1 here)",
            new Feature(WCP04, "WCP04_ExclusiveChoice", ""),
            new BPMNTestCaseBuilder().inputA().assertTask1(),
            new BPMNTestCaseBuilder().inputAB().assertTask1(),
            new BPMNTestCaseBuilder().inputB().assertTask2(),
            new BPMNTestCaseBuilder().inputC().assertTask3()
    );

    public static final Test WCP05_SIMPLE_MERGE = BPMNProcessBuilder.buildPatternProcess(
            "WCP05 Simple Merge: Checking the ability to merge multiple branches into a single branch with using a "
                    + "converging XOR gateway. The ScriptTask after the merging gateway must be triggered each time a "
                    + "token arrives.",
            new Feature(WCP05, "WCP05_SimpleMerge", ""),
            new BPMNTestCaseBuilder().inputA().assertTask1().assertTask4(),
            new BPMNTestCaseBuilder().inputB().assertTask2().assertTask4(),
            new BPMNTestCaseBuilder().inputC().assertTask3().assertTask4(),
            new BPMNTestCaseBuilder().inputAB().assertTask1().assertTask2().assertTask4().assertTask4(),
            new BPMNTestCaseBuilder().inputAC().assertTask1().assertTask3().assertTask4().assertTask4(),
            new BPMNTestCaseBuilder().inputBC().assertTask2().assertTask3().assertTask4().assertTask4(),
            new BPMNTestCaseBuilder().inputABC().assertTask1().assertTask2().assertTask3().assertTask4().assertTask4().assertTask4()
    );

    public static final Test WCP06_MULTI_CHOICE_INCLUSIVE_GATEWAY = BPMNProcessBuilder.buildPatternProcess(
            "WCP06 Multi Choice: Checking the ability to perform an OR-Split using an inclusive gateway. One or more branches should be created "
                    + "depending on the input. The third branch is only executed if no other condition is evaluated to true.",
            new Feature(WCP06, "WCP06_MultiChoice_InclusiveGateway", ""),
            new BPMNTestCaseBuilder().inputA().assertTask1(),
            new BPMNTestCaseBuilder().inputB().assertTask2(),
            new BPMNTestCaseBuilder().inputC().assertTask3(),
            new BPMNTestCaseBuilder().inputAB().assertTask1().assertTask2(),
            new BPMNTestCaseBuilder().inputAC().assertTask1(),
            new BPMNTestCaseBuilder().inputBC().assertTask2(),
            new BPMNTestCaseBuilder().inputABC().assertTask1().assertTask2()
    );

    public static final Test WCP06_MULTI_CHOICE_IMPLICIT = BPMNProcessBuilder.buildPatternProcess(
            "WCP06 Multi Choice: Checking the ability to perform an OR-Split using conditional sequence flows without a preceding gateway. " +
                    "One or more branches should be created depending on the input. " +
                    "The third branch is only executed if no other condition is evaluated to true.",
            new Feature(WCP06, "WCP06_MultiChoice_Implicit", ""),
            new BPMNTestCaseBuilder().inputA().assertTask1(),
            new BPMNTestCaseBuilder().inputB().assertTask2(),
            new BPMNTestCaseBuilder().inputC().assertTask3(),
            new BPMNTestCaseBuilder().inputAB().assertTask1().assertTask2(),
            new BPMNTestCaseBuilder().inputAC().assertTask1(),
            new BPMNTestCaseBuilder().inputBC().assertTask2(),
            new BPMNTestCaseBuilder().inputABC().assertTask1().assertTask2()
    );

    public static final Test WCP06_MULTI_CHOICE_COMPLEX_GATEWAY = BPMNProcessBuilder.buildPatternProcess(
            "WCP06 Multi Choice: Checking the ability to perform an OR-Split using a complex gateway. One or more branches should be created "
                    + "depending on the input. The third branch is only executed if no other condition is evaluated to true.",
            new Feature(WCP06, "WCP06_MultiChoice_ComplexGateway", ""),
            new BPMNTestCaseBuilder().inputA().assertTask1(),
            new BPMNTestCaseBuilder().inputB().assertTask2(),
            new BPMNTestCaseBuilder().inputC().assertTask3(),
            new BPMNTestCaseBuilder().inputAB().assertTask1().assertTask2(),
            new BPMNTestCaseBuilder().inputAC().assertTask1(),
            new BPMNTestCaseBuilder().inputBC().assertTask2(),
            new BPMNTestCaseBuilder().inputABC().assertTask1().assertTask2()
    );

    public static final Test WCP07_STRUCTURED_SYNCHRONIZING_MERGE = BPMNProcessBuilder.buildPatternProcess(
            "WCP07 StructuredSynchronizingMerge: Checks the ability to synchronize the merging of branches created "
                    + "earlier using a multiple choice (see WCP06).",
            new Feature(WCP07, "WCP07_StructuredSynchronizingMerge", ""),
            new BPMNTestCaseBuilder().inputA().assertTask1().assertTask3(),
            new BPMNTestCaseBuilder().inputB().assertTask2().assertTask3(),
            new BPMNTestCaseBuilder().inputAB().assertTask1().assertTask2().assertTask3()
    );

    public static final Test WCP08_MULTI_MERGE = BPMNProcessBuilder.buildPatternProcess(
            "WCP08 MultiMerge: Tests the convergence of two or more branches into a single path without synchronization."
                    + "The test is equivalent to WCP05 for BPMN.",
            new Feature(WCP08, "WCP08_MultiMerge", ""),
            new BPMNTestCaseBuilder().inputA().assertTask1().assertTask4(),
            new BPMNTestCaseBuilder().inputB().assertTask2().assertTask4(),
            new BPMNTestCaseBuilder().inputC().assertTask3().assertTask4(),
            new BPMNTestCaseBuilder().inputAB().assertTask1().assertTask2().assertTask4().assertTask4(),
            new BPMNTestCaseBuilder().inputAC().assertTask1().assertTask3().assertTask4().assertTask4(),
            new BPMNTestCaseBuilder().inputBC().assertTask2().assertTask3().assertTask4().assertTask4(),
            new BPMNTestCaseBuilder().inputABC().assertTask1().assertTask2().assertTask3().assertTask4().assertTask4().assertTask4()
    );

    public static final Test WCP09_STRUCTURED_DISCRIMINATOR_COMPLEXGATEWAY = BPMNProcessBuilder.buildPatternProcess(
            "WCP09 Structured Discriminator: Implementation of WCP09 using a merging"
                    + "ComplexGateway with activationCount>=1. I.e, the gateway fires upon completion of the first "
                    + "incoming token and is then disabled.",
            new Feature(WCP09, "WCP09_Structured_Discriminator_ComplexGateway", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask2().assertTask3()
    );

    public static final Test WCP09_STRUCTURED_DISCRIMINATOR_MULTI_INSTANCE = BPMNProcessBuilder.buildPatternProcess(
            "WCP09 partial workaround using MultiInstance: The flow after a MultiInstance Activity should continue after"
                    + "the first instance has completed."
                    + "This covers only a special case for WCP09 Discriminator where one of various EQUAL activities are used.",
            new Feature(WCP09, "WCP09_Structured_Discriminator_MultiInstance", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask1().assertTask1().assertTask2().assertTask3()
    );

    public static final Test WCP_10_ARBITRARY_CYCLES = BPMNProcessBuilder.buildPatternProcess(
            "WCP10 arbitrary cycle: Structure is based on the example on workflowpatterns.com: The test consists of a "
                    + "series of scripttasks and exclusive gateways. After the creation of the log, task1 is logged, "
                    + "afterwards a counter integerVariable is incremented and task1 is repeated if the integerVariable "
                    + "is <2; otherwise task2 is logged which will be repeated if integerVariable<3",
            new Feature(WCP10, "WCP10_ArbitraryCycles", ""),
            new BPMNTestCaseBuilder().setIntegerVariable(2).assertTask1().assertTask2(),
            new BPMNTestCaseBuilder().setIntegerVariable(1).assertTask1().assertTask2().assertTask2(),
            new BPMNTestCaseBuilder().setIntegerVariable(0).assertTask1().assertTask1().assertTask2().assertTask2()
    );

    public static final Test WCP11_IMPLICIT_TERMINATION = BPMNProcessBuilder.buildPatternProcess(
            "A process that terminates when all contained activity instances have completed",
            new Feature(WCP11, "WCP11_ImplicitTermination", ""),
            new BPMNTestCaseBuilder().assertTask1()
    );

    public static final Test WCP12_MULTIPLE_INSTANCES_WITHOUT_SYNCHRONIZATION = BPMNProcessBuilder.buildPatternProcess(
            "A process which creates three instances of one script task using multiInstanceLoopCharacteristics, followed by a second activity. " +
                    "The behavior of the multi instance activity is set to None. Hence, a signal should be fired for every complete multi instance activity.",
            new Feature(WCP12, "WCP12_MultipleInstancesWithoutSynchronization", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask1().assertTask1().assertTask2().assertTask3().assertTask3().assertTask3()
    );

    public static final Test WCP13_MULTIPLE_INSTANCES_WITH_A_PRIORI_DESIGN_TIME_KNOWLEDGE = BPMNProcessBuilder.buildPatternProcess(
            "A process which creates three instances of one script task using multiInstanceLoopCharacteristics, followed by a second activity. " +
                    "The number of instances is hard-coded into the process. The behavior of the multi instance activity is set to 'All'.",
            new Feature(WCP13, "WCP13_MultipleInstancesWithAPrioriDesignTimeKnowledge", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask1().assertTask1().assertTask2()
    );

    public static final Test WCP14_MULTIPLE_INSTANCES_WITH_A_PRIORI_RUNTIME_KNOWLEDGE = BPMNProcessBuilder.buildPatternProcess(
            "A process with a multiple instances activity, where the loop cardinality is read from a variable at run-time. " +
                    "The behavior of the multi instance activity is set to 'All'.",
            new Feature(WCP14, "WCP14_MultipleInstancesWithAPrioriRuntimeKnowledge", ""),
            new BPMNTestCaseBuilder().setIntegerVariable(3).assertTask1().assertTask1().assertTask1().assertTask2()
    );

    public static final Test WCP16_DEFERRED_CHOICE = BPMNProcessBuilder.buildPatternProcess(
            "An event-based exclusive gateway with two possible branches wait for one out of two signals which are signaled depending on the input. "
                    + "Using a timer, it is ensured that the signals are signaled when the event-based gateway is already waiting for them. "
                    + "Based on EventBasedGateway_Signals",
            new Feature(WCP16, "WCP16_DeferredChoice", ""),
            new BPMNTestCaseBuilder().inputA().assertTask1().assertTask3().optionDelay(8000),
            new BPMNTestCaseBuilder().inputB().assertTask2().assertTask4().optionDelay(8000)
    );

    public static final Test WCP17_INTERLEAVED_PARALLEL_ROUTING = BPMNProcessBuilder.buildPatternProcess(
            "A set of activity instances is executed sequentially in an " +
                    "order that is decided at run time. No two activity instances of this set are " +
                    "active at the same point in time",
            new Feature(WCP17, "WCP17_InterleavedParallelRouting", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask2().assertTask3()
    );

    public static final Test WCP19_CANCEL_TASK = BPMNProcessBuilder.buildPatternProcess(
            "An activity can be canceled when it emits an error event.",
            new Feature(WCP19, "WCP19_CancelTask", ""),
            new BPMNTestCaseBuilder().assertTask2()
    );
    public static final Test WCP20_CANCEL_CASE_ERROR = BPMNProcessBuilder.buildPatternProcess(
            "Cancels a sub-process by emitting an error event inside the sub-process which is handled through a boundary event. "
                    + "See Error_BoundaryEvent_SubProcess_Interrupting",
            new Feature(WCP20, "WCP20_CancelCaseError", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask2()
    );

    public static final Test WCP20_CANCEL_CASE_CANCEL = BPMNProcessBuilder.buildPatternProcess(
            "Cancels a sub-process by emitting a cancel event inside the sub-process which is handled through a boundary event. "
                    + "See Cancel_Event",
            new Feature(WCP20, "WCP20_CancelCaseCancel", ""),
            new BPMNTestCaseBuilder().assertTask1().assertTask2()
    );

    public static final Test WCP20_CANCEL_CASE_TERMINATE = BPMNProcessBuilder.buildPatternProcess(
            "Cancels a process immediately by emitting a terminate event. See Terminate_Event",
            new Feature(WCP20, "WCP20_CancelCaseTerminate", ""),
            new BPMNTestCaseBuilder()
    );

    public static final List<Test> PATTERNS = Arrays.asList(
            WCP01_SEQUENCE,

            WCP02_PARALLEL_SPLIT,

            WCP03_SYNCHRONIZATION,

            WCP04_EXCLUSIVE_CHOICE,

            WCP05_SIMPLE_MERGE,

            WCP06_MULTI_CHOICE_INCLUSIVE_GATEWAY,
            WCP06_MULTI_CHOICE_IMPLICIT,
            WCP06_MULTI_CHOICE_COMPLEX_GATEWAY,

            WCP07_STRUCTURED_SYNCHRONIZING_MERGE,

            WCP08_MULTI_MERGE,

            // WCP09 direct solution:
            WCP09_STRUCTURED_DISCRIMINATOR_COMPLEXGATEWAY,
            // WCP09 workaround:
            WCP09_STRUCTURED_DISCRIMINATOR_MULTI_INSTANCE,

            WCP_10_ARBITRARY_CYCLES,

            WCP11_IMPLICIT_TERMINATION,

            WCP12_MULTIPLE_INSTANCES_WITHOUT_SYNCHRONIZATION,

            WCP13_MULTIPLE_INSTANCES_WITH_A_PRIORI_DESIGN_TIME_KNOWLEDGE,

            WCP14_MULTIPLE_INSTANCES_WITH_A_PRIORI_RUNTIME_KNOWLEDGE,

            // WCP15 is not supported

            WCP16_DEFERRED_CHOICE,

            WCP17_INTERLEAVED_PARALLEL_ROUTING,

            // WCP18 is not supported

            WCP19_CANCEL_TASK,

            // WCP20 here
            WCP20_CANCEL_CASE_ERROR,
            WCP20_CANCEL_CASE_CANCEL,
            WCP20_CANCEL_CASE_TERMINATE

    );
}
