package webrtc.example.com.webrtc_android.ssl;

import android.content.Context;
import com.squareup.okhttp.OkHttpClient;
import webrtc.example.com.webrtc_android.R;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


/**
 * @Auther: liuqi
 * @Date: 2019/3/15 16:54
 * @Description: 支持https协议请求
 * 如果不需要https 则初始化不需要调用MyOkhttpManager.getInstance().setTrustrCertificates()
 * 否则需要将公钥调用上面方法
 */
public class MySSLConnectionSocketFactory {
    static private MySSLConnectionSocketFactory mySSLConnectionSocketFactory = null;
    private InputStream mTrustrCertificate;
    private SSLSocketFactory sslSocketFactory;

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    static public MySSLConnectionSocketFactory getInstance() {
        if (mySSLConnectionSocketFactory == null) {
            mySSLConnectionSocketFactory = new MySSLConnectionSocketFactory();
        }
        return mySSLConnectionSocketFactory;
    }

    /**
     * 初始化ssl factory
     * @param context
     */
    public static void init(Context context) {
        X509TrustManager trustManager;
        MySSLConnectionSocketFactory mySSLConnectionSocketFactory=MySSLConnectionSocketFactory.getInstance();
        try {
            mySSLConnectionSocketFactory.setTrustrCertificates(context.getResources().openRawResource(R.raw.mycer));
            trustManager = mySSLConnectionSocketFactory.trustManagerForCertificates(mySSLConnectionSocketFactory.getTrustrCertificates());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            mySSLConnectionSocketFactory.setSslSocketFactory(sslContext.getSocketFactory());
        } catch (GeneralSecurityException e) {
            System.out.println("初始sslFactory 异常，详情："+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            System.out.println("############http协议下解析公钥错误，原因：" + e.getMessage());
            throw new AssertionError(e);
        }
    }

    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    public void setTrustrCertificates(InputStream in) {
        mTrustrCertificate = in;
    }

    public InputStream getTrustrCertificates() {
        return mTrustrCertificate;
    }



}
