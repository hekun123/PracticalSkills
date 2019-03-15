package ucas.iie.nsp.hk.niosslclient.SSL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;

public abstract class NioSSLProvider extends SSLProvider
{
   private final ByteBuffer buffer = ByteBuffer.allocate(32 * 1024);
   private final SocketChannel socketChannel;

   public NioSSLProvider(SocketChannel socketChannel, SSLEngine engine, int bufferSize, Executor ioWorker, Executor taskWorkers)
   {
      super(engine, bufferSize, ioWorker, taskWorkers);
      this.socketChannel = socketChannel;
   }
   
   @Override
   public void onOutput(ByteBuffer encrypted)
   {
      try
      {
         (this.socketChannel).write(encrypted);
      }
      catch (IOException exc)
      {
         throw new IllegalStateException(exc);
      }
   }

   public boolean processInput(ByteBuffer buffer)
   {
      buffer.flip();
      int bytes = buffer.limit() - buffer.position();
      if(bytes <= 0){
         return false;
      }
      ByteBuffer copy = ByteBuffer.allocate(bytes);
      copy.put(buffer);
      copy.flip();
      this.notify(copy);
      return true;
   }
}