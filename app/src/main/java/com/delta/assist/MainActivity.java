package com.delta.assist;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus, tvResults;
    private String[] keywords = {"http://", "https://", "auth", "key", "license", "hwid", "verify", "invalid", "t.me", "api.", "expire", "success", "login", "void"};
    private String[] urlsFound = new String[100];
    private int urlCount = 0;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        analyzeFile(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        tvResults = findViewById(R.id.tv_results);
        Button btnSelect = findViewById(R.id.btn_select_file);

        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            filePickerLauncher.launch(intent);
        });
    }

    private void analyzeFile(Uri uri) {
        tvStatus.setText("Durum: Dosya okunuyor ve taraniyor...");
        tvResults.setText("");
        urlCount = 0;

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] fileBytes = buffer.toByteArray();
                inputStream.close();

                StringBuilder sb = new StringBuilder();
                sb.append("--- ANALIZ SONUCLARI ---\n\n");
                sb.append("Bulunan Linkler:\n");

                // Linkleri Bul
                String text = new String(fileBytes, "ISO-8859-1");
                for (String kw : keywords) {
                    int index = 0;
                    while ((index = text.toLowerCase().indexOf(kw, index)) != -1) {
                        int end = index;
                        while (end < text.length() && isPrintable(text.charAt(end))) {
                            end++;
                        }
                        String found = text.substring(index, end);
                        if (found.length() > 5 && found.length() < 100) {
                            if (kw.equals("http://") || kw.equals("https://")) {
                                boolean exists = false;
                                for (int i = 0; i < urlCount; i++) {
                                    if (urlsFound[i].equals(found)) exists = true;
                                }
                                if (!exists && urlCount < 100) {
                                    urlsFound[urlCount++] = found;
                                    sb.append(" -> ").append(found).append("\n");
                                }
                            }
                        }
                        index += kw.length();
                    }
                }

                sb.append("\nBulunan Sifre/Anahtar Kelimeler:\n");
                for (String kw : keywords) {
                    if (kw.equals("http://") || kw.equals("https://")) continue;
                    int index = 0;
                    while ((index = text.toLowerCase().indexOf(kw, index)) != -1) {
                        int start = Math.max(0, index - 10);
                        int end = Math.min(text.length(), index + 30);
                        String found = text.substring(start, end).replaceAll("[^\\x20-\\x7E]", ".");
                        sb.append(" -> [").append(kw).append("]: ").append(found).append("\n");
                        index += kw.length();
                        if (sb.length() > 5000) break; // Cok uzun olmasin
                    }
                }

                runOnUiThread(() -> {
                    tvStatus.setText("Durum: Analiz tamamlandi!");
                    tvResults.setText(sb.toString());
                });

            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Hata: " + e.getMessage()));
            }
        }).start();
    }

    private boolean isPrintable(char c) {
        return (c >= 32 && c <= 126) || c == '\n';
    }
}
