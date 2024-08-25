import androidx.annotation.Keep
import com.google.gson.JsonSerializer

///**
// * @author allan
// * @date :2024/4/29 16:03
// * @description:
// */
//@Keep
//class TempInfo {
//    var sec: Int = 0
//    var curSec: Int = 0
//    var curTemp: Int = 0
//    var curTempAb: Int = 0
//    var temp: IntArray
//    var tempTile: IntArray
//
//    var params: Array<Param>
//
//    @Keep
//    class Param {
//        var tem: Int = 0
//    }
//
//    @Keep
//    class FullTempInfo {
//        var data: List<TempInfo>? = null
//        var code: String? = null
//        var msg: String? = null
//    }
//}


//class CutomBeatyPrintGson<T> {
//    fun standardizeFormat(json: TempInfo.FullTempInfo?): String {
//        val gson = GsonBuilder().setPrettyPrinting().registerTypeAdapter(TempInfo::class.java, SortedJsonSerializer()).create()
//        return gson.toJson(json)
//    }
//
//    private class SortedJsonSerializer<T> : JsonSerializer<T> {
//        override fun serialize(foo: T, type: Type, context: JsonSerializationContext): JsonElement {
//            val jo = JsonObject()
//            jo.add("sec", context.serialize(foo.sec))
//            jo.add("curSec", context.serialize(foo.curSec))
//            jo.add("curTemp", context.serialize(foo.curTemp))
//            jo.add("curTempAb", context.serialize(foo.curTempAb))
//            jo.add("temp", context.serialize(foo.temp))
//            jo.add("tempTile", context.serialize(foo.tempTile))
//            jo.add("params", context.serialize(foo.params))
//            return jo
//        }
//    }
//}