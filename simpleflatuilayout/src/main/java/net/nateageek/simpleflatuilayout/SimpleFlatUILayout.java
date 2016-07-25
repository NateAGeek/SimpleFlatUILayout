package net.nateageek.simpleflatuilayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by NateAGeek on 6/26/2016.
 */
public class SimpleFlatUILayout extends ViewGroup {

    public SimpleFlatUILayout(Context context) {
        super(context);
        init(context);
    }
    public SimpleFlatUILayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public SimpleFlatUILayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Init the measurements
        int measuredWidth  = 0;
        int measuredHeight = 0;

        //Get the info on how to layout this measurement
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);

        if(widthMode == MeasureSpec.EXACTLY){
            measuredWidth = widthSize;
        }
        if(heightMode == MeasureSpec.EXACTLY){
            measuredHeight = heightSize;
        }

        for (int i = 0; i < this.getChildCount(); i++) {
            final View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);

            //Get the maximum sizes for WRAP_CONTENT,
            // @TODO I want to move this outside of the loop if possible
            if(widthMode == MeasureSpec.AT_MOST) {
                int temp_max_right = view.getLeft() + view.getMeasuredWidth();
                if (measuredWidth < temp_max_right) {
                    measuredWidth = temp_max_right;
                }
            }
            if(heightMode == MeasureSpec.AT_MOST) {
                int temp_max_bottom = view.getTop() + view.getMeasuredHeight();
                if (measuredHeight < temp_max_bottom) {
                    measuredHeight = temp_max_bottom;
                }
            }
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom){
        for (int i = 0; i < this.getChildCount(); i++) {
            final View view = getChildAt(i);
            SimpleRect position = getRelativeLayout(view);
            view.layout(position.x,
                    position.y ,
                    position.x + position.width,
                    position.y + position.height);
        }
    }

    private SimpleRect getRelativeLayout(View contextView) {
        LayoutParams layoutParams = (LayoutParams) contextView.getLayoutParams();
        if(layoutParams.relation_view_id == -1 && layoutParams.alignment == -1){
            return new SimpleRect(layoutParams.xPosition,
                    layoutParams.yPosition,
                    contextView.getMeasuredWidth(),
                    contextView.getMeasuredHeight());
        }

        int calculated_x      = layoutParams.xPosition,
                calculated_y      = layoutParams.yPosition,
                calculated_width  = contextView.getMeasuredWidth(),
                calculated_height = contextView.getMeasuredHeight();

        //Get the relative associated view from the attributes
        final View relativeView;
        if(layoutParams.relation_view_id == -1){
            relativeView = this;
        }else {
            relativeView = findViewById(layoutParams.relation_view_id);
            //If we are messing with a relative view, we need to make sure to account for
            //it's offset when centering around it...
            calculated_x += relativeView.getLeft();
            calculated_y += relativeView.getTop();

        }

        //Are we doing any alignment for x axis?
        if((layoutParams.alignment & LayoutParams.ALIGNMENT_RIGHT) == LayoutParams.ALIGNMENT_RIGHT) {
            calculated_x += relativeView.getMeasuredWidth();
        } else if ((layoutParams.alignment & LayoutParams.ALIGNMENT_LEFT) == LayoutParams.ALIGNMENT_LEFT){
            calculated_x += -contextView.getMeasuredWidth();
        }

        //Are we doing any alignment for y axis?
        if((layoutParams.alignment & LayoutParams.ALIGNMENT_TOP) == LayoutParams.ALIGNMENT_TOP) {
            calculated_y += -contextView.getMeasuredHeight();
        } else if ((layoutParams.alignment & LayoutParams.ALIGNMENT_BOTTOM) == LayoutParams.ALIGNMENT_BOTTOM) {
            calculated_y += relativeView.getMeasuredHeight();
        }

        //Are we doing any alignment with center?
        if((layoutParams.alignment & LayoutParams.ALIGNMENT_CENTER) == LayoutParams.ALIGNMENT_CENTER) {
            //Are we doing top or bottom centering?
            if (layoutParams.alignment >= (LayoutParams.ALIGNMENT_TOP | LayoutParams.ALIGNMENT_CENTER)) {
                calculated_x += relativeView.getMeasuredWidth()/2 - calculated_width/2;

                //Right or left centering
            } else if (layoutParams.alignment >= (LayoutParams.ALIGNMENT_RIGHT | LayoutParams.ALIGNMENT_CENTER)) {
                calculated_y += relativeView.getMeasuredHeight()/2 - calculated_height/2;
                //General Centering
            } else if (layoutParams.alignment == LayoutParams.ALIGNMENT_CENTER){
                calculated_x += relativeView.getMeasuredWidth()/2 - calculated_width/2;
                calculated_y += relativeView.getMeasuredHeight()/2 - calculated_height/2;
            }
        } else if((layoutParams.alignment & LayoutParams.ALIGNMENT_PULL) == LayoutParams.ALIGNMENT_PULL){
            //Are we doing top or bottom centering?
            if (layoutParams.alignment >= (LayoutParams.ALIGNMENT_TOP | LayoutParams.ALIGNMENT_PULL)) {
                calculated_x = this.getRight() - calculated_width;

                //Right or left centering
            } else if (layoutParams.alignment >= (LayoutParams.ALIGNMENT_RIGHT | LayoutParams.ALIGNMENT_PULL)) {
                calculated_y = this.getBottom() - calculated_height;
            }
        }

        if((layoutParams.alignment & LayoutParams.ALIGNMENT_INTERNAL) == LayoutParams.ALIGNMENT_INTERNAL){
            //Doing internal alignment right or left
            if((layoutParams.alignment & LayoutParams.ALIGNMENT_LEFT) == LayoutParams.ALIGNMENT_LEFT){
                calculated_x += contextView.getMeasuredWidth();
            } else if((layoutParams.alignment & LayoutParams.ALIGNMENT_RIGHT) == LayoutParams.ALIGNMENT_RIGHT) {
                calculated_x -= contextView.getMeasuredWidth();
            }

            //Doing internal alignment top or bottom
            if((layoutParams.alignment & LayoutParams.ALIGNMENT_TOP) == LayoutParams.ALIGNMENT_TOP){
                calculated_y += contextView.getMeasuredHeight();
            } else if((layoutParams.alignment & LayoutParams.ALIGNMENT_BOTTOM) == LayoutParams.ALIGNMENT_BOTTOM) {
                calculated_y -= contextView.getMeasuredHeight();
            }
        }

        return new SimpleRect(calculated_x, calculated_y, calculated_width, calculated_height);
    }

    /**
     * This is the area for methods involving the layouts
     **/
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }
    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public static int ALIGNMENT_RIGHT     = 0b00000010,
                ALIGNMENT_LEFT     = 0b00000100,
                ALIGNMENT_TOP      = 0b00001000,
                ALIGNMENT_BOTTOM   = 0b00010000,
                ALIGNMENT_CENTER   = 0b00100000,
                ALIGNMENT_PULL     = 0b01000000,
                ALIGNMENT_INTERNAL = 0b00000001;

        private int xPosition = 0;
        private int yPosition = 0;
        private int alignment = 0;
        private int relation_view_id = -1;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.FlatUILayout_LayoutParams);
            try {
                xPosition = attributes.getDimensionPixelSize(R.styleable.FlatUILayout_LayoutParams_x_position,    0);
                yPosition = attributes.getDimensionPixelOffset(R.styleable.FlatUILayout_LayoutParams_y_position,  0);

                relation_view_id = attributes.getResourceId(R.styleable.FlatUILayout_LayoutParams_relation, -1);
                alignment = attributes.getInt(R.styleable.FlatUILayout_LayoutParams_align, 0);
            } finally {
                attributes.recycle();
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public int getXPosition() {
            return xPosition;
        }
        public int getYPosition() {
            return yPosition;
        }
        public int getRelativeByID() {
            return relation_view_id;
        }
        public int getAlignment() {
            return alignment;
        }

        public void setXPosition(int xPosition) {
            this.xPosition = xPosition;
        }
        public void setYPosition(int yPosition) {
            this.yPosition = yPosition;
        }
        public void setRelativeByID(int relative_view_id) {
            this.relation_view_id = relative_view_id;
        }
        public void setAlignment(int alignment) {
            this.alignment = alignment;
        }
    }

    //This is a simple dummy class for storing rectangles... Why I miss structs...
    private static class SimpleRect {
        protected int x = 0, y = 0, width = 0, height = 0;
        SimpleRect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width  = width;
            this.height = height;
        }
    }
}
