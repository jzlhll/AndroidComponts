package com.allan.mydroid.api;

import sp.EncryptString;
import sp.StringEncrypt;

@EncryptString
public class WSApisConst2 {
    public static final String PROCESS_CHUNK = sp.StringEncrypt.decrypt("rZVmjTNppHhSDpMILas8zz0gHH8r+XujQjIYF1TjBh8a") /*Encrypted: chunk*/;
    public static final String PROCESS_MERGING = StringEncrypt.decrypt("Rrv6vnEOOXp/CknbXXXYgJAw9MqH51MgHxPD8Bh9gFkfDbg=") /*Encrypted: merging*/;
    public static final String PROCESS_ABORTED = StringEncrypt.decrypt("K7Kv4VUqN9ufSGnFL+lW8By8gNNZZWMh3KdEVo2tSyu0cTY=") /*Encrypted: aborted*/;
    public static final String PROCESS_COMPLETED = StringEncrypt.decrypt("7vsfCBRYdVC9C7kfkv03q/WIKBQnE2B4xHOe+UMUMXM0I/VXZw==") /*Encrypted: completed*/;
    public static final String PROCESS_CHUNK_ERROR = StringEncrypt.decrypt("UDSlOtU9STNCSNwW1FkK5sGmehDsbGGl5xVPsadklWwNCZLRBTc=") /*Encrypted: chunkError*/;
    public static final String PROCESS_MERGE_ERROR = StringEncrypt.decrypt("V6hp8+7164r8NgoRBlzWS/WrErBevFUqLyBsNZoFTzqqZT32sBE=") /*Encrypted: mergeError*/;
}