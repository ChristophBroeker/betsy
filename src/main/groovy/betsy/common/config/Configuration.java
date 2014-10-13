package betsy.common.config;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Configuration {

    /**
     * Get the value of the given key.
     *
     * @param key Key in config to get value for
     * @return Value assigned to the key, or null if key is not set
     */
    public static String getString(final String key) {
        String result = properties.getProperty(key);

        if (result == null) {
            throw new ConfigurationException("No value found for key [" + key + "]");
        }

        return result;
    }

    public static String get(final String key) {
        return getString(key);
    }

    public static Path getDownloadsDir() {
        return Paths.get(properties.getProperty("downloads.dir"));
    }

    public static Path getAntHome() {
        return Paths.get(properties.getProperty("ant.home"));
    }

    static {

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("config.properties"), StandardCharsets.UTF_8)) {

            Properties props = new Properties();
            props.load(reader);

            properties = props;

        } catch (IOException e) {
            throw new RuntimeException("something went wrong", e);
        }
    }

    public static Properties properties;

    /**
     * Get the File where VirtualBox is installed in.
     *
     * @return directory file
     */
    public static Path getVboxHome() {
        Path vboxHomePath = Paths.get(properties.getProperty("virtual.vbox.home"));

        if (!Files.isDirectory(vboxHomePath)) {
            throw new ConfigurationException("Found [" + vboxHomePath + "] for key [virtual.vbox.home] " + "Path to VirtualBox directory");
        }

        return vboxHomePath;
    }

    /**
     * Get the VirtualBox Manage File
     *
     * @return file of VirtualBox Manage
     */
    public static Path getVBoxManage() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return getVboxHome().resolve("VBoxManage.exe");
        } else if (SystemUtils.IS_OS_UNIX) {
            return getVboxHome().resolve("VBoxManage");
        } else {
            throw new IllegalStateException("Incompatible OS running");
        }
    }

    /**
     * Get the VirtualBox WebService File
     *
     * @return file of VirtualBox WebService
     */
    public static Path getVBoxWebSrv() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return getVboxHome().resolve("VBoxWebSrv.exe");
        } else if (SystemUtils.IS_OS_UNIX) {
            return getVboxHome().resolve("vboxwebsrv");
        } else {
            throw new IllegalStateException("Incompatible OS running");
        }
    }

    public static boolean useRunningVM() {
        return Boolean.valueOf(properties.getProperty("virtual.useRunningVM"));
    }

    public static Path getVirtualDownloadDir() {
        return Paths.get("vm_download");
    }

    public static Integer getTimeToStartVboxWebService() {
        return Integer.parseInt(properties.getProperty("virtual.vbox.websrv.wait"));
    }

    public static void setPartnerIpAndPort(String newPartnerAddress) {
        properties.setProperty("partner.ipAndPort", newPartnerAddress);
    }

    public static Path getJava7Home() {
        // Trying to determine JDK7 Path using SysEnv
        String java7env = System.getenv("JAVA7_HOME");
        Path java7home;

        if (java7env != null) {
            java7home = Paths.get(java7env);
        } else {
            // Fallback to properties file
            java7home = Paths.get(properties.getProperty("java7.home"));
        }

        if (!Files.isDirectory(java7home)) {
            throw new ConfigurationException("Found [" + java7home + "] for key [java7.home] " + "Path to JAVA_HOME, but the directory does not exist!");
        }

        return java7home;
    }

    public static Path getJre7Home() {
        // Trying to determine JDK7 Path using SysEnv
        String jre7env = System.getenv("JRE7_HOME");
        Path result;

        if (jre7env != null) {
            result = Paths.get(jre7env);
        } else {
            // Fallback to properties file
            result = Paths.get(properties.getProperty("jre7.home"));
        }

        if (!Files.isDirectory(result)) {
            log.info("Found [" + result + "] for key [jre7.home] " + "Path to JRE_HOME, but the directory does not exist! -> trying JDK7 HOME");


            Path jreInJava7Home = getJava7Home().resolve("jre");

            if(!Files.isDirectory(jreInJava7Home)) {
                throw new ConfigurationException("No JRE of Java 7 could be found!");
            }

            result = jreInJava7Home;
        }

        return result;
    }

    private static final Logger log = Logger.getLogger(Configuration.class);

}
