/*
 * Copyright (c) 2019.
 * João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This file is part of the UNES Open Source Project.
 *
 * UNES is licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.forcetower.uefs.feature.shared

import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils

fun View.fadeIn() {
    if (visibility == VISIBLE) return
    val fade: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
    visibility = VISIBLE
    startAnimation(fade)
    requestLayout()
}

fun View.fadeOut() {
    if (visibility == INVISIBLE) return
    val fade: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
    visibility = INVISIBLE
    startAnimation(fade)
    requestLayout()
}

fun View.fadeOutGone() {
    if (visibility == GONE) return
    val fade: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
    fade.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            visibility = GONE
        }

        override fun onAnimationStart(animation: Animation?) {}
    })
    visibility = INVISIBLE
    startAnimation(fade)
    requestLayout()
}