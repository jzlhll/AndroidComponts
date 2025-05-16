package com.allan.androidlearning.transfer.htmlbeans

import androidx.annotation.Keep

class WSSendFileChunkResult(@Keep val uriUuid:String, @Keep val indexChunk:Int, @Keep val totalChunk:Int)