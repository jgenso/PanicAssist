package edu.cpp.preston.PanicAssist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class QuickMessagesActivity extends ActionBarActivity {

    private ArrayList<String> quickTexts;
    private QuickTextAdapter quickTextListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_messages);

        final SharedPreferences sharedPrefQuickTexts = this.getSharedPreferences(getString(R.string.preference_file_quick_text_key), Context.MODE_PRIVATE);

        quickTexts = new ArrayList<String>();
        for (int i = 0; i < 50; i++){ //gets preferences
            if (sharedPrefQuickTexts.contains("quicktext" + i)){
                quickTexts.add(sharedPrefQuickTexts.getString("quicktext" + i,"ERROR"));
            }
        }

        ListView listView = (ListView) findViewById(R.id.quickTextListView);
        quickTextListAdapter = new QuickTextAdapter(this, quickTexts);
        listView.setAdapter(quickTextListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showQuickTextDialog(quickTexts.get(i), i); //shows dialog for notification clicked
            }
        });

        ImageButton addImageButton = (ImageButton) findViewById(R.id.addQuickTextImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText messageEditText = (EditText) findViewById(R.id.quickTextEditText);
                if (messageEditText.getText().length() > 160){
                    Toast.makeText(getApplicationContext(), "Must be less than 160 characters", Toast.LENGTH_SHORT).show();
                } else if (messageEditText.getText().length() > 0){
                    quickTexts.add(messageEditText.getText().toString());
                    quickTextListAdapter.notifyDataSetChanged();

                    for (int i = 0; i < 50; i++){ //add text to prefecences
                        if (!sharedPrefQuickTexts.contains("quicktext" + i)){
                            SharedPreferences.Editor editor = sharedPrefQuickTexts.edit();
                            editor.putString("quicktext" + i, messageEditText.getText().toString());
                            editor.apply();

                            break;
                        }
                    }

                    messageEditText.setText("");
                }
            }
        });
    }

    private void showQuickTextDialog(String message, final int i){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message)
                .setTitle(R.string.quickMessageDialog);

        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final SharedPreferences sharedPrefPhoneNumbers = getActivity().getSharedPreferences(getString(R.string.preference_file_quick_text_key), Context.MODE_PRIVATE);

                for (int j = 0; j < 50; j++){ //removes text from preferences
                    if (sharedPrefPhoneNumbers.getString("quicktext" + j, "*").equalsIgnoreCase(quickTexts.get(i))){
                        SharedPreferences.Editor editor = sharedPrefPhoneNumbers.edit();
                        editor.remove("quicktext" + j);
                        editor.commit();
                        break;
                    }
                }

                quickTexts.remove(i);
                quickTextListAdapter.notifyDataSetChanged();
            }
        });

        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.create().show();
    }

    private Activity getActivity(){
        return this;
    }
}
