package com.helloheloo.collapsetextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;


/**
 * Created by yangming on 2017/11/24.
 */

public class CollapseTextView extends AppCompatTextView {

    private String original_text;
    private String expand_text;

    //原始文本 展开文本的paint
    TextPaint originalPaint, expandPaint;
    //原始文本 展开文本的layout
    StaticLayout originalLayout, expandLayout;
    //原始文本最后一行的矩形范围 不包含空白范围
    Rect lastLineRect_OriginalText = new Rect();

    Bitmap bmExpand, bmCollapse;
    int bmExpand_W, bmCollapse_W, bmExpand_H, bmCollapse_H;
    //展开图片 收缩图片的起始位置
    int bmExpand_startX, bmExpand_startY, bmCollapse_startX, bmCollapse_startY;
    //bmExpandRect   展开图片的矩形范围（以原始文本originalLayout为起点 (0,0)）
    //bmCollapseRect 收缩图片的矩形范围（以展开文本expandLayout为起点 (0,0)）
    //bmCollapseRect4View 收缩图片在界面中的实际矩形范围 (展开图片是从控件的（0，0）位置开始画，不需要转换在控件中的位置)
    Rect bmExpandRect, bmCollapseRect, bmCollapseRect4View;

    //标示展开 收缩  用于onDraw()
    boolean isExpand, isCollapse;
    //展开图片 收缩图片是否需要换行显示
    private boolean isExpandHuanhang, isCollapseHuanhang;
    //该控件的宽度
    private int width;

    //缩放因子，有时图片比单行文字高 需要缩放到文字的高度
    float scaleFactor;

