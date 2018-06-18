package com.onexeor.task.test.thetesttask

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.view.*
import android.widget.Toast
import java.util.*

class CellsView(context: Context?) : SurfaceView(context), SurfaceHolder.Callback {

    var numColumns: Int = 0
        set(numColumns) {
            field = numColumns
            calculateDimensions()
        }
    var numRows: Int = 0
        set(numRows) {
            field = numRows
            calculateDimensions()
        }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        super.setLayerType(View.LAYER_TYPE_NONE, paint)
    }

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1f
    private var cellWidth: Float = 0F
    private var cellHeight: Float = 0F
    private val blackLinePaint = Paint()
    private var cellChecked: Array<Array<CellModel>>? = null
    private var randomCellPaint = Paint()
    private var stopX: Float = 0F
    private var stopY: Float = 0F
    private var countChecked: Int = 0

    private var scalePointX: Float = 0F
    private var scalePointY: Float = 0F

    var mPaint: TextPaint? = null
    var cellCount: Int = 0

    var drawThread: DrawThread? = null

    init {
        holder.addCallback(this)
        blackLinePaint.style = Paint.Style.FILL_AND_STROKE
        randomCellPaint.style = Paint.Style.FILL_AND_STROKE
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    fun stopTread() {
        drawThread?.setRunning(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDimensions()
    }

    private fun calculateDimensions() {
        if (this.numColumns < 1 || this.numRows < 1 || numColumns > 100 && numRows > 100) {
            return
        }

        cellCount = this.numColumns * this.numRows

        cellWidth = width / this.numColumns.toFloat()
        cellHeight = cellWidth

        stopY = cellHeight * numRows.toFloat()
        stopX = cellWidth * numColumns.toFloat()

        cellChecked = Array(this.numColumns) { it: Int ->
            Array(this.numRows) { i: Int ->
                val posX = (it * cellWidth)
                val posY = (i * cellHeight)
                val rnd = Random()
                randomCellPaint.color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                val cell = CellModel(false, cellWidth, cellHeight, it, i, posX, posY)
                cell.color = Paint(randomCellPaint)
                cell
            }
        }

        invalidate()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
//            mScaleDetector?.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x / mScaleFactor + drawThread?.clipBounds_canvas?.left!!
                val y = event.y / mScaleFactor + drawThread?.clipBounds_canvas?.top!!
                val column = (x / cellWidth).toInt()
                val row = (y / cellHeight).toInt()

                if (column >= 0 && row >= 0 && column < numColumns && row < numRows && !cellChecked!![column][row].isChecked) {
                    cellChecked!![column][row].isChecked = !cellChecked!![column][row].isChecked
                    countChecked++
                    invalidate()
                }
            }
        }
        return true
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        var retry = true
        drawThread?.setRunning(false)
        while (retry) {
            try {
                drawThread?.join()
                retry = false
            } catch (e: InterruptedException) {
            }
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        drawThread = DrawThread(holder)
        drawThread?.numColumns = numColumns
        drawThread?.numRows = numRows
        drawThread?.setRunning(true)
        drawThread?.start()
    }

    var initialFocalPoints: FloatArray? = null

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scalePointX = detector.focusX + scrollX
            scalePointY = detector.focusY + scrollY
            mScaleFactor *= detector.scaleFactor

            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 10.0f))
            invalidate()
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            val startX = detector?.focusX!! + scrollX
            val startY = detector.focusY + scrollY

            initialFocalPoints = floatArrayOf(startX, startY)

            return true
        }
    }

    inner class DrawThread(private val surfaceHolder: SurfaceHolder) : Thread() {

        private var running = false
        var numColumns: Int = 0
            set(numColumns) {
                field = numColumns
                calculateDimensions()
            }
        var numRows: Int = 0
            set(numRows) {
                field = numRows
                calculateDimensions()
            }

        fun setRunning(running: Boolean) {
            this.running = running
        }

        var clipBounds_canvas: Rect? = null

        override fun run() {
            var canvas: Canvas?
            while (running) {
                canvas = null
                try {
                    canvas = surfaceHolder.lockCanvas()
                    if (canvas == null)
                        continue
                    if (this.numColumns == 0 || this.numRows == 0 || cellChecked == null || cellChecked!!.isEmpty()) {
                        return
                    }

                    canvas.drawColor(Color.GREEN)
                    clipBounds_canvas = canvas.clipBounds
                    canvas.drawColor(Color.WHITE)
                    getTextPaths()
                    drawTextAndPaintUpCheckedCells(canvas)
                    dawGrid(canvas)
                } finally {
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun getTextPaths() {
            if (mPaint == null) {
                mPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
                mPaint?.textSize = cellWidth / 2.5F
                mPaint?.color = Color.BLACK

                for (i in 0 until this.numRows) {
                    for (j in 0 until this.numColumns) {
                        val path = Path()
                        val cell = cellChecked!![i][j]
                        var rightRow = if (cell.row == 0) "" else cell.row.toString()
                        var rightColl = if (cell.coll == 0) "" else cell.coll.toString()
                        if (cell.row == 0 && cell.coll == 0)
                            rightRow = "0"
                        else if (cell.coll == 0)
                            rightColl = "0"
                        val str: String = rightRow + rightColl
                        val txtWidth = mPaint?.measureText(str)

                        mPaint?.getTextPath(str, 0, str.length, (i * cellWidth) + (cellWidth / 2) - (txtWidth!! / 2), (j * cellHeight) + (cellHeight / 1.5F), path)
                        path.close()
                        cell.txtPath = path
                    }
                }
            }
        }

        private fun drawTextAndPaintUpCheckedCells(canvas: Canvas) {
            canvas.save()
            canvas.scale(mScaleFactor, mScaleFactor, scalePointX, scalePointY)

            for (i in 0 until this.numRows) {
                for (j in 0 until this.numColumns) {
                    val cell = cellChecked!![i][j]
                    if (cell.isChecked && cell.color != null) {
                        canvas.drawRect(
                                cell.posX,
                                cell.posY,
                                cell.posX + cellWidth,
                                cell.posY + cellHeight,
                                cell.color)
                    } else if (cell.txtPath != null) {
                        canvas.drawPath(cell.txtPath, mPaint)
                    }
                }
            }
            if (countChecked == cellCount) {
                countChecked++ // nice decision X)
                Handler(Looper.getMainLooper()).post({ Toast.makeText(context, context.getString(R.string.all_painted_up), Toast.LENGTH_SHORT).show() })
            }
            canvas.restore()
        }

        private fun dawGrid(canvas: Canvas) {
            canvas.save()
            canvas.scale(mScaleFactor, mScaleFactor, scalePointX, scalePointY)
            for (i in 1..this.numColumns) {
                canvas.drawLine((i * cellWidth), 0f, (i * cellWidth), stopY, blackLinePaint)
            }
            for (i in 1..this.numRows) {
                canvas.drawLine(0f, (i * cellHeight), width.toFloat(), (i * cellHeight), blackLinePaint)
            }
            canvas.restore()
        }

    }
}