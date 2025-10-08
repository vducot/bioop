# annot.py
import os
import pickle
import pandas as pd
from goatools.obo_parser import GODag
from goatools.associations import read_gaf

class Element:
    """Represents an element of interest (gene/protein/etc.)"""
    def __init__(self, name):
        self.name = name
        self.go_terms = set()  # Set of GOTerm objects annotated to this element

    def add_go_term(self, go_term):
        self.go_terms.add(go_term)
        go_term.add_element(self)

def fetch_BP_annotations(eoi_set, gaf_file: str, godag: GODag, gaf_pickle=None):
    """
    Remplir self.go_terms avec tous les GO 'biological_process' annotant cet élément
    Args:
        gaf_file : le fichier GAF complet
        godag : objet GODag (ontologie GO)
    """
    # Load or build associations once
    if gaf_pickle and os.path.exists(gaf_pickle):
        try:
            with open(gaf_pickle, "rb") as f:
                gaf = pickle.load(f)
            print(f"Loaded associations from pickle: {gaf_pickle}")
        except Exception as e:
            raise IOError(f"Failed to load associations from pickle: {e}")
    else:
        if not os.path.exists(gaf_file):
            raise FileNotFoundError(f"GAF file not found: {gaf_file}")
        try:
            gaf = read_gaf(gaf_file, go2geneids=False, prt=None)
            if gaf_pickle:
                with open(gaf_pickle, "wb") as f:
                    pickle.dump(gaf, f)
                print(f"Saved associations to pickle: {gaf_pickle}")
        except Exception as e:
            raise IOError(f"Failed to read GAF file {gaf_file}: {e}")
            
    # For each element of interest
    for elem in eoi_set:
        elem.go_terms = set()
        for go_id in gaf.get(elem.name, set()):
            if go_id in godag and godag[go_id].namespace == "biological_process":
                elem.go_terms.add(go_id)

class GOTerm:
    """Represents a GO term with IC, ancestors, descendants, coverage info"""
    def __init__(self, term, godag: GODag):
        self.term = term
        self.godag = godag
        self.ancestors = set()
        self.descendants = set()
        self.elements = set()
        self.IC = None
        self.coverage = 0.0
        self.longname = self.get_annot_long_name(term)
        self._populate_ancestors_descendants()

    def get_annot_long_name(self, term: str):
        '''
        Fetch complete and human readable name of an GO term
        Args
            The GO term as string
        Returns
            The complete name (if found), else None
        Exception
            No exception
        '''
        match_file = "geneOntology-BP-label.tsv"
        match_df = pd.read_csv(match_file, sep="\t")
        term_series = match_df.loc[match_df.idAnnotation==term,'annotationLabel']
        if len(term_series) == 0:
            return None
        return term_series.values[0]
        
    def _populate_ancestors_descendants(self):
        """Use GODag from goatools to get ancestors/descendants"""
        if self.term in self.godag:
            goobj = self.godag[self.term]
            self.ancestors = set(goobj.get_all_parents())
            self.descendants = set(goobj.get_all_children())

    def add_element(self, element):
        self.elements.add(element)

    def update_coverage(self, eoi_set):
        """Coverage = #EOI annotated / total EOI"""
        self.coverage = len(self.elements & eoi_set) / len(eoi_set) if eoi_set else 0.0
        return self.coverage
