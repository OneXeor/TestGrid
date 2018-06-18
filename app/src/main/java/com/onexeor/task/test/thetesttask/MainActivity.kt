package com.onexeor.task.test.thetesttask

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    private lateinit var cells: CellsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog()
    }

    private fun dialog() {
        val edtCols = EditText(this)

        val ll = LinearLayout(this)
        ll.addView(edtCols)
        ll.setPadding(48, 48, 48, 48)

        edtCols.hint = "Кол-во столбцов"
        edtCols.inputType = InputType.TYPE_CLASS_NUMBER

        cells = CellsView(this)

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.grid_size))
                .setView(ll)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ready)) { p0, p1 ->

                    if (edtCols.text.toString().isEmpty())
                        cells.numColumns = 100
                    else cells.numColumns = Integer.parseInt(edtCols.text.toString())

                    cells.numRows = cells.numColumns

                    setContentView(cells)
                }
                .setNegativeButton("Закрыть") { p0, p1 ->
                    super.onBackPressed()
                }.show()
    }

    override fun onDestroy() {
        cells.stopTread()
        super.onDestroy()
    }

}
