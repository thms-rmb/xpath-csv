package no.ramsen;

import net.sf.saxon.s9api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ParseCsvTests {
    @Test
    void parse() {
        Processor processor = new Processor(false);
        processor.registerExtensionFunction(new ParseCsv());

        XPathCompiler compiler = processor.newXPathCompiler();
        compiler.declareNamespace(Constants.NS_PREFIX, Constants.NS_URI);

        XdmItem row = new XdmAtomicValue("1,2,3");
        XdmItem res;
        try {
            res = compiler.evaluateSingle("csv:parse-csv(.)", row);
        } catch (SaxonApiException ignored) {
            Assertions.fail(); return;
        }

        Assertions.assertTrue(res.matches(ItemType.ANY_ARRAY));
    }
}
