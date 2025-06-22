package com.allan.mydroid.api

import com.au.annos.EncryptString

@EncryptString
class WSApisConst {
    companion object {
        val API_WS_SEND_FILE_LIST = com.au.stringprotect.StringEncryptUtil.decrypt("p1eW7NPmSN2PclDvuIL2BXtVf5f9yEvUSJNvu4QHgsiJI9iiX3z4ehAk") /*Encrypted: s_sendFileList*/
        val API_WS_LEFT_SPACE = com.au.stringprotect.StringEncryptUtil.decrypt("CkRwbEy4Y91OAonPPHERSN7pgl/fK1VgDpxLR0XQAV9RexGvcb8z") /*Encrypted: s_leftSpace*/
        val API_WS_CLIENT_INIT_CALLBACK = com.au.stringprotect.StringEncryptUtil.decrypt("2fA8eA0cI0HdkZqqq+RS7alp3vzMgPA4SZdP/r/7vMbj1LZFWg8B/QvbUHw=") /*Encrypted: s_clientInitBack*/

        val API_WS_SEND_FILE_CHUNK = com.au.stringprotect.StringEncryptUtil.decrypt("uXKuRYm1UP3WEGcnYvbXI2CSR6tDIO94jVz17PUOezCrxSWz0YeKCots/g==") /*Encrypted: s_sendFileChunk*/
        val API_WS_SEND_SMALL_FILE_CHUNK = com.au.stringprotect.StringEncryptUtil.decrypt("MKetaGvX9ex7GXLkKaGHzqiOWbUof0fPjcgLcI2Im2ZcYtekqOFmOGMXLG/qc8yh") /*Encrypted: s_sendSmallFileChunk*/

        val API_WS_SEND_FILE_NOT_EXIST = com.au.stringprotect.StringEncryptUtil.decrypt("AHImHCu9rrtu2YgQRBVecoD6WMjWVtzdOeAGa87nQl1muqz4mEM2E3eFHQYmNA==") /*Encrypted: s_sendFileNotExist*/

        val API_WS_INIT = com.au.stringprotect.StringEncryptUtil.decrypt("fZtq6hoqHjhxVUx6jTWiN92HIxm7DeQnDvkYRnXHlcZXP5Hr") /*Encrypted: c_wsInit*/
        val API_WS_PING = com.au.stringprotect.StringEncryptUtil.decrypt("tjiPhtAdbvZeGiXxbGEWBQsEpcWjlRKw6p1JzZjmO/0CpQ==") /*Encrypted: c_ping*/
        val API_WS_REQUEST_FILE = com.au.stringprotect.StringEncryptUtil.decrypt("iSPvcl0WzMPmtzTWzQtEMvN9tMjNPvR0E6fQ+jO11G3IhNzq1N/JJ/I=") /*Encrypted: c_requestFile*/
        val API_WS_FILE_DOWNLOAD_COMPLETE = com.au.stringprotect.StringEncryptUtil.decrypt("ulJkCQgqHOzcs3p5W3thiyMoHuhiVwHFR9r+Y6Cvp0nw3e9DVpHr9Q+OmbNcEHtc5/c=") /*Encrypted: c_downloadFileComplete*/

        //textChat相关api
        val API_WS_TEXT_CHAT_MSG = com.au.stringprotect.StringEncryptUtil.decrypt("Kd4yAHRbyAn1rPyvO2hD+qzf5fkq8baxjt6jqp+/pgD/KTqCI+9jVxBwvL8=") /*Encrypted: cs_text_chat_msg*/
    }
}
