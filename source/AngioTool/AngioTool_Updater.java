package AngioTool;

import ij.IJ;
import ij.ImageJ;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Vector;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;

public class AngioTool_Updater {
   private int i;

   public void update() {
      URL url = this.getClass().getResource("AngioTool_Updater.class");
      String at_exe = url == null ? null : url.toString().replaceAll("%20", " ");
      if (at_exe == null) {
         JOptionPane.showMessageDialog(null, "Could not determine the location of " + at_exe);
      } else {
         int exclamation = at_exe.indexOf(33);
         at_exe = at_exe.substring(9, exclamation);
         System.out.println(at_exe);
         File file = new File(at_exe);
         if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "File not found: " + file.getPath());
         } else if (!file.canWrite()) {
            JOptionPane.showMessageDialog(null, "No write access: " + file.getPath());
         } else {
            String updatedVersion = this.getUpgradeVersion();
            JOptionPane.showMessageDialog(null, "update\nThe last AngioTool version is " + updatedVersion);
            byte[] exeFile = null;
            if (!this.checkAngioToolVersion(updatedVersion)) {
               exeFile = this.getExe(this.getLinkToUpdateVersion());
               if (exeFile == null) {
                  JOptionPane.showMessageDialog(null, "Unable to download exeFile from https://ccrod.cancer.gov/confluence/");
                  return;
               }

               JOptionPane.showMessageDialog(null, "Downloaded file " + exeFile.toString() + " is of size " + exeFile.length);
               this.saveExe(file, exeFile);
               JOptionPane.showMessageDialog(null, "Done");
            }
         }
      }
   }

   public void createNonValidationTrustManager() throws Exception {
      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
         @Override
         public X509Certificate[] getAcceptedIssuers() {
            return null;
         }

         @Override
         public void checkClientTrusted(X509Certificate[] certs, String authType) {
         }

         @Override
         public void checkServerTrusted(X509Certificate[] certs, String authType) {
         }
      }};
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      new HostnameVerifier() {
         @Override
         public boolean verify(String hostname, SSLSession session) {
            return true;
         }
      };
   }

   public Certificate[] retrieveServerCertificates(String hostName) {
      try {
         int port = 443;
         SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
         SSLSocket socket = (SSLSocket)factory.createSocket("ccrod.cancer.gov", port);
         socket.startHandshake();
         Certificate[] serverCerts = socket.getSession().getPeerCertificates();
         socket.close();
         return serverCerts;
      } catch (SSLPeerUnverifiedException var7) {
         System.out.println("e=" + var7);
      } catch (IOException var8) {
         System.out.println("e=" + var8);
      }

      return null;
   }

   public void listTrustedCertificates() {
      try {
         String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
         FileInputStream is = new FileInputStream(filename);
         KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
         String password = "changeit";
         keystore.load(is, password.toCharArray());
         PKIXParameters params = new PKIXParameters(keystore);

         for(TrustAnchor ta : params.getTrustAnchors()) {
            X509Certificate cert = ta.getTrustedCert();
            System.out.println(cert);
         }
      } catch (CertificateException var9) {
      } catch (KeyStoreException var10) {
      } catch (NoSuchAlgorithmException var11) {
      } catch (InvalidAlgorithmParameterException var12) {
      } catch (IOException var13) {
      }
   }

   public void addCertificateToKeyStore(File keystoreFile, char[] keystorePassword, String alias, Certificate cert) throws KeyStoreException, NoSuchAlgorithmException {
      try {
         KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
         FileInputStream in = new FileInputStream(keystoreFile);
         keystore.load(in, keystorePassword);
         in.close();
         keystore.setCertificateEntry(alias, cert);
         FileOutputStream out = new FileOutputStream(keystoreFile);
         keystore.store(out, keystorePassword);
         out.close();
      } catch (CertificateException var8) {
      } catch (NoSuchAlgorithmException var9) {
      } catch (FileNotFoundException var10) {
      } catch (KeyStoreException var11) {
      } catch (IOException var12) {
      }
   }

   boolean checkAngioToolVersion(String updatedVersion) {
      String ATVersion = "0.6a";
      int M = Integer.parseInt(ATVersion.substring(0, 1));
      int m = Integer.parseInt(ATVersion.substring(2, 3));
      String p = ATVersion.substring(3, 4);
      int Major = Integer.parseInt(updatedVersion.substring(0, 1));
      int minor = Integer.parseInt(updatedVersion.substring(2, 3));
      String P = updatedVersion.substring(3, 4);
      JOptionPane.showMessageDialog(
         null,
         "checkAngioToolVersion\nATVersion = "
            + ATVersion
            + " = "
            + M
            + " "
            + m
            + " "
            + p
            + "\n"
            + "WebVersion = "
            + updatedVersion
            + " = "
            + Major
            + " "
            + minor
            + " "
            + P
      );
      if (M >= Major && m >= minor && Character.toLowerCase(p.charAt(0)) >= Character.toLowerCase(P.charAt(0))) {
         return true;
      } else {
         JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            "checkAngioToolVersion\nAngioTool "
               + Major
               + "."
               + minor
               + ""
               + P
               + " is avilable for download. "
               + "Update AngioTool now from "
               + "https://ccrod.cancer.gov/confluence/display/ROB2/updates@232014",
            "Error",
            0
         );
         return false;
      }
   }

   boolean checkAngioToolVersion(int Major, int minor, String character) {
      String version = "0.6a";
      int M = Integer.parseInt(version.substring(0, 1));
      int m = Integer.parseInt(version.substring(2, 4));
      String p = version.substring(4, 5);
      if (M >= Major && m >= minor) {
         return true;
      } else {
         JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            "AngioTool "
               + Major
               + "."
               + minor
               + ""
               + character
               + " or higher is required to run AngioTool. "
               + "Update AngioTool now from http://Angiotool.nci.nih.gov",
            "Error",
            0
         );
         return false;
      }
   }

   String getLinkToUpdateVersion() {
      String url = "https://ccrod.cancer.gov/confluence/display/ROB2/updates@232014";
      String notes = this.openUrlAsString(url, 50);
      if (notes == null) {
         JOptionPane.showMessageDialog(
            null,
            "Unable to connect to "
               + url
               + ". You\n"
               + "may need to use the Edit>Options>Proxy Settings\n"
               + "command to configure ImageJ to use a proxy server."
         );
         return null;
      } else {
         int index = notes.indexOf("AngioTool_version: ");
         if (index == -1) {
            JOptionPane.showMessageDialog(null, "Release notes are not in the expected format");
            return null;
         } else {
            String version = notes.substring(index + 19, index + 23);
            index = notes.indexOf("Random: ");
            int index2 = notes.indexOf("*");
            if (index != -1 && index2 != -1) {
               String random_Number = notes.substring(index + 8, index2);
               return ATURLEncoder.encodePath(
                  "https://ccrod.cancer.gov/confluence/download/attachments/" + random_Number + "/AngioTool%20" + version + ".exe?api=v2"
               );
            } else {
               JOptionPane.showMessageDialog(null, "Release notes are not in the expected format");
               return null;
            }
         }
      }
   }

   String getUpgradeVersion() {
      String url = "https://ccrod.cancer.gov/confluence/display/ROB2/updates@232014";
      String notes = this.openUrlAsString(url, 50);
      if (notes == null) {
         JOptionPane.showMessageDialog(null, "Unable to connect to Angiotool's updater service.");
         return null;
      } else {
         int index = notes.indexOf("AngioTool_version: ");
         if (index == -1) {
            JOptionPane.showMessageDialog(null, "AngioTool can't find the most updated version");
            return null;
         } else {
            return notes.substring(index + 19, index + 23);
         }
      }
   }

   String openUrlAsString(String address, int maxLines) {
      StringBuffer sb = null;

      try {
         URL url = new URL(address);
         System.out.println("openUrlAsString\nurl= " + url);
         JOptionPane.showMessageDialog(null, "openUrlAsString\nurl= " + url);
         URLConnection conn = url.openConnection();
         System.out.println(conn);
         JOptionPane.showMessageDialog(null, "openUrlAsString\nconn= " + conn);
         InputStream in = conn.getInputStream();
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         sb = new StringBuffer();
         System.out.println("sb= " + sb);
         int count = 0;

         String line;
         while((line = br.readLine()) != null && count++ < maxLines) {
            sb.append(line + "\n");
         }

         in.close();
      } catch (IOException var10) {
         System.err.println("catch expection= " + var10);
         JOptionPane.showMessageDialog(null, "Catch expection= " + var10);
         sb = null;
      }

      return sb != null ? new String(sb) : null;
   }

   byte[] getExe(String address) {
      byte[] data;
      try {
         URL url = new URL(address);
         URLConnection uc = url.openConnection();
         int len = uc.getContentLength();
         JOptionPane.showMessageDialog(null, "getExe\nUpdater (url): " + address + "\n with length " + len);
         if (len <= 0) {
            return null;
         }

         InputStream in = uc.getInputStream();
         data = new byte[len];

         int count;
         for(int n = 0; n < len; n += count) {
            count = in.read(data, n, len - n);
            if (count < 0) {
               throw new EOFException();
            }
         }

         in.close();
      } catch (IOException var9) {
         JOptionPane.showMessageDialog(null, "getExe\nException = " + var9);
         return null;
      }

      JOptionPane.showMessageDialog(null, "getExe\nHere is the data = " + data);
      return data;
   }

   void saveExe(File f, byte[] data) {
      try {
         FileOutputStream out = new FileOutputStream(f);
         out.write(data, 0, data.length);
         out.close();
      } catch (IOException var4) {
         JOptionPane.showMessageDialog(null, var4.toString());
      }
   }

   String[] openUrlAsList(String address) {
      IJ.showStatus("Connecting to http://imagej.nih.gov/ij");
      Vector v = new Vector();

      try {
         URL url = new URL(address);
         InputStream in = url.openStream();
         BufferedReader br = new BufferedReader(new InputStreamReader(in));

         while(true) {
            String line = br.readLine();
            if (line == null) {
               br.close();
               break;
            }

            if (!line.equals("")) {
               v.addElement(line);
            }
         }
      } catch (Exception var7) {
      }

      String[] lines = new String[v.size()];
      v.copyInto(lines);
      IJ.showStatus("");
      return lines;
   }

   String version() {
      String version = "";

      try {
         Class ijClass = ImageJ.class;
         Field field = ijClass.getField("VERSION");
         version = (String)field.get(ijClass);
      } catch (Exception var4) {
      }

      return version;
   }
}
