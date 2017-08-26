package julioarellano.sensortilekit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import static julioarellano.sensortilekit.AppConstants.nodeTag;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//Manager manager = new Manager();
        final SharedPreferences sharedPreferences = this.getSharedPreferences(this.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

 String node_tag = sharedPreferences.getString(nodeTag,"");
final Node node  = Manager.getSharedInstance().getNodeWithTag(node_tag);
     //final  Node node = manager.getNodeWithTag(node_tag);

        (findViewById(R.id.featureListBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = FeatureListActivity.getStartIntent(MainActivity.this, node);
                startActivity(i);
            }
        });
    }
}
