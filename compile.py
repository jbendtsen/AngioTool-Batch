#!/usr/bin/python3

import os
import sys
import shutil
import zipfile
import subprocess

JAVA = "java"
JAVAC = "javac"
#JAVA = "/usr/lib/jvm/java-8-openjdk/bin/java"
#JAVAC = "/usr/lib/jvm/java-8-openjdk/bin/javac" 
#JAVA = "/usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/java"
#JAVAC = "/usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/javac"

if shutil.which(JAVAC) is None:
    print("Could not find '" + JAVAC + "'.")
    print("Make sure you have installed a Java Development Kit (not just the runtime)")
    print("and that '" + JAVAC + "' is on your PATH variable.")
    sys.exit(1)

simple_points_lut = None
for i in range(2):
    try:
        with open("lee94-simple-points.bin", "rb") as f:
            buf = f.read()
            if len(buf) == (1 << 23):
                simple_points_lut = buf
    except:
        pass

    if simple_points_lut or i == 1:
        break

    status = subprocess.run([JAVA, "GenerateLee94SimplePoints.java"])
    if status.returncode != 0:
        sys.exit(status.returncode)

if not simple_points_lut:
    print("Could not read lookup tables (ie. lee94-simple-points.bin)")
    sys.exit(1)

for src_folder in os.listdir("source"):
    if not os.path.isdir("source/" + src_folder):
        continue
    if src_folder[0] == "." or src_folder == "deprecated" or src_folder == "images" or src_folder == "META-INF":
        continue
    path = "source/" + src_folder + "/"
    for fname in os.listdir(path):
        if not fname.endswith(".tj"):
            continue
        with open(path + fname) as f:
            lines = f.read().splitlines()
        params = []
        makelist = []
        content = ""
        for line in lines:
            l = line.strip()
            if l.startswith("~param"):
                params.append(" ".join(l.split(" ")[1:]))
            elif l.startswith("~make"):
                p = " ".join(l.split(" ")[1:])
                makelist.append(p.split(","))
            else:
                content += line + "\n"
        for instance in makelist:
            text = content
            outname = instance[0]
            idx = 0
            while idx < len(params):
                text = text.replace("<" + params[idx] + ">", instance[idx+1])
                idx += 1
            with open(path + outname, "w") as f:
                f.write(text)

sep = ";" if os.name == "nt" else ":"
libs = []
libs_arg = "."
if os.path.isdir("source/lib"):
    libs = os.listdir("source/lib")
    if libs:
        libs_arg += sep + sep.join(["lib/" + f for f in libs])

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
        if name == "manual.html" or name.startswith("images") or name.startswith("META-INF"):
            asset_files.append(name)

stubbed_java = []
for root, dirs, files in os.walk("stubs"):
    for f in files:
        name = os.path.join(root, f)
        if f.endswith(".java"):
            stubbed_java.append(os.path.join("..", name))

with open("source/sources.txt", "w") as f:
    f.write("\n".join(java_files + stubbed_java))

status = subprocess.run([JAVAC, "--release=8", "-Xmaxerrs", "1000", "-d", "../build", "-cp", libs_arg, "@sources.txt"], cwd="source")
if status.returncode != 0:
    sys.exit(status.returncode)

for f in libs:
    with zipfile.ZipFile("source/lib/" + f) as zip:
        zip.extractall(path="build")

build_files = []
for root, dirs, files in os.walk("build"):
    if root.endswith("META-INF"):
        continue
    parts = root.split("/")
    if len(parts) == 1:
        parts = root.split("\\")
    if len(parts) > 1 and parts[1] == "ij":
        continue
    for f in files:
        build_files.append(os.path.join(root, f))

try:
    os.remove("AngioTool2.jar")
except:
    pass

with zipfile.ZipFile("AngioTool2.jar", compression=zipfile.ZIP_DEFLATED, compresslevel=6, mode="w") as zip:
    for f in asset_files:
        zip.write("source/" + f, arcname=f)
    for f in build_files:
        zip.write(f, arcname=f[6:])
    zip.writestr("lee94-simple-points.bin", simple_points_lut)
    zip.write("plugins.config")

# java -Dsun.java2d.uiScale=2 -jar AngioTool2.jar
