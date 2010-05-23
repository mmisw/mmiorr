package org.mmisw.ont2dot.impl.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.ont2dot.BaseDotGenerator;

import com.hp.hpl.jena.ontology.DataRange;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Jena-based implementation. 
 * 
 * @author Carlos Rueda
 */
public class DotGeneratorJenaImpl extends BaseDotGenerator {
	
	/** Representation to use when a property has not explicitly given range */
	private static final String ANY = "Any";

	private OntModel _ontModel;
	
	private JenaInfo _info;
	
	
	private final Set<Resource> _generatedClasses = new HashSet<Resource>();
	private final Set<Resource> _generatedInstances = new HashSet<Resource>();
	
	
	
	/**
	 * All the separated classes, set up by {@link #_setSeparatedClasses()}.
	 */
	private final Set<Resource> _separatedClasses = new HashSet<Resource>();


	public DotGeneratorJenaImpl() {
		super();
	}
	
	public void loadModel(String ontUri) {
		super.loadModel(ontUri);
		this._ontModel = ModelLoader.loadModel(_ontUri, includeImports);
		_info = new JenaInfo(_ontModel);
		_info.setUseLabel(useLabel);
	}


	private void _verifyState() {
		if ( _info == null ) {
			throw new IllegalStateException();
		}
	}

	@Override
	protected void generateContents() {
		_verifyState();
		_setSeparatedClasses();
		_outCommonAttributes();
		_outAll();
	}
	
	private void _setSeparatedClasses() {
		_separatedClasses.clear();
		for ( String classUri : _separatedRootClassesUris ) {
			Resource clazz = _info.getClass(classUri);
			if ( clazz != null ) {
				_separatedClasses.add(clazz);
				_addSeparatedChildren(clazz);
			}
			else {
				// not in the ontology -- ignore.
				System.err.println(" WARNING: " +classUri+ ": class not found");
				continue;
			}
		}
	}

	/** recursive routine to add all descendents */
	private void _addSeparatedChildren(Resource superClazz) {
		for ( Resource clazz : _info.getSubClasses(superClazz) ) {
			// check it's not already contained to properly handle possible cycles:
			if ( ! _separatedClasses.contains(clazz) ) {
				_separatedClasses.add(clazz);
				_addSeparatedChildren(clazz);
			}
		}
	}

	private void _outCommonAttributes() {
		pw.println("  node [ fontname=\"helvetica\", fontsize=14, ];");
		pw.println("  edge [ fontname=\"helvetica\", fontsize=10, ];");
	}
	
	private void _outAll() {
		
		StringBuffer dataRangeEdges = null;//new StringBuffer();
	
		_outDataRanges(dataRangeEdges == null);

		_outClasses(dataRangeEdges);
		if ( dataRangeEdges != null ) {
			_outEdgesToDataRanges(dataRangeEdges);
		}
		
		_outSubClassDeclarations();
		
		_outObjectProperties();

		boolean includeTypesInLabel = false;
		boolean withDataTypeProps = true;
		_outInstances(includeTypesInLabel, withDataTypeProps);
		if( ! includeTypesInLabel ) {
			_outInstantiations();
		}
		
		_outOtherStatements();
	
	}
	
	
	private void _outDataRanges(boolean includeName) {
		
		Map<String, DataRange> dataRanges = _info.getDataRanges();
		if ( dataRanges.isEmpty() ) {
			return;
		}
		
		pw.println("\n" +
				" //////////\n" +
				" // data ranges");

		_outNodeDataRangeStyle(includeName);
		
		
		for ( String id : dataRanges.keySet() ) {
			DataRange dataRange = dataRanges.get(id);
			String name = _info.getDataRangeName(id);
			
			StringBuffer label = new StringBuffer("{");
			String separator = "";
			if ( includeName ) {
				label.append(name);
				separator = "|";
			}
			List<String> list = _getDataRangeElementList(dataRange);
			// sort and left-justify the elements:
			Collections.sort(list);
			for ( String elem : list ) {
				label.append(separator +elem+ "\\l");
				separator = "|";
			}
			label.append("}");
				
			pw.println("  \"" +id+ "\"  [ label=\"" +label+ "\" ]");
		}
	}
	
