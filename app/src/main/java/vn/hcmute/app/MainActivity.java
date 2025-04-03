package vn.hcmute.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hcmute.app.constant.Const;
import vn.hcmute.app.model.ImageUpload;
import vn.hcmute.app.network.api.ServiceApi;
import vn.hcmute.app.util.RealPathUtil;

public class MainActivity extends AppCompatActivity {
    private EditText edtUserName;
    private ImageView imgChoose, imgUpload;
    private Button btnChoose, btnUpload;
    private TextView tvUserName;
    private Uri mUri;
    public static final int MY_REQUEST_CODE = 100;
    public static final String TAG = MainActivity.class.getName();

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == MainActivity.RESULT_OK) {
                        // request code
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imgChoose.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        anhXa();
        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        if(intent != null){
            String username = intent.getStringExtra("username");

            // gan vao textview va edt
            if(username != null){
                edtUserName.setText(username);
                tvUserName.setText(username);
            }
        }

        // bat su kien nut chon anh
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
                // chooseImage();
            }
        });

        // bat su kien upload anh
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri != null){
                    uploadImage();
                }
            }
        });
    }

    private void uploadImage() {
        String username =edtUserName.getText().toString().trim();
        RequestBody requestUsername = RequestBody.create(MediaType.parse("multipart/form-data"), username);
        // create RequestBody instance from file
        String IMAGE_PATH = RealPathUtil.getRealPath(this, mUri);
        Log.e("image path: ", IMAGE_PATH);
        File file = new File(IMAGE_PATH);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part partBodyAvatar =
                MultipartBody.Part.createFormData(Const.MY_IMAGES, file.getName(), requestFile);

        // goi retrofit
        ServiceApi.serviceApi.upload(requestUsername, partBodyAvatar).enqueue(new Callback<List<ImageUpload>>() {
            @Override
            public void onResponse(Call<List<ImageUpload>> call, Response<List<ImageUpload>> response) {
                List<ImageUpload> imageUploads = response.body();
                if(imageUploads.size() > 0){
                    for(int i = 0 ; i < imageUploads.size(); i++){
                        tvUserName.setText(imageUploads.get(i).getUsername());

                        Glide.with(MainActivity.this)
                                .load(imageUploads.get(i).getAvatar())
                                .into(imgUpload);
                        Toast.makeText(getApplicationContext(), "thanh cong", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "That bai", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ImageUpload>> call, Throwable t) {
                Log.e("TAG", t.toString());
                Toast.makeText(getApplicationContext(), "Goi API that bai", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void anhXa() {
        edtUserName = findViewById(R.id.edtUserName);
        imgChoose = findViewById(R.id.imgChoose);
        imgUpload = findViewById(R.id.imgUpload);
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        tvUserName = findViewById(R.id.tvUserName);
    }

    public static String[] permissions() {
        String[] s;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            s = storage_permissions_33;
        } else {
            s = storage_permissions;
        }
        return s;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select picture"));
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(permissions(), MY_REQUEST_CODE);
        }
    }

    public static String[] storage_permissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permissions_33 = {
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO,
    };

}