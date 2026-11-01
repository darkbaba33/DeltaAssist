package com.delta.assist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Switch;

public class MenuView extends LinearLayout {
    private final WindowManager wm;
    private final WindowManager.LayoutParams params;
    private float initialX, initialY, initialTouchX, initialTouchY;

    private LinearLayout menuContent;
    private ImageView logoBtn;
    private LinearLayout tabVisuals, tabAim;
    private TextView btnVisuals, btnAim, titleText;
    private GradientDrawable menuBg;
    
    private final int COLOR_BG = Color.parseColor("#E6100A1C");
    private int currentColorIndex = 0;
    private final int[] THEMES = {0xFFB388FF, 0xFF00E5FF, 0xFFFF5252, 0xFF69F0AE, 0xFFFFAB40};

    public MenuView(Context context, WindowManager wm, WindowManager.LayoutParams params) {
        super(context);
        this.wm = wm;
        this.params = params;
        setOrientation(VERTICAL);
        
        // 1. KÜÇÜK LOGO (Varsayılan Simge)
        logoBtn = new ImageView(context);
        logoBtn.setImageResource(android.R.drawable.sym_def_app_icon); 
        logoBtn.setColorFilter(Config.themeColor);
        logoBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(130, 130);
        logoParams.setMargins(10, 10, 10, 10);
        logoBtn.setLayoutParams(logoParams);
        
        GradientDrawable logoBg = new GradientDrawable();
        logoBg.setShape(GradientDrawable.OVAL);
        logoBg.setColor(Color.parseColor("#CC000000"));
        logoBg.setStroke(4, Config.themeColor);
        logoBtn.setBackground(logoBg);
        
        addView(logoBtn);

        // 2. ANA MENÜ İÇERİĞİ (Başlangıçta gizli)
        menuContent = new LinearLayout(context);
        menuContent.setOrientation(VERTICAL);
        menuContent.setPadding(30, 30, 30, 30);
        menuContent.setVisibility(GONE);

        menuBg = new GradientDrawable();
        menuBg.setColor(COLOR_BG);
        menuBg.setCornerRadius(40f);
        menuBg.setStroke(4, Config.themeColor);
        menuContent.setBackground(menuBg);

        titleText = new TextView(context);
        titleText.setText("Buse cheat delta force");
        titleText.setTextColor(Config.themeColor);
        titleText.setTextSize(16f);
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 20);
        menuContent.addView(titleText);

        Button colorBtn = new Button(context);
        colorBtn.setText("🎨 Tema Rengini Değiştir");
        colorBtn.setTextSize(12f);
        colorBtn.setOnClickListener(v -> changeTheme());
        menuContent.addView(colorBtn);

        LinearLayout tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(HORIZONTAL);
        tabsLayout.setGravity(Gravity.CENTER);
        tabsLayout.setPadding(0, 20, 0, 20);

        btnVisuals = createTabButton("Görseller");
        btnAim = createTabButton("Nişan");
        
        tabsLayout.addView(btnVisuals);
        tabsLayout.addView(btnAim);
        menuContent.addView(tabsLayout);

        FrameLayout contentArea = new FrameLayout(context);
        contentArea.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        tabVisuals = createVisualsContent(context);
        tabAim = createAimContent(context);
        contentArea.addView(tabVisuals);
        contentArea.addView(tabAim);
        
        btnVisuals.performClick();
        menuContent.addView(contentArea);

        addView(menuContent);

        // Logoya Tıklayınca Menüyü Aç/Kapat
        logoBtn.setOnClickListener(v -> {
            menuContent.setVisibility(menuContent.getVisibility() == VISIBLE ? GONE : VISIBLE);
        });

        // Sürükleme Mantığı (Logodan tutunca)
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
        ((GradientDrawable) logoBtn.getBackground()).setStroke(4, Config.themeColor);
        logoBtn.setColorFilter(Config.themeColor);
    }

    private TextView createTabButton(String text) {
        TextView btn = new TextView(getContext());
        btn.setText(text);
        btn.setTextSize(16f);
        btn.setPadding(40, 15, 40, 15);
        btn.setTextColor(Color.GRAY);
        btn.setOnClickListener(v -> {
            boolean isVisuals = text.equals("Görseller");
            btnVisuals.setTextColor(isVisuals ? Config.themeColor : Color.GRAY);
            btnAim.setTextColor(isVisuals ? Color.GRAY : Config.themeColor);
            tabVisuals.setVisibility(isVisuals ? VISIBLE : GONE);
            tabAim.setVisibility(isVisuals ? GONE : VISIBLE);
        });
        return btn;
    }

    private LinearLayout createVisualsContent(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);
        layout.addView(createSwitch("Kutu (ESP)", Config.showBox, (b) -> Config.showBox = b));
        layout.addView(createSwitch("Hedef Çizgisi", Config.showLine, (b) -> Config.showLine = b));
        layout.addView(createSwitch("İsim Etiketi", Config.showName, (b) -> Config.showName = b));
        layout.addView(createSwitch("Görsel İskelet", Config.showSkeleton, (b) -> Config.showSkeleton = b));
        return layout;
    }

    private LinearLayout createAimContent(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);
        layout.addView(createSwitch("Aim Assist (Oto Nişan)", Config.aimEnabled, (b) -> Config.aimEnabled = b));

        TextView fovText = new TextView(context);
        fovText.setText("FOV Alanı");
        fovText.setTextColor(Color.WHITE);
        fovText.setPadding(0, 20, 0, 10);
        layout.addView(fovText);

        SeekBar fovBar = new SeekBar(context);
        fovBar.setMax(100);
        fovBar.setProgress((int)(Config.fovFraction * 100));
        fovBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 10) Config.fovFraction = progress / 100f;
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(fovBar);
        return layout;
    }

    private LinearLayout createSwitch(String text, boolean isChecked, final OnToggleListener listener) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setPadding(0, 15, 0, 15);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(15f);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tv);

        Switch sw = new Switch(getContext());
        sw.setChecked(isChecked);
        sw.setOnCheckedChangeListener((buttonView, checked) -> listener.onToggle(checked));
        row.addView(sw);
        return row;
    }
    private interface OnToggleListener { void onToggle(boolean isChecked); }
}