	private void _outNodeDataRangeStyle(boolean includingName) {
		pw.println("  node [ shape=record, fillcolor=burlywood1, style=filled, " +
				"fontsize=" +(includingName ? 10 : 8)+ ", ]; ");
	}


			
	private void _outClasses(StringBuffer dataRangeEdges) {
		
		pw.println("\n" +
				" //////////\n" +
				" // classes");

		_outNodeClassStyles();
		
		for ( Resource clazz : _info.getClazzes() ) {
			String name = clazz.getURI();
			String label = _info.getLabel(clazz);
			
			
			//
			String shapeRecord = "";
			String suffix = _getDataTypePropsLabelSuffixForClass(clazz, dataRangeEdges);
			if ( suffix != null ) {
				label = "{" +label + suffix+ "}";
				shapeRecord = "shape=record,";
			}
		
			pw.println("  \"" +name+ "\"   [ " +shapeRecord+ " label=\"" +label+ "\" ]");
			
			_generatedClasses.add(clazz);
		}
		
	}
	
	private String _getDataTypePropsLabelSuffixForClass(Resource clazz, StringBuffer dataRangeEdges) {

		Collection<Resource> props = _info.getProperties(clazz);
		if ( props == null ) {
			return null;
		}
		
		// collect the fields (each field: {prdLabel|objLabel}) in this list;
		// then sort it; then put everything in a string: 
		List<String> fields = new ArrayList<String>();
		
		for ( final Resource prop : props ) {
			String prdLabel = _info.getLabel(prop);
			
			for ( Resource range : _info.getRanges(prop) ) {

				if ( XSD.getURI().equals(range.getNameSpace()) ) {
					// datatype property
					String objLabel = _info.getLabel(range);

					fields.add("{" +prdLabel+ "|" +objLabel+ "}");
				}

				else if ( _separatedClasses.contains(range) ) {
					// tree rooted at range should be separated. 
					// So, only generate the connection domain -> range if the domain 
					// is contained in the tree rooted at range:
					if ( _info.presentInTree(clazz, range) ) {
						// connection is generated elsewhere.
					}
					else {
						String objLabel = _info.getLabel(range);
						fields.add("{" +prdLabel+ "|" +objLabel+ "}");
					}
				}

				else if ( range.equals(RDFS.Resource) ) {
					fields.add("{" +prdLabel+ "|" +ANY+ "}");
				}

				else if (range.isAnon() ) {
					String id = range.getId().getLabelString();

					if ( dataRangeEdges == null ) {
						String objLabel = "?";
						String name = _info.getDataRangeName(id);
						if ( name != null ) {
							objLabel = "[" +name+ "]";
						}

						fields.add("{" +prdLabel+ "|" +objLabel+ "}");
					}
					else {
						dataRangeEdges.append("  \"" +clazz.getURI()+ "\"  ->  \"" +id+ "\"  [ label=\"" +prdLabel+ "\" ]; \n");
					}
				}
			}
		}
		if ( fields.size() > 0 ) {
			Collections.sort(fields);
			StringBuffer sb = new StringBuffer();
			for ( String field : fields ) {
				sb.append("|" + field);
			}
			return sb.toString();
		}
		else {
			return null;
		}
	}

	
	private void _outEdgesToDataRanges(StringBuffer dataRangeEdges) {
		pw.println("\n" +
				" ////////////////////\n" +
				" // edges to data ranges");

		_outEdgeToDataRangeStyle();
		
		pw.println(dataRangeEdges);
	}
	
