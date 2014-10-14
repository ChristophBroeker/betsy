package betsy.bpmn;

import betsy.bpmn.model.BPMNNamespaceContext;
import betsy.bpmn.model.BPMNProcess;
import configuration.bpmn.BPMNProcessRepository;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.*;

public class ProcessValidator {

    private List<BPMNProcess> processes;

    private DocumentBuilder builder;

    private XPath xpath;

    public static final String[] ALLOWED_LOG_MESSAGES = new String[]{
            "taskNotInterrupted", "callableElementExecuted", "timerInternal", "taskInstanceExecuted", "timerEvent", "default", "timerExternal",
            "signaled", "lane2", "end", "lane1", "started", "task1", "CREATE_LOG_FILE", "task2",
            "task3", "false", "subprocess", "normalTask", "interrupted", "condition", "success",
            "true", "compensate", "transaction"
    };

    public void validate() {
        processes = new BPMNProcessRepository().getByName("ALL");

        setUpXmlObjects();
        assertNamingConventionsCorrect();
        assertLogMessagesCorrect();
    }

    private void assertNamingConventionsCorrect() {
        for (BPMNProcess process : processes) {
            try {
                Document doc = builder.parse(process.getResourceFile().toString());

                XPathExpression definitionExpression = xpath.compile("//*[local-name() = 'definitions' and @id='" + process.getName() + "Test']");
                NodeList nodeList = (NodeList) definitionExpression.evaluate(doc, XPathConstants.NODESET);
                if (nodeList.getLength() != 1) {
                    throw new IllegalStateException("No definitions element with id '" + process.getName() + "Test' found in process " + process.getName());
                }

                XPathExpression processExpression = xpath.compile("//*[local-name() = 'process' and @id='" + process.getName() + "']");
                nodeList = (NodeList) processExpression.evaluate(doc, XPathConstants.NODESET);
                if (nodeList.getLength() != 1) {
                    throw new IllegalStateException("No process element with id '" + process.getName() + "' found in process " + process.getName());
                }

            } catch (SAXException | XPathExpressionException | IOException e) {
                throw new IllegalStateException("Validation failed for file " + process.getResourceFile().toString(), e);
            }
        }
    }

    private void assertLogMessagesCorrect() {
        Set<String> messages = new HashSet<>();

        for (BPMNProcess process : processes) {
            try {
                Document doc = builder.parse(process.getResourceFile().toString());

                XPathExpression definitionExpression = xpath.compile("//*[local-name() = 'script']");
                NodeList nodeList = (NodeList) definitionExpression.evaluate(doc, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    String textContent = nodeList.item(i).getTextContent();

                    if(textContent.contains(",")){
                        for(String x : textContent.split(",")) {
                            addMessage(messages, process, x);
                        }
                    } else {
                        addMessage(messages, process, textContent);
                    }
                }

            } catch (SAXException | XPathExpressionException | IOException e) {
                throw new IllegalStateException("Validation failed for file " + process.getResourceFile().toString(), e);
            }
        }

        // TODO move this to the production code

        String[] actualMessages = messages.toArray(new String[messages.size()]);

        Arrays.sort(actualMessages);
        Arrays.sort(ALLOWED_LOG_MESSAGES);

        Assert.assertArrayEquals(ALLOWED_LOG_MESSAGES, actualMessages);
    }

    private void addMessage(Set<String> assertions, BPMNProcess process, String x) {
        if(!Arrays.asList(ALLOWED_LOG_MESSAGES).contains(x)) {
            System.out.println(x + " in " + process.getResourceFile());
        }

        assertions.add(x);
    }

    private void setUpXmlObjects() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            xpath = xPathfactory.newXPath();
            xpath.setNamespaceContext(new BPMNNamespaceContext());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Could not validate process files", e);
        }
    }
}