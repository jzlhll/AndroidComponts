package com.allan.mydroid.api

import sp.EncryptString
import sp.StringEncrypt

@EncryptString
class WSApisConst {
    companion object {
val API_WS_SEND_FILE_LIST = /*Encrypted: s_sendFileList*/ sp.StringEncrypt.decrypt("7K2mwJcAxrhPptB/T+GfftZk82muEErYXjd9WIE9THMCFPYN0Vh+PlVF")
val API_WS_LEFT_SPACE = /*Encrypted: s_leftSpace*/ sp.StringEncrypt.decrypt("+WuJaIy+QeWuq0nmyHrXxCyRf1vtWieganG0KZ81uI2E0WBVcQ9f")
val API_WS_CLIENT_INIT_CALLBACK = /*Encrypted: s_clientInitBack*/ sp.StringEncrypt.decrypt("b2KcERESo3BdC5Sjlj+ye+x3iTuoNEV6Z2QgtIm4SWXKQFQ19eekH/6X0aM=")

val API_WS_SEND_FILE_CHUNK = /*Encrypted: s_sendFileChunk*/ sp.StringEncrypt.decrypt("Fb/NnKf6qrVlYO2bE++GBlDIwQKkBuA3ZRCZYyjO7ZyXsVf14nihD6zkpA==")
val API_WS_SEND_SMALL_FILE_CHUNK = /*Encrypted: s_sendSmallFileChunk*/ sp.StringEncrypt.decrypt("lAuzByeYrTNEieEmFMhSrAqejemY68pIMH+VVN6+UY8moYil/RBG76ewmOVQz4Gg")

val API_WS_SEND_FILE_NOT_EXIST = /*Encrypted: s_sendFileNotExist*/ sp.StringEncrypt.decrypt("nZ+2oGKcxGCMwGUIltnCEs8GRbgLxwUz4Ivq4WAzyBCxhMfovpeyY0j61WUY0w==")

val API_WS_INIT = /*Encrypted: c_wsInit*/ sp.StringEncrypt.decrypt("qSsu5EQIKjb86D+hEJ5ynu1MmUI7HWdq+3jMrxKvY+VC4C9+")
val API_WS_PING = /*Encrypted: c_ping*/ sp.StringEncrypt.decrypt("VZb1Tu+UrT2TZ71DczpD1ZAfdcf3wRPAIjnxXGVqo4iW5w==")
val API_WS_REQUEST_FILE = /*Encrypted: c_requestFile*/ sp.StringEncrypt.decrypt("PdydqXWqax71+62PZ2wrKDKN81A354yw1V3o25egFWl4P8rzXc7IrvQ=")
val API_WS_FILE_DOWNLOAD_COMPLETE = /*Encrypted: c_downloadFileComplete*/ sp.StringEncrypt.decrypt("NdqwLWhHHKWOFbVA8Oy5u1S5qDTLKYUAF+NZvOMWQLXlDlkI7CnRnJ0M7FG/ti6wVKo=")

        //textChat相关api
val API_WS_TEXT_CHAT_MSG = /*Encrypted: cs_text_chat_msg*/ sp.StringEncrypt.decrypt("p8c+qxtSoNE05RY2bjTxsJQ7Z58TNeAYiCFndCtxSyqZ7WHMSQ2OEFLtFtk=")
    }
}