	private void _outEdgeToDataRangeStyle() {
		pw.println("  edge [ dir=back, color=darkgreen, fontcolor=darkgreen, fontsize=8, arrowhead=vee, arrowtail=none, arrowsize=0.8]; "
		);
	}

	
	private List<String> _getDataRangeElementList(DataRange dataRange) {
		List<String> list = new ArrayList<String>();
		if ( dataRange != null ) {
			RDFList rdfList = dataRange.getOneOf();
			while ( rdfList != null && ! rdfList.isEmpty() ) {
				RDFNode elem = rdfList.getHead();
				Literal obj = (Literal) elem;
				String name = obj.getLexicalForm();
				list.add(name);
				rdfList = rdfList.getTail();
			}
		}
		return list;
	}
	

	private void _outSubClassDeclarations() {
		
		pw.println("\n" +
				" ////////////////////////\n" +
				" // subclass declarations");

		_outSubclassStyle();
		
		for ( Resource clazz : _info.getClazzes() ) {
			for ( Resource superClazz : _info.getSuperClasses(clazz) ) {
				String superClazzName = superClazz.getURI();
			
				String subClazzName = clazz.getURI();
				
				if ( _generatedClasses.contains(superClazz) && _generatedClasses.contains(clazz) ) {
					pw.println("  \"" +superClazzName+ "\"  ->  \"" + subClazzName+ "\""  );
				}
			}
		}
		
	}



	private void _outInstances(boolean includeTypesInLabel, boolean withDataTypeProps) {
		
		pw.println("\n" +
				" /////////////\n" +
				" // instances");

		_outNodeInstanceStyle();

		for ( Resource instance : _info.getInstances() ) {
			Set<Resource> types = _info.getTypes(instance);
			if ( types != null ) {
				String instName = instance.getURI();
				String label = instance.getLocalName();
				
				if ( includeTypesInLabel ) {
					label += ": ";
					String comma = "";
					for ( Resource type : types ) {
						String clazzLabel = _info.getLabel(type);
						label += comma + clazzLabel;
						comma = ", ";
					}
				}
				
				String shapeRecord = "";
				
				if ( withDataTypeProps ) {
					String suffix = _getDataTypePropsLabelSuffixForInstance(instance);
					if ( suffix != null ) {
						label = "{" +label + suffix+ "}";
						shapeRecord = "shape=record,";
					}
				}
				
				pw.println("  \"" +instName+ "\"   [ " +shapeRecord+ " label=\"" +label+ "\" ]");
				
				_generatedInstances.add(instance);
			}
		}
	}

	
	private String _getDataTypePropsLabelSuffixForInstance(Resource instance) {
		// collect the fields (each field: {prdLabel|objLabel}) in this list;
		// then sort it; then put everything in a string: 
		List<String> fields = new ArrayList<String>();

		Set<Statement> stmts = _info.getDataTypePropertyInstantiations(instance);
		if ( stmts != null ) {
			for (Statement stmt : stmts ) {
				Property prd = stmt.getPredicate();
				Literal obj = (Literal) stmt.getObject();
				
				String prdLabel = _info.getLabel(prd);
				String objLabel = obj.getString();
				
				fields.add("{" +prdLabel+ "|" +objLabel+ "}");
			}
		}
		
		if ( fields.size() > 0 ) {
			Collections.sort(fields);
			StringBuffer sb = new StringBuffer();
			for ( String field : fields ) {
				sb.append("|" + field);
			}
			return sb.toString();
		}
		else {
			return null;
		}

	}
	
	
	private void _outInstantiations() {
		
		pw.println("\n" +
				" //////////////////\n" +
				" // instanciations");

		_outEdgeInstantiationStyle();
		
		
		for ( Resource instance : _info.getInstances() ) {
			String instName = instance.getURI();
			Set<Resource> types = _info.getTypes(instance);
			if ( types != null ) {
				
				for ( Resource type : types ) {
					
					if ( _generatedClasses.contains(type) ) {
						String clazzName = type.getURI();
						pw.println("  \"" +clazzName+ "\"  ->  \"" + instName+ "\" ");
					}
				}
			}
		}
	}

	
	private void _outObjectProperties() {
		
		pw.println("\n" +
				" ////////////////////\n" +
				" // properties");

		_outEdgeOtherRelationStyle();
		
		for ( Resource prop : _info.getAllProperties() ) {
			
			for ( Resource domain: _info.getDomains(prop) ) {
				
				for ( Resource range: _info.getRanges(prop) ) {
					
					if ( _separatedClasses.contains(range) ) {
						// tree rooted at range should be separated. 
						// So, only generate the connection domain -> range if the domain 
						// is contained in the tree rooted at range:
						if ( _info.presentInTree(domain, range) ) {
							// OK, generate the connection.
						}
						else {
							// skip the connection for this property:
							continue;
						}
					}
					
					String domainName = domain.getURI();
					String rangeName = range.getURI();
					
					if ( _generatedClasses.contains(domain) && _generatedClasses.contains(range) ) {

						String label = _info.getLabel(prop);;
						pw.println("  \"" +domainName+ "\"  ->  \"" +rangeName+ "\"  [ label=\"" +label+ "\" ]");
					}
				}
			}
		}
		
	}

