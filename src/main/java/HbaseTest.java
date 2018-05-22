import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HbaseTest {
  public static final LinkedBlockingQueue<String> strlist = new LinkedBlockingQueue<String>();
  private static final LinkedBlockingQueue<File> filelist = new LinkedBlockingQueue<File>();
  private static final Log log = LogFactory.getLog(HbaseTest.class);

  public static void main(String[] args) {
    String path = null;
    int threads = 0;
    String zookeeper = null;
    String port = null;
    String parent = null;
    String tablename = null;
    String family = null;
    String qualiy = null;
    int batch = 0;

    if (args.length != 9) {
      log.warn("args length is incorrect, please input again !");
      log.info("args 0: file path");
      log.info("args 1: thread numbers");
      log.info("args 2: hbase zookeeper");
      log.info("args 3: hbase port");
      log.info("args 4: hbase parent");
      log.info("args 6: hbase table name");
      log.info("args 7: hbase table family");
      log.info("args 8: hbase table quality");
      log.info("args 9: batch numbers");

      System.exit(0);
      Runtime.getRuntime().gc();
    }

    path = args[0];
    threads = Integer.parseInt(args[1]);
    zookeeper = args[2];
    port = args[3];
    parent = args[4];
    tablename = args[5];
    family = args[6];
    qualiy = args[7];
    batch = Integer.parseInt(args[8]);

    FilesList(path);
    ReadFile();

    for (int i = 0; i < threads; i++) {
      BatchGet bg = new BatchGet(zookeeper, port, parent, tablename, family, qualiy, batch);
      Thread td = new Thread(bg);
      td.start();
    }
  }

  private static void FilesList(String dir) {
    if (null != dir && dir.trim().length() > 0) {
      File file = new File(dir.trim());
      if (file.isDirectory()) {
        File[] listFile = file.listFiles();
        if (null != listFile) {
          for (File f : listFile) {
            if (f.isFile()) {
              filelist.add(f);
            }
          }
        }
      }
    }
  }

  private static void ReadFile() {
    while (true) {
      File file = null;
      try {
        file = filelist.poll(3, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (null == file) {
        break;
      }

      FileInputStream fis = null;
      try {
        fis = new FileInputStream(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
      BufferedReader reader = new BufferedReader(isr);

      String line = null;
      try {
        while (null != (line = reader.readLine())) {
          strlist.add(line);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}