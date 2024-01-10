#!/usr/bin/python3

import os
import sys
import shutil
import zipfile
import subprocess

JAVAC = "javac"

sep = ";" if os.name == "nt" else ":"
libs = os.listdir("source/lib")
libs_arg = "." + sep + sep.join(["lib/" + f for f in libs])

if os.path.exists("build"):
    shutil.rmtree("build")

os.mkdir("build")

java_files = []
asset_files = []
for root, dirs, files in os.walk("source"):
    for f in files:
        name = os.path.join(root, f)[7:]
        if f.endswith(".java") and not name.startswith("deprecated"):
            java_files.append(name)
        if name.startswith("images/") or name.startswith("icons") or name.startswith("META-INF") or name.startswith("doc"):
            asset_files.append(name)

with open("source/sources.txt", "w") as f:
    f.write("\n".join(java_files))

status = subprocess.run([JAVAC, "-source", "8", "-target", "8", "-d", "../build", "-cp", libs_arg, "@sources.txt"], cwd="source")
if status.returncode != 0:
    sys.exit(status.returncode)

for f in libs:
    with zipfile.ZipFile("source/lib/" + f) as zip:
        zip.extractall(path="build")

build_files = []
for root, dirs, files in os.walk("build"):
    if root.endswith("META-INF"):
        continue
    for f in files:
        build_files.append(os.path.join(root, f))

try:
    os.remove("AngioTool-Batch.jar")
except:
    pass

with zipfile.ZipFile("AngioTool-Batch.jar", compression=zipfile.ZIP_DEFLATED, compresslevel=4, mode="w") as zip:
    for f in asset_files:
        zip.write("source/" + f, arcname=f)
    for f in build_files:
        zip.write(f, arcname=f[6:])

# java -Dsun.java2d.uiScale=2 -jar AngioTool-Batch.jar
