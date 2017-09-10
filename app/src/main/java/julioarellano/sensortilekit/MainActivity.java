package julioarellano.sensortilekit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureCompass;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import static com.st.BlueSTSDK.Features.FeatureAccelerationEvent.DetectableEvent.PEDOMETER;
import static java.lang.Boolean.TRUE;
import static julioarellano.sensortilekit.AppConstants.nodeTag;
import static julioarellano.sensortilekit.NodeContainerFragment.NODE_TAG;

public class MainActivity extends AppCompatActivity {
    FeaturePedometer featurePedometer;
    FeatureCompass featureCompass;
    FeatureHumidity featureHumidity;
    FeaturePressure featurePressure;
    FeatureTemperature featureTemperature;
    FeatureAccelerationEvent featureAccelerationEvent;
    long steps = 1;
    float temperature = 0;
    float pressure;
    long stepsOLD = 0;
    TextView number;
    TextView tvTemperature;
    TextView tvPressure;
    Node mNode = null;
    float[] array = new float[50];
    int i = 0;
    private NodeContainerFragment mNodeContainer;
    private final static String NODE_FRAGMENT = MainActivity.class.getCanonicalName() + "" +
            ".NODE_FRAGMENT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        number = (TextView) (findViewById(R.id.stepsNumber));
        tvTemperature = (TextView) (findViewById(R.id.tvTemperature));

        tvPressure = (TextView) (findViewById(R.id.tvPressure));
        final SharedPreferences sharedPreferences = this.getSharedPreferences(this.getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);
// gets node from other activities
        //put this in on resume as well
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


        featureTemperature = mNode.getFeature(FeatureTemperature.class);
        featureAccelerationEvent = mNode.getFeature(FeatureAccelerationEvent.class);
        featurePressure = mNode.getFeature(FeaturePressure.class);

        mNode.addNodeStateListener(mNodeStatusListener);//adds the node


    }

    @Override
    protected void onStop() {

        mNode.removeNodeStateListener(mNodeStatusListener);
        super.onStop();
    }


    private Feature.FeatureListener stepListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if (featurePedometer != null) ;
            steps = featurePedometer.getSteps(sample);
            //if (steps != stepsOLD) {
            // stepsOLD = steps;
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    number.setText(String.valueOf(steps));
                }
            });


            // }
        }
    };

    private Feature.FeatureListener AccelerometerSteps = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if (featureAccelerationEvent != null) {
                steps = featureAccelerationEvent.getPedometerSteps(sample);
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

        }
    };

    private Feature.FeatureListener temperatureListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if (featureTemperature != null) {
                temperature = featureTemperature.getTemperature(sample);
                if (i < 49) {
                    array[i] = pressure;
                    i++;
                }
                if (i == 49) {
                    i = 0;
                    return;
                } //gets samples to graph
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTemperature.setText(String.valueOf(temperature));
                    }
                });
            }
        }
    };

    private Feature.FeatureListener pressureListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if (featurePressure != null) {
                pressure = featurePressure.getPressure(sample);
                //if (pressure < 1) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvPressure.setText(String.valueOf(pressure));
                    }
                });
                // }


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

                        featureAccelerationEvent.addFeatureListener(AccelerometerSteps);
                        mNode.enableNotification(featureAccelerationEvent);
                        featureAccelerationEvent.detectEvent(PEDOMETER, TRUE);
                        mNode.readFeature(featureAccelerationEvent);

                        featurePressure.addFeatureListener(pressureListener);
                        mNode.enableNotification(featurePressure);
                        mNode.readFeature(featurePressure);


                    }
                });

            }
        }
    };
}
