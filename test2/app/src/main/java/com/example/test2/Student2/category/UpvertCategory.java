package com.example.test2.Student2.category;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.test2.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class UpvertCategory extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "category_id";

    private CategoryDatabase db;
    private CategoryDAO categoryDao;
    private final Executor io = Executors.newSingleThreadExecutor();

    private TextView tvTitle;
    private EditText etName, etColor;
    private View colorPreview;
    private Button btnSave, btnBack;

    private Integer editingId = null;
    private Category loaded; // držimo učitanu kategoriju za edit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upvert_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // DB
        db = Room.databaseBuilder(getApplicationContext(), CategoryDatabase.class, "category").build();
        categoryDao = db.categoryDAO();

        // Views
        tvTitle = findViewById(R.id.tvTitle);
        etName = findViewById(R.id.etName);
        etColor = findViewById(R.id.etColor);
        colorPreview = findViewById(R.id.viewColorPreview);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Da li je prosleđen ID?
        if (getIntent() != null && getIntent().hasExtra(EXTRA_CATEGORY_ID)) {
            int id = getIntent().getIntExtra(EXTRA_CATEGORY_ID, -1);
            if (id > 0) {
                editingId = id;
                tvTitle.setText("Edit Category");
                loadCategory(id);
            } else {
                tvTitle.setText("Add Category");
                setPreviewFromHex("#EEEEEE");
            }
        } else {
            tvTitle.setText("Add Category");
            setPreviewFromHex("#EEEEEE");
        }

        colorPreview.setOnClickListener(v -> showColorPalette());

        btnSave.setOnClickListener(v -> saveCategory());
    }

    private void loadCategory(int id) {
        io.execute(() -> {
            try {
                Category cat = categoryDao.getById(id);

                runOnUiThread(() -> {
                    if (cat == null) {
                        Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    loaded = cat;
                    etName.setText(cat.name != null ? cat.name : "");
                    etColor.setText(cat.colorHex != null ? cat.colorHex : "");
                    setPreviewFromHex(cat.colorHex);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading category", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }


    private void saveCategory() {
        String name = etName.getText().toString().trim();
        String hex = normalizeHex(etColor.getText().toString());

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (hex == null) {
            etColor.setError("Invalid color. Use #RRGGBB or #AARRGGBB");
            return;
        }

        setPreviewFromHex(hex);

        io.execute(() -> {
            Category c;
            if (editingId != null) {
                // edit
                c = (loaded != null) ? loaded : new Category(name, hex);
                c.id = (loaded != null) ? loaded.id : editingId;
                c.name = name;
                c.colorHex = hex;
            } else {
                // add
                c = new Category(name, hex);
            }

            categoryDao.upsert(c);

            runOnUiThread(() -> {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    // --- Paleta boja (jednostavna) ---
    private void showColorPalette() {
        // nekoliko lepih boja
        final int[] COLORS = new int[]{
                Color.parseColor("#F44336"), // red
                Color.parseColor("#E91E63"), // pink
                Color.parseColor("#9C27B0"), // purple
                Color.parseColor("#3F51B5"), // indigo
                Color.parseColor("#2196F3"), // blue
                Color.parseColor("#03A9F4"), // light blue
                Color.parseColor("#00BCD4"), // cyan
                Color.parseColor("#009688"), // teal
                Color.parseColor("#4CAF50"), // green
                Color.parseColor("#8BC34A"), // light green
                Color.parseColor("#CDDC39"), // lime
                Color.parseColor("#FFEB3B"), // yellow
                Color.parseColor("#FFC107"), // amber
                Color.parseColor("#FF9800"), // orange
                Color.parseColor("#FF5722")  // deep orange
        };

        CharSequence[] labels = new CharSequence[COLORS.length];
        for (int i = 0; i < COLORS.length; i++) {
            labels[i] = String.format("#%06X", (0xFFFFFF & COLORS[i]));
        }

        new AlertDialog.Builder(this)
                .setTitle("Pick a color")
                .setItems(labels, (d, which) -> {
                    String hex = labels[which].toString();
                    etColor.setText(hex);
                    setPreviewFromHex(hex);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setPreviewFromHex(String maybeHex) {
        String norm = normalizeHex(maybeHex);
        if (norm == null) norm = "#EEEEEE";
        try {
            colorPreview.setBackgroundColor(Color.parseColor(norm));
        } catch (IllegalArgumentException e) {
            colorPreview.setBackgroundColor(Color.parseColor("#EEEEEE"));
        }
    }

    // --- Normalizacija HEX-a (kao ranije) ---
    private static final Pattern HEX_COLOR =
            Pattern.compile("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$");

    private static String normalizeHex(String input) {
        if (input == null) return null;
        String s = input.trim().toUpperCase();
        if (s.isEmpty()) return null;
        if (!s.startsWith("#")) s = "#" + s;
        if (HEX_COLOR.matcher(s).matches()) return s;
        return null;
    }
}
