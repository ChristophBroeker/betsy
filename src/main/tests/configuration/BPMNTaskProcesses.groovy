package configuration

import betsy.data.BPMNProcess
import betsy.data.BPMNTestCase

class BPMNTaskProcesses {
    static BPMNProcessBuilder builder = new BPMNProcessBuilder()

    public static final BPMNProcess SIMPLE = builder.buildTaskProcess(
            "SequenceFlow", "de.uniba.dsg", "1.0", "A Test for the basic process using a script task",
            [
                    new BPMNTestCase().buildSimple()
            ]
    )

    public static final BPMNProcess MULTI_SEQUENTIAL = builder.buildTaskProcess(
            "MultiSequencialTask", "de.uniba.dsg", "1.0", "A simple Test for a 3 times sequentially looped script task",
            [
                    new BPMNTestCase().buildMulti3()
            ]
    )

    public static final List<BPMNProcess> TASKS = [
            SIMPLE,
            MULTI_SEQUENTIAL
    ].flatten() as List<BPMNProcess>
}
