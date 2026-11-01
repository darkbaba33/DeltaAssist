package com.delta.assist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuView extends LinearLayout {
    private final WindowManager wm;
    private final WindowManager.LayoutParams params;
    private float initialX, initialY, initialTouchX, initialTouchY;

    private LinearLayout menuContent;
    private ImageView logoBtn;
    private TextView btnVisuals, btnAim, titleText;
    private LinearLayout tabVisuals, tabAim;
    private GradientDrawable menuBg;
    
    private final int COLOR_BG = Color.parseColor("#E6100A1C");
    private int currentColorIndex = 0;
    private final int[] THEMES = {0xFFB388FF, 0xFF00E5FF, 0xFFFF5252, 0xFF69F0AE, 0xFFFFAB40};

    public MenuView(Context context, WindowManager wm, WindowManager.LayoutParams params) {
        super(context);
        this.wm = wm;
        this.params = params;
        setOrientation(VERTICAL);
        
        logoBtn = new ImageView(context);
        logoBtn.setImageResource(android.R.drawable.sym_def_app_icon); 
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(120, 120);
        logoParams.setMargins(10, 10, 10, 10);
        logoBtn.setLayoutParams(logoParams);
        
        GradientDrawable logoBg = new GradientDrawable();
        logoBg.setShape(GradientDrawable.OVAL);
        logoBg.setColor(Color.parseColor("#CC000000"));
        logoBg.setStroke(4, Config.themeColor);
        logoBtn.setBackground(logoBg);
        
        addView(logoBtn);

        menuContent = new LinearLayout(context);
        menuContent.setOrientation(VERTICAL);
        menuContent.setPadding(40, 40, 40, 40);
        menuContent.setVisibility(GONE);

        menuBg = new GradientDrawable();
        menuBg.setColor(COLOR_BG);
        menuBg.setCornerRadius(40f);
        menuBg.setStroke(4, Config.themeColor);
        menuContent.setBackground(menuBg);

        titleText = new TextView(context);
        titleText.setText("Buse cheat delta force");
        titleText.setTextColor(Config.themeColor);
        titleText.setTextSize(18f);
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 30);
        menuContent.addView(titleText);

        Button colorBtn = new Button(context);
        colorBtn.setText("Tema Rengini Degistir");
        colorBtn.setTextSize(12f);
        colorBtn.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) { changeTheme(); }
        });
        menuContent.addView(colorBtn);

        LinearLayout tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(HORIZONTAL);
        tabsLayout.setGravity(Gravity.CENTER);
        tabsLayout.setPadding(0, 30, 0, 30);

        btnVisuals = createTabButton("Gorseller");
        btnAim = createTabButton("Nisan");
        
        tabsLayout.addView(btnVisuals);
        tabsLayout.addView(btnAim);
        menuContent.addView(tabsLayout);

        tabVisuals = new LinearLayout(context);
        tabVisuals.setOrientation(VERTICAL);
        tabVisuals.addView(createToggleRow("Kutu (ESP)", Config.showBox, new OnToggle() {
            @Override public void toggle(boolean b) { Config.showBox = b; }
        }));
        tabVisuals.addView(createToggleRow("Hedef Cizgisi", Config.showLine, new OnToggle() {
            @Override public void toggle(boolean b) { Config.showLine = b; }
        }));
        tabVisuals.addView(createToggleRow("Isim Etiketi", Config.showName, new OnToggle() {
            @Override public void toggle(boolean b) { Config.showName = b; }
        }));
        tabVisuals.addView(createToggleRow("Gorsel Iskelet", Config.showSkeleton, new OnToggle() {
            @Override public void toggle(boolean b) { Config.showSkeleton = b; }
        }));

        tabAim = new LinearLayout(context);
        tabAim.setOrientation(VERTICAL);
        tabAim.addView(createToggleRow("Aim Assist", Config.aimEnabled, new OnToggle() {
            @Override public void toggle(boolean b) { Config.aimEnabled = b; }
        }));

        menuContent.addView(tabVisuals);
        menuContent.addView(tabAim);
        btnVisuals.performClick();

        addView(menuContent);

        logoBtn.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                menuContent.setVisibility(menuContent.getVisibility() == VISIBLE ? GONE : VISIBLE);
            }
        });

        logoBtn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x; initialY = params.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        return false; 
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

    private void changeTheme() {
        currentColorIndex = (currentColorIndex + 1) % THEMES.length;
        Config.themeColor = THEMES[currentColorIndex];
        titleText.setTextColor(Config.themeColor);
        menuBg.setStroke(4, Config.themeColor);
        GradientDrawable bg = (GradientDrawable) logoBtn.getBackground();
        if (bg != null) bg.setStroke(4, Config.themeColor);
    }

    private TextView createTabButton(String text) {
        TextView btn = new TextView(getContext());
        btn.setText(text);
        btn.setTextSize(16f);
        btn.setPadding(40, 15, 40, 15);
        btn.setTextColor(Color.GRAY);
        btn.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                boolean isVis = text.equals("Gorseller");
                btnVisuals.setTextColor(isVis ? Config.themeColor : Color.GRAY);
                btnAim.setTextColor(isVis ? Color.GRAY : Config.themeColor);
                tabVisuals.setVisibility(isVis ? VISIBLE : GONE);
                tabAim.setVisibility(isVis ? GONE : VISIBLE);
            }
        });
        return btn;
    }

    private LinearLayout createToggleRow(String text, boolean initial, final OnToggle listener) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setPadding(0, 20, 0, 20);
        
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16f);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(p);
        row.addView(tv);

        final TextView stateTv = new TextView(getContext());
        stateTv.setText(initial ? "[ ACIK ]" : "[ KAPALI ]");
        stateTv.setTextColor(initial ? Color.GREEN : Color.RED);
        stateTv.setTextSize(16f);
        
        final boolean[] state = {initial};
        stateTv.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                state[0] = !state[0];
                stateTv.setText(state[0] ? "[ ACIK ]" : "[ KAPALI ]");
                stateTv.setTextColor(state[0] ? Color.GREEN : Color.RED);
                listener.toggle(state[0]);
            }
        });
        row.addView(stateTv);
        return row;
    }
    
    private interface OnToggle { void toggle(boolean isChecked); }
}
