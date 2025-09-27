###
# Main file
# M2 Bio-info 2025 - OOP - Project 1
# Gwendoline & Vincent
###

import argparse
import textwrap
import annot
import algo
import os

MATRIX_FILE = 'whole_annotation_genome.dump'

def main(args):
    elements = annot.read_elements(args.elements_file)
    # Check if the whole annotation matrix has already been computed
    try:
        with open(MATRIX_FILE, 'r'):
            # Load in memory
            pass
    except:
        # Calculate the matrix
        wag = algo.AnnotMatrix(args.gaf_file)

    annot_summary = algo.run_algo(elements, wag)
    

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
                prog='python bissap.py',
                formatter_class=argparse.RawDescriptionHelpFormatter,
                description=textwrap.dedent('''\
                        ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCG
                        #          BISSAP Project              #
                        #   Gwendoline Iborra & Vincent Ducot  #
                        #        M2 Bio-Info - 2025            #
                        ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCG'''))

    parser.add_argument('elements_file', help='Filename of elements (genes or protein) list')
    parser.add_argument('--gaf_file', action='store_true', help='GAF file blabla', default="files/goa_human.gaf")
    parser.add_argument('--obo_file', action='store_true', help='Like a hobo', default="files/go-basic.obo")
    parser.add_argument('--threshold', action='store_true', default=False,
                        help='Some threshold')
    args = parser.parse_args()
    main(args)
