package com.allan.androidlearning.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.allan.androidlearning.databinding.ActivityJsHtmlBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd
import com.au.module_androidui.toast.ToastUtil.toastOnTop
import com.github.lzyzsd.jsbridge.BridgeUtil
import com.github.lzyzsd.jsbridge.DefaultHandler
import com.google.gson.Gson

/**
 * @author au
 * @date :2023/10/8 10:26
 * @description:
 */

@EntryFrgName
class WebBridgeFragment : BindingFragment<ActivityJsHtmlBinding>() {
    data class Info(val name:String, val age:Int, val desc:String?, val index:Int, val other:String? = null)
    var mIndex = 1

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.root.onClick {
            binding.webView.callHandler("registerNative",
                "jkadfjkfadjkfjk djkfj1290309 ajdfk mkadf12893918 " +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803 jadf" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803 jadf" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803 jadffd sfjka 128912ui94 jkaef fasdf " +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803 jadf adfasdf asdf dasfasd f" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803 jadf adfasdf asdf adfdaf a afds" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803  adf df asdr234 34165 547 65 cb" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803  adf df asdr234 34165 547 65 cb" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803  adf df asdr234 34165 547 65 cb" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803  adf df asdr234 34165 547 65 cb" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803  adf df asdr234 34165 547 65 cb" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803  adf df asdr234 34165 547 65 cb" +
                        "dfjakdfjk z89zcznsfk 3904-=21930 ml]]dafi u12803  adf df asdr234 34165 547 65 cb" +
                        "djfkadsjfkadjf1 1920309 mlmz, ./d -1293-" + Math.random()) {
                Log.d(BridgeUtil.TAG, "registerNative callback $it")
            }
        }

        binding.webView.setDefaultHandler(DefaultHandler())
        binding.webView.loadUrl("file:///android_asset/getuserinfo.html")
        binding.webView.registerHandler("getUserInfo") {
                data, func->
            val index = (Math.random() * 10).toInt()
            Log.d("WebBridgeFragment", "data $data $index")
            if (index > 5) {
                val json = Gson().toJson(Info("allan", 18, "hello", mIndex++))
                func.onCallBack(json)
            } else {
                val json = Gson().toJson(Info("bob", 24, "dfjkafdjkdfjkfa " +
                        "asjfk ajdkf 12893 dajkf 12893 jadkf 182bbnn93 df jadfk" +
                        " 12839 jasdkf 18293 jasdkf 12893 jadksfbnbn 12839 " +
                        "jasdkf 12893 fadsjfkajkd 12930zdmzmfd  adfdf zcz [p" +
                        "jasdkf 12893 fadsjfkajkd 12930zdmzmfd  adfdf zcz [p" +
                        "jasdkf 12893 fadsjfkajkd 12930zdmzmfd  adfdf zcz [p" +
                        "jasdkf 12893 fadsjfkajkd 12930zdmzmfd  adfdf zcz [p" +
                        "jasdkf 12893 fadsjfkajkd 12930zdmzmfd owqpeqopewr[p" +
                        " pkzldfjla queior128039 zdfjk nbnb 12839 nbnbnbnnbnnb 12893", mIndex++,
                    other = "zjzjkzjjkfk, djkfj , dfjkds 12idfkdf 902394opopdf[];'.fklf[[]ll;;[]sfkfdfk190490[]dfioadfj189328194,84914i9 []2489294," +
                            "dfkjdkfjkd 902139i0 jfkaff 9012391 jkdfjk zfd mzkcx 903jk f. kjdf][]21km1289"+
                            "dfkjdkfjkd 902139i0 jfkaff 9012391 jkdfjk zfd mzkcx 903jk f. kjdf][]21km1289"+
                            "dfkjdkfjkd 902139i0 jfkaff 9012391 jkdfjk zfd mzkcx 903jk f. kjdf][]21km1289"+
                            "dfkjdkfjkd 902139i0 jfkaff 9012391 jkdfjk zfd mzkcx 903jk f. kjdf][]21km1289"))
                func.onCallBack(json)
            }

            Globals.mainHandler.postDelayed({
                logd { "aaaaaaaaaa" }
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("myapp://app")
                    ).also { it.flags = it.flags or Intent.FLAG_ACTIVITY_NEW_TASK }
                )
                toastOnTop("start app!")
            }, 3000)
        }
    }
}