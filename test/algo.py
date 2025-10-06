# algo.py
import numpy as np
import math
import pickle
import zipfile
from typing import Tuple
import pandas as pd
from annot import GOTerm

MATRIX_FILE = 'whole_annotation_genome.zip'

class AnnotMatrix:
    '''
    The matrix representing all the annotations for all the given elements
    '''
    def __init__(self, gaf_file):
        self.matrix, self.go_ids, self.elements_ids = self.generate_whole_matrix(gaf_file)
        self.elements_number = len(self.elements_ids)
        self.go_number = len(self.go_ids)

    def dump_to_file(annotMatrix, filename):
        # Save the object to a file
        print("Dump the object to "+filename)
        zf = zipfile.ZipFile(filename, 'w', zipfile.ZIP_DEFLATED)
        zf.writestr('wag.matrix', pickle.dumps(annotMatrix))
        zf.close()
        # with open(MATRIX_FILE, 'wb') as fout:
        #     pickle.dump(matrix, fout)

    def generate_whole_matrix(self, gaf_file) -> Tuple[np.ndarray, dict, dict]:
        '''
        Generate the matrix representing all the annotations for all the elements, based on the gaf_file
        Initialize a dict with all elements of the gaf file and another with all annotations
        Create a matrix where [i,j] = 1 if the element j is annoted by the annotation i
        Args
            The GAF filename to read
        Returns
            The matrix
            A dict of all elements with their index
            A dict of all annotations with their index
        Exception
            FileNotFoundError or ParserError or Exception
        '''
        # Column 1 is Uniprot ID, 4 is ID GO and 8 the GO Aspect
        df = pd.read_csv(gaf_file, sep='\t', comment='!', header=None, usecols=[1,4,8])
        #ic(df.shape)
        # Filter on "Biological Process"
        df = df[df[8] == 'P']
        #print(df.head)
        # Get unique IDs and init the matrix with zeros
        # Column 4 is ID GO
        go_unique_number = len(df[4].unique())
        # Column 1 is Uniprot ID
        element_unique_number= len(df[1].unique())

        matrix = np.zeros((go_unique_number, element_unique_number), dtype=int)

        go_ids = dict()
        element_ids = dict()
        # Read all GAF file and set (i,j) to 1 in the matrix if i annotates j
        #ic(df.shape)
        for i, row in df.iterrows():
            e_id = row[1]
            go_id = row[4]
            # Check if we have already seen this element
            if e_id in element_ids.keys():
                # Fetch the index in the matrix
                e_index = element_ids[e_id]
            else:
                # Add the element to the dict
                element_ids[e_id] = len(element_ids)
                e_index = element_ids[e_id]

            # Same for the GO terms
            if go_id in go_ids.keys():
                # Fetch the index in the matrix
                go_index = go_ids[go_id]
            else:
                # Add the element to the dict
                go_ids[go_id] = len(go_ids)
                go_index = go_ids[go_id]

            matrix[go_index, e_index] = 1

        #ic(go_ids)
        #ic(element_ids)
        #np.set_printoptions(threshold=sys.maxsize)
        #print(matrix)
        #ic(matrix.shape)

        return matrix, go_ids, element_ids

def score(annot: GOTerm):
    """Score = IC * coverage"""
    if annot.IC is None or annot.coverage is None:
        raise ValueError(f"IC or coverage not computed for {annot.term}")
    return annot.IC * annot.coverage

def compute_IC(annot, wag):
    """Compute Information Content using global AnnotMatrix."""
    if annot.term not in wag.go_ids:
        return 0.0
    idx = wag.go_ids[annot.term]
    p = sum(wag.matrix[idx]) / wag.elements_number
    return -math.log2(p) if p>0 else 0.0

def run_algo(candidates, eoi_set, wag, verbose=True):
    summary = set()
    ElmtsAnnotatedBySummary = set()
    iteration = 1

    # Compute IC for all candidates
    for c in candidates:
        c.IC = compute_IC(c, wag)

    while candidates and (eoi_set - ElmtsAnnotatedBySummary):
        if verbose:
            print(f"\n--- Iteration {iteration} ---")
            print(f"Remaining candidates: {[c.term for c in candidates]}")

        # Score = IC * coverage on remaining elements
        scores = {}
        for c in candidates:
            new_elements = c.elements - ElmtsAnnotatedBySummary
            effective_cov = len(new_elements) / len(eoi_set)
            score = effective_cov * c.IC
            scores[c] = score
            if verbose:
                print(f"Candidate: {c.term} | IC={c.IC:.2f} | new_cov={effective_cov:.2f} | score={score:.4f}")

        max_score = max(scores.values())
        cWMS = {c for c, s in scores.items() if s==max_score}

        # Remove descendants
        descendants = set()
        for c in cWMS:
            descendants |= {d for d in candidates if d.term in c.descendants}
        candidates -= descendants

        # Add to summary
        summary |= cWMS
        candidates -= cWMS
        ElmtsAnnotatedBySummary |= {e for c in summary for e in c.elements}

        if verbose:
            print(f"Selected cWMS: {[c.term for c in cWMS]}")
            print(f"Removed descendants: {[d.term for d in descendants]}")
            print(f"Total elements annotated: {len(ElmtsAnnotatedBySummary)}/{len(eoi_set)}")
            print(f"Candidates left: {[c.term for c in candidates]}")

        iteration += 1

    if verbose:
        print("\n--- Algorithm finished ---")
        print(f"Final summary ({len(summary)} GO terms): {[c.term for c in summary]}")
    return summary
