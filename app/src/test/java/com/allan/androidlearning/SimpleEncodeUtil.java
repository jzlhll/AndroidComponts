package com.allan.androidlearning;

class SimpleEncodeUtil {
    private final String original;
    private final char startChar;
    private final int[] indexes;
 
    public SimpleEncodeUtil(String original, char startChar, int[] indexes) { 
        this.original  = original; 
        this.startChar  = startChar; 
        this.indexes  = indexes; 
    } 
 
    public String decode() { 
        StringBuilder sb = new StringBuilder(); 
        for (int index : indexes) { 
            char ch = (char) (startChar + index); 
            sb.append(ch);  
        } 
        return sb.toString();  
    } 
 
    private String toClassCreatorString() { 
        StringBuilder sb = new StringBuilder("intArrayOf("); 
        for (int index : indexes) { 
            sb.append(index).append(',');  
        } 
        sb.append(')');  
        String str = "val originalStr = SimpleEncodeUtil(\"\", '%s', %s).decode() // originalString: %s"; 
        return String.format(str,  "" + startChar, sb.toString(),  original); 
    } 
 
    public static String originalToStringSplitClassString(String originalStr) { 
        char[] chars = originalStr.toCharArray();  
        char[] rands = {'0', '1', '2', '3', '4', '5'}; 
        char startChar = rands[(int) (Math.random()  * rands.length)];  
        int[] indexes = new int[chars.length]; 
        for (int i = 0; i < chars.length;  i++) { 
            indexes[i] = chars[i] - startChar; 
        } 
        return new SimpleEncodeUtil(originalStr, startChar, indexes).toClassCreatorString(); 
    } 
 
    public static void main(String[] args) { 
        String originalStr = "com.ss.android.lark";  
        String classStr = SimpleEncodeUtil.originalToStringSplitClassString(originalStr);  
        System.out.println(classStr);  
    }
} 