/*
 * A real damped spring that is integrated every frame.
 *
 * Unlike a fixed-duration animation, the motion depends only on physics:
 * position and velocity are carried over when the target changes mid-flight,
 * so gestures and interruptions stay continuous instead of jumping.
 *
 * Equation (unit mass):  x'' = -stiffness * (x - target) - 2 * zeta * sqrt(stiffness) * x'
 */
package com.android.launcher3.anim

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

class SpringMotion(
    initialPosition: Float,
    stiffness: Float = SmoothSpringPresets.SMOOTH.stiffness,
    dampingRatio: Float = SmoothSpringPresets.SMOOTH.dampingRatio,
    private val minVisibleChange: Float = DEFAULT_MIN_VISIBLE_CHANGE,
) {
    init {
        require(stiffness > 0f) { "stiffness must be positive" }
        require(dampingRatio > 0f) { "dampingRatio must be positive" }
    }

    var stiffness: Float = stiffness
        set(value) {
            require(value > 0f) { "stiffness must be positive" }
            field = value
        }

    var dampingRatio: Float = dampingRatio
        set(value) {
            require(value > 0f) { "dampingRatio must be positive" }
            field = value
        }

    /** Current position. */
    var position: Float = initialPosition
        private set

    /** Current velocity in units per second. */
    var velocity: Float = 0f
        private set

    /** Where the spring is pulling to. */
    var target: Float = initialPosition
        private set

    /** True once the spring is within [minVisibleChange] of its target and nearly still. */
    val isAtRest: Boolean
        get() = abs(position - target) < minVisibleChange && abs(velocity) < minVisibleChange

    /**
     * Changes the target and keeps the current velocity, so a mid-flight change
     * (for example a new swipe direction) flows on from where the motion is now.
     */
    fun retarget(newTarget: Float) {
        target = newTarget
    }

    /** Hands over a velocity from a gesture, in units per second. */
    fun setVelocity(newVelocity: Float) {
        velocity = newVelocity
    }

    /** Jumps directly to [value] and stops. */
    fun snapTo(value: Float) {
        position = value
        target = value
        velocity = 0f
    }

    /**
     * Advances the spring by [deltaSeconds].
     *
     * Large frame gaps (for example after a jank spike) are clamped, and the
     * step is split into fixed sub-steps so the integration stays stable even
     * with high stiffness values.
     *
     * @return true while the spring is still moving.
     */
    fun update(deltaSeconds: Float): Boolean {
        if (isAtRest) {
            position = target
            velocity = 0f
            return false
        }
        val dt = deltaSeconds.coerceIn(0f, MAX_FRAME_SECONDS)
        if (dt <= 0f) return true

        val steps = ceil(dt / MAX_SUBSTEP_SECONDS).toInt().coerceAtLeast(1)
        val h = dt / steps
        val damping = 2f * dampingRatio * sqrt(stiffness)
        repeat(steps) {
            val acceleration = -stiffness * (position - target) - damping * velocity
            velocity += acceleration * h
            position += velocity * h
        }
        return !isAtRest
    }

    companion object {
        const val DEFAULT_MIN_VISIBLE_CHANGE = 0.001f
        private const val MAX_FRAME_SECONDS = 1f / 30f
        private const val MAX_SUBSTEP_SECONDS = 1f / 240f
    }
}
