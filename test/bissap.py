# bissap.py
import argparse
import textwrap
import zipfile
import pickle
from annot import Element, GOTerm, fetch_BP_annotations
from pathlib import Path
from algo import AnnotMatrix, run_algo
import algo
import os
import re
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
                name = re.split('\t', line)[0].strip()
                if name:
                    elements.add(Element(name))
        return elements
    except Exception as e:
        raise IOError(f"Error reading elements file {filename}: {e}")

def main(args):
    eoi_set = read_elements(args.dataset)
    print(f"Loaded {len(eoi_set)} elements of interest")
    # --- Load GO data ---
    godag = GODag(args.obo_file)

    fetch_BP_annotations(eoi_set, args.gaf_file, godag, GAF_PICKLE)
    if args.verbose:
        for e in eoi_set:
            print(f"{e.name}: {len(e.go_terms)} BP GO terms")
    
    if args.just_scores:
        # We only want scores from external annotations (ie ORA, GSEA)
        print("scores")
        # Filter 
        pass
    else:
        # --- Load or compute the whole annotation matrix ---
        # Check if the whole annotation matrix has already been computed
        annot_matrix = None
        if Path.exists(Path(MATRIX_FILE)):
            if zipfile.is_zipfile(MATRIX_FILE):
                print("Zip file found, loading the matrix")
                zf = zipfile.ZipFile(MATRIX_FILE, 'r')
                annot_matrix = pickle.loads(zf.open('wag.matrix').read())
                    #zip.extractall()
            else:
                print(MATRIX_FILE+" is not a zip file")
                with open(MATRIX_FILE, 'rb') as fin:
                    # Load in memory
                    annot_matrix = pickle.load(MATRIX_FILE)
        else:
            # Calculate the matrix
            print("Calculating the whole genome annotation matrix")
            annot_matrix = algo.AnnotMatrix(args.gaf_file)
            algo.AnnotMatrix.dump_to_file(annot_matrix, MATRIX_FILE)
        print(f"Annotation matrix: {annot_matrix.elements_number} elements x {annot_matrix.go_number} GO terms")

        # --- Build GOTerm candidates ---
        candidates_dict = {}
        for e in eoi_set:
            for go_id in e.go_terms:
                if go_id not in candidates_dict:
                    term = GOTerm(go_id, godag)
                    candidates_dict[go_id] = term
                candidates_dict[go_id].elements.add(e)
        # Filter by coverage threshold
        candidates = set()
        for term in candidates_dict.values():
            term.update_coverage(eoi_set)
            if term.coverage >= args.threshold:
                candidates.add(term)
                #if args.verbose:
                    #print(f"[CANDIDATE] {term.term} | coverage={term.coverage:.2f} | elements={[e.name for e in term.elements]}")

        # --- Run algorithm ---
        summary = run_algo(candidates, eoi_set, annot_matrix, verbose=args.verbose)

    print(f"\nSummary produced with {len(summary)} GO terms\n")
    total_score = 0
    for idx, term in enumerate(summary, 1):
        score = term.IC * term.coverage
        total_score += score
        print(f"{idx}. {term.term} | {term.longname} | IC={term.IC:.2f} | coverage={term.coverage:.2f} | score={score:.2f} | elements={[e.name for e in term.elements]}")
    print(f"Score mean : {total_score/len(summary):.2f}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog='python bissap.py',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        description=textwrap.dedent('''\
            ##########################################
            #          BISSAP Project               #
            #   Gwendoline Iborra & Vincent Ducot   #
            #        M2 Bio-Info - 2025             #
            ##########################################''')
    )

    parser.add_argument("dataset", help="Filename of elements (genes or proteins) list")
    parser.add_argument("--obo_file", required=True, help='GO OBO file path')
    parser.add_argument("--gaf_file", required=True, help='GAF file path')
    parser.add_argument("--threshold", type=float, default=0.05, help="FDR threshold for overrepresented GO terms")
    parser.add_argument("--verbose", action="store_true", help="Enable verbose logging")
    parser.add_argument("--just_scores", action="store_true", help="Only compute stores from external annotations list")

    args = parser.parse_args()
    main(args)
