###
# Main file
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###
import argparse
import textwrap
import zipfile
import pickle
from annot import Element, GOTerm, fetch_BP_annotations, get_overrepresented_terms
from pathlib import Path
from algo import AnnotMatrix, run_algo
import algo
import os
from goatools.obo_parser import GODag

MATRIX_FILE = 'whole_annotation_genome.zip'
GAF_PICKLE = "gaf.pkl"

def read_elements(filename: str) -> set[Element]:
    """
    Read a file with one element per line
    Args
        filename: path to the file
    Returns
        A list of Element
    Exceptions
        Raises an exception if the file cannot be read
    """
    if not os.path.exists(filename):
        raise FileNotFoundError(f"Elements file not found: {filename}")
    
    elements = set()
    try:
        with open(filename, "r") as f:
            for i, line in enumerate(f, 1):
                name = line.split('\t')[0].strip()
                if name:
                    elements.add(Element(name))
        return elements
    except Exception as e:
        raise IOError(f"Error reading elements file {filename}: {e}")

def main(args):
    print("Loading elements of interest...")
    eoi_set = read_elements(args.dataset)
    print(f"{len(eoi_set)} elements loaded from '{args.dataset}'")
    print("\nLoading Gene Ontology data...")
    godag = GODag(args.obo_file)

    print("\nFetching Biological Process (BP) annotations...")
    fetch_BP_annotations(eoi_set, args.gaf_file, godag, GAF_PICKLE)
    if args.verbose:
        print("Detailed annotation counts per element:")
        for e in eoi_set:
            print(f"{e.name}: {len(e.go_terms)} BP GO terms")
            
    # --- Load or compute the whole annotation matrix ---
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
        annot_matrix = algo.AnnotMatrix(args.gaf_file)
        algo.AnnotMatrix.dump_to_file(annot_matrix, MATRIX_FILE)
        print(f"Annotation matrix: {annot_matrix.elements_number} elements x {annot_matrix.go_number} GO terms")

    # --- Build GOTerm candidates ---
    # Get overrepresented terms
    print("\nIdentifying overrepresented GO terms...")
    overrepresented_terms = get_overrepresented_terms(eoi_set, godag, args.gaf_file, GAF_PICKLE, args.threshold)
    overrepresented_ids = {go.term for go in overrepresented_terms}
    all_candidate_terms = {go_term for elem in eoi_set for go_term in elem.go_terms}
    candidates = {go for go in all_candidate_terms if go in overrepresented_ids}
    # Get the corresponding GOTerm objects
    candidates_objects = {go for go in overrepresented_terms if go.term in candidates}
    print(f"Found {len(candidates)} overrepresented GO terms with threshold {args.threshold}")

    # --- Update GOTerm coverage and linked elements ---
    for go in candidates_objects:
        go.elements.clear()
        for elem in eoi_set:
            if go.term in elem.go_terms:
                go.add_element(elem)
        go.update_coverage(eoi_set)

    if args.verbose:
        print("\nCandidate summary:")
        for go_id in sorted(candidates):
            go_obj = next((x for x in overrepresented_terms if x.term == go_id), None)
            if go_obj:
                print(f"{go_obj.term:<10} | p={go_obj.pvalue:.2e} | cov={go_obj.coverage:.2f}")

    # --- Run algorithm ---
    print("\nRunning summarization algorithm...")
    summary = run_algo(candidates_objects, eoi_set, annot_matrix)
    print("\nSummary completed.")
    print(f"{len(summary)} representative GO terms selected.\n")

    # Compact final report
    print("\nFinal Summary:")
    for idx, term in enumerate(sorted(summary, key=lambda x: x.IC, reverse=True), 1):
        elements_list = ", ".join(e.name for e in term.elements)
        print(f"{term.term} | IC={term.IC:.2f} | cov={term.coverage:.2f} | elements: [{elements_list}]")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog="python bissap.py",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        description=textwrap.dedent("""\
            ##########################################
            #             BISSAP Project             #
            #   Gwendoline Iborra & Vincent Ducot    #
            #          M2 Bioinformatics 2025        #
            ##########################################
            """),
    )

    parser.add_argument("dataset", help="Filename of elements (genes or proteins) list")
    parser.add_argument("--obo_file", required=True, help="Path to GO OBO file")
    parser.add_argument("--gaf_file", required=True, help="Path to GAF file")
    parser.add_argument("--threshold", type=float, default=0.05, help="FDR threshold for overrepresented GO terms (default: 0.05)",)
    parser.add_argument("--verbose", action="store_true", help="Enable detailed logging")

    args = parser.parse_args()
    main(args)
    #run example:
    #python bissap.py datasets/dataset03.csv --obo_file go-basic.obo --gaf_file goa_human.gaf --threshold 0.05 --verbose