/*
 * Smooth spring presets for launcher motion.
 *
 * Central place for spring tuning so that app open/close, folder and
 * overview motion share one feel. Switch [active] to change the whole launcher.
 */
package com.android.launcher3.anim

object SmoothSpringPresets {

    /** Stiffness and damping ratio of a spring. Damping ratio < 1 means slight overshoot. */
    data class Preset(
        val name: String,
        val stiffness: Float,
        val dampingRatio: Float,
    )

    /** Soft settle, almost no overshoot. Default for smooth, Hyper-OS-like motion. */
    val SMOOTH = Preset(name = "smooth", stiffness = 260f, dampingRatio = 0.8f)

    /** Quicker and a bit lively, for users who prefer snappy transitions. */
    val SNAPPY = Preset(name = "snappy", stiffness = 420f, dampingRatio = 0.72f)

    /** Slow, floaty, for large surfaces like overview and wallpaper. */
    val GENTLE = Preset(name = "gentle", stiffness = 160f, dampingRatio = 0.9f)

    /** Preset used by all spring animations that opt in. */
    @JvmStatic
    var active: Preset = SMOOTH
}
