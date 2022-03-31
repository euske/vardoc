#!/usr/bin/env python
import sys

def main(argv):
    import fileinput
    projs = {}
    vals = {}
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
        if not text: continue
        if name in vals:
            d = vals[name]
        else:
            d = vals[name] = {}
        d[(typ,proj)] = text
    print(projs)
    threshold = len(projs)//5
    for (name,d) in sorted(vals.items(), key=lambda x:len(x[1]), reverse=True):
        if len(d) < threshold: continue
        print(f'{len(d)} {name}')
        for ((typ,proj),text) in d.items():
            print(f'  {proj} {text}')
    return 0

if __name__ == '__main__': sys.exit(main(sys.argv))
