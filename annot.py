###
# Annotation functions and classes
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###
from typing import Self
import os
import sys
import pickle
from goatools.obo_parser import GODag
from goatools.associations import read_gaf
from goatools.go_enrichment import GOEnrichmentStudy
from icecream import ic

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

    def get_all_ancestors(self) -> set:
        '''
        Returns all ancestor (is_a) GOTerms (direct and indirect) of the term.
        Args
            None
        Returns
            A set of GOTerm
        '''
        ancestors = set()
        stack = list(self.parent)
        while stack:
            p = stack.pop()
            if p not in ancestors:
                ancestors.add(p)
                stack.extend(p.parent)
        return ancestors

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
            ancestors_set.add(go)
            ancestors_set.update(go.get_all_ancestors())
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
    if not os.path.exists(filename):
        raise FileNotFoundError(f"Elements file not found: {filename}")
    
    elements = []
    try:
        with open(filename, "r") as f:
            for i, line in enumerate(f, 1):
                name = line.split('\t')[0].strip()
                if name:
                    elements.append(Element(name))
        return elements
    except Exception as e:
        raise IOError(f"Error reading elements file {filename}: {e}")


def fetch_annot_from_goatools(elements: list[Element], godag, assoc: dict):
    """
    Precompute parents/children using goatools.
    Fetch GO annotations from GAF file and keep only biological_process.
    Uses pickle to save/load associations for speed.
    Updates cover_elements for all GO terms (direct + ancestors)
    Args
        A list of Element to annotate
        The GO ontology
        A GAF file
    Returns
        None (modifies elements in place)
    Exceptions
        None
    """
    # Precompute parent/child mapping
    go2parents = {go_id: set() for go_id in godag.keys()}
    go2children = {go_id: set() for go_id in godag.keys()}
    for go_id, go_obj in godag.items():
        for parent in go_obj.parents:
            go2parents[go_id].add(parent.id)
            go2children[parent.id].add(go_id)

    for elem in elements:
        # Direct BP GO terms
        go_ids = [go for go in assoc.get(elem.name, set()) if godag[go].namespace == "biological_process"]
        if not go_ids:
            continue

        # Directs GO + all ancestors through is_a relationship
        all_go_ids = set(go_ids) # subgraph of all relevant GO terms (direct + ancestors)
        stack = list(go_ids)
        while stack:
            current = stack.pop()
            for p in go2parents.get(current, []):
                if p not in all_go_ids:
                    all_go_ids.add(p)
                    stack.append(p)

        # Create GOTerm objects for all GO in subgraph
        goterms_sub = {go_id: GOTerm(go_id, "biological_process") for go_id in all_go_ids}

        # Link parent/children
        for go_id, go_obj in goterms_sub.items(): # for each GO term in subgraph
            parent_ids = go2parents.get(go_id, set())
            go_obj.parent = [goterms_sub[p] for p in parent_ids if p in goterms_sub] # link to parents in subgraph
            for p_obj in go_obj.parent:
                p_obj.children.append(go_obj) # link child to parent

        # Assign only direct GO terms to element
        elem.goterms = [goterms_sub[go_id] for go_id in go_ids]

        # Update cover_elements for all GO terms in subgraph
        for go_obj in goterms_sub.values():
            go_obj.cover_elements.add(elem.name)


def get_overrepresented_terms(elements: list[Element], godag, assoc: dict, pval_threshold: float = 0.05) -> list[GOTerm]:
    """
    Mark GO terms as overrepresented using GOEnrichmentStudy.
    Updates fdr and overrepresented flags.
    Args
        A list of Element
        The GO ontology
        A GAF file
        The p-value threshold (default 0.05)
    Returns
        A list of overrepresented GOTerm
    Exceptions
        None
    """
    background = list(assoc.keys())
    study_set = [elem.name for elem in elements]

    old_stdout = sys.stdout
    sys.stdout = open(os.devnull, 'w')  # suppress internal prints

    goea_obj = GOEnrichmentStudy(background, assoc, godag, methods=["fdr_bh"])
    results = goea_obj.run_study(study_set)

    sys.stdout.close()
    sys.stdout = old_stdout

    # Map GO ID -> existing GOTerm
    go_to_goterm = {go.term: go for elem in elements for go in elem.goterms}
    overrep_terms = []
    for r in results:
        go_obj = go_to_goterm.get(r.GO, GOTerm(r.GO, "biological_process"))

        go_obj.fdr = r.p_fdr_bh
        go_obj.overrepresented = r.p_fdr_bh < pval_threshold
        go_obj.cover_elements.update(r.study_items)

        if go_obj.overrepresented:
            overrep_terms.append(go_obj)

    return overrep_terms


def build_data(go_obo_file, gaf_file, elements_file, assoc_pickle=None, fdr_threshold=0.05):
    """
    Build the full data structure: elements, GODag, overrepresented GO terms.
    Loads GAF / pickle once for efficiency.
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

    # Load or build associations once
    if assoc_pickle and os.path.exists(assoc_pickle):
        try:
            with open(assoc_pickle, "rb") as f:
                assoc = pickle.load(f)
            print(f"Loaded associations from pickle: {assoc_pickle}")
        except Exception as e:
            raise IOError(f"Failed to load associations from pickle: {e}")
    else:
        if not os.path.exists(gaf_file):
            raise FileNotFoundError(f"GAF file not found: {gaf_file}")
        try:
            assoc = read_gaf(gaf_file, go2geneids=False, prt=None)
            if assoc_pickle:
                with open(assoc_pickle, "wb") as f:
                    pickle.dump(assoc, f)
                print(f"Saved associations to pickle: {assoc_pickle}")
        except Exception as e:
            raise IOError(f"Failed to read GAF file {gaf_file}: {e}")
        
    print("Fetching GO annotations...")
    fetch_annot_from_goatools(elements, godag, assoc)
    print("Identifying overrepresented GO terms...")
    overrep_terms = get_overrepresented_terms(elements, godag, assoc, pval_threshold=fdr_threshold)
    ic(len(overrep_terms))
    ic(len(set(overrep_terms)))

    return elements, godag, overrep_terms