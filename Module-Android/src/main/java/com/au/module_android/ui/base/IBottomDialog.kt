package com.au.module_android.ui.base

interface IBottomDialog<D:IBaseDialog> : IBaseDialog{

    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    var onDismissBlock:((D)->Unit)?

    /**
     * 尽量早一点调用。在show之前。如果是继承，则放在init{}
     */
    var onShownBlock:((D)->Unit)?
}