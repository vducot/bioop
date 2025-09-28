###
# Annotation functions and classes
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###
from typing import Self
import os
import pickle
from goatools.obo_parser import GODag
from goatools.associations import read_gaf
from goatools.gosubdag.gosubdag import GoSubDag
from goatools.go_enrichment import GOEnrichmentStudy

class GOTerm:
    '''
    Represents a GeneOntology term
    '''
    def __init__(self, term, namespace):
        self.term = term
        self.namespace = namespace # Aspects: BP, MF, CC
        self.parent: list['GOTerm'] = [] # list of parent GOTerm objects
        self.children: list['GOTerm']  = [] # list of child GOTerm objects
        self.IC = 0
        self.coverage = 0
        self.score = 0
        self.fdr = 0
        self.cover_elements = set()
        self.overrepresented = False

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
    
    def __init__(self, name):
        self.name = name # The element's name (eg P06132 or SOX2)
        self.goterms: list[GOTerm] = []  # list of GOTerms directly annotated to the element

    def get_all_ancestors(self) -> list[GOTerm]:
        '''
        Returns all ancestor (is_a) GOTerms (direct and indirect) of the element's annotations.
        Args
            None
        Returns
            A list of GOTerm
        '''
        ancestors_set = set()
        for go in self.goterms:
            ancestors_set.add(go.term) # include direct term in ancestors?
            for p in go.parent:
                ancestors_set.add(p.term)
        return list(ancestors_set)
    
def read_elements(filename: str) -> list[Element]:
    """
    Read a file with one element per line
    Args
        filename: path to the file
    Returns
        A list of Element
    Exceptions
        Raises an exception if the file cannot be read
    """
    elements = []
    try:
        with open(filename, "r") as f:
            for line in f:
                name = line.strip()
                if name:
                    elements.append(Element(name))
        return elements
    except Exception as e:
        raise Exception(f"Error reading elements from file {filename}: {e}")


def fetch_annot_from_goatools(elements: list[Element], godag, gaf_file, assoc_pickle = None):
    """
    Fetch GO annotations from GAF file and keep only biological_process.
    Args
        A list of Element to annotate
        The GO ontology
        The path to the GAF file
        The optional path to a pickle file to save/load the associations
    Returns
        None (modifies elements in place)
    Exceptions
        Raises an exception if the GAF file cannot be read
    """
    # Optional use of pickle file
    if assoc_pickle and os.path.exists(assoc_pickle):
        try:
            with open(assoc_pickle, "rb") as f:
                assoc = pickle.load(f)
        except Exception as e:
            raise Exception(f"Error reading associations from pickle file {assoc_pickle}: {e}")
    else:
        try:
            assoc = read_gaf(gaf_file, go2geneids=False, prt=None)
            if assoc_pickle:
                with open(assoc_pickle, "wb") as f:
                    pickle.dump(assoc, f)
        except Exception as e:
            raise Exception(f"Error reading GAF file {gaf_file}: {e}")

    for elem in elements:
        # Direct BP GO terms
        go_ids = [go for go in assoc.get(elem.name, set()) if godag[go].namespace == "biological_process"]
        if not go_ids:
            continue

        # Directs GO + all ancestors through is_a relationship
        all_go_ids = set(go_ids) # subgraph of all relevant GO terms (direct + ancestors)
        for go_id in go_ids:
            gosubdag_tmp = GoSubDag([go_id], godag, prt=None)
            all_go_ids.update(gosubdag_tmp.rcntobj.go2ancestors.get(go_id, set()))

        # Create GOTerm objects for all GO in subgraph
        goterms_sub = {go_id: GOTerm(go_id, "biological_process") for go_id in all_go_ids}

        # Link parent/children
        gosubdag_full = GoSubDag(list(all_go_ids), godag, prt=None) # full subdag
        for go_id, go_obj in goterms_sub.items():
            parent_ids = gosubdag_full.rcntobj.go2parents.get(go_id, set()) # get parent IDs from full subdag
            go_obj.parent = [goterms_sub[p] for p in parent_ids if p in goterms_sub]
            for p_obj in go_obj.parent:
                p_obj.children.append(go_obj)

        # Assign only direct GO terms to element
        elem.goterms = [goterms_sub[go_id] for go_id in go_ids]

def build_data(go_obo_file, gaf_file, elements_file, assoc_pickle = None):
    """
    Build the full data structure and return both elements and godag
    Args
        The path to the GO OBO file
        The path to the GAF file
        The path to the elements file
        The optional path to a pickle file to save/load the associations
    Returns
        A tuple (elements, godag)
    Exceptions
        Raises an exception if any file cannot be read
    """
    print("Loading GO ontology...")
    godag = GODag(go_obo_file)
    print("Reading elements...")
    elements = read_elements(elements_file)
    print("Fetching GO annotations...")
    fetch_annot_from_goatools(elements, godag, gaf_file, assoc_pickle)
    return elements, godag

def get_overrepresented_terms(elements: list[Element], godag, gaf_file, pval_threshold: float = 0.05) -> list[GOTerm]:
    """
    Marqued GO terms as 'overrepresented' if pval < threshold.
    Args
        A list of Element
        The GO ontology
        The path to the GAF file
        The p-value threshold (default 0.05)
    Returns
        A list of overrepresented GOTerm
    Exceptions
        Raises an exception if the GAF file cannot be read
    """
    pass