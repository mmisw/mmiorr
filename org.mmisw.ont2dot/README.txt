MMI Ont2Dot Ontology-to-Graphviz converter
Carlos Rueda - MMI/MBARI

Ont2Dot generates a diagram for an OWL ontology. This diagram is actually
encoded in a format that Graphviz (http://graphviz.org/) tools can process to
generate the final output format.

Example usage: 

# generate dot (device.dot in this example)
$ java -jar ont2dot.jar http://mmisw.org/ont/mmi/device

  Note: the above jar execution assumes that the dependent jars are in the classpath.
  Using Maven, you can run:
  $ mvn exec:java -Dexec.args="http://mmisw.org/ont/mmi/device"
  Or, you can generate a self-contained jar with:
  $ mvn assembly:assembly

# use Graphviz to generate the desired graphical representation, eg:
$ dot -Tpng device.dot > device.png

NOTE: The implementation is functional for both classes and individuals but
incomplete in many aspects. Command-line interface is not very flexible and not
all parameters in the code are processed. The code structure is intended to
facilitate various possible implementations of a very generic IDotGenerator
interface. Only a Jena-based implementation is included at the moment. A next
version will probably include an implementation based on the OWL API -
http://owlapi.sf.net/.

Enjoy.