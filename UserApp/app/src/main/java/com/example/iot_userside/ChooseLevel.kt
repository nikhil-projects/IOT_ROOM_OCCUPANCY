package com.example.iot_userside

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ChooseLevel : BaseActivity() {
    private lateinit var myWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val faq = mutableMapOf<String, Int>() //map to store a list of questions and answers
        faq["Level -1"] = -1
        faq["Level 0"] = 0
        faq["Level 1"] = 1
        faq["Level 2"] = 2
        faq["Level 3"] = 3
        faq["Level 4"] = 4
        faq["Level 5"] = 5
        faq["Level 6"] = 6
        faq["Level 7"] = 7
        faq["Level 8"] = 8
        faq["Level 9"] = 9

        scrollView {
            verticalLayout {
                /*programmatically adding buttons based on the number of faq's, this method was
                adopted to reduce the amount of code and make adding more faq's easy */
                for (f in faq) {
                    button {
                        background
                        text = f.key
                        background = if (f.value == 4) {
                            buttonGreen()
                        } else {
                            buttonGrey()
                        }
                        onClick {
                            if (f.value == 4) {
                                switchActivity(ChooseRoom())
                            } else {
                                toast("Data on this floor is not available")
                            }

                        }
                    }.lparams(width = matchParent) {
                        topMargin = dip(10)
                    }
                }
            }
        }
    }
}
