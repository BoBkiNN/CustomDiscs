import yaml

f = open("config.yml")
text = f.read()
f.close()

data: dict = yaml.full_load(text)

discs: list[str] = data["discs"]
names: list[str] = data["names"]
new = dict(data)
new.pop("names")
new_discs: list[dict] = []
i = 0
for r in discs:
    try:
        dd = r.split("=")
        it = dd[0]
        s = dd[1]
        cmd = int(dd[2])
        rn: str = names[i]
        n = rn.split("=")[1]
        nd = {"sound": s,
              "name": n,
              "item": it,
              "cmd": cmd}
        print(f"Converted {i} {nd}")
        new_discs.append(nd)
    except:
        print(f"Failed convert {i}")
    i+=1
new["discs"] = new_discs
f = open("config-old.yml", "w")
f.write(text)
f.close()
f = open("config.yml", "w")
f.write(yaml.dump(new, sort_keys=False, allow_unicode=True))
f.close()