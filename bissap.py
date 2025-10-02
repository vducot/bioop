###
# Main file
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###

import argparse
import textwrap
import annot
from algo import AnnotMatrix, run_algo
import algo
import os
import pickle
import zipfile
from pathlib import Path
from icecream import ic

MATRIX_FILE = 'whole_annotation_genome.zip'
ASSOC_PICKLE = "assoc.pkl"

def summarize_per_element(elements, include_ancestors=False):
    print("\n=== Per-element GO summary ===\n")

    for elem in elements:
        direct_go = elem.goterms
        if include_ancestors:
            total_go = set(direct_go)
            for go in direct_go:
                total_go.update(go.parent)
        else:
            total_go = set(direct_go)

        overrep_go = [go for go in total_go if go.overrepresented]

        print(f"Element: {elem.name}")
        print(f"  # Direct GO: {len(direct_go)}")
        print(f"  # Total GO: {len(total_go)}")
        print(f"  Direct GO terms: {', '.join(go.term for go in direct_go)}")
        print(f"  Overrepresented GO terms ({len(overrep_go)}): {', '.join(go.term for go in overrep_go)}\n")

def summarize_global(elements):
    print("\n=== Global GO summary ===\n")
    all_go = {go for elem in elements for go in elem.goterms}
    overrep_go = [go for go in all_go if go.overrepresented]

    print(f"Total GO terms found: {len(all_go)}")
    print(f"Total overrepresented GO terms: {len(overrep_go)}\n")

    if overrep_go:
        print("Top overrepresented GO terms (sorted by FDR, up to 10):")
        for go in sorted(overrep_go, key=lambda g: g.fdr)[:10]:
            print(f"- {go.term} | NS: {go.namespace} | FDR: {go.fdr:.3e} | Covered elements: {len(go.cover_elements)}")
    print("\n")

def summarize_annotations(summary_terms):
    print("\n=== Summary Annotations ===\n")
    for term in summary_terms:
        print(f"{term.term} | Coverage: {len(term.cover_elements)} elements")
    print(f"\nTotal summary GO terms: {len(summary_terms)}\n")

def main(args):
    # --- Load GO data ---
    print(f"Building GO annotations using OBO file {args.obo_file} and GAF file {args.gaf_file}...")
    elements, godag, overrep_terms = annot.build_data(
        args.obo_file, args.gaf_file, args.elements_file, ASSOC_PICKLE, fdr_threshold=args.threshold
    )
    ic(len(overrep_terms))

    summarize_per_element(elements, include_ancestors=True)
    summarize_global(elements)

    # --- Load or compute the whole annotation matrix ---
    # Check if the whole annotation matrix has already been computed
    annotMatrix = None
    if Path.exists(Path(MATRIX_FILE)):
        if zipfile.is_zipfile(MATRIX_FILE):
            print("Zip file found, loading the matrix")
            zf = zipfile.ZipFile(MATRIX_FILE, 'r')
            annotMatrix = pickle.loads(zf.open('wag.matrix').read())
                #zip.extractall()
        else:
            print(MATRIX_FILE+" is not a zip file")
            with open(MATRIX_FILE, 'rb') as fin:
                # Load in memory
                annotMatrix = pickle.load(MATRIX_FILE)
    else:
        # Calculate the matrix
        print("Calculating the whole genome annotation matrix")
        annotMatrix = algo.AnnotMatrix(args.gaf_file)
        algo.AnnotMatrix.dump_to_file(annotMatrix, MATRIX_FILE)
        
    # --- Run summarization algorithm ---
    print("Running annotation summarization...")
    annot_summary = algo.run_algo(elements, annotMatrix)
    summarize_annotations(annot_summary)

    # --- Optional: show top GO terms by coverage ---
    top_by_coverage = sorted(annot_summary, key=lambda g: len(g.cover_elements), reverse=True)[:10]
    print("Top GO terms by coverage (up to 10):")
    for go in top_by_coverage:
        print(f"- {go.term} | Coverage: {len(go.cover_elements)} elements")
    

if __name__ == '__main__':
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

    parser.add_argument('elements_file', help='Filename of elements (genes or proteins) list')
    parser.add_argument('--gaf_file', default="files/goa_human.gaf", help='GAF file path')
    parser.add_argument('--obo_file', default="files/go-basic.obo", help='GO OBO file path')
    parser.add_argument('--threshold', type=float, default=0.05, help='FDR threshold for overrepresented GO terms')

    args = parser.parse_args()
    main(args)