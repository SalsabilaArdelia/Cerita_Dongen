package com.example.cerita_dongeng.activities;

import static com.example.cerita_dongeng.utils.Constant.KEY_NAME;
import static com.example.cerita_dongeng.utils.Constant.PREFS_NAME;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cerita_dongeng.R;
import com.example.cerita_dongeng.model.ModelMain;
import com.example.cerita_dongeng.utils.Constant;
import com.example.cerita_dongeng.utils.FavoriteCallback;
import com.example.cerita_dongeng.utils.FirebaseHelper;
import com.example.cerita_dongeng.utils.SharedPreference;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    public static final String DETAIL_DONGENG = "DETAIL_DONGENG";
    private TextToSpeech tts;
    private boolean isSpeaking = false;

    String idUser, idStory, strJudul, strCerita;
    ModelMain modelMain;
    Toolbar toolbar;
    TextView tvJudul, tvCerita;

    MaterialFavoriteButton btnFavorite;
    MaterialFavoriteButton btnSuara;
    ImageView btnEdit, btnDelete;

    private FirebaseHelper db;
    SharedPreferences sharedPref;
    Boolean isLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        db = new FirebaseHelper();
        sharedPref = SharedPreference.INSTANCE.initPref(getApplicationContext(), PREFS_NAME);

        // Set transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        toolbar = findViewById(R.id.toolbar);
        tvJudul = findViewById(R.id.tvJudul);
        tvCerita = findViewById(R.id.tvCerita);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnSuara = findViewById(R.id.btnSuara);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        String name = sharedPref.getString(KEY_NAME, "");
        if (name.equalsIgnoreCase("admin")) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        }

        // Get data intent
        modelMain = (ModelMain) getIntent().getSerializableExtra(DETAIL_DONGENG);
        if (modelMain != null) {
            idStory = modelMain.getId();
            strJudul = modelMain.getStrJudul();
            strCerita = modelMain.getStrCerita();

            tvJudul.setText(strJudul);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvCerita.setText(Html.fromHtml(strCerita, Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvCerita.setText(Html.fromHtml(strCerita));
            }
        }

        idUser = sharedPref.getString(Constant.KEY_ID, "");

        // Setup Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("id", "ID"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getApplicationContext(), "Bahasa tidak didukung!", Toast.LENGTH_SHORT).show();
                }

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        isSpeaking = true;
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        isSpeaking = false;
                    }

                    @Override
                    public void onError(String utteranceId) {
                        isSpeaking = false;
                    }
                });
            }
        });

        btnSuara.setOnClickListener(view -> {
            if (tts != null) {
                if (isSpeaking) {
                    tts.stop();
                    isSpeaking = false;
                    btnSuara.setImageResource(R.drawable.suara_off);
                    Toast.makeText(getApplicationContext(), "Suara dihentikan", Toast.LENGTH_SHORT).show();
                } else {
                    String text = tvCerita.getText().toString();
                    if (!text.isEmpty()) {
                        Bundle params = new Bundle();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "storyRead");
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "storyRead");
                        btnSuara.setImageResource(R.drawable.suara_on);
                        Toast.makeText(getApplicationContext(), "Membacakan cerita...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dataFavorite();

        btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(DetailActivity.this, AddActivity.class);
            intent.putExtra(DetailActivity.DETAIL_DONGENG, modelMain);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(view -> deleteData());
    }

    private void dataFavorite() {
        db.favoriteExist(idStory, idUser, isFavorite -> {
            btnFavorite.setFavorite(isFavorite);
            btnFavorite.setOnFavoriteChangeListener((buttonView, favorite) -> {
                if (favorite) {
                    db.addFavorite(idStory, strJudul, strCerita, idUser, isFav -> {
                        String message = isFav ? "Added Favorite" : "Failed Added Favorite";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    db.removeFavorite(idStory, idUser, isFav -> {
                        String message = isFav ? "Deleted Favorite" : "Failed Deleted Favorite";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void deleteData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Apakah Anda yakin ingin menghapus data ini?");
        builder.setPositiveButton("Hapus", (dialog, which) -> {
            db.deleteData(idStory, isDelete -> {
                if (isDelete) {
                    Toast.makeText(getApplicationContext(), "Data berhasil dihapus!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    startActivity(new Intent(DetailActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Data gagal dihapus!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        if (on) {
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
        }
        window.setAttributes(layoutParams);
    }
}
