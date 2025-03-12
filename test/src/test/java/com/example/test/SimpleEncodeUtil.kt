package com.example.test

class SimpleEncodeUtil {
    class Encoder(private val origStr:String) {
        fun originalToStringSplitClassString() : String {
            val chars = origStr.toCharArray()
            val rands = arrayOf(1, 2, 3, 4, 5)
            val offset = rands[(Math.random() * rands.size).toInt()]
            val indexes = mutableListOf<Int>()
            indexes.add(offset + 100)
            for (ch in chars) {
                val index = ch - offset
                indexes.add(index.code)
            }
            return toClassCreatorString(indexes.toIntArray())
        }

        private fun toClassCreatorString(indexes: IntArray): String {
            val sb = StringBuilder("intArrayOf(")
            for (index in indexes) {
                sb.append(index).append(',')
            }
            sb.append(')')
            val str = " // originalString: %s\nval originalStr = SimpleEncodeUtil.Decoder().decode(%s)"
            val str2 = "// originalString: %s\nAppNative.simpleDecoder(%s)"
            return String.format(str, origStr, sb.toString()) + "\n" + String.format(str2, origStr, sb.toString())
        }
    }

    class Decoder {
        //不校验indexes的数组是否为空
        fun decode(indexes: IntArray) : String {
            val sb = StringBuilder()
            val offset = indexes[0] - 100
            var i = 1
            while(i < indexes.size) {
                val ch = (indexes[i] + offset).toChar()
                sb.append(ch)
                i++
            }
            return sb.toString()
        }
     }
}