public void addConnection(Link other, String anchorText) {
   if (other == null || other == this) return;

   String text = anchorText != null ? anchorText : "";

   this.connections.putIfAbsent(other, text);
   
   other.connections.putIfAbsent(this, text);
}