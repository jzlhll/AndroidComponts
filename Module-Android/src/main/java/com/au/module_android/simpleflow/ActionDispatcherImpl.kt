package com.au.module_android.simpleflow

class ActionDispatcherImpl : IActionDispatcher {
    private var _actionStore: VMActionStore? = null

    override fun getActionStore() : VMActionStore {
        val store = _actionStore
        if (store != null) {
            return store
        }
        val newStore = VMActionStore()
        _actionStore = newStore
        return newStore
    }

    override fun dispatch(action: IStateAction) {
        getActionStore().dispatch(action)
    }
}