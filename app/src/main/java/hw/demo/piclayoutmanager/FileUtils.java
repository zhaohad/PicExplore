package hw.demo.piclayoutmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static final String ROOT_DIR_NAME = "HWPic";
    public static final String PATH_ROOT = new File(Environment.getExternalStorageDirectory(), ROOT_DIR_NAME).getAbsolutePath();

    private static void validRootFile() {
        File f = new File(PATH_ROOT);
        if (f.exists() && !f.isDirectory()) {
            f.delete();
        }
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    public static ArrayList<File> getDirs(String dirPath) {
        validRootFile();
        ArrayList<File> dirs = null;
        File dirFile = new File(dirPath);
        if (dirFile.exists() && dirFile.isDirectory()) {
            File[] files = dirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            });
            if (files != null) {
                dirs = new ArrayList<>(Arrays.asList(files));
            }
        }
        return dirs;
    }

    public static File uriToFile(Context context, Uri uri) {
        String path = null;
        if (uri == null) {
            return null;
        }

        if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
        }

        File f = path == null ? null : new File(path);
        return f;
    }

    public static final boolean copyFile(String src, String dst) {
        Path srcPath = new File(src).toPath();
        Path dstPath = new File(dst).toPath();
        boolean suc = false;
        try {
            Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            suc = true;
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }
        return suc;
    }

    public static final ArrayList<File> getMediaFilesInDir(File dir) {
        ArrayList<File> files = new ArrayList<>();
        if (dir == null || !dir.isDirectory()) {
            return files;
        }

        for (File f : dir.listFiles()) {
            if (isMediaFile(f)) {
                files.add(f);
            }
        }
        return files;
    }

    public static final boolean isMediaFile(File f) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(f.toURI().toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mimeType.startsWith("image")) {
            return true;
        }
        return false;
    }
}
