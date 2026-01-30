package codes.matheus.encoding;

import codes.matheus.exceptions.EncodingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ZipEncoding implements Encoding<File> {
    @Override
    public @NotNull File encode(@NotNull File file) {
        @NotNull File zip = new File(file.getAbsoluteFile() + ".zip");
        try (@NotNull FileOutputStream output = new FileOutputStream(zip)) {
            @NotNull ZipOutputStream zipOutputStream = new ZipOutputStream(output);
            zipRec(file, file.getName(), zipOutputStream);
            zipOutputStream.finish();
            return zip;
        } catch (IOException e) {
            throw new EncodingException("Failed to zip: " + e.getMessage());
        }
    }

    public @NotNull File decode(@NotNull File file) {
        @NotNull File unzip = new File(file.getAbsolutePath().replace(".zip", ""));
        if (!unzip.exists()) {
            unzip.mkdirs();
        }

        try (@NotNull ZipInputStream input = new ZipInputStream(new FileInputStream(file))) {
            @Nullable ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                @NotNull File newFile = new File(unzip, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (@NotNull FileOutputStream fos = new FileOutputStream(newFile)) {
                        input.transferTo(fos);
                    }
                }
                input.closeEntry();
            }
            return unzip;
        } catch (IOException e) {
            throw new EncodingException("Failed to unzip: " + e.getMessage());
        }
    }

    private void zipRec(@NotNull File file, @NotNull String fileName, @NotNull ZipOutputStream zipOutput) throws IOException {
        if (file.isHidden()) return;
        if (file.isDirectory()) {
            @NotNull String directoryEntryName = fileName.endsWith("/") ? fileName : fileName + "/";
            zipOutput.putNextEntry(new ZipEntry(directoryEntryName));
            zipOutput.closeEntry();
            @NotNull File[] children = file.listFiles();

            if (children != null) {
                for (@NotNull File child : children) {
                    zipRec(child, directoryEntryName + child.getName(), zipOutput);
                }
            }
        } else {
            try (@NotNull FileInputStream input = new FileInputStream(file)) {
                @NotNull ZipEntry entry = new ZipEntry(fileName);
                zipOutput.putNextEntry(entry);
                input.transferTo(zipOutput);
                zipOutput.closeEntry();
            }
        }
    }
}
