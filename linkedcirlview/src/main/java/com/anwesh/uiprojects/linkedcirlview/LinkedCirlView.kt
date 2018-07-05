package com.anwesh.uiprojects.linkedcirlview

/**
 * Created by anweshmishra on 05/07/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val CIRL_NODES : Int = 5
class LinkedCirlView (ctx : Context) : View (ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    var linkedCirlListener : LinkedCirlListener? = null

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    fun addListener(onComplete : (Int) -> Unit, onReset : (Int) -> Unit) {
        linkedCirlListener = LinkedCirlListener(onComplete, onReset)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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
                if (j == scales.size  || j == -1) {
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
            val r : Float = gap/4
            val index : Int = i % 2
            val lineGap : Float = 2 * w / (3 * CIRL_NODES)
            prev?.draw(canvas, paint)
            val sf : Int = 1 - 2 * index
            paint.strokeWidth = Math.min(w, h) / 60
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = Color.parseColor("#f44336")
            paint.style = Paint.Style.STROKE
            canvas.save()
            canvas.translate(0.04f * w + i * gap + gap * state.scales[0], h/2)
            canvas.drawCircle(0f, 0f, r, paint)
            canvas.save()
            canvas.rotate(180f * index + 180f * sf * state.scales[1])
            canvas.drawLine(-r/3, r/3, r/3, r/3, paint)
            canvas.restore()
            canvas.restore()
            canvas.drawLine(w/6 + i * lineGap, 0.8f * h, w / 6 + i * lineGap + lineGap/2 * (state.scales[0] + state.scales[1]), 0.8f * h, paint)
        }

        fun update(stopcb : (Int, Float) -> Unit) {
           state.update {
               stopcb(i, it)
           }
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

    data class LinkedCirl (var i : Int) {

        private var curr : CirlNode = CirlNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            curr.update {j, scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(j, scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedCirlView) {

        private val linkedCirl : LinkedCirl = LinkedCirl(0)

        private val animator : LCAnimator = LCAnimator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            linkedCirl.draw(canvas, paint)
            animator.animate {
                linkedCirl.update {j, scale ->
                    animator.stop()
                    when (scale) {
                        1f -> {
                            view.linkedCirlListener?.onComplete?.invoke(j)
                        }
                        0f -> {
                            view.linkedCirlListener?.onReset?.invoke(j)
                        }
                    }
                }
            }
        }

        fun handleTap() {
            linkedCirl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LinkedCirlView {
            val view : LinkedCirlView = LinkedCirlView(activity)
            activity.setContentView(view)
            return view
        }
    }

    data class LinkedCirlListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)
}