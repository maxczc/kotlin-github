package com.czc.max.base.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.widget.AppCompatTextView
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.czc.max.base.R

/**
 * @author czc
 * @since 5/12/17
 */
class SimpleTextView : AppCompatTextView {

  companion object {
    const val BOTTOM_FULL = 2
  }

  private var rightText: String//右边的文字
  private var rightTextSize: Float
  private var rightMaxLines: Int
  private var divideType: Int

  private var rightTextPaint: TextPaint
  private var linePaint: Paint
  private var staticLayout: StaticLayout

  constructor(context: Context?) : this(context, null)
  constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    val ta = context!!.obtainStyledAttributes(attrs, R.styleable.SimpleTextView)
    rightText = ta.getString(R.styleable.SimpleTextView_rightText)
    rightTextSize = ta.getDimension(R.styleable.SimpleTextView_rightTextSize, 10F)
    rightMaxLines = ta.getInt(R.styleable.SimpleTextView_rightMaxLine, Int.MAX_VALUE)
    divideType = ta.getInt(R.styleable.SimpleTextView_divideLineType, BOTTOM_FULL)
    val rightTextColor = ta.getColor(R.styleable.SimpleTextView_rightTextColor, Color.WHITE)
    val divideColor = ta.getColor(R.styleable.SimpleTextView_divideLineColor, Color.parseColor("#ededed"))
    ta.recycle()
    rightTextPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    rightTextPaint.color = rightTextColor
    rightTextPaint.textSize = rightTextSize
    staticLayout = StaticLayout.Builder.obtain(rightText, 0, rightText.length, rightTextPaint,
        Resources.getSystem().displayMetrics.widthPixels)
        .setMaxLines(rightMaxLines)
        .setEllipsize(TextUtils.TruncateAt.END).build()

    linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    linePaint.color = divideColor

  }

  private fun getOffsetY(): Float {
    var top = 0
    if (compoundDrawables != null && compoundDrawables[1] != null) {
      top = compoundDrawables[1].intrinsicHeight + compoundDrawablePadding
    }
    return ((measuredHeight - staticLayout.height) / 2).toFloat() + top + paddingTop - paddingBottom
  }


  private fun getOffsetX(): Float {
    var left = paddingLeft
    if (compoundDrawables != null && compoundDrawables[0] != null) {
      left += compoundDrawables[0].intrinsicWidth + compoundDrawablePadding
    }
    var right = paddingRight
    if (compoundDrawables != null && compoundDrawables[2] != null) {
      right = compoundDrawables[2].intrinsicWidth + compoundDrawablePadding
    }
    var rightTextWidth = 0F
    val lineCount = staticLayout.lineCount
    (0 until lineCount).toList()
        .forEach {
          rightTextWidth = Math.max(rightTextWidth,
              rightTextPaint.measureText(rightText.substring(staticLayout.getLineStart(it),
                  staticLayout.getLineEnd(it))))
        }
    val total = left + right + paint.measureText(text.toString())
    var result: Float
    if (rightTextWidth > total) {
      result = measuredWidth - total
      staticLayout = StaticLayout.Builder.obtain(rightText, 0, rightText.length, rightTextPaint,
          result.toInt() - 20)
          .setMaxLines(rightMaxLines)
          .setEllipsize(TextUtils.TruncateAt.END).build()
      result = measuredWidth - result - right + 20
      requestLayout()
    } else {
      result = measuredWidth - rightTextWidth - right - 20
    }
    return result
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    canvas!!.save()
    canvas.translate(getOffsetX(), getOffsetY())
    staticLayout.draw(canvas)
    canvas.restore()
    if (divideType == BOTTOM_FULL) {
      canvas.drawLine(0F, measuredHeight.toFloat(), measuredWidth.toFloat(), measuredHeight.toFloat(),
          linePaint)
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
    val width = View.MeasureSpec.getSize(widthMeasureSpec)
    val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
    val height = View.MeasureSpec.getSize(heightMeasureSpec)
    val w: Int
    val h: Int
    val fontMetrics = paint.fontMetrics
    w = if (widthMode == View.MeasureSpec.EXACTLY)
      width else paint.measureText(text.toString()).toInt()
    h = if (heightMode == View.MeasureSpec.EXACTLY) {
      height
    } else {
      (fontMetrics.bottom - fontMetrics.top).toInt() * staticLayout.lineCount
      staticLayout.height + paddingTop + paddingBottom
    }
    setMeasuredDimension(w, h)
  }
}