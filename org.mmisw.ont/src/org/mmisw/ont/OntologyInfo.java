package org.mmisw.ont;

/**
 * Some general info about an ontology.
 * @author Carlos Rueda
 */
public class OntologyInfo {
	
	private String id;
	private String ontologyId;
	private String filePath;
	
	private String filename;
	
	private String uri;
	
	private String displayLabel;

	private String author;
	private String version;

	private String versionStatus;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}

	public String getOntologyId() {
		return ontologyId;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
		this.version = version;
	}

    public String getVersionStatus() {
        return versionStatus;
    }
    public void setVersionStatus(String versionStatus) {
        this.versionStatus = versionStatus;
    }
}
