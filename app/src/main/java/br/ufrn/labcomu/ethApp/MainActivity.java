package br.ufrn.labcomu.ethApp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.AdminFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;


import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {

    private Web3j web3;
    private Admin web3j;
    private Credentials credentials;
    private BigInteger nonce;
    private String fileName;
    private File path;
    private String hexValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        synchronized (this) {
            checkPermissions();
        }

        web3 = Web3jFactory.build(new HttpService("http://192.168.1.17:8545"));
        web3j = AdminFactory.build(new HttpService("http://192.168.1.17:8545"));

    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 101);
            }
        }
    }

    public void createWallet(View view) {

        synchronized (this) {
            // Create new wallet

            path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
            if (!path.exists()) {
                path.mkdir();
            }
            try {
                fileName = WalletUtils.generateLightNewWalletFile("senha", new File(path.getPath()));
                Log.d("FILE", fileName);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (CipherException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public void loadCredentials(View view) throws IOException {

        try {
            credentials = WalletUtils.loadCredentials("senha", path.getPath() + "/" + fileName);
            Log.d("CREDENTIALS", credentials.getAddress());

            //GET NONCE
            getNonce();

        } catch (CipherException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void getNonce() throws ExecutionException, InterruptedException {
        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

        nonce = ethGetTransactionCount.getTransactionCount();

    }

    public void transationRawTest(View view) {

        BigInteger gasprice = BigInteger.valueOf(0);
        BigInteger gaslimit = BigInteger.valueOf(1048575);
        BigInteger value = BigInteger.valueOf(0);
        PatientMedicalRecordTransaction pmrt= new PatientMedicalRecordTransaction("0", "1",
                "OP", "OP","T",1,"Outro");

        //STRING
        Gson gson= new Gson();
        String pmrtStr= gson.toJson(pmrt);

        //BYTES
//        byte[] pmrtBytes= SerializationUtils.serialize(pmrt);
//        String pmrtStr = null;
//        try {
//            pmrtStr = new String(pmrtBytes, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,gasprice, gaslimit,"0xee0250c19ad59305b2bdb61f34b45b72fe37154f",value, pmrtStr);

        Log.d("Raw", rawTransaction.getNonce().toString());
        Log.d("Cred", credentials.getAddress());

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        hexValue = Numeric.toHexString(signedMessage);

        new RawTransationtask().execute();

    }

    class RawTransationtask extends AsyncTask<String, Void, String> {

        EthSendTransaction ethSendTransaction;

        protected String doInBackground(String... urls) {
            try {
                ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {

            String transactionHash = ethSendTransaction.getTransactionHash();

            Log.d("TRANSACTION", transactionHash);

        }
    }



}