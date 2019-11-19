// export CLASSPATH=`ls -1 *.jar | tr '\n' ':'`

import java.text.SimpleDateFormat;
import java.util.Date;
import groovy.util.slurpersupport.NodeChildren;

import org.bridgedb.IDMapperException;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.rdb.construct.DBConnector;
import org.bridgedb.rdb.construct.DataDerby;
import org.bridgedb.rdb.construct.GdbConstruct;
import org.bridgedb.rdb.construct.GdbConstructImpl3;

commitInterval = 500
genesDone = new java.util.HashSet();
linksDone = new java.util.HashSet();

unitReport = new File("creation.xml")
// unitReport << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
unitReport << "<testsuite tests=\"12\">\n"

GdbConstruct database = GdbConstructImpl3.createInstance(
  "wikidata_diseases", new DataDerby(), DBConnector.PROP_RECREATE
);
database.createGdbTables();
database.preInsert();

blacklist = new HashSet<String>();
//blacklist.add("C00350") //Example of blacklist item.

////Registering Datasources to create mappings
omimDS = BioDataSource.OMIM //SysCode: Om
//doDS = BioDataSource.DISEASE_ONTOLOGY //Not part of BridgeDb yet!!
//cuiDS = BioDataSource.UMLS_CUI //Not part of BridgeDb yet!!
//orphaDS = BioDataSource.ORPHANET //Not part of BridgeDb yet!!
//meshDS = BioDataSource.MESHID //Not part of BridgeDb yet!!

//chemblDS = DataSource.register ("Cl", "ChEMBL compound").asDataSource() //example registry when DataSource is not supported in BridgeDb yet.

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

//def cleanKey(String inchikey) {
//   String cleanKey = inchikey.trim()
//   if (cleanKey.startsWith("InChIKey=")) cleanKey = cleanKey.substring(9)
//   cleanKey
//} //Shouldn't be needed, since we're working with diseases....


//// load the Wikidata content

