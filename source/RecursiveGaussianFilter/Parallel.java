package RecursiveGaussianFilter;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class Parallel {
   private static final int NSQT = 6;
   private static ForkJoinPool _pool = new ForkJoinPool();
   private static boolean _serial = false;

   public static void loop(int end, Parallel.LoopInt body) {
      loop(0, end, 1, 1, body);
   }

   public static void loop(int begin, int end, Parallel.LoopInt body) {
      loop(begin, end, 1, 1, body);
   }

   public static void loop(int begin, int end, int step, Parallel.LoopInt body) {
      loop(begin, end, step, 1, body);
   }

   public static void loop(int begin, int end, int step, int chunk, Parallel.LoopInt body) {
      checkArgs(begin, end, step, chunk);
      if (!_serial && end > begin + chunk * step) {
         Parallel.LoopIntAction task = new Parallel.LoopIntAction(begin, end, step, chunk, body);
         if (Parallel.LoopIntAction.inForkJoinPool()) {
            task.invoke();
         } else {
            _pool.invoke(task);
         }
      } else {
         for(int i = begin; i < end; i += step) {
            body.compute(i);
         }
      }
   }

   public static <V> V reduce(int end, Parallel.ReduceInt<V> body) {
      return reduce(0, end, 1, 1, body);
   }

   public static <V> V reduce(int begin, int end, Parallel.ReduceInt<V> body) {
      return reduce(begin, end, 1, 1, body);
   }

   public static <V> V reduce(int begin, int end, int step, Parallel.ReduceInt<V> body) {
      return reduce(begin, end, step, 1, body);
   }

   public static <V> V reduce(int begin, int end, int step, int chunk, Parallel.ReduceInt<V> body) {
      checkArgs(begin, end, step, chunk);
      if (!_serial && end > begin + chunk * step) {
         Parallel.ReduceIntTask<V> task = new Parallel.ReduceIntTask<>(begin, end, step, chunk, body);
         return (V)(Parallel.ReduceIntTask.inForkJoinPool() ? task.invoke() : _pool.invoke(task));
      } else {
         V v = body.compute(begin);

         for(int i = begin + step; i < end; i += step) {
            V vi = body.compute(i);
            v = body.combine(v, vi);
         }

         return v;
      }
   }

   public static void setParallel(boolean parallel) {
      _serial = !parallel;
   }

   private static void checkArgs(int begin, int end, int step, int chunk) {
      Check.argument(begin < end, "begin<end");
      Check.argument(step > 0, "step>0");
      Check.argument(chunk > 0, "chunk>0");
   }

   private static int middle(int begin, int end, int step) {
      return begin + step + (end - begin - 1) / 2 / step * step;
   }

   public interface LoopInt {
      void compute(int var1);
   }

   private static class LoopIntAction extends RecursiveAction {
      private int _begin;
      private int _end;
      private int _step;
      private int _chunk;
      private Parallel.LoopInt _body;

      LoopIntAction(int begin, int end, int step, int chunk, Parallel.LoopInt body) {
         assert begin < end : "begin < end";

         this._begin = begin;
         this._end = end;
         this._step = step;
         this._chunk = chunk;
         this._body = body;
      }

      @Override
      protected void compute() {
         if (this._end > this._begin + this._chunk * this._step && getSurplusQueuedTaskCount() <= 6) {
            int middle = Parallel.middle(this._begin, this._end, this._step);
            Parallel.LoopIntAction l = new Parallel.LoopIntAction(this._begin, middle, this._step, this._chunk, this._body);
            Parallel.LoopIntAction r = middle < this._end ? new Parallel.LoopIntAction(middle, this._end, this._step, this._chunk, this._body) : null;
            if (r != null) {
               r.fork();
            }

            l.compute();
            if (r != null) {
               r.join();
            }
         } else {
            for(int i = this._begin; i < this._end; i += this._step) {
               this._body.compute(i);
            }
         }
      }
   }

   public interface ReduceInt<V> {
      V compute(int var1);

      V combine(V var1, V var2);
   }

   private static class ReduceIntTask<V> extends RecursiveTask<V> {
      private int _begin;
      private int _end;
      private int _step;
      private int _chunk;
      private Parallel.ReduceInt<V> _body;

      ReduceIntTask(int begin, int end, int step, int chunk, Parallel.ReduceInt<V> body) {
         assert begin < end : "begin < end";

         this._begin = begin;
         this._end = end;
         this._step = step;
         this._chunk = chunk;
         this._body = body;
      }

      @Override
      protected V compute() {
         if (this._end > this._begin + this._chunk * this._step && getSurplusQueuedTaskCount() <= 6) {
            int middle = Parallel.middle(this._begin, this._end, this._step);
            Parallel.ReduceIntTask<V> l = new Parallel.ReduceIntTask<>(this._begin, middle, this._step, this._chunk, this._body);
            Parallel.ReduceIntTask<V> r = middle < this._end ? new Parallel.ReduceIntTask<>(middle, this._end, this._step, this._chunk, this._body) : null;
            if (r != null) {
               r.fork();
            }

            V v = l.compute();
            if (r != null) {
               v = this._body.combine(v, r.join());
            }

            return v;
         } else {
            V v = this._body.compute(this._begin);

            for(int i = this._begin + this._step; i < this._end; i += this._step) {
               V vi = this._body.compute(i);
               v = this._body.combine(v, vi);
            }

            return v;
         }
      }
   }
}
