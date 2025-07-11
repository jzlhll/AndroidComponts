package sp

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
/**
 * @param mode  替换模式 可选0或者1，默认0。
 *
 *     0  仅支持静态的final常量。这也是开发规范要求。
 *     java：修改static final String常量；
 *     kotlin：const val String或者文件级const val String。
 *
 *     1  不仅支持0模式，还支持任意全局。todo。
 *     java：String a = "str"；final String a = "str";
 *     kotlin: val a = "str"，或者var a = "str";
 *
 *     2 不仅仅支持0，1模式，还支持修改任意函数内的字符串变量。todo。
 */
annotation class EncryptString(val mode:Int = 0)