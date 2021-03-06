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
val parts : Int = 4
val scGap : Float = 0.02f / parts
val sizeFactor : Float = 10f
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")
val strokeFactor : Float = 90f
val delay : Long = 20


fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawMoveUpBox(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(3, parts)
    val sf0 : Float = sf.divideScale(0, parts)
    save()
    translate(0f, (h - size) * (1f - sf1))
    drawRect(RectF(0f, size * (1 - sf0), size, size), paint)
    restore()
    for (j in 0..1) {
        val sfj : Float = sf.divideScale((j + 1), parts)
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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.draw(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class MUBNode(var i : Int, val state : State = State()) {

        private var next : MUBNode? = null
        private var prev : MUBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = MUBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMUBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : MUBNode {
            var curr : MUBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class MoveBoxUp(var i : Int) {

        private var curr : MUBNode = MUBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : MoveBoxUpView) {

        private val animator : Animator = Animator(view)
        private val mub : MoveBoxUp = MoveBoxUp(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun draw(canvas : Canvas){
            canvas.drawColor(backColor)
            mub.draw(canvas, paint)
            animator.animate {
                mub.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            mub.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : MoveBoxUpView {
            val view : MoveBoxUpView = MoveBoxUpView(activity)
            activity.setContentView(view)
            return view
        }
    }
}