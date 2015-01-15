package com.example.jorge.chatterbot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jorge.chatterbot.libs.ChatterBot;
import com.example.jorge.chatterbot.libs.ChatterBotFactory;
import com.example.jorge.chatterbot.libs.ChatterBotSession;
import com.example.jorge.chatterbot.libs.ChatterBotType;

import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Principal extends Activity implements TextToSpeech.OnInitListener {

    private boolean reproductor = false;
    private int IDActividadTTS = 1;
    private int IDActividadHablar= 2;
    private TextToSpeech tts;
    private TextView tvTexto;
    private String fraseRobot;
    private String fraseUsu;
    private RadioButton lengES, lengUK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        tvTexto = (TextView)findViewById(R.id.tvTexto);
        lengES = (RadioButton)findViewById(R.id.rbEsp);
        lengUK = (RadioButton)findViewById(R.id.rbIngles);
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, IDActividadTTS);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IDActividadTTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }else{
            if(requestCode==IDActividadHablar && resultCode == RESULT_OK){
                ArrayList<String> textos = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                String s = textos.get(0).toString();
                ChatBot nuevaTarea = new ChatBot();
                nuevaTarea.execute(s);
            }
        }
    }

    private class ChatBot extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Principal.this);
            pDialog.setMessage("Enviando...");
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            String texto = params[0];
            fraseUsu = texto;
            String respuesta="";
            try {
                ChatterBotFactory factory = new ChatterBotFactory();
                ChatterBot bot1 = factory.create(ChatterBotType.CLEVERBOT);
                ChatterBotSession bot1session = bot1.createSession();
                respuesta = bot1session.think(texto);
            } catch (Exception ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
            return respuesta;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            tvTexto.append("Tú: "+fraseUsu+"\n\n"+"Cleverbot: "+result+"\n\n");
            fraseRobot = result;
            reproducir();
            pDialog.dismiss();
        }

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            reproductor = true;
            tts.setPitch(3);
            tts.setSpeechRate(1);

        } else {
            reproductor = false;
            Toast.makeText(this, "No se puede usar TTS", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    public void hablar(View v){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla ahora");
        i.putExtra(RecognizerIntent.
                        EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                3000);
        startActivityForResult(i, IDActividadHablar);
    }

    public void reproducir(){
        if(reproductor){
            if(tts.isSpeaking()){

            }else{
                if(lengUK.isChecked()){
                    tts.setLanguage(Locale.UK);
                }else if (lengES.isChecked()){
                    tts.setLanguage(new Locale ("es", "ES"));
                }
                tts.speak(fraseRobot, TextToSpeech.QUEUE_FLUSH, null);
            }
        }else{
        }
    }
}
