package pebl.benchmark.test.steps;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import pebl.MapAdapter;
import pebl.benchmark.test.TestStep;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class ExecuteScript extends TestStep {

    @XmlElement(required = true)
    private final String name;

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(MapAdapter.class)
    private final Map<String, String> parameters;

    ExecuteScript() {
        this("", Collections.emptyMap());
    }

    public ExecuteScript(String name, Map<String, String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
