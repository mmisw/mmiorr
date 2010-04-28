MMI Ont2Dot
Carlos Rueda - MMI/MBARI

Ont2Dot generates a diagram for an OWL ontology. This diagram is actually
encoded in a format that Graphviz (http://graphviz.org/) tools can process to
generate the final output format.

Example usage:  (command-line interface is not very flexible at the moment)

$ java -jar ont2dot.jar http://mmisw.org/ont/mmi/device > device.dot
$ dot -Tpng device.dot > device.png

NOTE: The implementation is functional but incomplete. I'll be doing
improvements as time permits. The code structure is intended to facilitate
various possible implementations of a very generic IDotGenerator interface. Only
a Jena-based implementation is included at the moment. A next version will
probably include an implementation based on the OWL API - http://owlapi.sf.net/.

Enjoy.