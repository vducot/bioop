###
# Annotation functions and classes
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###
from typing import Self

class GOTerm:
    '''
    Represents a GeneOntology term
    '''
    def __init__(self, term):
        self.term = term
        self.parent = []
        self.children = []
        self.IC = 0
        self.coverage = 0
        self.score = 0
        self.fdr = 0
        self.cover_elements = set()

    def add_parent(self, other_parent: Self):
        self.parent.append(other_parent)

    def add_child(self, child: Self):
        self.children.append(child)

    def get_all_ancestors(self) -> list[GOTerm]:
        '''
        Args
            None
        Returns
            A list of all GOTerm that are ancestors of the current GOTerm
        Exception
            ??
        '''
        pass

class Element:
    '''
    Represents an element of interest
    '''
    
    def __init__(self, name, elem_type):
        self.name = name # The element's name (eg P06132 or SOX2)
        self.type = elem_type # The element's type (Protein or Gene)
        self.goterm = None # A GOTerm object as direct annotation

    def get_all_ancestors(self) -> list[GOTerm]:
        '''
        Args
            None
        Returns
            A list of all GOTerm that define the element
        Exception
            ??
        '''
        pass