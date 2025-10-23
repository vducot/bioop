# BISSAP – Projet Annotations

**Authors:** Vincent Ducot & Gwendoline Iborra

BISSAP is a Python tool that summarizes Gene Ontology (GO) Biological Process annotations for a set of biological elements (genes or proteins). It selects the most informative GO terms based on coverage and specificity (IC or entropy), while removing redundant terms.

**Installation:**

```bash
git clone https://github.com/vducot/bissap.git
cd bissap
pip install -r requirements.txt
```

**Required files:**

* Dataset file: one element per line
* GO ontology (`.obo`)
* GO annotations (`.gaf`)
* GO BP label file

**Usage:**

```bash
python bissap.py <dataset_file> --obo_file <go.obo> --gaf_file <goa.gaf> [--threshold 0.05] [--verbose] [--score IC or H]
```

**Limitations:**

* The full genome/proteome annotation matrix is generated on first run. To change it after, you have to delete the file 'whole_annotation_genome.dump' and run the program with another GAF file.
* Tested mainly on protein datasets, the results may vary with genes or other GAF/OBO sources.