package com.example.studentinformationmanagement;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.checkerframework.checker.nullness.qual.NonNull;
public class LoginActivity extends AppCompatActivity {
    private EditText tenDN, matkhau;
    private Button btnDangNhap;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        tenDN = findViewById(R.id.edtEmai);
        matkhau = findViewById(R.id.edtPassword);
        btnDangNhap = findViewById(R.id.btnlogin);
        btnDangNhap.setOnClickListener(view -> {
            String email,password;
            email = tenDN.getText().toString();
            password = matkhau.getText().toString();
            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                Toast.makeText( LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText( LoginActivity.this, "Đăng nhập thành công",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, Employee.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText( LoginActivity.this, "Đăng nhập thất bại",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}