	private void _outOtherStatements() {
		
		pw.println("\n" +
				" ////////////////////\n" +
				" // other statements");

		_outEdgeOtherRelationStyle();
		
		for ( Statement stmt : _info.getStatements() ) {
			
			// TODO for now, ignoring statements about ontology resource
			if ( _info.containsOntology(stmt.getSubject()) ) {
				continue;
			}
			
			Resource sbj = stmt.getSubject();
			String sbjName = sbj.getURI();
			Property prd = stmt.getPredicate();
			RDFNode obj = stmt.getObject();
			String objName = obj.isResource() ? 
					((Resource) obj).getURI() : ((Literal) obj).getString();
					
			if ( obj.isAnon() ) {
				String id = ((Resource) obj).getId().getLabelString();
				objName = id;
				Map<String, Restriction> restrictions = _info.getRestrictions();
				Restriction restr = restrictions.get(id);
				if ( restr != null ) {
					objName = _getRestrictionDescription(restr);
				}
			}
			else if ( objName == null ) {
				objName = "?";
			}
			
			String label = _info.getLabel(prd);
			
			if ( _generatedClasses.contains(sbj) 
			||   _generatedInstances.contains(sbj) ) 
			{
				pw.println("  \"" +sbjName+ "\"  ->  \"" + objName+ "\"  [ label=\"" +label+ "\" ]");
			}
		}
		
	}

	
	/** Gets a string representation of the given restriction.
	 * NOTE: impl very preliminar and incomplete, based on checking possible type of restriction 
	 * using the various restr.isSomething methods. I would expect some appropriate
	 * visitor here to make this "switch"  (note the RDFVisitor is no the one).
	 */
	private String _getRestrictionDescription(Restriction restr) {

		final StringBuffer sb = new StringBuffer();

		if ( restr.isSomeValuesFromRestriction() ) {
			sb.append("some ...");
		}
		else if ( restr.isCardinalityRestriction() ) {
			sb.append("card ...");
		}
		else if ( restr.isAllDifferent() ) {
			sb.append("allDif ...");
		}
		else {
			sb.append("...");
		}
		
		String descrip = "(Restr: " +sb+ ")";
		
		return descrip;
	}

	private void _outEdgeOtherRelationStyle() {
		pw.println("  edge [ dir=back, color=darkgreen, fontcolor=darkgreen, fontsize=10, arrowhead=vee, arrowtail=none, arrowsize=1.0]; "
		);
	}

	
	private void _outEdgeInstantiationStyle() {
		pw.println("  edge [ dir=normal, color=black, fontcolor=black, label=\"a\", fontsize=10, arrowtail=vee, arrowhead=none, arrowsize=1.0];");
	}

	private void _outSubclassStyle() {
		pw.println("  edge   [ dir=back, arrowtail=onormal, arrowhead=none, arrowsize=2.0, ]; ");
	}

}
