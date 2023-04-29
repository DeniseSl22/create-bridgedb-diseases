@Grab(group='org.bridgedb', module='org.bridgedb.bio', version='3.0.22-SNAPSHOT')
@Grab(group='org.bridgedb', module='org.bridgedb.rdb.construct', version='3.0.22-SNAPSHOT')

import java.text.SimpleDateFormat;
import java.util.Date;
import groovy.xml.XmlSlurper;
import groovy.xml.slurpersupport.NodeChildren;

import org.bridgedb.IDMapperException;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.bridgedb.rdb.construct.DBConnector;
import org.bridgedb.rdb.construct.DataDerby;
import org.bridgedb.rdb.construct.GdbConstruct;
import org.bridgedb.rdb.construct.GdbConstructImpl3;

commitInterval = 500
genesDone = new java.util.HashSet();
linksDone = new java.util.HashSet();

//unitReport = new File("creationNEW.xml")
// unitReport << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//unitReport << "<testsuite tests=\"12\">\n"

GdbConstruct database = GdbConstructImpl3.createInstance(
  "wikidata_diseases", new DataDerby(), DBConnector.PROP_RECREATE
);
database.createGdbTables();
database.preInsert();

blacklist = new HashSet<String>();
//blacklist.add("C00350") //Example of blacklist item.

// Registering Datasources to create mappings
DataSourceTxt.init()
wikidataDS = DataSource.getExistingBySystemCode("Wd")
omimDS = DataSource.getExistingBySystemCode("Om")
doDS = DataSource.getExistingBySystemCode("Do")
cuiDS = DataSource.getExistingBySystemCode("Cu")
orphaDS = DataSource.getExistingBySystemCode("On")
meshDS = DataSource.getExistingBySystemCode("Me")
icd9DS = DataSource.getExistingBySystemCode("ICD9")
icd10DS = DataSource.getExistingBySystemCode("ICD10")
icd11DS = DataSource.getExistingBySystemCode("ICD11")
mondoDS = DataSource.register ("Mo", "MONDO").asDataSource()

String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
database.setInfo("BUILDDATE", dateStr);
database.setInfo("DATASOURCENAME", "WIKIDATA");
database.setInfo("DATASOURCEVERSION", "WIKIDATA" + dateStr);
database.setInfo("DATATYPE", "Disease"); //Not sure if this causes trouble later on...
database.setInfo("SERIES", "standard_diseases"); //Not sure if this causes trouble later on...

def addXRef(GdbConstruct database, Xref ref, String node, DataSource source, Set genesDone, Set linkesDone) {
   id = node.trim()
   if (id.length() > 0) {
     // println "id($source): $id"
     ref2 = new Xref(id, source);
     if (!genesDone.contains(ref2.toString())) {
       if (database.addGene(ref2) != 0) {
          println "Error (addXRef.addGene): " + database.recentException().getMessage()
          println "                 id($source): $id"
       }
       genesDone.add(ref2.toString())
     }
     if (!linksDone.contains(ref.toString()+ref2.toString())) {
       if (database.addLink(ref, ref2) != 0) {
         println "Error (addXRef.addLink): " + database.recentException().getMessage()
         println "                 id(origin):  " + ref.toString()
         println "                 id($source): $id"
       }
       linksDone.add(ref.toString()+ref2.toString())
     }
   }
}

def addAttribute(GdbConstruct database, Xref ref, String key, String value) {
   id = value.trim()
   // println "attrib($key): $id"
   if (id.length() > 255) {
     println "Warn: attribute does not fit the Derby SQL schema: $id"
   } else if (id.length() > 0) {
     if (database.addAttribute(ref, key, value) != 0) {
       println "Error (addAttrib): " + database.getException().getMessage()
     }
   }
}

//// load the Wikidata content

// OMIM IDs
counter = 0
error = 0
new File("omim2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], omimDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (OMIM)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


// Disease Ontology IDs
counter = 0
error = 0
new File("do2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], doDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (DiseaseOntology)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


// UMLS CUI IDs
counter = 0
error = 0
new File("cui2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], cuiDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (UMLSCUI)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.



// Orphanet IDs
counter = 0
error = 0
new File("orpha2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], orphaDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (OrphaNet)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


// MeSH Descriptor IDs
counter = 0
error = 0
new File("mesh2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], meshDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (MeSH)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.

//icd9DS 
counter = 0
error = 0
new File("icd92wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], icd9DS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (ICD-9)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


//icd10DS 
counter = 0
error = 0
new File("icd102wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], icd10DS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (ICD-10)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.

//icd11DS 
counter = 0
error = 0
new File("icd112wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], icd11DS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (ICD-11)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


//mondoDS 
counter = 0
error = 0
new File("icd112wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], mondoDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (Mondo)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.





// Wikidata names
counter = 0
error = 0
new File("names2wikidata.tsv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split("\t")
  if (fields.length >= 2) {
    rootid = fields[0].replace("<","").replace(">","").substring(31)
    //key = fields[1].trim()
    synonym = fields[1].trim().replace("\"","").replace("@en","")
    Xref ref = new Xref(rootid, wikidataDS);
    if (!genesDone.contains(ref.toString())) {
      addError = database.addGene(ref);
      if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
     error += addError
      linkError = database.addLink(ref,ref);
      if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
      error += linkError
      genesDone.add(ref.toString())
    }
    if (synonym.length() > 0 && !synonym.equals(rootid)) {
      println "Adding synonym: " + synonym
      addAttribute(database, ref, "Symbol", synonym)
      //addXRef(database, ref, key, inchikeyDS, genesDone, linksDone);
    } else {
      println "Not adding synonym: " + synonym
    }
    //if (key.length() > 0) {
    //  addAttribute(database, ref, "InChIKey", key);
    //}
  }
  counter++
  if (counter % commitInterval == 0) {
    println "errors: " + error + " (label)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"NamesFound\"/>\n"
//unitReport << "</testsuite>\n"

database.commit();
database.finalize();
