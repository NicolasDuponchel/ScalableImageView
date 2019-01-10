package duponchel.nicolas.scalableimageview

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.ImageView
import duponchel.nicolas.scalableimageview.ScalableImageView.MatrixCropType.*

/**
 * A {@link ImageView} which allows custom scale types app:matrixType="fitTop/fitBottom".
 * Use it if you need a scaleType="cropCenter|fitTop" or scaleType="cropCenter|fitBottom"
 */
class ScalableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : ImageView(context, attrs, defStyleAttr) {

    private val matrixType: MatrixCropType

    init {
        scaleType = ImageView.ScaleType.MATRIX
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ScalableImageView, defStyleAttr, 0)
        matrixType = MatrixCropType.getType(typedArray.getString(R.styleable.ScalableImageView_matrixType))
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        computeDrawableMatrix()
        return super.setFrame(l, t, r, b)
    }

    private fun computeDrawableMatrix() = drawable?.let {
        val matrix = imageMatrix
        val viewWidth = (measuredWidth - (paddingLeft + paddingRight)).toFloat()
        val viewHeight = (measuredHeight - (paddingTop + paddingBottom)).toFloat()
        val drawableHeight = it.intrinsicHeight.toFloat()
        val drawableWidth = it.intrinsicWidth.toFloat()
        val scale =
            if (it.intrinsicWidth * viewHeight > it.intrinsicHeight * viewWidth)
                viewHeight / drawableHeight
            else
                viewWidth / drawableWidth
        val offset = viewHeight / scale

        val drawableRect = when (matrixType) {
            FIT_BOTTOM -> RectF(0f, drawableHeight - offset, drawableWidth, drawableHeight - TRUNCATE_PATCH)
            FIT_TOP -> RectF(0f, TRUNCATE_PATCH, drawableWidth, offset)
        }
        val viewRect = RectF(0f, 0f, viewWidth, viewHeight)
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.FILL)
        imageMatrix = matrix
    }

    companion object {
        private const val TRUNCATE_PATCH = 0.5f
    }

    private enum class MatrixCropType(val id: String) {
        FIT_TOP("0"), FIT_BOTTOM("1");

        companion object {
            fun getType(id: String?) = values().firstOrNull { it.id == id } ?: FIT_BOTTOM
        }
    }
}