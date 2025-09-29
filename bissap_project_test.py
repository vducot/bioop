# test.py
from annot import build_data

GO_OBO_FILE = "go-basic.obo"
GAF_FILE = "goa_human.gaf"
PROTEINS_FILE = r"datasets\dataset04.csv"
ASSOC_PICKLE = "assoc.pkl"
FDR_THRESHOLD = 0.05

def summarize_per_element(elements, include_ancestors=False):
    print("\n=== Per-element GO summary ===\n")

    for elem in elements:
        direct_go = elem.goterms
        if include_ancestors:
            total_go = set(direct_go)
            for go in direct_go:
                total_go.update(go.parent)
        else:
            total_go = set(direct_go)

        overrep_go = [go for go in total_go if go.overrepresented]

        print(f"Element: {elem.name}")
        print(f"  # Direct GO: {len(direct_go)}")
        print(f"  # Total GO: {len(total_go)}")
        print(f"  Direct GO terms: {', '.join(go.term for go in direct_go)}")
        print(f"  Overrepresented GO terms ({len(overrep_go)}): {', '.join(go.term for go in overrep_go)}\n")

def summarize_global(elements):
    print("\n=== Global GO summary ===\n")
    all_go = {go for elem in elements for go in elem.goterms}
    overrep_go = [go for go in all_go if go.overrepresented]

    print(f"Total GO terms found: {len(all_go)}")
    print(f"Total overrepresented GO terms: {len(overrep_go)}\n")

    if overrep_go:
        print("Top overrepresented GO terms (sorted by FDR, up to 10):")
        for go in sorted(overrep_go, key=lambda g: g.fdr)[:10]:
            print(f"- {go.term} | NS: {go.namespace} | FDR: {go.fdr:.3e} | Covered elements: {len(go.cover_elements)}")
    print("\n")


# Main execution
if __name__ == "__main__":
    print("Building GO data...")
    elements, godag, overrep_terms = build_data(
        GO_OBO_FILE, GAF_FILE, PROTEINS_FILE, ASSOC_PICKLE, fdr_threshold=FDR_THRESHOLD
    )
    # Summary per element
    summarize_per_element(elements, include_ancestors=True)
    # Global summary
    summarize_global(elements)