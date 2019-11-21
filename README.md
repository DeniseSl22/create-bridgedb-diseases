Create BridgeDb Identity Mapping files
======================================

This groovy script creates a Derby file for BridgeDb [1,2] for use in PathVisio,
etc.

The script will be tested with Wikidata [3,4] from November 2019, and is based on the [create_bridgedb_metabolites repository](https://github.com/bridgedb/create-bridgedb-metabolites)

We're greatfull for all that worked on identifier mappings in this/these project(s):

- http://wikidata.org/

Everyone can contribute ID mappings to Wikidata.

![](https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Wikidata_stamp.png/288px-Wikidata_stamp.png)

Releases
--------

The files are released via the BridgeDb Website: #Fix link: http://www.bridgedb.org/mapping-databases/hmdb-metabolite-mappings/

The mapping files are also archived on Figshare: #Fix link: https://figshare.com/search?q=metabolite+bridgedb+mapping+database&quick=1

License
-------

This repository: New BSD.

Derby License -> http://db.apache.org/derby/license.html
BridgeDb License -> http://www.bridgedb.org/browser/trunk/LICENSE-2.0.txt

Run the script and test the results
-----------------------------------

1. add the jars to your classpath, e.g. on Linux with:

  export CLASSPATH=\`ls -1 *.jar | tr '\n' ':'\`

2. make sure the Wikidata files are saved

2.1 ID mappings

A set of SPARQL queries have been compiled and saved in the wikidata/ folder.
These queries can be manually executed at http://query.wikidata.org/. These
queries download mappings from Wikidata for OMIM (omim.rq),
Disease Ontology (do.rq), UMLS CUI (cui.rq), Orphanet (orpha.rq),
MeSH descriptor IDs (mesh.rq)-> coming soon.

However, you can also use the below curl command line operations.

  ```
  curl -H "Accept: text/csv" --data-urlencode query@wikidata/omim.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o omim2wikidata.csv
  curl -H "Accept: text/csv" --data-urlencode query@wikidata/do.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o do2wikidata.csv
  curl -H "Accept: text/csv" --data-urlencode query@wikidata/cui.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o cui2wikidata.csv
  curl -H "Accept: text/csv" --data-urlencode query@wikidata/orpha.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o orpha2wikidata.csv
  curl -H "Accept: text/csv" --data-urlencode query@wikidata/mesh.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o mesh2wikidata.csv  
  curl -H "Accept: text/csv" --data-urlencode query@wikidata/icd9.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o icd92wikidata.csv
  curl -H "Accept: text/csv" --data-urlencode query@wikidata/icd10.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o icd102wikidata.csv
  curl -H "Accept: text/csv" --data-urlencode query@wikidata/icd11.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o icd112wikidata.csv
  
  ```

4.2 Get Disease Labels

With a similar SPARQL query (names.rq) the disease labels (English only) can be downloaded as simple TSV and saved as "names2wikidata.tsv"
(note that this file is TAB separated):

  ```
  curl -H "Accept: text/tab-separated-values" --data-urlencode query@wikidata/names.rq -G https://query.wikidata.org/bigdata/namespace/wdq/sparql -o names2wikidata.tsv
  ```

5. Update the [createDerby.groovy file](https://github.com/bridgedb/create-bridgedb-hmdb/blob/master/createDerby.groovy#L61) with the new version numbers ("DATASOURCEVERSION" field) and run the script with Groovy: #Update line

  ```
  export CLASSPATH=`ls -1 *.jar | tr '\n' ':'`
  groovy createDerby.groovy
  ```

6. Test the resulting Derby file by opening it in PathVisio

7. Use the BridgeDb QC tool to compare it with the previous mapping file

The BridgeDb repository has a tool to perform quality control (qc) on ID
mapping files:

  ```
  sh qc.sh old.bridge new.bridge
  ```

8. Upload the data to Figshare and update the following pages:

* http://www.bridgedb.org/mapping-databases/hmdb-metabolite-mappings/ #Update link
* http://bridgedb.org/data/gene_database/ #Update link

9. Tag this repository with the DOI of the latest release.

To ensure we know exactly which repository version was used to generate
a specific release, the latest commit used for that release is tagged
with the DOI on Figshare. To list all current tags:

  ```
  git tag
  ```

To make a new tag, run:

  ```
  git tag $DOR
  ````

where $DOI is replaced with the DOI of the release.

10. Inform downstream projects

At least the following projects need to be informed about the availability of the new mapping database:

* BridgeDb webservice
* WikiPathways RDF generation team (Jenkins server)
* WikiPathways indexer (supporting the WikiPathways web service)

References
----------

1. http://bridgedb.org/
2. Van Iersel, M. P., Pico, A. R., Kelder, T., Gao, J., Ho, I., Hanspers, K., Conklin, B. R., Evelo, C. T., Jan. 2010. The BridgeDb framework: standardized access to gene, protein and metabolite identifier mapping services. BMC bioinformatics 11 (1), 5+. http://dx.doi.org/10.1186/1471-2105-11-5
3. Vrandečić, Denny. "Wikidata: a new platform for collaborative data collection." Proceedings of the 21st International Conference on World Wide Web. ACM, 2012. https://doi.org/10.1145/2187980.2188242
4. Mietchen D, Hagedorn G, Willighagen E, Rico M, Gómez-Pérez A, Aibar E, Rafes K, Germain C, Dunning A, Pintscher L, Kinzler D (2015) Enabling Open Science: Wikidata for Research (Wiki4R). Research Ideas and Outcomes 1: e7573. https://doi.org/10.3897/rio.1.e7573
