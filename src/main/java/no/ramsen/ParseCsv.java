package no.ramsen;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class ParseCsv extends ExtensionFunctionDefinition {
    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] {
                SequenceType.SINGLE_STRING,
                new MapType(BuiltInAtomicType.STRING, SequenceType.SINGLE_ITEM).getResultType(),
        };
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return MIN_ARGS;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return MAX_ARGS;
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return ArrayItemType.SINGLE_ARRAY;
    }

    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(Constants.NS_PREFIX, Constants.NS_URI, LOCAL_NAME);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext xPathContext, Sequence[] sequences) throws XPathException {
                boolean withHeader = false;
                String content = sequences[0].head().getStringValue();
                Reader reader = new StringReader(content);
                CSVFormat parser = CSVFormat.RFC4180;

                if (sequences.length > 1) {
                    MapItem options = (MapItem) sequences[1].head();
                    for (KeyValuePair pair : options.keyValuePairs()) {
                        String key = pair.key.getStringValue();
                        if (key.equals("delimiter")) {
                            String delimiter = pair.value.getStringValue();
                            parser = parser.withDelimiter(delimiter.charAt(0));
                        } else if (key.equals("with-header")) {
                            withHeader = pair.value.effectiveBooleanValue();
                            if (withHeader) {
                                parser = parser.withFirstRecordAsHeader();
                            }
                        }
                    }
                }

                List<CSVRecord> sourceRecords;
                try {
                    sourceRecords = parser.parse(reader).getRecords();
                } catch (IOException error) {
                    throw new XPathException(String.format("CSV parser error: %s", error.getMessage()));
                }

                List<GroundedValue> targetRecords = new ArrayList<>(sourceRecords.size());
                for (CSVRecord sourceRecord : sourceRecords) {
                    if (withHeader) {
                        DictionaryMap targetRecord = new DictionaryMap();
                        for (Entry<String, String> entry : sourceRecord.toMap().entrySet()) {
                            targetRecord.initialPut(
                                    entry.getKey(),
                                    new StringValue(entry.getValue())
                            );
                        }
                        targetRecords.add(targetRecord);
                    } else {
                        List<GroundedValue> targetRecord = new ArrayList<>(sourceRecord.size());
                        for (String value : sourceRecord) {
                            targetRecord.add(new StringValue(value));
                        }
                        targetRecords.add(new SimpleArrayItem(targetRecord));
                    }
                }
                return new SimpleArrayItem(targetRecords);
            }
        };
    }

    private static final int MIN_ARGS = 1;
    private static final int MAX_ARGS = 2;
    private static final String LOCAL_NAME = "parse-csv";
}
