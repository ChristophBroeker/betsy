package betsy.bpmn.engines.jbpm;

import betsy.common.config.Configuration;
import betsy.common.tasks.ConsoleTasks;
import betsy.common.tasks.FileTasks;
import betsy.common.tasks.WaitTasks;
import betsy.common.util.ClasspathHelper;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class JbpmEngine620 extends JbpmEngine {

    @Override
    public String getName() {
        return "jbpm620";
    }

    @Override
    public Path getXsltPath() {
        return ClasspathHelper.getFilesystemPathFromClasspathPath("/bpmn/" + super.getName());
    }

    @Override
    public String getJbossName() {
        return "wildfly-8.1.0.Final";
    }

    @Override
    public String getLogFileNameForShutdownAnalysis() {
        return "server.log";
    }

    @Override
    public void install() {
        JbpmInstaller jbpmInstaller = new JbpmInstaller();
        jbpmInstaller.setDestinationDir(getServerPath().getParent());
        jbpmInstaller.setFileName("jbpm-6.2.0.Final-installer-full.zip");
        jbpmInstaller.install();
    }

    @Override
    protected String createProcessHistoryURL(String deploymentId) {
        return getJbpmnUrl()+ "/rest/history/instance/1";
    }
//    @Override
//    public void startup() {
////        Path pathToJava7 = Configuration.getJava7Home();
////        FileTasks.assertDirectory(pathToJava7);
////
////        Map<String, String> map = new LinkedHashMap<>(1);
////        map.put("JAVA_HOME", pathToJava7.toString());
////        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build(getServerPath().resolve("jbpm-installer"), getAntPath().toAbsolutePath() + "/ant -q start.demo.noeclipse"), map);
////        Map<String, String> map1 = new LinkedHashMap<>(1);
////        map1.put("JAVA_HOME", pathToJava7.toString());
////        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getServerPath().resolve("jbpm-installer"), getAntPath().toAbsolutePath() + "/ant -q start.demo.noeclipse"), map1);
////        //waiting for jbpm-console for deployment and instantiating
////        WaitTasks.waitForSubstringInFile(180000, 5000, getJbossLogDir().resolve("server.log"), "JBAS018559: Deployed \"jbpm-console.war\"");
////    }
//
//    private Path getJbossLogDir() {
//       return getJbossStandaloneDir().resolve("log");
//    }
//
//    public Path getJbossStandaloneDir() {
//        return getServerPath().resolve("jbpm-installer").resolve(getJbossName()).resolve("standalone");
//
//
//
//    }

}
