#!/usr/bin/env python
import sys
import gensim

def main(argv):
    import fileinput
    projs = {}
    vals = []
    for line in fileinput.input():
        if not line.startswith('+'): continue
        line = line[1:].strip()
        (typ,_,line) = line.partition(' ')
        if typ != 'FIELD': continue
        (path,_,line) = line.partition(' ')
        (proj,_,path) = path.partition('/')
        (loc,_,line) = line.partition(' ')
        (name,_,line) = line.partition(' ')
        projs[proj] = projs.get(proj,0) + 1
        name = name.lower()
        text = line
        tokens = gensim.utils.simple_preprocess(text)
        if not tokens: continue
        vals.append( ((typ,proj,name),tokens) )
    print(projs)
    dictionary = gensim.corpora.Dictionary( tokens for (_,tokens) in vals )
    print(dictionary)
    corpus = [ dictionary.doc2bow(tokens) for (_,tokens) in vals ]
    index = gensim.similarities.MatrixSimilarity(corpus)
    print(index)
    threshold = 0.80
    for (i,vec_bow) in enumerate(corpus):
        ((typ0,proj0,name0),tokens0) = vals[i]
        sims = [ (j,score) for (j,score) in enumerate(index[vec_bow])
                 if threshold < score and vals[j][0][1] != proj0 ]
        if not sims: continue
        sims = sorted(sims, key=lambda item: -item[1])
        print(f'** {typ0}:{proj0}/{name0} {" ".join(tokens0)}')
        for (j, score) in sims[:10]:
            ((typ1,proj1,name1),tokens1) = vals[j]
            print(f'  {score} {typ1}:{proj1}/{name1} {" ".join(tokens1)}')
        print()
    return 0

if __name__ == '__main__': sys.exit(main(sys.argv))
