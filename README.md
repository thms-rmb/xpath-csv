# XPath CSV

XPath CSV is a CSV library for XPath, XQuery and XSLT.

## `csv:parse-csv`

### Summary

Parses a CSV formatted string and returns an array
structure. Depending on the options passed, the array members will be
`array(xs:string)` or `map(xs:string, xs:string)`.

### Signatures

```
csv:parse-csv($csv-text as xs:string) as array(*)
```

```
csv:parse-csv($csv-text as xs:string, $options as map(*)) as array(*)
```
