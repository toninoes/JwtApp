package uca.ruiz.antonio.jwtapp.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uca.ruiz.antonio.jwtapp.R;
import uca.ruiz.antonio.jwtapp.data.Preferencias;
import uca.ruiz.antonio.jwtapp.data.io.MyApiAdapter;
import uca.ruiz.antonio.jwtapp.data.mapping.Authority;
import uca.ruiz.antonio.jwtapp.data.mapping.Login;
import uca.ruiz.antonio.jwtapp.data.mapping.TokenResponse;
import uca.ruiz.antonio.jwtapp.data.mapping.UserResponse;
import uca.ruiz.antonio.jwtapp.util.Validacion;


/**
 * Created by toni on 07/06/2018.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText et_email;
    private EditText et_password;
    private ProgressDialog progressDialog;
    private static String token;
    private CheckBox chk_recordar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.entrando));

        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    intentoLogin();
                    return true;
                }
                return false;
            }
        });

        Button btn_entrar = (Button) findViewById(R.id.btn_entrar);
        btn_entrar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                intentoLogin();
            }
        });

        chk_recordar = (CheckBox) findViewById(R.id.chk_recordar);
        Boolean recordarMail = Preferencias.get(this).getBoolean("recordar", false);
        chk_recordar.setChecked(recordarMail);

        if(recordarMail) {
            et_email.setText(Preferencias.get(this).getString("email", "email"));
        }
    }


    /**
     * Intenta iniciar sesión mediante el formulario de inicio de sesión.
     * Si hay errores de formulario (correo electrónico no válido, campos faltantes, etc.), se
     * presentan los errores y no se realiza ningún intento de inicio de sesión.
     */
    private void intentoLogin() {
        // Resetear errores
        et_email.setError(null);
        et_password.setError(null);

        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Valida campo Password
        if (!Validacion.tamPassword(password)) {
            et_password.setError(getString(R.string.error_tam_password_invalida));
            focusView = et_password;
            cancel = true;
        }

        // Valida campo Email.
        if (!Validacion.tamEmail(email)) {
            et_email.setError(getString(R.string.error_tam_email_invalido));
            focusView = et_email;
            cancel = true;
        } else if (!Validacion.formatoEmail(email)) {
            et_email.setError(getString(R.string.error_formato_email_invalido));
            focusView = et_email;
            cancel = true;
        }

        if (cancel) {
            // Se ha producido un error: no se intenta el login y se focaliza en el
            // primer campo del formulario con error.
            focusView.requestFocus();
        } else {
            login(email, password);
        }
    }

    private boolean esPasswordValida(String password) {
        return password.length() >= 4;
    }

    /**
     * Aquí es donde hacemos la llamada al servidor para hacer login gracias a Retrofit.
     * @param username
     * @param password
     */
    private void login(String username, String password) {
        progressDialog.show();

        Login login = new Login(username, password);

        Call<TokenResponse> call = MyApiAdapter.getApiService().login(login);
        call.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if(response.isSuccessful()) {
                    token = "Bearer " + response.body().getToken();
                    definirPreferencias(token);
                    obtenerUsuario(token);
                    irMain(token);
                } else {
                    progressDialog.cancel();
                    Toast.makeText(LoginActivity.this, "Login incorrecto !", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                progressDialog.cancel();
                Toast.makeText(LoginActivity.this, "error :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irMain(String token) {
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }

    private void definirPreferencias(String token) {
        Preferencias.getEditor(this).putString("token", token).commit();
        Preferencias.getEditor(this).putString("email", et_email.getText().toString()).commit();
        Preferencias.getEditor(this).putBoolean("recordar", chk_recordar.isChecked()).commit();
    }

    private void obtenerUsuario(String token) {
        Call<UserResponse> call = MyApiAdapter.getApiService().getUser(token);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.isSuccessful()) {
                    UserResponse user = response.body();
                    definirUsuario(user);
                } else {
                    Toast.makeText(LoginActivity.this, "Token incorrecto !", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "error :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void definirUsuario(UserResponse user) {

        Preferencias.getEditor(this).putString("nombre", user.getFirstname()).commit();
        Preferencias.getEditor(this).putString("apellidos", user.getLastname()).commit();
        Preferencias.getEditor(this).putBoolean("activo", user.getEnabled()).commit();

        // Reiniciamos los roles a falso todos
        Preferencias.getEditor(this).putBoolean("ROLE_ADMIN", false).commit();
        Preferencias.getEditor(this).putBoolean("ROLE_SANITARIO", false).commit();
        Preferencias.getEditor(this).putBoolean("ROLE_PACIENTE", false).commit();

        // establecemos los roles que nos llega del servidor
        for(Authority rol: user.getAuthorities()) {
            Preferencias.getEditor(this).putBoolean(rol.getName().toString(), true).commit();
        }

    }

}

