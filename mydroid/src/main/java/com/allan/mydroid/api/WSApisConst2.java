package com.allan.mydroid.api;

import com.au.annos.EncryptString;

@EncryptString
public class WSApisConst2 {
    public static final String PROCESS_CHUNK = "chunk";
    public static final String PROCESS_MERGING = "merging";
    public static final String PROCESS_ABORTED = "aborted";
    public static final String PROCESS_COMPLETED = "completed";
    public static final String PROCESS_CHUNK_ERROR = "chunkError";
    public static final String PROCESS_MERGE_ERROR = "mergeError";

    public final String testFinal = "has empty end ";
    private String testNormal = " has empty open";
    String testPackage = "package";

    public static final String encypt = com.au.stringprotect.StringCrypto.decrypt("", "");

    public void testFunc() {
        String strInFun = "string in function";
        var stringVar = "String var";
    }
}
