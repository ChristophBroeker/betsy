package betsy.bpmn.engines.camunda

import betsy.bpmn.engines.BPMNEngine
import betsy.bpmn.model.BPMNProcess
import betsy.bpmn.model.BPMNTestBuilder
import betsy.bpmn.model.BPMNTestCase
import betsy.bpmn.reporting.BPMNTestcaseMerger
import betsy.common.config.Configuration
import betsy.common.tasks.*

import java.nio.file.Path

class CamundaEngine extends BPMNEngine {

    @Override
    String getName() {
        "camunda"
    }

    String getCamundaUrl() {
        "http://localhost:8080"
    }

    String getTomcatName() {
        "apache-tomcat-7.0.33"
    }

    Path getTomcatDir() {
        serverPath.resolve("server").resolve(tomcatName)
    }

    @Override
    void deploy(BPMNProcess process) {
        FileTasks.copyFileIntoFolder(process.targetPath.resolve("war").resolve("${process.name}.war"), tomcatDir.resolve("webapps"))

        //wait until it is deployed
        WaitTasks.sleep(15000)
    }

    @Override
    void buildArchives(BPMNProcess process) {
        XSLTTasks.transform(xsltPath.resolve("../scriptTask.xsl"),
                process.resourcePath.resolve("${process.name}.bpmn"),
                process.targetPath.resolve("war/WEB-INF/classes/${process.name}.bpmn-temp"))

        XSLTTasks.transform(xsltPath.resolve("camunda.xsl"),
                process.targetPath.resolve("war/WEB-INF/classes/${process.name}.bpmn-temp"),
                process.targetPath.resolve("war/WEB-INF/classes/${process.name}.bpmn"))

        FileTasks.deleteFile(process.targetPath.resolve("war/WEB-INF/classes/${process.name}.bpmn-temp"))

        new CamundaResourcesGenerator(groupId: process.groupId,
                processName: process.name,
                srcDir: process.resourcePath,
                destDir: process.targetPath.resolve("war"),
                version: process.version).generateWar()
    }

    @Override
    void buildTest(BPMNProcess process) {
        new BPMNTestBuilder(packageString: "${name}.${process.group}",
                logDir: tomcatDir.resolve("bin"),
                process: process
        ).buildTests()
    }

    @Override
    String getEndpointUrl(BPMNProcess process) {
        "http://localhost:8080/engine-rest/engine/default"
    }

    @Override
    void storeLogs(BPMNProcess process) {
        FileTasks.mkdirs(process.targetLogsPath)

        // TODO only copy log files from tomcat, the other files are files for the test
        FileTasks.copyFilesInFolderIntoOtherFolder(getTomcatDir().resolve("logs"), process.getTargetLogsPath());

        for (BPMNTestCase tc : process.getTestCases()) {
            FileTasks.copyFileIntoFolder(getTomcatDir().resolve("bin").resolve("log" + tc.getNumber() + ".txt"), process.getTargetLogsPath());
        }
    }

    @Override
    void install() {
        new CamundaInstaller(destinationDir: serverPath, tomcatName: getTomcatName()).install()
    }

    @Override
    void startup() {
        Path pathToJava7 = Configuration.getJava7Home();
        FileTasks.assertDirectory(pathToJava7)

        Path pathToJre7 = Configuration.getJre7Home();
        FileTasks.assertDirectory(pathToJre7)

        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build(serverPath, "camunda_startup.bat"), ["JAVA_HOME": pathToJava7.toString(), "JRE_HOME": pathToJre7.toString()])
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(serverPath.resolve("camunda_startup.sh")), ["JAVA_HOME": pathToJava7.toString(), "JRE_HOME": pathToJre7.toString()])
        WaitTasks.waitForAvailabilityOfUrl(30_000, 500, getCamundaUrl());
    }

    @Override
    void shutdown() {
        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build("taskkill").values("/FI", "WINDOWTITLE eq Tomcat"))
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(serverPath.resolve("camunda_shutdown.sh")))
    }

    @Override
    boolean isRunning() {
        URLTasks.isUrlAvailable(getCamundaUrl());
    }

    @Override
    void testProcess(BPMNProcess process) {
        for (BPMNTestCase testCase : process.testCases) {
            new CamundaTester(testCase: testCase,
                    testSrc: process.getTargetTestSrcPathWithCase(testCase.number),
                    restURL: getEndpointUrl(process),
                    reportPath: process.getTargetReportsPathWithCase(testCase.number),
                    testBin: process.getTargetTestBinPathWithCase(testCase.number),
                    key: process.name,
                    logDir: tomcatDir.resolve("logs")).runTest()
        }

        new BPMNTestcaseMerger(process.getTargetReportsPath()).mergeTestCases();
    }
}
