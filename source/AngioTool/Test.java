package AngioTool;

public class Test {
   public static void main(String[] args) {
      new AngioTool_Updater();
      String java_home = System.getProperty("java.home");
      System.out.println("System.getProperty(\"java.home\")= " + java_home);
      System.out.println("cacerts file = " + java_home + "\\lib\\security\\cacerts");
      String a = "https://ccrod.cancer.gov/confluence/download/attachments/ 12345563 /AngioTool 0.08b.exe?api=v2";
      System.out.println(a + "\n" + ATURLEncoder.encodePath(a));
   }
}
