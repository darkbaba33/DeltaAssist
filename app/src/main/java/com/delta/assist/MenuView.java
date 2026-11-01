package com.delta.assist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
    
    private final int COLOR_BG = Color.parseColor("#EE0B041A"); // Koyu Mor/Siyah
    private final int COLOR_ACCENT = Color.parseColor("#D500F9"); // Neon Mor

    public MenuView(Context context, WindowManager wm, WindowManager.LayoutParams params) {
        super(context);
        this.wm = wm;
        this.params = params;
        setOrientation(VERTICAL);
        
        // --- LOGO BUTONU ---
        logoBtn = new ImageView(context);
        logoBtn.setImageResource(android.R.drawable.sym_def_app_icon); 
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(120, 120);
        logoParams.setMargins(10, 10, 10, 10);
        logoBtn.setLayoutParams(logoParams);
        GradientDrawable logoBg = new GradientDrawable();
        logoBg.setShape(GradientDrawable.OVAL);
        logoBg.setColor(Color.parseColor("#CC000000"));
        logoBg.setStroke(4, COLOR_ACCENT);
        logoBtn.setBackground(logoBg);
        addView(logoBtn);

        // --- ANA MENÜ İÇERİĞİ ---
        menuContent = new LinearLayout(context);
        menuContent.setOrientation(VERTICAL);
        menuContent.setPadding(50, 50, 50, 50);
        menuContent.setVisibility(GONE);
        
        // Menünün genişliği sabitlendi (Dikey yazıyı engeller)
        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menuContent.setLayoutParams(menuParams);

        menuBg = new GradientDrawable();
        menuBg.setColor(COLOR_BG);
        menuBg.setCornerRadius(40f);
        menuBg.setStroke(4, COLOR_ACCENT);
        menuContent.setBackground(menuBg);

        // Başlık
        titleText = new TextView(context);
        titleText.setText("BUSE CHEAT");
        titleText.setTextColor(COLOR_ACCENT);
        titleText.setTextSize(24f);
        titleText.setGravity(Gravity.CENTER);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setPadding(0, 0, 0, 30);
        menuContent.addView(titleText);

        // Sekmeler (Tabs)
        LinearLayout tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(HORIZONTAL);
        tabsLayout.setGravity(Gravity.CENTER);
        tabsLayout.setPadding(0, 20, 0, 20);

        btnVisuals = createTabButton("GÖRSELLER");
        btnAim = createTabButton("NİŞAN");
        tabsLayout.addView(btnVisuals);
        tabsLayout.addView(btnAim);
        menuContent.addView(tabsLayout);

        // Sekme İçerikleri
        tabVisuals = new LinearLayout(context);
        tabVisuals.setOrientation(VERTICAL);
        tabVisuals.addView(createToggleRow("Kutu (Box ESP)", Config.showBox, b -> Config.showBox = b));
        tabVisuals.addView(createToggleRow("Hedef Çizgisi", Config.showLine, b -> Config.showLine = b));
        tabVisuals.addView(createToggleRow("İsim Etiketi", Config.showName, b -> Config.showName = b));
        tabVisuals.addView(createToggleRow("İskelet (Fake)", Config.showSkeleton, b -> Config.showSkeleton = b));

        tabAim = new LinearLayout(context);
        tabAim.setOrientation(VERTICAL);
        tabAim.addView(createToggleRow("Aim Assist", Config.aimEnabled, b -> Config.aimEnabled = b));

        menuContent.addView(tabVisuals);
        menuContent.addView(tabAim);
        btnVisuals.performClick();

        addView(menuContent);

        // --- LOGO TIKLAMA VE SÜRÜKLEME ---
        logoBtn.setOnClickListener(v -> {
            menuContent.setVisibility(menuContent.getVisibility() == VISIBLE ? GONE : VISIBLE);
        });

        logoBtn.setOnTouchListener((v, event) -> {
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
        });
    }

    private TextView createTabButton(String text) {
        TextView btn = new TextView(getContext());
        btn.setText(text);
        btn.setTextSize(16f);
        btn.setPadding(40, 15, 40, 15);
        btn.setTextColor(Color.GRAY);
        btn.setTypeface(null, android.graphics.Typeface.BOLD);
        btn.setOnClickListener(v -> {
            boolean isVis = text.equals("GÖRSELLER");
            btnVisuals.setTextColor(isVis ? COLOR_ACCENT : Color.GRAY);
            btnAim.setTextColor(isVis ? Color.GRAY : COLOR_ACCENT);
            tabVisuals.setVisibility(isVis ? VISIBLE : GONE);
            tabAim.setVisibility(isVis ? GONE : VISIBLE);
        });
        return btn;
    }

    private LinearLayout createToggleRow(String text, boolean initial, final OnToggle listener) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setPadding(0, 25, 0, 25);
        
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(18f);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(p);
        row.addView(tv);

        final TextView stateTv = new TextView(getContext());
        stateTv.setText(initial ? "AÇIK" : "KAPALI");
        stateTv.setTextColor(initial ? Color.GREEN : Color.RED);
        stateTv.setTextSize(16f);
        stateTv.setTypeface(null, android.graphics.Typeface.BOLD);
        
        final boolean[] state = {initial};
        stateTv.setOnClickListener(v -> {
            state[0] = !state[0];
            stateTv.setText(state[0] ? "AÇIK" : "KAPALI");
            stateTv.setTextColor(state[0] ? Color.GREEN : Color.RED);
            listener.toggle(state[0]);
        });
        row.addView(stateTv);
        return row;
    }
    private interface OnToggle { void toggle(boolean isChecked); }
}
