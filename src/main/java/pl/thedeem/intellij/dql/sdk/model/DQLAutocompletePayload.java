package pl.thedeem.intellij.dql.sdk.model;

public class DQLAutocompletePayload {
   public String query;
   public Long cursorPosition;

   public DQLAutocompletePayload() {
   }

   public DQLAutocompletePayload(String query, Long cursorPosition) {
      this.query = query;
      this.cursorPosition = cursorPosition;
   }
}
