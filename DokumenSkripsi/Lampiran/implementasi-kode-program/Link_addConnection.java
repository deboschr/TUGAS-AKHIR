public void addConnection(Link other, String anchorText) {
   if (other == null || other == this) return;

   this.connections.putIfAbsent(other, anchorText != null ? anchorText : "");
   other.connections.putIfAbsent(this, anchorText != null ? anchorText : "");
}