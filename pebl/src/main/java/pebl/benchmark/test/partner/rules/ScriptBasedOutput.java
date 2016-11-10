package pebl.benchmark.test.partner.rules;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class ScriptBasedOutput extends NoOutput {

    @XmlValue
    private final String groovyScript;

    ScriptBasedOutput() {
        this("");
    }

    public ScriptBasedOutput(String groovyScript) {
        this.groovyScript = Objects.requireNonNull(groovyScript);
    }

    public String getGroovyScript() {
        return groovyScript;
    }
}
