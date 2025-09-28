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

class AnnotMatrix:
    '''
    The matrix representing all the annotations for all the given elements
    '''
    def __init__(self, gaf_file):
        matrix, elements_list, annot_list = self.generate_whole_matrix(gaf_file)
        elements_number = len(elements_list)
        annot_number = len(annot_list)

    def generate_whole_matrix(self, gaf_file) -> Tuple[np.ndarray, list[annot.Element], list[annot.GOTerm]]:
        '''
        Generate the matrix representing all the annotations for all the elements, based on the gaf_file
        Initialize a list with all elements of the gaf file and another with all annotations
        Create a matrix where [i,j] = 1 if the element j is annoted by the annotation i
        Args
            The GAF filename to read
        Returns
            The matrix
            A list of all elements
            A list of all annotations
        Exception
            IOError or FileNotFoundError
        '''
        try:
            with open(gaf_file, 'r'):
            # Load in memory
                pass
        except FileNotFoundError:
            print(f"The file {gaf_file} was not found.")
            sys.exit(1)
        except IOError:
            print("An error occurred while reading the file {gaf_file}.")
            sys.exit(1)

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
    try:
        annot_index = wag.annot_list[annot]
    except ValueError:
        print(f"Error, the annotation {annot} is not in the pre-calculated data. Exiting.")
        sys.exit(404)

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
        if e.goterm == annot:
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

def run_algo(eoi, wag) -> set[annot.GOTerm]:
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
    candidates = annot.get_overrepresented(eoi, ...) # List
    elts_annot_by_cand = set()
    for c in candidates:
        elts_annot_by_cand.add(c.cover_elements)
    
    elts_annot_by_summary = set()

    # Main loop
    # While there is still
    # - candidates and
    # - elements annotated by candidates but not annotated by summary annotations
    while (len(candidates) != 0 and len(elts_annot_by_cand) - len(elts_annot_by_cand - elts_annot_by_summary)):
        # Compute candidates with max score
        cWNS = get_max_score_element(candidates)

        # Remove the annotations from cWNS that have a descendant in cWNS with the same coverage (= keep the most precise)
        #@TODO

        # Remove the descendants of cWNS from candidates
        candidates_to_remove = set()
        for a in candidates:
            if a.parent in cWNS or a.parent in candidates_to_remove:
                candidates_to_remove.add(a)

        # Summary = summary U cWNS
        summary.update(cWNS)

        # Remove cWNS annotations from candidates, as we add them to summary
        for a in cWNS:
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
        # @TODO Whyyyy ? ça va pas virer trop de choses dès le début ??

    # Prune redondant annotations from the summary
    # = annot that are covered by > 1 other annot of the summary ??

    # Annot from summary that have an ancestor in the summary
    to_remove = set()
    for annot in summary:
        ancestors = annot.get_all_ancestors()
        # That have an ancestor in the summary
        for ancestor in ancestors:
            if ancestor in summary:
                #1 The ancestor annotates >= 1 EOI that is not annotated by any other annot from the summary
                for elt in ancestor.cover_elements():
                    elt_anc = elt.get_all_ancestors()
                    if elt_anc == set(ancestor):
                        print(f"Annotation {annot.term} marked to prune")
                        # Discard the descendant
                        to_remove.add(annot)
                #2 All the EOI are annotated by >=1 annot from the summary that is different from the ancestor
                counter = 0
                for e in eoi:
                    elt_annots = e.get_all_ancestors()
                    # Annotations different from the ancestor
                    annot_diff_from_anc = set(elt_annots) - set(ancestor)
                    # that are from the summary
                    annot_is_in_summary = [a for annot in annot_diff_from_anc if a in summary ]
                    if len(annot_is_in_summary) >= 1:
                        counter += 1
                # All the EOI
                if counter == len(eoi):
                    # Discard the ancestor
                    to_remove.add(ancestor)

    pass