// CAS registry numbers
counter = 0
error = 0
new File("cas2wikidata.csv").eachLine { line,number ->
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
  addXRef(database, ref, fields[1], casDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (CAS)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n"

// PubChem registry numbers
counter = 0
error = 0
new File("pubchem2wikidata.csv").eachLine { line,number ->
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
  addXRef(database, ref, fields[1], pubchemDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (PubChem)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"PubChemFound\"/>\n"

// KEGG registry numbers
counter = 0
error = 0
new File("kegg2wikidata.csv").eachLine { line,number ->
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
  keggID = fields[1]
  if (keggID.charAt(0) == 'C') {
    addXRef(database, ref, keggID, keggDS, genesDone, linksDone);
  } else if (keggID.charAt(0) == 'D') {
    addXRef(database, ref, keggID, keggDrugDS, genesDone, linksDone);
  } else {
    println "unclear KEGG ID ($rootid): " + keggID
  }
  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (KEGG)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"KEGGFound\"/>\n"

// ChemSpider registry numbers
counter = 0
error = 0
new File("cs2wikidata.csv").eachLine { line,number ->
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
  addXRef(database, ref, fields[1], chemspiderDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (ChemSpider)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"ChemSpiderFound\"/>\n"

//// IUPHAR registry numbers
//counter = 0
//error = 0
//new File("gpl2wikidata.csv").eachLine { line,number ->
//  if (number == 1) return // skip the first line
//
//  fields = line.split(",")
//  rootid = fields[0].substring(31)
//  Xref ref = new Xref(rootid, wikidataDS);
// if (!genesDone.contains(ref.toString())) {
//    addError = database.addGene(ref);
//    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
//    error += addError
//    linkError = database.addLink(ref,ref);
//    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
//    error += linkError
//   genesDone.add(ref.toString())
//  }
//
//  // add external identifiers
//  addXRef(database, ref, fields[1], iupharDS, genesDone, linksDone);
//
//  counter++
//  if (counter % commitInterval == 0) {
//    println "Info: errors: " + error + " (IUPHAR)"
//    database.commit()
//  }
//}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"IUPHARFound\"/>\n"

//// ChEMBL Compound registry numbers
//counter = 0
//error = 0
//new File("chembl2wikidata.csv").eachLine { line,number ->
//  if (number == 1) return // skip the first line
//
//  fields = line.split(",")
//  rootid = fields[0].substring(31)
//  Xref ref = new Xref(rootid, wikidataDS);
// if (!genesDone.contains(ref.toString())) {
//    addError = database.addGene(ref);
//    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
//    error += addError
//    linkError = database.addLink(ref,ref);
//    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
//    error += linkError
//   genesDone.add(ref.toString())
//  }
//
//  // add external identifiers
//  addXRef(database, ref, fields[1], chemblDS, genesDone, linksDone);
//
//  counter++
//  if (counter % commitInterval == 0) {
//    println "Info: errors: " + error + " (ChEMBL)"
//    database.commit()
//  }
//}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CHEMBLFound\"/>\n"

//// Drugbank Compound registry numbers
//counter = 0
//error = 0
//new File("drugbank2wikidata.csv").eachLine { line,number ->
//  if (number == 1) return // skip the first line
//
//  fields = line.split(",")
//  rootid = fields[0].substring(31)
//  Xref ref = new Xref(rootid, wikidataDS);
// if (!genesDone.contains(ref.toString())) {
//    addError = database.addGene(ref);
//    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
//    error += addError
//    linkError = database.addLink(ref,ref);
//    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
//    error += linkError
//   genesDone.add(ref.toString())
//  }
//
//  // add external identifiers
//  addXRef(database, ref, fields[1], drugbankDS, genesDone, linksDone);
//
//  counter++
//  if (counter % commitInterval == 0) {
//    println "Info: errors: " + error + " (Drugbank)"
//    database.commit()
//  }
//}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"DrugbankFound\"/>\n"


// LIPID MAPS registry numbers
counter = 0
error = 0
new File("lm2wikidata.csv").eachLine { line,number ->
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
  addXRef(database, ref, fields[1], lmDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (LIPIDMAPS)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"LipidMapsFound\"/>\n"

// HMDB registry numbers
counter = 0
error = 0
new File("hmdb2wikidata.csv").eachLine { line,number ->
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
  hmdbid = fields[1]
  if (hmdbid.length() == 11) {
    hmdbid = "HMDB" + hmdbid.substring(6) // use the pre-16 August 2017 identifier pattern
  }
  addXRef(database, ref, hmdbid, BioDataSource.HMDB, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (HMDB)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"HMDBFound\"/>\n"

// EPA CompTox Dashboard numbers
counter = 0
error = 0
new File("comptox2wikidata.csv").eachLine { line,number ->
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
  addXRef(database, ref, fields[1], dtxDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (EPA CompTox Dashboard)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"EPACompToxFound\"/>\n"

// ChEBI registry numbers
counter = 0
error = 0
new File("chebi2wikidata.csv").eachLine { line,number ->
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
  shortid = fields[1]
  chebiid = "CHEBI:" + shortid
  Xref chebiRef = new Xref(rootid, BioDataSource.CHEBI);
  addXRef(database, ref, shortid, BioDataSource.CHEBI, genesDone, linksDone);
  addXRef(database, ref, chebiid, BioDataSource.CHEBI, genesDone, linksDone);
  addXRef(database, chebiRef, rootid, wikidataDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (ChEBI)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"ChEBIFound\"/>\n"

// KNApSAcK registry numbers
counter = 0
error = 0
new File("knapsack2wikidata.csv").eachLine { line,number ->
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
  addXRef(database, ref, fields[1], knapsackDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (ChEBI)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"KNApSAcKFound\"/>\n"

// Wikidata names
counter = 0
error = 0
new File("names2wikidata.tsv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split("\t")
  if (fields.length >= 3) {
    rootid = fields[0].replace("<","").replace(">","").substring(31)
    key = fields[1].trim()
    synonym = fields[2].trim().replace("\"","").replace("@en","")
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
      addXRef(database, ref, key, inchikeyDS, genesDone, linksDone);
    } else {
      println "Not adding synonym: " + synonym
    }
    if (key.length() > 0) {
      addAttribute(database, ref, "InChIKey", key);
    }
  }
  counter++
  if (counter % commitInterval == 0) {
    println "errors: " + error + " (label)"
    database.commit()
  }
}
unitReport << "  <testcase classname=\"WikidataCreation\" name=\"NamesFound\"/>\n"
unitReport << "</testsuite>\n"

database.commit();
database.finalize();
