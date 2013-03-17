package speechfactory;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;

public class SpeechFactory extends Factory implements TextToSpeech.OnInitListener  {
	
	
	private TextToSpeech textToSpeech;
	
	@Override
	public void init(Context context){
		if(textToSpeech == null){
			textToSpeech = new TextToSpeech(context, this);
		}
	}

	@Override
	public void speak(String toSay) {
		textToSpeech.speak(toSay, TextToSpeech.QUEUE_FLUSH, null);
		
	}

	@Override
	public void close() {
		textToSpeech.shutdown();
		
	}

	@Override
	public void onInit(int status) {
		Locale loc = new Locale("en_CA", "", "");
		  if (textToSpeech.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE) {
			  textToSpeech.setLanguage(loc);
		    }
		
	}

}
