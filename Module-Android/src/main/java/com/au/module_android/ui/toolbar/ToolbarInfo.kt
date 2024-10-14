package com.au.module_android.ui.toolbar

import android.view.MenuItem

/**
 * @author allan
 * @date :2024/10/14 14:09
 * @description:
 */
data class ToolbarInfo(val title:String? = null, val hasBackIcon:Boolean = true, val titleCenter:Boolean = true, val menuBean:MenuBean? = null)
data class MenuBean(val menuXml: Int, val showWhenOnCreate:Boolean, val onMenuItemBlock:((MenuItem)->Unit))