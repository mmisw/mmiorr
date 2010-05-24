MMI Ont2Dot Ontology-to-Graphviz converter
Carlos Rueda - MMI/MBARI

Ont2Dot generates a diagram for an OWL ontology. This diagram is actually
encoded in a format that Graphviz (http://graphviz.org/) tools can process to
generate the final output format.

Example usage (maven): 

# Generate dot (device.dot in this example)
$ mvn exec:java -Dexec.args="http://mmisw.org/ont/mmi/device"

  Note: you can generate a self-contained jar with:
  $ mvn assembly:assembly
  and then ejecute the jar directly:
  $ java -jar target/ont2dot-xxxxxx.jar http://mmisw.org/ont/mmi/device

# Use Graphviz to generate the desired graphical representation, eg:
$ dot -Tpng device.dot > device.png

Call the program with no arguments to get a synopsis of usage.

Please note: 

- The implementation is functional for both classes and individuals but
incomplete in many aspects. Command-line interface is not very flexible and not
all parameters in the code are processed. Handling of anonymous nodes is
minimal, so there may be missing elements in the output; "null" labels or
descriptions in the output may also occur.

- The code structure is very preliminary but intended to facilitate various
possible implementations of a generic IDotGenerator interface. Only a Jena-based
implementation is included at the moment. A next version will probably include
an implementation based on the OWL API - http://owlapi.sf.net/.

Enjoy.