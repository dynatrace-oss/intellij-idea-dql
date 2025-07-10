package pl.thedeem.intellij.dql.fileProviders;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DQLRecordFieldVirtualFile extends DQLVirtualFile<Object> {
   private final static List<String> JSON_DQL_TYPES = List.of("timeframe", "record", "array");
   private final String columnType;

   public DQLRecordFieldVirtualFile(@NotNull String name, @NotNull Object content, @NotNull String columnType) {
      super(name, content);
      this.columnType = columnType;
   }

   @Override
   protected @NotNull FileType getBaseFileType() {
      return JSON_DQL_TYPES.contains(columnType) ? JsonFileType.INSTANCE : PlainTextFileType.INSTANCE;
   }

   @Override
   protected @NotNull String getDocumentContent() {
      if (JSON_DQL_TYPES.contains(columnType)) {
         return new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(content.toString()));
      }
      return super.getDocumentContent();
   }
}
