package com.anwesh.uiprojects.moveboxupview

/**
 * Created by anweshmishra on 30/07/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.graphics.RectF
import android.content.Context

val nodes : Int = 5
val scGap : Float = 0.02f
val sizeFactor : Float = 10f
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")
val strokeFactor : Float = 90f
val delay : Long = 20
val parts : Int = 3

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawMoveUpBox(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(2, parts)
    save()
    translate(0f, (h - size) * (1f - sf1))
    drawRect(RectF(0f, 0f, size, size), paint)
    restore()
    for (j in 0..1) {
        val sfj : Float = sf.divideScale(j, 2)
        drawLine(size * j, 0f, size * j, (h - size) * (sfj - sf1), paint)
    }
}

fun Canvas.drawMUBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(gap * (i + 1), 0f)
    drawMoveUpBox(scale, w, h, paint)
    restore()
}

class MoveBoxUpView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}