package com.onexeor.task.test.thetesttask

import android.graphics.Paint
import android.graphics.Path

class CellModel(var isChecked: Boolean, var width: Float, var height: Float, var coll: Int, var row: Int, var posX: Float, var posY: Float) {

    var color: Paint? = null
    var txtPath: Path? = null

}
