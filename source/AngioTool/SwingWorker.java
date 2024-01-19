package AngioTool;

import javax.swing.SwingUtilities;

public abstract class SwingWorker {
   private Object value;
   private Thread thread;
   private SwingWorker.ThreadVar threadVar;

   protected synchronized Object getValue() {
      return this.value;
   }

   private synchronized void setValue(Object x) {
      this.value = x;
   }

   public abstract Object construct();

   public void finished() {
   }

   public void interrupt() {
      Thread t = this.threadVar.get();
      if (t != null) {
         t.interrupt();
      }

      this.threadVar.clear();
   }

   public Object get() {
      while(true) {
         Thread t = this.threadVar.get();
         if (t == null) {
            return this.getValue();
         }

         try {
            t.join();
         } catch (InterruptedException var3) {
            Thread.currentThread().interrupt();
            return null;
         }
      }
   }

   public SwingWorker() {
      final Runnable doFinished = new Runnable() {
         @Override
         public void run() {
            SwingWorker.this.finished();
         }
      };
      Runnable doConstruct = new Runnable() {
         @Override
         public void run() {
            try {
               SwingWorker.this.setValue(SwingWorker.this.construct());
            } finally {
               SwingWorker.this.threadVar.clear();
            }

            SwingUtilities.invokeLater(doFinished);
         }
      };
      Thread t = new Thread(doConstruct);
      this.threadVar = new SwingWorker.ThreadVar(t);
   }

   public void start() {
      Thread t = this.threadVar.get();
      if (t != null) {
         t.start();
      }
   }

   private static class ThreadVar {
      private Thread thread;

      ThreadVar(Thread t) {
         this.thread = t;
      }

      synchronized Thread get() {
         return this.thread;
      }

      synchronized void clear() {
         this.thread = null;
      }
   }
}
