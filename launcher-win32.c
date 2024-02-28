#include <jni.h>

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#include "angiotool_jar.h"

#define bail(message) { MessageBoxA(NULL, message, "Error", MB_OK); return 1; }
#define IS_FOLDER(bits) (bits) != INVALID_FILE_ATTRIBUTES && ((bits) & FILE_ATTRIBUTE_DIRECTORY)

#define PATH_SIZE 1024
#define MAIN_CLASS_NAME "AngioTool/AngioTool"
#define MAIN_METHOD_NAME "main"

BOOL addString(char *str, int *strPos, int strSize, const char *toAdd);
BOOL stripFromLast(char *str, int *strPos, char ch);
BOOL checkIsFolder(char *path);

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PSTR lpCmdLine, int nCmdShow) {
    BOOL (WINAPI *func_SetProcessDPIAware)() = GetProcAddress(GetModuleHandle("user32.dll"), "SetProcessDPIAware");
    if (func_SetProcessDPIAware != NULL)
        func_SetProcessDPIAware();

    char path[PATH_SIZE];
    int pos = 0;
    addString(path, &pos, PATH_SIZE, "C:\\Program Files\\Java");
    if (!checkIsFolder(path)) {
        stripFromLast(path, &pos, '\\');
        addString(path, &pos, PATH_SIZE, " (x86)\\Java");
        if (!checkIsFolder(path))
            bail("Could not find path to Java installation");
    }

    addString(path, &pos, PATH_SIZE, "\\*");

    WIN32_FIND_DATAA pathInfo = {0};
    HANDLE javaFolderHandle = FindFirstFileA(path, &pathInfo);
    if (javaFolderHandle == INVALID_HANDLE_VALUE)
        bail("Java installation was empty");

    int highestVersion = 0;
    char javaVersion[PATH_SIZE];
    javaVersion[0] = 0;
    while (TRUE) {
        if (IS_FOLDER(pathInfo.dwFileAttributes)) {
            int n = 0;
            char *p = pathInfo.cFileName;
            char prev = 0;
            BOOL stoppedAdding = FALSE;
            while (*p) {
                char c = *p;
                if (!stoppedAdding && c >= '0' && c <= '9') {
                    BOOL skip = c == '1' && (prev < '0' || prev > '9') && p[1] && (p[1] < '0' || p[1] > '9');
                    if (!skip)
                        n = n * 10 + (c - '0');
                }
                else if (n > 0) {
                    stoppedAdding = TRUE;
                }
                prev = c;
                p++;
            }
            int len = p - pathInfo.cFileName;
            if (n >= highestVersion && len < PATH_SIZE) {
                highestVersion = n;
                for (int i = 0; i < len; i++)
                    javaVersion[i] = pathInfo.cFileName[i];
                javaVersion[len] = 0;
            }
        }

        if (!FindNextFileA(javaFolderHandle, &pathInfo))
            break;
    }

    CloseHandle(javaFolderHandle);

    if (javaVersion[0] == 0)
        bail("Could not find Java version in Java installation path");

    path[--pos] = 0;

    BOOL isPathBuilt = addString(path, &pos, PATH_SIZE, javaVersion);
    if (isPathBuilt)
        isPathBuilt = addString(path, &pos, PATH_SIZE, "\\bin\\server\\jvm.dll");

    if (!isPathBuilt)
        bail("Failed to build path to jvm.dll");

    HMODULE javaServer = LoadLibraryA(path);

    if (javaServer == NULL) {
        stripFromLast(path, &pos, '\\');
        stripFromLast(path, &pos, '\\');
        addString(path, &pos, PATH_SIZE, "\\client\\jvm.dll");
        javaServer = LoadLibraryA(path);
    }

    if (javaServer == NULL) {
        stripFromLast(path, &pos, '\\');
        stripFromLast(path, &pos, '\\');
        addString(path, &pos, PATH_SIZE, "\\jvm.dll");
        javaServer = LoadLibraryA(path);
    }

    if (javaServer == NULL) {
        stripFromLast(path, &pos, '\\');
        char errorMsg[PATH_SIZE + 128];
        int errorPos = 0;
        addString(errorMsg, &errorPos, 128, "Failed to open server\\jvm.dll, client\\jvm.dll or jvm.dll in ");
        addString(errorMsg, &errorPos, PATH_SIZE + 128, path);
        bail(errorMsg);
    }

    jint (*func_JNI_CreateJavaVM)(JavaVM**, void**, JavaVMInitArgs*) = GetProcAddress(javaServer, "JNI_CreateJavaVM");
    if (func_JNI_CreateJavaVM == NULL)
        bail("Failed to obtain function pointer to JNI_CreateJavaVM");

    path[0] = 0;
    {
        DWORD size = GetTempPathA(PATH_SIZE, path);
        pos = (int)size;
    }
    if (pos <= 0)
        bail("Failed to obtain temporary path");

    addString(path, &pos, PATH_SIZE, "AngioTool-Batch");
    if (!checkIsFolder(path)) {
        if (!CreateDirectoryA(path, NULL))
            bail("Failed to create temporary directory");
    }

    addString(path, &pos, PATH_SIZE, "\\AngioTool-Batch.jar");
    HANDLE jarFileHandle = CreateFileA(path, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    if (jarFileHandle != INVALID_HANDLE_VALUE) {
        DWORD filePos = 0;
        while (TRUE) {
            if (!WriteFile(jarFileHandle, AngioTool_Batch_jar, AngioTool_Batch_jar_len - filePos, &filePos, NULL))
                bail("Failed to write to temporary .jar");
            if (filePos >= AngioTool_Batch_jar_len)
                break;
        }

        CloseHandle(jarFileHandle);
    }
    /*
    else {
        bail("Failed to create temporary .jar in temp directory");
    }
    */

    char javaClassPath[PATH_SIZE];
    int javaClassPathPos = 0;
    addString(javaClassPath, &javaClassPathPos, PATH_SIZE, "-Djava.class.path=");
    for (int i = 0; i < pos; i++)
        javaClassPath[javaClassPathPos++] = path[i]; // == '\\' ? '/' : path[i];
    //javaClassPath[javaClassPathPos++] = '\\';
    javaClassPath[javaClassPathPos] = 0;

    JavaVMOption jvmOption = {0};
    jvmOption.optionString = &javaClassPath[0];

    JavaVMInitArgs vmArgs = {0};
    vmArgs.version  = JNI_VERSION_1_8;
    vmArgs.nOptions = 1;
    vmArgs.options  = &jvmOption;

    JavaVM *vm;
    JNIEnv *env;
    jint res = func_JNI_CreateJavaVM(&vm, (void **)&env, &vmArgs);
    if (res != JNI_OK)
        bail("Failed to create Java VM");

    jclass mainClass = (*env)->FindClass(env, MAIN_CLASS_NAME);
    if (mainClass == NULL)
        bail("Failed to find " MAIN_CLASS_NAME " class"); // (javaClassPath);

    jmethodID mainMethodId = (*env)->GetStaticMethodID(env, mainClass, MAIN_METHOD_NAME, "([Ljava/lang/String;)V");
    if (mainMethodId == NULL)
        bail("Failed to find " MAIN_METHOD_NAME " function");

    jobjectArray mainArgs = (*env)->NewObjectArray(env, 0, (*env)->FindClass(env, "java/lang/String"), NULL);
    if (mainArgs == NULL)
        bail("Failed to construct main args");

    (*env)->CallStaticVoidMethod(env, mainClass, mainMethodId, mainArgs);

    (*vm)->DestroyJavaVM(vm);
    return 0;
}

BOOL addString(char *str, int *strPos, int strSize, const char *toAdd) {
    int pos = *strPos;
    int lenToAdd = strlen(toAdd);
    if (pos + lenToAdd >= strSize)
        return FALSE;

    for (int i = 0; i < lenToAdd; i++)
        str[pos+i] = toAdd[i];

    pos += lenToAdd;
    str[pos] = 0;
    *strPos = pos;
    return TRUE;
}

BOOL stripFromLast(char *str, int *strPos, char ch) {
    int pos = *strPos;
    int i;
    for (i = pos-1; i >= 0; i--) {
        if (str[i] == ch)
            break;
    }
    if (i < 0)
        return FALSE;

    str[i] = 0;
    *strPos = i;
    return TRUE;
}

BOOL checkIsFolder(char *path) {
    DWORD bits = GetFileAttributesA(path);
    return IS_FOLDER(bits);
}