    public CollapseTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public CollapseTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CollapseTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CollapseTextView, defStyle, 0);
        original_text = a.getString(R.styleable.CollapseTextView_originalText);
        float originalTextSize = a.getDimension(R.styleable.CollapseTextView_originalTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        int originalTextColor = a.getColor(R.styleable.CollapseTextView_originalTextColor, Color.parseColor("#999999"));


        expand_text = a.getString(R.styleable.CollapseTextView_expandText);
        float expandTextSize = a.getDimension(R.styleable.CollapseTextView_expandTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        int expandTextColor = a.getColor(R.styleable.CollapseTextView_expandTextColor, Color.parseColor("#666666"));

        originalPaint = new TextPaint();
        originalPaint.setTextSize(originalTextSize);
        originalPaint.setColor(originalTextColor);

        expandPaint = new TextPaint();
        expandPaint.setTextSize(expandTextSize);
        expandPaint.setColor(expandTextColor);


        //展开图片
        Drawable expandDrawable = a.getDrawable(R.styleable.CollapseTextView_expandImage);
        if (expandDrawable != null && expandDrawable instanceof BitmapDrawable) {
            BitmapDrawable bmExpandDrawable = (BitmapDrawable) expandDrawable;
            bmExpand = bmExpandDrawable.getBitmap();
        }
        if (bmExpand == null) {
            bmExpand = BitmapFactory.decodeResource(getResources(), R.mipmap.but_open_text);
        }

        //收缩图片
        Drawable collapseDrawable = a.getDrawable(R.styleable.CollapseTextView_collapseImage);
        if (collapseDrawable != null && collapseDrawable instanceof BitmapDrawable) {
            BitmapDrawable bmCollapseDrawable = (BitmapDrawable) collapseDrawable;
            bmCollapse = bmCollapseDrawable.getBitmap();
        }
        if (bmCollapse == null) {
            bmCollapse = BitmapFactory.decodeResource(getResources(), R.mipmap.but_close_text);
        }

        bmExpand_W = bmExpand.getWidth();
        bmExpand_H = bmExpand.getHeight();

        bmCollapse_W = bmCollapse.getWidth();
        bmCollapse_H = bmCollapse.getHeight();

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //得到控件的宽度
        width = MeasureSpec.getSize(widthMeasureSpec);

        //初始化文本显示的layout 因为layout的创建需要指定宽度
        if (initLayout()) {
            //控件的高度
            int height;
            if (!isExpand) { //没有展开文本时的高度
                if (isExpandHuanhang) { //点击的展开图片是否在原始文本的下一行
                    height = originalLayout.getHeight() + bmExpand_H + getPaddingTop() + getPaddingBottom();//在下一行 加上图片的高度bmExpand_H
                } else {
                    height = originalLayout.getHeight() + getPaddingTop() + getPaddingBottom();//不在下一行
                }
                setMeasuredDimension(width, height);

            } else {//展开文本时的高度
                if (isCollapseHuanhang) { //点击的收缩图片是否在收缩文本的下一行
                    height = bmExpandRect.bottom + bmCollapse.getHeight() + expandLayout.getHeight() + getPaddingTop() + getPaddingBottom();//在下一行 加上图片的高度bmCollapse.getHeight()
                } else {
                    height = bmExpandRect.bottom + expandLayout.getHeight() + getPaddingTop() + getPaddingBottom();//不在下一行
                }
                setMeasuredDimension(width, height);
            }
            Log.e("xxx","height : " + height);
        }

    }

    /**
     * 初始化文本展示的相关参数
     */
    private boolean initLayout() {
        isExpandHuanhang = false;
        isCollapseHuanhang = false;
        if (original_text == null || original_text.length() == 0 || expand_text == null || expand_text.length() == 0) {
            return false;
        }

        //文字高度 用于收缩图片的缩放 如果图片太大，则缩放到文字的高度

        Rect textBounds = new Rect();
        originalPaint.getTextBounds(original_text, 0, 4, textBounds);
        float textHeight = textBounds.bottom - textBounds.top;
        //获取缩放因子
        if (textHeight != bmExpand_H) {
            scaleFactor = textHeight / (float) bmExpand_H;
        }

        //--------------------------------------------------------------原始文本的相关参数 start--------------------------------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            originalLayout = StaticLayout.Builder.obtain(original_text, 0, original_text.length(), originalPaint, width - getPaddingLeft() - getPaddingRight()).build();
        } else {
            originalLayout = new StaticLayout(original_text, originalPaint, width - getPaddingLeft() - getPaddingRight(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        }


        //原始文本的行数
        int lineCount = originalLayout.getLineCount();
        //获取最后一行文本的矩形坐标 不包含空白的宽度
        originalLayout.getLineBounds(lineCount - 1, lastLineRect_OriginalText);
        //最后一行文本的右边距离
        int original_last_line_right = (int) originalLayout.getLineRight(lineCount - 1);// 同 lastLineRect_OriginalText.right

        //点击的展开图片是否需要换行显示 （显示在最右边）
        if (original_last_line_right + bmExpand_W > width - getPaddingLeft() - getPaddingRight()) {
            //需要换行显示
            //获取图片显示的位置 以0，0坐标为起点
            bmExpand_startX = width - getPaddingRight() - getPaddingLeft() - bmExpand_W;
            bmExpand_startY = lastLineRect_OriginalText.bottom;//原始文本的最后一行的底部距离
            //设置标识，在onmeasure中设置控件的总高度
            isExpandHuanhang = true;
        } else {
            //不需要换行显示
            bmExpand_startX = width - getPaddingRight() - getPaddingLeft() - bmExpand_W;
            bmExpand_startY = lastLineRect_OriginalText.top;//原始文本的最后一行的顶部距离
        }
        //展开图片的显示的矩形范围，用于点击事件的判断
        bmExpandRect = new Rect(bmExpand_startX, bmExpand_startY, bmExpand_startX + bmExpand_W, bmExpand_startY + bmExpand_H);
        //--------------------------------------------------------------原始文本的相关参数 end--------------------------------------------------------------


        //--------------------------------------------------------------展开文本的相关参数 start--------------------------------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            expandLayout = StaticLayout.Builder.obtain(expand_text, 0, expand_text.length(), expandPaint, width - getPaddingLeft() - getPaddingRight()).build();
        } else {
            expandLayout = new StaticLayout(expand_text, expandPaint, width - getPaddingLeft() - getPaddingRight(), Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        }

        //展开文本的行数
        int expandLineCount = expandLayout.getLineCount();
        //最后一行的矩形范围，以0，0坐标为起点
        int expand_last_line_top = expandLayout.getLineTop(expandLineCount - 1);
        int expand_last_line_right = (int) expandLayout.getLineRight(expandLineCount - 1);
        int expand_last_line_bottom = expandLayout.getLineBottom(expandLineCount - 1);

        //点击的收缩图片是否需要换行显示
        if (expand_last_line_right + bmCollapse_W > width - getPaddingRight() - getPaddingLeft()) {
            //图片显示的位置 以0，0坐标为起点
            bmCollapse_startX = width - getPaddingRight() - getPaddingLeft() - bmCollapse_W;
            bmCollapse_startY = expand_last_line_bottom;//最后一行文本的底部距离
            //需要换行
            isCollapseHuanhang = true;
        } else {
            bmCollapse_startX = width - getPaddingRight() - getPaddingLeft() - bmCollapse_W;
            bmCollapse_startY = expand_last_line_top;//最后一行文本的顶部距离
        }
        //收缩图片显示的矩形范围（以expandLayout的0，0为起点）
        bmCollapseRect = new Rect(bmCollapse_startX, bmCollapse_startY, bmCollapse_startX + bmCollapse.getWidth(), bmCollapse_startY + bmCollapse.getHeight());
        //获取图片在界面中实际显示的矩形范围（加上原始文本的高度 用于点击事件的判断）
        bmCollapseRect4View = new Rect(bmCollapseRect);
        bmCollapseRect4View.top += bmExpandRect.bottom;//展开图片有时会换行显示，才是原始文本的实际高度
        bmCollapseRect4View.bottom += bmExpandRect.bottom;
        //--------------------------------------------------------------展开文本的相关参数 end--------------------------------------------------------------

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("xxx", "ondraw...");
        if (expandLayout == null || originalLayout == null) {
            return;
        }
        //padding参数
        float clipTop = getPaddingTop();
        float clipBottom = getBottom() - getTop() - getPaddingBottom();
        float clipLeft = getPaddingLeft();
        float clipRight = getRight() - getLeft() - getPaddingRight();

        //保存画布
        canvas.save();
        //裁剪出padding的范围
        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
        //移动到padding 左上角开始画
        canvas.translate(clipLeft, clipTop);
        //画原始文本
        originalLayout.draw(canvas);

        if (isExpand) {  //展开的时候
            //保存画布，（这里是已经画的原始文本）
            canvas.save();
            //移动画布到原始文本的最下方
            canvas.translate(0, bmExpandRect.bottom);
            //画展开的文本
            expandLayout.draw(canvas);

            //图片缩放
            if (scaleFactor > 0) {
                //保存画布
                canvas.save();
                //移动画布到缩放图片的位置
                canvas.translate(bmCollapse_startX, bmCollapse_startY);
                //画布缩放
                canvas.scale(scaleFactor, scaleFactor);
                //画图片
                canvas.drawBitmap(bmCollapse, 0, 0, null);
                //存储画好的图片
                canvas.restore();
            } else { //没有缩放 直接画图片
                canvas.drawBitmap(bmCollapse, bmCollapse_startX, bmCollapse_startY, null);
            }
            //存储画好的展开文本
            canvas.restore();

        } else {//没有展开的时候，需要画展开图片
            if (scaleFactor > 0) {
                canvas.save();
                canvas.translate(bmExpand_startX, bmExpand_startY);
                canvas.scale(scaleFactor, scaleFactor);
                canvas.drawBitmap(bmExpand, 0, 0, null);
                canvas.restore();
            } else {
                canvas.drawBitmap(bmExpand, bmExpand_startX, bmExpand_startY, null);
            }

        }
        //存储padding裁剪
        canvas.restore();
    }

    private boolean isExpandClick, isCollapseClick;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //当前点击的地方
        int clickX = (int) event.getX();
        int clickY = (int) event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e("xxx", "ACTION_DOWN....");
                //  没有展开文本的时候 && 点击展开图片的范围
                if (!isExpand && bmExpandRect.contains(clickX - getPaddingLeft(), clickY - getPaddingTop())) {
                    //标示已经点击展开图片
                    isExpandClick = true;
                    //清除收缩标识
                    isCollapseClick = false;

                    return true;
                }
                //没有收缩文本的时候 && 点击收缩图片的范围
                if (!isCollapse && bmCollapseRect4View.contains(clickX - getPaddingLeft(), clickY - getPaddingTop())) {
                    //标示已经点击收缩图片
                    isCollapseClick = true;
                    //清除展开标识
                    isExpandClick = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("xxx", "ACTION_MOVE....");
                break;
            case MotionEvent.ACTION_UP:
                Log.e("xxx", "ACTION_UP....");
                //点击了展开图片的时候（只有在没有展开的时候才会进入这里）
                if (isExpandClick) {
                    //标示展开，用于onDraw()
                    isExpand = true;
                    isCollapse = false;
                    //重新测量高度
                    requestLayout();
                    //重新画图
                    invalidate();
                    if (mExpandListener != null) {
                        mExpandListener.onExpand();
                    }
                    //执行点击事件
                    performClick();
                    return true;
                }
                //点击了收缩图片的时候（只有在没有收缩的时候才会进入这里）
                if (isCollapseClick) {
                    //标示收缩，用于onDraw()
                    isCollapse = true;
                    isExpand = false;
                    //重新测量高度
                    requestLayout();
                    //重新画图
                    invalidate();
                    if (mCollapseListener != null) {
                        mCollapseListener.onCollapse();
                    }
                    //执行点击事件
                    performClick();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setOriginalText(String originalText){
        original_text = originalText;
    }

    public void setExpandText(String expandText){
        expand_text = expandText;
    }

    //对外接口 收缩
    public interface CollapseListener {
        void onCollapse();
    }

    //对外接口 展开
    public interface ExpandListener {
        void onExpand();
    }

    ExpandListener mExpandListener;
    CollapseListener mCollapseListener;

    public void setExpandListener(ExpandListener expandListener) {
        mExpandListener = expandListener;
    }

    public void setCollapseListener(CollapseListener collapseListener) {
        mCollapseListener = collapseListener;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }


}