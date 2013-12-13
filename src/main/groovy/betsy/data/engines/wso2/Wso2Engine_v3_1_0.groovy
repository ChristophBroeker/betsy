package betsy.data.engines.wso2

import betsy.Configuration
import betsy.data.BetsyProcess
import betsy.data.engines.LocalEngine
import betsy.tasks.ConsoleTasks
import betsy.tasks.FileTasks
import betsy.tasks.WaitTasks

import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.Files.copy

class Wso2Engine_v3_1_0 extends LocalEngine {

    @Override
    Path getXsltPath() {
        return Paths.get("src/main/xslt/ode");
    }

    @Override
    String getName() {
        return "wso2_v3_1_0";
    }

    @Override
    void install() {
        FileTasks.deleteDirectory(getServerPath());

        String fileName = getZipFileName()

        ant.get(dest: Configuration.get("downloads.dir"), skipexisting: true) {
            ant.url url: "https://lspi.wiai.uni-bamberg.de/svn/betsy/${fileName}"
        }

        ant.unzip src: Configuration.getPath("downloads.dir").resolve(fileName), dest: getServerPath()

        FileTasks.createFile(getServerPath().resolve("startup.bat"), "start startup-helper.bat")
        FileTasks.createFile(getServerPath().resolve("startup-helper.bat"), "TITLE wso2server\ncd ${getBinDir().toAbsolutePath()} && call wso2server.bat")
    }

    public String getZipFileName() {
        "wso2bps-3.1.0.zip"
    }

    @Override
    void startup() {
        ConsoleTasks.executeOnWindows(ConsoleTasks.CliCommand.build(getServerPath(), "startup.bat"))
        WaitTasks.sleep(2000);
        ant.waitfor(maxwait: "60", maxwaitunit: "second", checkevery: "500") {
            and {
                resourcecontains(resource: getLogsFolder().resolve("wso2carbon.log"),
                        substring: "WSO2 Carbon started in ")
            }
        }
    }

    public Path getLogsFolder() {
        getCarbonHome().resolve("repository").resolve("logs")
    }

    public Path getBinDir() {
        getCarbonHome().resolve("bin")
    }

    public Path getCarbonHome() {
        getServerPath().resolve("wso2bps-3.1.0")
    }

    @Override
    void shutdown() {
        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build("taskkill").values("/FI", "WINDOWTITLE eq wso2server"))

        // required for jenkins - may have side effects but this should not be a problem in this context
        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build("taskkill").values("/FI", "WINDOWTITLE eq Administrator:*"))
    }

    @Override
    void failIfRunning() {

    }

    @Override
    void deploy(BetsyProcess process) {
        copy(process.getTargetPackageFilePath(), getDeploymentDir().resolve(process.getTargetPackageFilePath().getFileName()))

        ant.waitfor(maxwait: "60", maxwaitunit: "second", checkevery: "500") {
            and {
                resourcecontains(resource: getLogsFolder().resolve("wso2carbon.log"),
                        substring: "{org.apache.ode.bpel.engine.BpelServerImpl} -  Registered process")
            }
        }

        WaitTasks.sleep(20000);
    }

    public Path getDeploymentDir() {
        getCarbonHome().resolve("repository").resolve("deployment").resolve("server").resolve("bpel")
    }

    @Override
    void buildArchives(BetsyProcess process) {
        packageBuilder.createFolderAndCopyProcessFilesToTarget(process)

        // engine specific steps
        ant.xslt(in: process.bpelFilePath, out: process.targetBpelPath.resolve("deploy.xml"), style: xsltPath.resolve("bpel_to_ode_deploy_xml.xsl"))
        ant.replace(file: process.targetBpelPath.resolve("TestInterface.wsdl"), token: "TestInterfaceService", value: "${process.name}TestInterfaceService")
        ant.replace(file: process.targetBpelPath.resolve("deploy.xml"), token: "TestInterfaceService", value: "${process.name}TestInterfaceService")

        packageBuilder.replaceEndpointTokenWithValue(process)
        packageBuilder.replacePartnerTokenWithValue(process)

        packageBuilder.bpelFolderToZipFile(process)
    }

    @Override
    String getEndpointUrl(BetsyProcess process) {
        return "http://localhost:9763/services/${process.name}TestInterfaceService"
    }

    @Override
    void storeLogs(BetsyProcess process) {
        FileTasks.mkdirs(process.targetLogsPath)
        ant.copy(todir: process.targetLogsPath) {
            ant.fileset(dir: getLogsFolder())
        }
    }
}
