###
# Algorithm functions
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###

import math
import annot
import sys
from typing import Tuple
import numpy as np
import pandas as pd
from icecream import ic
import pickle
import zipfile

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

def compute_IC(annot, wag) -> float:
    '''
    Compute the Information Content index
    IC(a) = -log2(p(a)) with p(a) = len(annotedBy(a) in total elements) / len(total_elements)
    ie the probability of finding this annotation when analyzing the whole genome
    Args 
        The annotation to compute as a GOTerm object
        An AnnotMatrix object
    Returns
        The Information Content index as a float
    Exception
        ValueError if the given annotation is not in our database
    '''
    annot_index = 0
    if annot.term not in wag.go_ids: # The annotation from obo file that is not in our database
        return 0.0
    annot_index = wag.go_ids[annot.term]
    # Probability of finding this annotation in the whole genome
    p = sum(wag.matrix[annot_index]) / wag.elements_number
    return -math.log(p, 2)

def compute_score(annot, eoi, wag) -> float:
    '''
    Compute a score of an annotation that reflects :
    - its general relevance
    - the number of elements of interest it annotates
    score = IC(a) x len(annotatedBy(a) in elements of interest)
    Args
        The annotation to compute as a GOTerm object
        Elements of interest, as a list of Element objects
        An AnnotMatrix object
    Returns
        The score as an float
    Exception
        None
    '''
    annototed_by_a = 0
    for e in eoi:
        if e.goterms == annot:
            annototed_by_a +=1
    return compute_IC(annot, wag) * annototed_by_a

def get_max_score_element(annot_list) -> set[annot.GOTerm]:
    '''
    Get the element with max score in an annotation list
    Args
        A list of GOTerm objects
    Returns
        A set of GOTerm objects with the max score within the annotations of annot_list
    Exception
        ValueError if the list is empty
    '''
    if len(annot_list) == 0:
        ValueError("Error, can get the max score from an empty list")

    max_score = annot_list[0].score
    max_elt = annot_list[0]
    for a in annot_list[1:]:
        if a.score > max_score:
            max_score = a.score
            max_elt = a

    # Keep only annotations with the max score
    annots_with_max_score = set()
    for a in annot_list:
        if a.score == max_score:
            annots_with_max_score.add(a)
    return annots_with_max_score

def run_algo(eoi, wag, candidates) -> set[annot.GOTerm]:
    '''
    Run the algorithm to summarize annotations of a set of elements
    Args
        A list of Elements Of Interest (genes or proteins)
        An AnnotMatrix object
    Returns
        A set of GOTerm objects that describe the elements of interest with the best compromise between precision and cover
    Exception
        ???
    '''
    # Initialization
    summary = set()
    elts_annot_by_cand = set()

    for c in candidates:
        elts_annot_by_cand.update(c.cover_elements)
    
    elts_annot_by_summary = set()

    # Initial score computation
    for c in candidates:
        c.score = compute_score(c, eoi, wag)

    # Main loop
    # While there is still
    # - candidates and
    # - elements annotated by candidates but not annotated by summary annotations
    while len(candidates) > 0 and len(elts_annot_by_cand - elts_annot_by_summary) > 0:
        # Compute candidates with max score
        cWNS = get_max_score_element(candidates)

        # Remove the annotations from cWNS that have a descendant in cWNS with the same coverage (= keep the most precise)
        cWNS_to_remove = set()
        for a in cWNS:
            for d in a.children:
                if d in cWNS and a.cover_elements == d.cover_elements:
                    cWNS_to_remove.add(a)
        cWNS -= cWNS_to_remove

        # Remove the descendants of cWNS from candidates
        candidates_to_remove = set()
        for a in candidates:
            if any(p in cWNS for p in a.parent) or any(p in candidates_to_remove for p in a.parent):
                candidates_to_remove.add(a)
        candidates = candidates - candidates_to_remove

        # Summary = summary U cWNS
        summary.update(cWNS)

        # Remove cWNS annotations from candidates, as we add them to summary
        # candidates = [c for c in candidates if c not in cWNS]
        for a in cWNS:
            if a in candidates:
                candidates.remove(a)

        # Update elements annotated by candidates and elements annotated by summary
        for e in eoi:
            ancestors = e.get_all_ancestors()
            for a in ancestors:
                if a in summary:
                    elts_annot_by_summary.add(e)
                if a in candidates and e not in elts_annot_by_cand:
                    elts_annot_by_cand.add(e)

        # Remove from candidates the annotations that are not associated with any element
        # not yet annotated by summary
        for a in candidates:
            # If intersection with elts_annot_by_summary and a.cover_elements is empty
            if len(a.cover_elements & elts_annot_by_summary) == 0:
                # Remove the annot from candidates
                candidates.remove(a)


    ## Prune redondant annotations from the summary
    # = annot that are covered by > 1 other annot of the summary ??

    # Annot from summary that have an ancestor in the summary
    to_remove = set()
    # 1. Remove descendants only if fully covered by multiple ancestors
    for annot in summary:
        ancestors = annot.get_all_ancestors()
        for anc in ancestors:
            if anc in summary:
                # a) remove descendant if ancestor annotates at least one element not covered by other summary GO
                for elt_name in anc.cover_elements:
                    e = next((el for el in eoi if el.name == elt_name), None)
                    if e and all(a not in summary or a == anc for a in e.get_all_ancestors()):
                        to_remove.add(annot)
                # b) remove ancestor if all elements annotated by at least one other summary GO
                counter = 0
                for e in eoi:
                    e_ancestors = e.get_all_ancestors()
                    other_summary_anc = [a for a in e_ancestors if a in summary and a != anc]
                    if other_summary_anc:
                        counter += 1
                if counter == len(eoi):
                    to_remove.add(anc)
    summary -= to_remove

    return summary