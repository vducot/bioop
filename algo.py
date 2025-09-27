###
# Algorithm functions
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###

import math
import annot
import sys

class AnnotMatrix:
    '''
    The matrix representing all the annotations for all the given elements
    '''
    def __init__(self, gaf_file):
        matrix, elements_list, annot_list = self.generate_whole_matrix(gaf_file)
        elements_number = len(elements_list)
        annot_number = len(annot_list)

    def generate_whole_matrix(self, gaf_file):
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

def compute_IC(annot, wag):
    '''
    Compute the Information Content index
    IC(a) = -log2(p(a)) with p(a) = len(annotedBy(a) in total elements) / len(total_elements)
    ie the probability of finding this annotation when analyzing the whole genome
    Args 
        The annotation to compute as a GOTerm object
        An AnnotMatrix object
    Returns
        The Information Content index
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

def compute_score(annot, eoi, wag):
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
        The score as an double
    Exception
        None
    '''
    annototed_by_a = 0
    for e in eoi:
        if e.goterm == annot:
            annototed_by_a +=1
    return compute_IC(annot, wag) * annototed_by_a

def run_algo(eoi, wag):
    '''
    Run the algorithm to summarize annotations of a set of elements
    Args
        A list of Elements Of Interest (genes or proteins)
        An AnnotMatrix object
    Returns
        A list of annotations
    Exception
        ???
    '''
    pass