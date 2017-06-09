package com.leon.barchartdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {

    private static final String TAG = "BarChartView";

    private Paint mBarPaint;
    private Paint mAxisPaint;


    private float[] mDataList;//柱状图数据列表
    private String[] mHorizontalAxis;//水平方向x轴坐标


    private float mBarWidth;
    private float mMax;//数据集合的最大值
    private int mGap;//坐标文本与柱状条之间间隔的变量
    private int mRadius;
    private int mSelectedIndex = -1;
    private int mBarGrowStep = 15;
    private boolean enableGrowAnimation = true;

    private static final int DELAY = 10;

    private Rect mTextRect;
    private RectF mTemp;

    private List<Bar> mBars = new ArrayList<Bar>();

    public BarChartView(Context context) {
        this(context, null);
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();//初始化方法
    }

    private void init() {
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setTextSize(20);
        mAxisPaint.setTextAlign(Paint.Align.CENTER);

        mBarPaint = new Paint();
        mBarPaint.setColor(Color.BLUE);
        mBarPaint.setAntiAlias(true);

        mTextRect = new Rect();
        mTemp = new RectF();
        mBarWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        mGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBars.clear();//清空柱状条Bar的集合

        //去除padding，计算绘制所有柱状条所占的宽和高
        int width = w - getPaddingLeft() - getPaddingRight();
        int height = h - getPaddingTop() - getPaddingBottom();

        //按照数据集合的大小平分宽度
        int step = width / mDataList.length;

        //mBarWidth为柱状条宽度的变量，可以设置，mRadius为柱状条宽度的一半
        mRadius = (int) (mBarWidth / 2);

        //计算第一条柱状条的左边位置
        int barLeft = getPaddingLeft() + step / 2 - mRadius;

        //通过坐标文本画笔计算绘制x轴第一个坐标文本占据的矩形边界，这里主要获取其高度，为计算maxBarHeight提供数据
        mAxisPaint.getTextBounds(mHorizontalAxis[0], 0, mHorizontalAxis[0].length(), mTextRect);

        //计算柱状条高度的最大像素大小，mTextRect.height为底部x轴坐标文本的高度，mGap为坐标文本与柱状条之间间隔的变量
        int maxBarHeight = height - mTextRect.height() - mGap;

        //计算柱状条最大像素大小与最大数据值的比值
        float heightRatio = maxBarHeight / mMax;

        //遍历数据集合，初始化所有的柱状条Bar对象
        for (float data : mDataList) {
            Bar bar = new Bar();//创建柱状条对象
            bar.value = data;//设置原始数据
            bar.transformedValue = bar.value * heightRatio;//计算原始数据对应的像素高度大小
            //计算绘制柱状条的四个位置
            bar.left = barLeft;
            bar.top = (int) (getPaddingTop() + maxBarHeight - bar.transformedValue);
            bar.right = (int) (barLeft + mBarWidth);
            bar.bottom = getPaddingTop() + maxBarHeight;

            //初始化绘制柱状条时当前的top值，用作动画
            bar.currentTop = bar.bottom;

            //将初始化好的Bar添加到集合中
            mBars.add(bar);
            //更新柱状条左边位置，为初始化下一个Bar对象做准备
            barLeft += step;
        }
    }

    /**
     * 绘制柱状图
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (enableGrowAnimation) {
            drawBarsWidthAnimation(canvas);
        } else {
            drawBars(canvas);
        }
    }


    private void drawBarsWidthAnimation(Canvas canvas) {
        int minTop = getHeight();
        for (int i = 0; i < mDataList.length; i++) {
            Bar bar = mBars.get(i);
            String axis = mHorizontalAxis[i];
            canvas.drawText(axis, bar.left + mBarWidth / 2, getHeight() - getPaddingBottom(), mAxisPaint);

            bar.currentTop -= mBarGrowStep;
            bar.done = bar.currentTop <= bar.top;
            if (bar.done) {
                bar.currentTop = bar.top;
                if (bar.top < minTop) {
                    minTop = bar.top;
                }
            }
            mTemp.set(bar.left, bar.currentTop, bar.right, bar.bottom);
            canvas.drawRoundRect(mTemp, mRadius, mRadius, mBarPaint);
        }

        if (minTop > getPaddingTop()) {
            postInvalidateDelayed(DELAY);
        } else {
            //动画结束
            enableGrowAnimation = false;
        }
    }


    /**
     * 绘制柱状条，没有动画效果的模式
     */
    private void drawBars(Canvas canvas) {
        //遍历所有的Bar对象，一个个绘制
        for (int i = 0; i < mBars.size(); i++) {
            Bar bar = mBars.get(i);

            //绘制底部x轴坐标文本
            String axis = mHorizontalAxis[i];//获取对应位置的坐标文本
            //计算绘制文本的起始位置坐标(textX，textY)，textX为柱状条的中线位置，由于我们对画笔mAxisPaint设置了
            //Paint.Align.CENTER，所以绘制出来的文本的中线与柱状条的中线是重合的
            float textX = bar.left + mRadius;
            float textY = getHeight() - getPaddingBottom();
            canvas.drawText(axis, textX, textY, mAxisPaint);

            if (i == mSelectedIndex) {
                mBarPaint.setColor(Color.RED);
                float x = bar.left + mRadius;
                float y = bar.top - mGap;
                canvas.drawText(String.valueOf(bar.value), x, y, mAxisPaint);
            } else {
                mBarPaint.setColor(Color.BLUE);
            }
            mTemp.set(bar.left, bar.top, bar.right, bar.bottom);
            //绘制圆角矩形
            canvas.drawRoundRect(mTemp, mRadius, mRadius, mBarPaint);
            //绘制直角矩形
            //canvas.drawRect(mTemp, mBarPaint);
        }
    }


    /**
     * 设置水平方向x轴坐标值
     * @param horizontalAxis 坐标值数组，如{"1", "2", "3", "4","5", "6", "7", "8", "9", "10", "11", "12"}
     */
    public void setHorizontalAxis(String[] horizontalAxis) {
        mHorizontalAxis = horizontalAxis;
    }

    /**
     * 设置柱状图数据
     * @param dataList 数据数组，如{12, 24, 45, 56, 89, 70, 49, 22, 23, 10, 12, 3}
     * @param max 数据数组中的最大值，如89，最大值用来计算绘制时的高度比例
     */
    public void setDataList(float[] dataList, int max) {
        mDataList = dataList;
        mMax = max;
    }

    private class Bar {
        //绘制柱状条的四个位置
        int left;
        int top;
        int right;
        int bottom;

        float value;//柱状条原始数据的大小
        float transformedValue;//柱状条原始数据大小转换成对应的像素大小

        int currentTop;//柱状图动画中会用到，表示当前柱状条当前的top值，取值范围为0到top
        boolean done = false;//动画是否结束

        boolean isInside(float x, float y) {
            return x > left && x < right && y > top && y < bottom;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enableGrowAnimation) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < mBars.size(); i++) {
                    if (mBars.get(i).isInside(event.getX(), event.getY())) {
                        enableGrowAnimation = false;
                        mSelectedIndex = i;
                        invalidate();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mSelectedIndex = -1;
                enableGrowAnimation = false;
                invalidate();
                break;
        }
        return true;
    }
}
