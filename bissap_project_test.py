from goatools.obo_parser import GODag
from goatools.associations import read_gaf
#from goatools.anno.gaf_reader import GafReader #j'arrive pas à le faire fonctionner
from goatools.go_enrichment import GOEnrichmentStudy
import math
import os
#pour la visualisation
from goatools.gosubdag.gosubdag import GoSubDag
from goatools.godag_plot import plot_gos #plot_gos_pdf est censé exister

# parameters
GO_OBO_FILE = "go-basic.obo" # fichier obo pour goatools
GAF_FILE = "goa_human.gaf" # fichier gaf de l'humain
PROTEINS_L = ["P06132","P08397","P10746","P13196","P13716","P22557","P22830","P36551","P50336","Q7KZN9","Q12887"] #liste des protéines d'intérêt
FDR_THRESHOLD = 0.05 #comment choisir ce seuil?

print("Chargement de l'ontologie GO...")
go_dag = GODag(GO_OBO_FILE)
#print(go_dag)
print("Lecture du fichier GAF...")
assoc = read_gaf(GAF_FILE)
print(assoc)

# étape1 : test de surreprésentation
#de ce que j'ai compris: GOEnrichmentStudy prépare le test avec background + associations GO + hiérarchie
goea_obj = GOEnrichmentStudy(
    list(assoc.keys()),  # toutes les protéines annotées dans le GAF
    assoc,               # dictionnaire protéine -> set(GO)
    go_dag,
    methods=["fdr_bh"] #quel méthode de correction utiliser ? (fdr_bh, bonferroni, holm, sidak, none)
    #pvalcalc='fisher_scipy_stats' # methode de calcul du p-value par défaut #aucune idée de ce que ça change
)
# puis run_study(PROTEINS_L) teste chaque GO pour savoir s’il est surreprésenté dans le set d’intérêt
print(goea_obj)

results = goea_obj.run_study(PROTEINS_L)

# étape2 : selection des candidats
#dans la doc:
#r.GO          # ID du GO term
#r.name        # nom du terme GO
#r.p_uncorrected  # p-value brute
#r.p_fdr_bh       # p-value corrigée FDR
#r.study_count    # nombre de protéines de ton set d’intérêt annotées avec ce GO
#r.pop_count      # nombre de protéines du background annotées avec ce GO
candidats = [r.GO for r in results if r.p_fdr_bh < FDR_THRESHOLD]
#print(candidats)
candidats_info = [(r.GO, r.name, r.p_fdr_bh) for r in results if r.p_fdr_bh < FDR_THRESHOLD]
#print(candidats_info)

#test visalisation
#gosubdag = GoSubDag(candidats, go_dag)
#go_ids = list(gosubdag.keys())
#plot_gos("candidats_GO.png", go_ids, go_dag)
#plot_gos("candidats_GO.png", candidats, go_dag)
#ça marche pas, je sais pas pourquoi


# faire dic des protéines associées à chaque GO
#ajouter le label de surassociation

#utilise les excels à la con 