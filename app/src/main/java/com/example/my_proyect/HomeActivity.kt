package com.example.my_proyect

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.activity_home.*

enum class ProviderType {
    BASIC,
    GOOGLE
}

class HomeActivity : AppCompatActivity() {

    private val db= FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        
        //Setup
        val bundle= intent.extras
        val email=bundle?.getString("email")
        val provider= bundle?.getString("provider")
        val usuario= bundle?.getString("usuario")

        Toast.makeText(this, "Bienvenido, gracias por usar la aplicación", Toast.LENGTH_LONG).show()
        setup(email ?:"", provider ?:"")

        //Guardado de datos
        val prefs= getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        //Remote config
        pruebaButton.visibility= View.INVISIBLE
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener{ task ->
            if (task.isSuccessful ){
                val showErrorButton=Firebase.remoteConfig.getBoolean("show_error_button")
                val errorButtonText=Firebase.remoteConfig.getString("error_button_text")

                if (showErrorButton){
                    pruebaButton.visibility= View.VISIBLE
                }

                pruebaButton.text = errorButtonText
            }
        }
    }

    private fun setup(email:String, provider:String) {
        title= "Inicio"
        emailTextView.text=email
        providerTextView.text=provider

        logOutButton.setOnClickListener {

            //Borrado de datos
            val prefs= getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

        saveButton.setOnClickListener{
            db.collection("users").document(email).set(
                    hashMapOf("provider" to provider, "address" to addressTextView.text.toString(),
                            "phone"  to phoneTextView.text.toString())
            )
            Toast.makeText(this, "dentro del save", Toast.LENGTH_SHORT).show()
        }

        getButton.setOnClickListener{
            db.collection("users").document(email).get().addOnSuccessListener {
                addressTextView.setText(it.get("address") as String?)
                phoneTextView.setText(it.get("phone") as String?)
            }
            Toast.makeText(this, "dentro del get", Toast.LENGTH_SHORT).show()
        }

        deleteButton.setOnClickListener{
            db.collection("users").document(email).delete()
            Toast.makeText(this, "dentro del delete", Toast.LENGTH_SHORT).show()
        }
    }

}