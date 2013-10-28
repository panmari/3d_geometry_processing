from scipy.sparse import csr_matrix
import scipy.sparse.linalg as sp
import scipy.linalg as la
import numpy as np

import sys, getopt

def readArray(array_file):
    "returns the 1D array contained iin the file"
    f= open(array_file, "r")
    b= []
    for line in f:
        b.append(float(line))
    f.close()
    return np.array(b)


def doSymEigs(argv):
    #parse the commandline input
    file_i = ''
    file_j = ''
    file_val = ''
    file_x_out = ''
    k = 0
    try:
        opts, args = getopt.getopt(argv, "i:j:v:o:k:",
                                   ["ifile=","jfile=",
                                   "valfile=",
                                   "outPrefix=", "k="])
    except getopt.GetoptError:
        print 'doSparseEig.py -i <inputfile> -j <inputfile> -v <inputfile> -o <outputfile> -k <numEvs>'
        sys.exit(2)
    
    for opt, arg in opts:
        if opt in ("-i", "--ifile"):
            file_i = arg
        elif opt in ("-j", "--jfile"):
            file_j = arg
        elif opt in ("-o", "--outPrefix"):
            file_x_out = arg
        elif opt in ("-v", "--valfile"):
            file_val = arg
        elif opt in ("-k", "--k"):
            k = int(arg)

    vecfile = file_x_out
    vecfile += '_vecs'
    valfile = file_x_out + '_vals'
    
    print "out: %s, %s\n" % (vecfile, valfile)
    #read in the specified files
    rows = readArray(file_i)
    cols = readArray(file_j)
    vals = readArray(file_val)
    
    #do the work
    ij = [rows,cols]
    mat = csr_matrix((vals,ij))
    
    #vals, vecs = sp.eigsh(mat,k, sigma = 0, which = 'LM', maxiter = 1000)
    #vals, vecs = sp.eigsh(mat,k, which = 'SM', maxiter = 1000)
    vals, vecs = la.eigh(mat.toarray())
    
    ix = np.argsort(abs(vals))
    vals = vals[ix]
    vecs = vecs[:,ix]
    
    #write out result
    f = open(vecfile, 'w')
    for columns in vecs.T:
        for x_ij in columns:
            f.write('%f ' %(x_ij))
        f.write('\n')
    f.close()
    
    f = open(valfile, 'w')
    for xi in vals:
        f.write('%f \n' %(xi))
    f.close()
    
    print 'Evd: success!'

    
if __name__ == '__main__':
    doSymEigs(sys.argv[1:])