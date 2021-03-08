package com.meanwhile.recyclerview_bounce

import android.graphics.Canvas
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView

/** The magnitude of translation distance while the list is over-scrolled. */
private const val OVERSCROLL_TRANSLATION_MAGNITUDE = 0.2f

/** The magnitude of translation distance when the list reaches the edge on fling. */
private const val FLING_TRANSLATION_MAGNITUDE = 0.5f

/**
 * Replace edge effect by a bounce
 */
class BounceEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {

    override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {

        return object : EdgeEffect(recyclerView.context) {

            // A reference to the [SpringAnimation] for this RecyclerView used to bring the item back after the over-scroll effect.
            var translationAnim: SpringAnimation? = null

            val sign: Int get() = when(direction) {
                DIRECTION_BOTTOM,
                DIRECTION_RIGHT -> {
                    -1
                }
                DIRECTION_LEFT,
                DIRECTION_TOP -> {
                    1
                }
                else -> 1
            }

            override fun onPull(deltaDistance: Float) {
                super.onPull(deltaDistance)
                handlePull(deltaDistance)
            }

            override fun onPull(deltaDistance: Float, displacement: Float) {
                super.onPull(deltaDistance, displacement)
                handlePull(deltaDistance)
            }

            private fun handlePull(deltaDistance: Float) {
                // This is called on every touch event while the list is scrolled with a finger.

                // Translate the recyclerView with the distance
                val sign = this.sign
                when(direction) {
                    DIRECTION_BOTTOM,
                    DIRECTION_TOP -> {
                        val translationYDelta = sign * recyclerView.width * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
                        recyclerView.translationY += translationYDelta
                    }
                    DIRECTION_LEFT,
                    DIRECTION_RIGHT -> {
                        val translationXDelta = sign * recyclerView.height * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
                        recyclerView.translationX += translationXDelta
                    }
                }
                translationAnim?.cancel()
            }

            override fun onRelease() {
                super.onRelease()
                // The finger is lifted. Start the animation to bring translation back to the resting state.
                val shouldAnimate = when(direction) {
                    DIRECTION_BOTTOM,
                    DIRECTION_TOP -> {
                        recyclerView.translationY != 0f
                    }
                    DIRECTION_LEFT,
                    DIRECTION_RIGHT -> {
                        recyclerView.translationX != 0f
                    }
                    else -> return
                }
                if (shouldAnimate) {
                    translationAnim = createAnim()?.also { it.start() }
                }
            }

            override fun onAbsorb(velocity: Int) {
                super.onAbsorb(velocity)

                // The list has reached the edge on fling.
                val sign = this.sign
                val translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE
                translationAnim?.cancel()
                translationAnim = createAnim()?.setStartVelocity(translationVelocity)?.also { it.start() }
            }

            override fun draw(canvas: Canvas?): Boolean {
                // don't paint the usual edge effect
                return false
            }

            override fun isFinished(): Boolean {
                // Without this, will skip future calls to onAbsorb()
                return translationAnim?.isRunning?.not() ?: true
            }

            private fun createAnim(): SpringAnimation? {
                val property = when(direction) {
                    DIRECTION_BOTTOM,
                    DIRECTION_TOP -> {
                        SpringAnimation.TRANSLATION_Y
                    }
                    DIRECTION_LEFT,
                    DIRECTION_RIGHT -> {
                        SpringAnimation.TRANSLATION_X
                    }
                    else -> return null
                }
                return SpringAnimation(recyclerView, property)
                    .setSpring(SpringForce()
                        .setFinalPosition(0f)
                        .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                        .setStiffness(SpringForce.STIFFNESS_LOW)
                    )
            }

        }
    }
}
