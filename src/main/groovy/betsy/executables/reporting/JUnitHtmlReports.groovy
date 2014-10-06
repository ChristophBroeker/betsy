package betsy.executables.reporting

import ant.tasks.AntUtil
import betsy.config.Configuration;
import betsy.tasks.ConsoleTasks
import betsy.tasks.FileTasks
import org.apache.log4j.Logger

import java.nio.file.Path

class JUnitHtmlReports {

    private static final Logger log = Logger.getLogger(JUnitHtmlReports.class)

    /**
     * tests folder
     */
    Path path

    public void create() {
        Path antBinFolder = Configuration.antHome.resolve("bin").toAbsolutePath()

        log.info "creating reporting ant scripts"
        FileTasks.createFile(path.resolve("build.xml"), createAntReportFile());

        log.info "executing reporting ant scripts"
        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build(path, antBinFolder.resolve("ant.bat").toString()))

        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(path, "chmod").values("+x", antBinFolder.resolve("ant").toString()))
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(path, antBinFolder.resolve("ant").toString()))
    }

    private String createAntReportFile() {
        """
<project name="${path}" default="reports">

    <target name="reports">

        <mkdir dir="reports"/>
        <junitreport todir="reports">
            <fileset dir=".">
                <include name="**/TEST-*.xml"/>
            </fileset>
            <report format="frames" todir="reports/html"/>
        </junitreport>

    </target>

</project>"""
    }

}
