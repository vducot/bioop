###
# Algorithm functions
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###
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
        print("Dump the object to "+filename)
        zf = zipfile.ZipFile(filename, 'w', zipfile.ZIP_DEFLATED)
        zf.writestr('wag.matrix', pickle.dumps(annotMatrix))
        zf.close()

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
        df = df[df[8] == 'P'] # Filter on "Biological Process"
        # Get unique IDs and init the matrix with zeros
        go_unique_number = len(df[4].unique())
        element_unique_number= len(df[1].unique())

        matrix = np.zeros((go_unique_number, element_unique_number), dtype=int)

        go_ids = dict()
        element_ids = dict()
        # Read all GAF file and set (i,j) to 1 in the matrix if i annotates j
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

        return matrix, go_ids, element_ids

def compute_IC(annot, wag):
    """
    Compute Information Content (IC) using the annotation matrix
    Args:
        GO term
        Global annotation matrix
    Returns:
        IC value (0 if term not in matrix)
    """
    if annot.term not in wag.go_ids.keys():
        print(f"{annot.term} not found in the matrix")
        return 0.0
    idx = wag.go_ids[annot.term]
    p = sum(wag.matrix[idx]) / wag.elements_number
    return -math.log2(p)

def compute_H(annot, wag):
    """
    Compute hentropy using global AnnotMatrix
    H(annot)= -p(annot)*log2(p(annot))
    Args:
        GO term
        Global annotation matrix
    Returns:
        H value (0 if term not in matrix)
    """
    if annot.term not in wag.go_ids.keys():
        print(f"{annot.term} not found in the matrix")
        return 0.0
    idx = wag.go_ids[annot.term]
    p = sum(wag.matrix[idx]) / wag.elements_number
    return -p * math.log2(p)

def run_algo(candidates, eoi_set, wag, score_type: str = "IC"):
    """
    Run the greedy summarization algorithm for GO terms
    Args:
        Candidate GO terms
        Elements of interest
        Annotation matrix
        Whether to print summary info
    Returns:
        Selected summary GO terms
    """
    if score_type not in {"IC", "H"}:
        raise ValueError(f"Invalid score_type '{score_type}'. Must be 'IC' or 'H'.")
    summary = set()
    ElmtsAnnotatedBySummary = set()

    # Compute IC for all candidates
    for c in candidates:
        if score_type == "IC":
            c.score = compute_IC(c, wag)
        else:
            c.score = compute_H(c, wag)

    while candidates and (eoi_set - ElmtsAnnotatedBySummary):
        scores = {}
        for c in candidates:
            new_elements = c.elements - ElmtsAnnotatedBySummary
            effective_cov = len(new_elements) / len(eoi_set)
            score = effective_cov * c.score
            scores[c] = score
            
        max_score = max(scores.values())
        cWMS = {c for c, s in scores.items() if s==max_score}

        # Remove descendants
        descendants = set()
        for c in cWMS:
            descendants |= {d for d in candidates if d.term in c.descendants}
        candidates -= descendants

        # Update summary
        summary |= cWMS
        candidates -= cWMS
        ElmtsAnnotatedBySummary |= {e for c in summary for e in c.elements}

    # Pruning
    to_remove = set()
    # 1. Remove descendants only if fully covered by multiple ancestors
    for annot in summary:
        ancestors = annot.ancestors
        for anc in ancestors:
            if anc in summary:
                # a) remove descendant if ancestor annotates at least one element not covered by other summary GO
                for elt_name in anc.cover_elements:
                    e = next((el for el in eoi_set if el.name == elt_name), None)
                    if e and all(a not in summary or a == anc for a in e.get_all_ancestors()):
                        to_remove.add(annot)
                # b) remove ancestor if all elements annotated by at least one other summary GO
                counter = 0
                for e in eoi_set:
                    e_ancestors = e.get_all_ancestors()
                    other_summary_anc = [a for a in e_ancestors if a in summary and a != anc]
                    if other_summary_anc:
                        counter += 1
                if counter == len(eoi_set):
                    to_remove.add(anc)
    summary -= to_remove

    return summary