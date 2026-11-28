import re,sys,glob,collections,statistics as st
def region(x,z):
    if x>=8000: return "warmup"
    if abs(x+352)<=400 and abs(z-1616)<=400: return "snowy"
    return "far"
def wall(tr):
    # per region: time from forceload add step to the last await of that region
    steps=re.findall(r"> (.*?)\n(?:.*\n)*?# step \d+ \((?:command|await)\): ([\d.]+)s",tr)
    return None
def transcript_times(path):
    txt=open(path).read()
    blocks=re.split(r"\n(?=> )",txt)
    out=[];cur=None;total=collections.OrderedDict();name=None;n=0
    # walk chunks: a command block ends with '# step N (command): Ts'
    for b in blocks:
        m=re.search(r"# step \d+ \(\w+\): ([\d.]+)s",b)
        cmd=b.split("\n")[0]
        if "forceload add" in cmd:
            n+=1;cur=0.0;name=f"r{n}"
        if cur is not None and m and "forceload remove" not in cmd and "locate" not in cmd:
            cur+=float(m.group(1))
        if "forceload remove" in cmd and cur is not None:
            total[name]=cur;cur=None
    return list(total.values())
def csv(path):
    rows=[l.rstrip("\n").split(",") for l in open(path)]
    return rows
modes=["on","off","headonly"]
print("== wall seconds per region (warmup, snowy, far-ne, far-nw, far-se, far-sw) ==")
for m in modes:
    for r in (1,2):
        t=transcript_times(f"{m}-{r}.transcript.txt")
        print(m,r,[round(x,1) for x in t],"sum excl warmup",round(sum(t[1:]),1))
print()
for m in ("on","headonly"):
  for r in (1,2):
    rows=csv(f"{m}-{r}.csv")
    H=[x for x in rows if x[2]=="H"]; R=[x for x in rows if x[2]=="R"]
    print(f"== {m}-{r}: place calls(H)={len(H)}, R records={len(R)}")
    for reg in ("warmup","snowy","far"):
        h=[x for x in H if region(int(x[3]),int(x[4]))==reg]
        if not h: continue
        skip=sum(1 for x in h if x[6]=="1")
        hn=[int(x[5]) for x in h]
        print(f"  {reg}: H n={len(h)} skipped(no snow nearby)={skip} ({100*skip/len(h):.0f}%) head ms total={sum(hn)/1e6:.1f} mean={sum(hn)/len(hn)/1e3:.0f}us")
        rr=[x for x in R if region(int(x[3]),int(x[4]))==reg]
        c=collections.Counter(x[8] for x in rr)
        print("    R stages:",dict(c))
        for stage in ("EARLY","NOBOTH","FULL"):
            s=[x for x in rr if x[8]==stage]
            if s:
                b=sum(int(x[5]) for x in s);sm=sum(int(x[6]) for x in s);ps=sum(int(x[7]) for x in s)
                print(f"    {stage}: n={len(s)} boundary ms={b/1e6:.1f} sampling ms={sm/1e6:.1f} passes ms={ps/1e6:.1f} mean total us={(b+sm+ps)/len(s)/1e3:.0f}")
        tot=sum(hn)+sum(int(x[5])+int(x[6])+int(x[7]) for x in rr)
        print(f"    mixin total ms={tot/1e6:.1f}")
