package julioarellano.sensortilekit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureCompass;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import static julioarellano.sensortilekit.AppConstants.nodeTag;
import static julioarellano.sensortilekit.NodeContainerFragment.NODE_TAG;

public class MainActivity extends AppCompatActivity {
    FeaturePedometer featurePedometer;
    FeatureCompass featureCompass;
    FeatureHumidity featureHumidity;
    FeatureTemperature featureTemperature;
    FeatureAccelerationEvent featureAccelerationEvent;
    long steps = 1;
    float temperature = 0;
    long stepsOLD = 0;
    TextView number;
     Node mNode = null;
    private NodeContainerFragment mNodeContainer;
    private final static String NODE_FRAGMENT = MainActivity.class.getCanonicalName() + "" +
            ".NODE_FRAGMENT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        number = (TextView) (findViewById(R.id.stepsNumber));


        final SharedPreferences sharedPreferences = this.getSharedPreferences(this.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        String node_tag = sharedPreferences.getString(nodeTag, "");
      mNode = Manager.getSharedInstance().getNodeWithTag(node_tag);
        if (savedInstanceState == null) {
            Intent i = getIntent();
            mNodeContainer = new NodeContainerFragment();
            mNodeContainer.setArguments(i.getExtras());

            getFragmentManager().beginTransaction()
                    .add(mNodeContainer, NODE_FRAGMENT).commit();

        } else {
            mNodeContainer = (NodeContainerFragment) getFragmentManager()
                    .findFragmentByTag(NODE_FRAGMENT);

        }

        featurePedometer = mNode.getFeature(FeaturePedometer.class);
        featureCompass = mNode.getFeature(FeatureCompass.class);
        //featureHumidity = node.getFeature(FeatureHumidity.class);
        featureTemperature = mNode.getFeature(FeatureTemperature.class);

        mNode.addNodeStateListener(mNodeStatusListener);


        //featurePedometer.addFeatureListener(stepListener);


        (findViewById(R.id.featureListBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = FeatureListActivity.getStartIntent(MainActivity.this, mNode);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStop() {
        //featurePedometer.removeFeatureListener(stepListener);
        featureTemperature.removeFeatureListener(temperatureListener);
        super.onStop();
    }

    private Feature.FeatureListener stepListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if (featurePedometer != null) ;
            steps = featurePedometer.getSteps(sample);
            if (steps != stepsOLD) {
                stepsOLD = steps;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        number.setText(String.valueOf(steps));
                    }
                });


            }
        }
    };

    private Feature.FeatureListener temperatureListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if (featureTemperature != null) {
                temperature = featureTemperature.getTemperature(sample);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        number.setText(String.valueOf(temperature));
                    }
                });
            }
        }
    };

    public static Intent getStartIntent(Context c, @NonNull Node node) {
        Intent i = new Intent(c, MainActivity.class);
        i.putExtra(NODE_TAG, node.getTag());
        i.putExtras(NodeContainerFragment.prepareArguments(node));
        return i;
    }

    public Node.NodeStateListener mNodeStatusListener = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if (newState == Node.State.Connected) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        featureTemperature.addFeatureListener(temperatureListener);
                        mNode.enableNotification(featureTemperature);
                        mNode.readFeature(featureTemperature);
                    }
                });

            }
        }
    };
}
