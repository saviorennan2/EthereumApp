package br.ufrn.labcomu.ethsavio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.AdminFactory;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.Subscription;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * @Author: Sekhar Kuppa
 * This class contains a simple ethereum wallet creation using web3j in your own device/emulator
 */

public class MainActivity extends AppCompatActivity {

    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    private File walletPathFile;
    private Web3j web3;
    private Admin web3j;
    private Credentials credentials;
    private BigInteger nonce;
    private PersonalUnlockAccount personalUnlockAccount;
    String walletPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        synchronized (this) {
            checkPermissions();
        }

        synchronized (this) {
            // Create new wallet

                String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
            try {
                String fileName = WalletUtils.generateLightNewWalletFile("password", new File(path));
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

        web3 = Web3jFactory.build(new HttpService("http://192.168.1.17:8545"));
        web3j = AdminFactory.build(new HttpService("http://192.168.1.17:8545"));

        //CREATE WALLET
        try {
            walletPath = WalletUtils.generateFullNewWalletFile("senha", walletPathFile);
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

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 101);
            }
        }
    }

    public void createWallet(View view) throws IOException, ExecutionException, InterruptedException {
//        String walletPath = WalletUtils.generateFullNewWalletFile("yourownpassword", walletPathFile);
//        Toast.makeText(this, "Wallet created successfully.", Toast.LENGTH_SHORT).show();
        // using a raw transaction
        BigInteger nonce = BigInteger.valueOf(25);
        BigInteger gasprice = BigInteger.valueOf(0);
        BigInteger gaslimit = BigInteger.valueOf(1048575);
        BigInteger value = BigInteger.valueOf(0);

        RawTransaction rawTransaction = RawTransaction.createContractTransaction(
                nonce,
                gasprice, gaslimit
                ,
                value,
                "0x1234");
        Credentials credentials = null;

        try {
            credentials = WalletUtils.loadCredentials("senha", "/sdcard/EthWallet/");
        } catch (CipherException e) {
            e.printStackTrace();
        }

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
        String transactionHash = ethSendTransaction.getTransactionHash();

        EthGetTransactionReceipt transactionReceipt =
                web3.ethGetTransactionReceipt(transactionHash).send();


        String contractAddress = transactionReceipt.getTransactionReceipt().getContractAddress();
        Log.d("Contract", contractAddress);

    }

    public void connectEth(View view) throws ExecutionException, InterruptedException {
        try {
            Credentials credentials = WalletUtils.loadCredentials(
                    "password",
                    "/storage/emulated/0/Download/UTC--2018-03-16T19-05-15.125--833e56c5df2a654372a252658006af4d3158e9f3.json");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        new WebClientTask().execute("");

    }

    class WebClientTask extends AsyncTask<String, Void, String> {

        private Exception exception;
        Web3ClientVersion web3ClientVersion = null;

        protected String doInBackground(String... urls) {
            Log.d("BACK", "Tentando...");
            try {
                web3ClientVersion = web3.web3ClientVersion().sendAsync().get();

                // TESTEEEE

                EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                        "0x5db10750e8caff27f906b41c71b3471057dd2004", DefaultBlockParameterName.LATEST).sendAsync().get();

                nonce = ethGetTransactionCount.getTransactionCount();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return "";
        }

        @Override
        protected void onPostExecute(String s) {
//            String clientVersion = web3ClientVersion;
            Log.d("POST", "terminei " + web3ClientVersion);
            Log.d("NONCE", nonce.toString());
            //Toast.makeText(MainActivity.this, clientVersion, Toast.LENGTH_SHORT).show();
        }
    }

    class TransactionTask extends AsyncTask<String, Void, String> {


        EthSendTransaction transactionResponse = null;


        protected String doInBackground(String... urls) {
            Log.d("TRANSACTION", "Tentando...");

            BigInteger gasprice = BigInteger.valueOf(0);
            BigInteger gaslimit = BigInteger.valueOf(1048575);
            BigInteger value = BigInteger.valueOf(0);

            Transaction transaction = Transaction.createEtherTransaction("0x5db10750e8caff27f906b41c71b3471057dd2004", nonce, gasprice, gaslimit,
                    "0xee0250c19ad59305b2bdb61f34b45b72fe37154f", value);

            try {
                transactionResponse = web3.ethSendTransaction(transaction).sendAsync().get();
            } catch (ExecutionException e) {
                Log.d("TRANSACTION", "Erro1!" + e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.d("TRANSACTION", "Erro2!");
                e.printStackTrace();
            }
//            try {
//                Log.d("UNLOCK", "tentando unlock");
//                personalUnlockAccount = web3j.personalUnlockAccount("0x5db10750e8caff27f906b41c71b3471057dd2004", "").sendAsync().get();
//            } catch (ExecutionException e) {
//                Log.d("UNLOCK", "erro unlock1"+ e);
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                Log.d("UNLOCK", "erro unlock2"+ e);
//                e.printStackTrace();
//            }


            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            //
            Log.d("TRANSACTIONPOS", "FIM!");
            //Log.d("TRANSACTION", "Tentando..."+ personalUnlockAccount);
            //if (personalUnlockAccount.accountUnlocked()) {
            // send a transaction
            //BigInteger nonce = BigInteger.valueOf(0);
//
            //}
            Toast.makeText(MainActivity.this, transactionResponse + "", Toast.LENGTH_SHORT).show();

        }
    }

    public void loadCredentials(View view) throws IOException, CipherException {
        new WebClientTask().execute("");

//        credentials =
//                WalletUtils.loadCredentials(
//                        "Your wallet password",
//                        "Your wallet stored path");
//        log.info("Credentials loaded Address is::: " + credentials.getAddress());

    }

    public void transferFunds(View view) {
        new TransactionTask().execute("");
    }
}