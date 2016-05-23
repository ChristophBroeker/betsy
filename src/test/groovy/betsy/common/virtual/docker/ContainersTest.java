package betsy.common.virtual.docker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static betsy.common.config.Configuration.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Christoph Broeker
 * @version 1.0
 */
public class ContainersTest {


    private DockerMachine dockerMachine;
    private Path docker = Paths.get(get("docker.dir"));
    private Path images = docker.resolve("image");

    @Before
    public void setUp() throws Exception {
        dockerMachine = DockerMachines.create(get("dockermachine.test.name"), get("dockermachine.test.ram"), get("dockermachine.test.cpu"));
    }

    @After
    public void tearDown() throws Exception {
        DockerMachines.remove(dockerMachine);
    }


    @Test
    public void create() throws Exception {
        String containerName = "test";
        Container container = Containers.create(dockerMachine, containerName, "ubuntu", "sleep 10m" );
        assertEquals("The name of the containers should be the same.", container.getName(), Containers.getAll(dockerMachine).get(containerName).getName());
        Containers.remove(dockerMachine, container);
    }

    @Test
    public void createConstraints() throws Exception {
        String containerName = "test";
        java.util.Optional<Image> betsyImage = java.util.Optional.ofNullable(Images.getAll(dockerMachine).get("betsy"));
        boolean betsyImageWasCreated = true;
        if (!betsyImage.isPresent()) {
            betsyImageWasCreated = false;
            betsyImage = java.util.Optional.ofNullable(Images.build(dockerMachine, images.resolve("betsy").toAbsolutePath(), "betsy"));
        }
        Container container = Containers.create(dockerMachine, containerName, "betsy", 4000, 300, "calibrate", "bpel", "ode", "sequence");
        assertEquals("The name of the containers should be the same.", container.getName(), Containers.getAll(dockerMachine).get(containerName).getName());
        Containers.remove(dockerMachine, container);
        if (betsyImage.isPresent() && !betsyImageWasCreated) {
            Images.remove(dockerMachine, betsyImage.get());
        }
    }

    @Test
    public void remove() throws Exception {
        String containerName = "test";
        Container container = Containers.create(dockerMachine, containerName, "hello-world");
        Containers.remove(dockerMachine, container);
        assertNull("The container should be null.", Containers.getAll(dockerMachine).get(container.getName()));
    }

    @Test
    public void removeAll() throws Exception {
        Container container = Containers.create(dockerMachine, "test", "hello-world");
        int size = Containers.getAll(dockerMachine).size();
        ArrayList<Container> containers = new ArrayList<>();
        containers.add(container);
        Containers.removeAll(dockerMachine, containers);
        assertEquals("The container should be null.", --size, Containers.getAll(dockerMachine).size());

    }

    @Test
    public void getAll() throws Exception {
        String containerName = "test";
        Container container = Containers.create(dockerMachine, containerName, "hello-world");
        HashMap<String, Container> containerHashMap = Containers.getAll(dockerMachine);
        assertEquals("The containers should be the same", container, containerHashMap.get(containerName));
    }


    @Test
    public void run() throws Exception {
        String containerName = "test";
        Container container = Containers.run(dockerMachine, containerName, "ubuntu", "sleep 10m" );
        assertEquals("The name of the containers should be the same.", container.getName(), Containers.getAll(dockerMachine).get(containerName).getName());
        Containers.remove(dockerMachine, container);
    }

    @Test
    public void runConstraints() throws Exception {
        String containerName = "test";
        java.util.Optional<Image> betsyImage = java.util.Optional.ofNullable(Images.getAll(dockerMachine).get("betsy"));
        boolean betsyImageWasCreated = true;
        if (!betsyImage.isPresent()) {
            betsyImageWasCreated = false;
            betsyImage = java.util.Optional.ofNullable(Images.build(dockerMachine, images.resolve("betsy").toAbsolutePath(), "betsy"));
        }
        Images.build(dockerMachine, images.resolve("betsy").toAbsolutePath(), "betsy");
        Container container = Containers.run(dockerMachine, containerName, "betsy", 4000, 300, "calibrate", "bpel", "ode", "sequence");
        assertEquals("The name of the containers should be the same.", container.getName(), Containers.getAll(dockerMachine).get(containerName).getName());
        Containers.remove(dockerMachine, container);
        if (betsyImage.isPresent() && !betsyImageWasCreated) {
            Images.remove(dockerMachine, betsyImage.get());
        }
    }
}