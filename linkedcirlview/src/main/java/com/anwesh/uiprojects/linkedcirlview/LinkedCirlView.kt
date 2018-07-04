package com.anwesh.uiprojects.linkedcirlview

/**
 * Created by anweshmishra on 05/07/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val CIRL_NODES : Int = 5
class LinkedCirlView (ctx : Context) : View (ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class LCState(var j : Int = 0, var dir : Float = 0f, var prevScale : Float = 0f) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += 0.1f * dir
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size - 1 || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class LCAnimator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

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

    data class CirlNode (var i : Int, val state : LCState = LCState()) {

        var prev : CirlNode? = null

        var next : CirlNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < CIRL_NODES - 1) {
                next = CirlNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = 0.9f * w / CIRL_NODES
            val r : Float = gap/10
            val index : Int = i % 2
            val sf : Int = 1 - 2 * index
            paint.strokeWidth = Math.min(w, h) / 60
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = Color.parseColor("#f44336")
            paint.style = Paint.Style.STROKE
            canvas.save()
            canvas.translate(0.1f * w + i * gap + gap * state.scales[0], h/2)
            canvas.drawCircle(0f, 0f, r, paint)
            canvas.save()
            canvas.rotate(180f * index + 180f * sf * state.scales[1])
            canvas.drawLine(-r/3, r/3, r/3, r/3, paint)
            canvas.restore()
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
           state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CirlNode {
            var curr : CirlNode? = prev
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
}