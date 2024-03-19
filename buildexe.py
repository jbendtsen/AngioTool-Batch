#!/usr/bin/python3

import os
import sys
import shutil
import pathlib
import subprocess

WINDRES_64 = "/usr/bin/x86_64-w64-mingw32-windres"
WINDRES_32 = "/usr/bin/i686-w64-mingw32-windres"
JAVA_JNI_PATH = "/usr/lib/jvm/java-21-openjdk/include"
JAVA_JNI_MD_PATH = "/usr/lib/jvm/java-21-openjdk/include/windows"

errors = []
jar_file = None
try:
    with open("AngioTool-Batch.jar", "rb") as f:
        jar_file = f.read()
except:
    pass
if not jar_file:
    errors.append("Could not open AngioTool-Batch.jar. Try running ./compile.py first.")

if shutil.which(WINDRES_32) is None:
    errors.append("Could not find " + WINDRES_32)
if shutil.which(WINDRES_64) is None:
    errors.append("Could not find " + WINDRES_64)
if not os.path.isdir(JAVA_JNI_PATH):
    errors.append("Could not find JNI include path " + JAVA_JNI_PATH)
if not os.path.isdir(JAVA_JNI_MD_PATH):
    errors.append("Could not find JNI machine deps include path " + JAVA_JNI_MD_PATH)

tcc_path = None
if len(sys.argv) < 2:
    errors.append("Path to TCC is required, eg ./buildexe.py ~/Downloads/tinycc")
else:
    if not os.path.isdir(sys.argv[1]):
        errors.append("Could not find path to TCC \"" + sys.argv[1] + "\"")
    else:
        tcc_path = sys.argv[1].replace("~", str(pathlib.Path.home())) + "/"

if errors:
    print("\n".join(errors))
    sys.exit(1)

print("Generating angiotool_jar.h...")

hex_offsets = [0x30 if i < 10 else 0x57 for i in range(16)]
space_char  = [0x0a if i == 15 else 0x20 for i in range(16)]

data_header = bytearray(len(jar_file) * 6)
i = -1
while i < len(jar_file) - 1:
    i += 1
    h = (jar_file[i] >> 4) & 0xf
    l = jar_file[i] & 0xf
    data_header[6*i] = 0x30
    data_header[6*i+1] = 0x78
    data_header[6*i+2] = h + hex_offsets[h]
    data_header[6*i+3] = l + hex_offsets[l]
    data_header[6*i+4] = 0x2c
    data_header[6*i+5] = space_char[i & 15]

print("Writing angiotool_jar.h...")

with open("angiotool_jar.h", "wb") as f:
    f.write(bytes("static unsigned char AngioTool_Batch_jar[] = {\n", "utf8"))
    f.write(data_header)
    f.write(bytes("\n};\nstatic unsigned int AngioTool_Batch_jar_len = " + str(len(jar_file)) + ";\n", "utf8"))

print("Building EXE resource...")

os.system("echo \"this ICON images/ATIcon20.ico\" | " + WINDRES_32 + " -J rc -o launcher32.coff")
os.system("echo \"this ICON images/ATIcon20.ico\" | " + WINDRES_64 + " -J rc -o launcher64.coff")

print("Compiling...")

compile_args = [
    "-I" + tcc_path + "include",
    "-I" + tcc_path + "win32/include",
    "-I" + tcc_path + "win32/include/winapi",
    "-I" + JAVA_JNI_PATH,
    "-I" + JAVA_JNI_MD_PATH,
    "-L" + tcc_path,
    "-L" + tcc_path + "win32/lib",
    "-Wl,-subsystem=windows",
    "launcher-win32.c",
    "-luser32"
]

subprocess.run([tcc_path + "i386-win32-tcc.exe",   "-m32"] + compile_args + ["launcher32.coff", "-o", "AngioTool-Batch-32.exe"])
subprocess.run([tcc_path + "x86_64-win32-tcc.exe", "-m64"] + compile_args + ["launcher64.coff", "-o", "AngioTool-Batch-64.exe"])
