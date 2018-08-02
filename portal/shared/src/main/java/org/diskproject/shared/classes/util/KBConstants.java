package org.diskproject.shared.classes.util;

public class KBConstants {
  private static String diskuri = "https://knowledgecaptureanddiscovery.github.io/DISK-Ontologies/disk/1.1.0/ontology.ttl";
  private static String neurouri = "https://knowledgecaptureanddiscovery.github.io/DISK-Ontologies/neuro/release/1.0.0/ontology.ttl";
  private static String omicsuri = "https://knowledgecaptureanddiscovery.github.io/DISK-Ontologies/omics/release/0.0.0/ontology.owl";
  private static String hypuri = "https://knowledgecaptureanddiscovery.github.io/DISK-Ontologies/hypothesis/0.0.1/ontology.owl";

  private static String owlns = "http://www.w3.org/2002/07/owl#";
  private static String rdfns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private static String rdfsns = "http://www.w3.org/2000/01/rdf-schema#";
  private static String xsdns = "http://www.w3.org/2001/XMLSchema#";

  private static String dctermsns = "http://purl.org/dc/terms/";
  private static String dcns = "http://purl.org/dc/elements/1.1/";

  public static String DISKURI() {
    return diskuri;
  }
  
  public static String DISKNS() {
    return "http://disk-project.org/ontology/disk#";
  }
  
  public static String OMICSURI() {
    return omicsuri;
  }

  public static String OMICSNS() {
    return "http://disk-project.org/ontology/omics#";
  }

  public static String NEUROURI() {
    return neurouri;
  }

  public static String NEURONS() {
    return "https://w3id.org/disk/ontology/neuro#";
  }

  public static String HYPURI() {
    return hypuri;
  }

  public static String HYPNS() {
    return "http://disk-project.org/ontology/hypothesis#";
  }
 
  public static String DCTERMSNS() {
    return dctermsns;
  }

  public static String DCNS() {
    return dcns;
  }

  public static String OWLNS() {
    return owlns;
  }

  public static String RDFNS() {
    return rdfns;
  }

  public static String RDFSNS() {
    return rdfsns;
  }
  
  public static String XSDNS() {
    return xsdns;
  }

}
