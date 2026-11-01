package com.delta.assist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MenuView extends LinearLayout {
    private final WindowManager wm;
    private final WindowManager.LayoutParams params;
    private float initialX, initialY;
    private float initialTouchX, initialTouchY;

    public MenuView(Context context, WindowManager wm, WindowManager.LayoutParams params) {
        super(context);
        this.wm = wm;
        this.params = params;
        setOrientation(VERTICAL);
        setPadding(30, 30, 30, 30);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#CC001020"));
        bg.setCornerRadius(25f);
        bg.setStroke(3, Color.CYAN);
        setBackground(bg);

        TextView title = new TextView(context);
        title.setText("Buse naz delta force");
        title.setTextColor(Color.CYAN);
        title.setTextSize(18f);
        title.setGravity(Gravity.CENTER);
        addView(title);

        Switch espSwitch = new Switch(context);
        espSwitch.setText("Enable ESP");
        espSwitch.setTextColor(Color.WHITE);
        espSwitch.setChecked(true);
        espSwitch.setOnCheckedChangeListener((v, isChecked) -> Config.espEnabled = isChecked);
        addView(espSwitch);

        Switch aimSwitch = new Switch(context);
        aimSwitch.setText("Enable Aim");
        aimSwitch.setTextColor(Color.WHITE);
        aimSwitch.setOnCheckedChangeListener((v, isChecked) -> Config.aimEnabled = isChecked);
        addView(aimSwitch);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x; initialY = params.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = (int) (initialX + (event.getRawX() - initialTouchX));
                        params.y = (int) (initialY + (event.getRawY() - initialTouchY));
                        wm.updateViewLayout(MenuView.this, params);
                        return true;
                }
                return false;
            }
        });
    }
}
