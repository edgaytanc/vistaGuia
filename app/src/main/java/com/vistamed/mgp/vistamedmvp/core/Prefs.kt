package com.vistamed.mgp.vistamedmvp.core

import android.content.Context

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("vistamed_prefs", Context.MODE_PRIVATE)
    var targetLabel: String?
        get() = sp.getString("target_label", null)
        set(value) { sp.edit().putString("target_label", value).apply() }
}
