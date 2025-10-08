import sys
from pathlib import Path
import zipfile
import pickle

from bissap import read_elements
from algo import AnnotMatrix
from annot import fetch_BP_annotations

from goatools.obo_parser import GODag

from icecream import ic

GAF_FILE = "files\\goa_human.gaf"
OBO_FILE = "files\\go-basic.obo"
MATRIX_FILE = "whole_annotation_genome.zip"
GAF_PICKLE = "gaf.pkl"

# USAGE : compute_scores_from_external.py elements_of_interest annotation
print("Loading Gene Ontology data...")
godag = GODag(OBO_FILE)
eoi_file = sys.argv[0]
annot_file = sys.argv[1]
eoi_set = read_elements(eoi_file)

print("\nPreparing global annotation matrix...")
annot_matrix = None
if Path.exists(Path(MATRIX_FILE)):
    if zipfile.is_zipfile(MATRIX_FILE):
        print("Zip file found, loading the matrix")
        zf = zipfile.ZipFile(MATRIX_FILE, 'r')
        annot_matrix = pickle.loads(zf.open('wag.matrix').read())
    else:
        print(MATRIX_FILE+" is not a zip file")
        with open(MATRIX_FILE, 'rb') as fin:
            annot_matrix = pickle.load(MATRIX_FILE)
else:
    # Calculate the matrix
    print("Calculating the whole genome annotation matrix")
    annot_matrix = AnnotMatrix(GAF_FILE)
    AnnotMatrix.dump_to_file(annot_matrix, MATRIX_FILE)
    print(f"Annotation matrix: {annot_matrix.elements_number} elements x {annot_matrix.go_number} GO terms")

fetch_BP_annotations(eoi_set, GAF_FILE, godag, GAF_PICKLE)

# Filter annotations to keep the given ones
print(len(godag))
print(godag[0])
# for e in eoi_set:
#     print(f"{e.name}: {len(e.go_terms)} BP GO terms")

# for e in eoi_set:
#     for go_id in e.go_terms:
#         if go_id not in candidates_dict:
#             term = GOTerm(go_id, godag)
#             candidates_dict[go_id] = term
#         candidates_dict[go_id].elements.add(e)

# for c in candidates:
#     c.IC = compute_IC(c, wag)