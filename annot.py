###
# Annotation functions and classes
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###
import os
import pickle
from goatools.obo_parser import GODag
from goatools.associations import read_gaf
from goatools.go_enrichment import GOEnrichmentStudy
import sys

class Element:
    """
    Represents an element of interest (gene/protein/etc.)
    """
    def __init__(self, name):
        self.name = name
        self.go_terms = set()  # Set of GOTerm objects annotated to this element

    def add_go_term(self, go_term):
        self.go_terms.add(go_term)
        go_term.add_element(self)

def fetch_BP_annotations(eoi_set, gaf_file: str, godag: GODag, gaf_pickle=None):
    """
    Annotate each element with biological process GO terms. 
    Loads GO associations from a GAF file (or from a cached pickle),
    and adds 'biological_process' GO terms to each element.
    Args:
        As et of elements of interest.
        A path to the GAF annotation file.
        GO ontology DAG object.
        Optional path to a pickle cache.
    Returns:
        None (updates elements)
    Exceptions:
        FileNotFoundError: If the GAF file does not exist.
        IOError: If reading or writing the pickle fails.
    """
    # Load or build GAF associations
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
            
    # Assign biological process GO terms
    for elem in eoi_set:
        elem.go_terms = set()
        for go_id in gaf.get(elem.name, set()):
            if go_id in godag and godag[go_id].namespace == "biological_process":
                elem.go_terms.add(go_id)

class GOTerm:
    """
    Represents a Gene Ontology (GO) term.
    """
    def __init__(self, term, godag: GODag):
        self.term = term
        self.godag = godag
        self.ancestors = set()
        self.descendants = set()
        self.elements = set()
        self.IC = None
        self.coverage = 0.0
        self.overrepresented = False
        self.pvalue = 0.0
        self._populate_ancestors_descendants()

    def _populate_ancestors_descendants(self):
        """
        Populate ancestor and descendant GO terms using GODag.
        """
        if self.term in self.godag:
            goobj = self.godag[self.term]
            self.ancestors = set(goobj.get_all_parents())
            self.descendants = set(goobj.get_all_children())

    def add_element(self, element):
        self.elements.add(element)

    def update_coverage(self, eoi_set):
        """
        Compute and update the coverage for this GO term.
        Args:
            Aet of elements of interest.
        Returns:
            A fraction of EOI annotated with this term.
        """
        self.coverage = len(self.elements & eoi_set) / len(eoi_set) if eoi_set else 0.0
        return self.coverage
    
def get_overrepresented_terms(eoi_set, godag: GODag, gaf_file, gaf_pickle=None, pval_threshold: float = 0.05) -> set:
    """
    Perform GO enrichment analysis to find overrepresented terms.
    Args:
        Aset or list of Element objects
        godag: GODag instance
        Path to the GAF annotation file.
        Optional path to a pickle cache.
        Significance threshold (default: 0.05)
    Returns:
        A set of GOTerm objects marked as overrepresented
    Exceptions:
        FileNotFoundError: If the GAF file does not exist.
        IOError: If reading or writing the pickle fails.
    """
    # Load or build associations
    if gaf_pickle and os.path.exists(gaf_pickle):
        try:
            with open(gaf_pickle, "rb") as f:
                gaf = pickle.load(f)
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
        except Exception as e:
            raise IOError(f"Failed to read GAF file {gaf_file}: {e}")
        
    # Prepare study and background sets
    study_set  = {elem.name for elem in eoi_set}
    background = list(gaf.keys())

    old_stdout = sys.stdout
    sys.stdout = open(os.devnull, 'w') # suppress internal prints
    # Build GOEA object
    goeaobj = GOEnrichmentStudy(
        background,           # Population totale
        gaf,                      # Associations gène -> GO terms
        godag,                    # Ontologie GO
        propagate_counts=True,    # Propage les annotations aux ancêtres
        alpha=pval_threshold,     # Seuil de significativité
        methods=['fdr_bh']        # Méthodes de correction multiple
    )
    results = goeaobj.run_study(study_set)
    sys.stdout.close()
    sys.stdout = old_stdout

    # Collect overrepresented terms
    overrepresented_terms = set()
    for r in results:
        if r.p_fdr_bh < pval_threshold and r.NS == "BP":
            # Créer un GOTerm si nécessaire
            go_term = GOTerm(r.GO, godag)
            go_term.pvalue = r.p_fdr_bh
            go_term.overrepresented = True
            overrepresented_terms.add(go_term)

    return overrepresented_terms