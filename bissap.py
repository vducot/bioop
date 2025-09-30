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
import pickle
import zipfile
from pathlib import Path

MATRIX_FILE = 'whole_annotation_genome.zip'

def main(args):
    elements = annot.read_elements(args.elements_file)
    # Check if the whole annotation matrix has already been computed
    annotMatrix = None
    if Path.exists(Path(MATRIX_FILE)):
        if zipfile.is_zipfile(MATRIX_FILE):
            print("Zip file found")
            zf = zipfile.ZipFile(MATRIX_FILE, 'r')
            annotMatrix = pickle.loads(zf.open('wag.matrix').read())
                #zip.extractall()
        else:
            print(MATRIX_FILE+" is not a zip file")
            with open(MATRIX_FILE, 'rb') as fin:
                # Load in memory
                annotMatrix = pickle.load(MATRIX_FILE)
    else:
        # Calculate the matrix
        print("Calculating the whole genome annotation matrix")
        annotMatrix = algo.AnnotMatrix(args.gaf_file)
        algo.AnnotMatrix.dump_to_file(annotMatrix, MATRIX_FILE)
        

    # print("Object loaded, type "+str(type(annotMatrix)))
    # wag = annotMatrix.matrix
    # print("Matrix loaded, type "+str(type(wag)))
    # print("Matrix shape "+str(wag.shape))
    annot_summary = algo.run_algo(elements, annotMatrix)
    

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
