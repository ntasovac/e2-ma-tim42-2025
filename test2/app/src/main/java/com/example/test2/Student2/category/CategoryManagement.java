package com.example.test2.Student2.category;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.test2.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CategoryManagement extends AppCompatActivity {

    private CategoryDatabase db;
    private CategoryDAO categoryDao;
    private Executor executor = Executors.newSingleThreadExecutor();
    private CategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_management);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = Room.databaseBuilder(getApplicationContext(),
                        CategoryDatabase.class, "category")
                .build();
        categoryDao = db.categoryDAO();


        /*
        executor.execute(() -> {
            categoryDao.deleteAll(); // nova metoda u DAO
            runOnUiThread(() -> Toast.makeText(this, "All categories deleted!", Toast.LENGTH_SHORT).show());
        });
         */


        // RecyclerView setup
        RecyclerView recyclerView = findViewById(R.id.categoryRecyclerView);

        adapter = new CategoryAdapter(categoryList, category -> {
            Intent intent = new Intent(CategoryManagement.this, UpvertCategory.class);
            intent.putExtra(UpvertCategory.EXTRA_CATEGORY_ID, category.id); // <-- isti ključ
            startActivity(intent);
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadCategories(); // initial load

        // Buttons
        Button addButton = findViewById(R.id.addCategoryButton);
        Button editButton = findViewById(R.id.editCategoryButton);

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpvertCategory.class);
            // bez extras → Add mode
            startActivity(intent);
        });

        /*
        addButton.setOnClickListener(v -> executor.execute(() -> {
            String newName = "Sport";
            int count = categoryList.size();
            count++;
            String newColor = String.format("#FF000%X", count);

            if (categoryDao.getByColor(newColor) != null) {
                runOnUiThread(() -> Toast.makeText(this, "Color already used!", Toast.LENGTH_SHORT).show());
                return;
            }

            Category category = new Category(newName, newColor);
            categoryDao.upsert(category);

            runOnUiThread(() -> {
                Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show();
                loadCategories(); // refresh list
            });
        }));

         */

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());


        editButton.setOnClickListener(v -> executor.execute(() -> {
            List<Category> categories = categoryDao.getAll();
            if (categories.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No categories available!", Toast.LENGTH_SHORT).show());
                return;
            }

            Category category = categories.get(0);
            String newColor = "#00FF00";

            if (categoryDao.getByColor(newColor) != null) {
                runOnUiThread(() -> Toast.makeText(this, "Color already used!", Toast.LENGTH_SHORT).show());
                return;
            }

            category.colorHex = newColor;
            categoryDao.upsert(category);

            runOnUiThread(() -> {
                Toast.makeText(this, "Category color updated!", Toast.LENGTH_SHORT).show();
                loadCategories(); // refresh list
            });
        }));
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadCategories(); // reload data from DB
    }

    private void loadCategories() {
        executor.execute(() -> {
            List<Category> categories = categoryDao.getAll();
            runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(categories);
                adapter.notifyDataSetChanged();
            });
        });
    }
}
