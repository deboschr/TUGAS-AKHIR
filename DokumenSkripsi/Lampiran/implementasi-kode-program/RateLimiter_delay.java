public synchronized void delay() {
   long now = System.currentTimeMillis();
   long waitTime = lastRequestTime + INTERVAL - now;

   if (waitTime > 0) {
      try {
         Thread.sleep(waitTime);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }
   
   lastRequestTime = System.currentTimeMillis();
}