package hw.demo.piclayoutmanager;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class ExploreActivity extends AppCompatActivity {
    private boolean mIsSendMode;

    private RecyclerView mRcvFiles;
    private ExploreAdapter mAdapter;
    private ArrayList<Uri> mSendUris;
    private ShowType mShowType = ShowType.ALL;
    private Handler mHandler;
    private PicLayoutManager mPicLayoutManager;

    private enum ShowType {
        ALL,
        SEND_MODE,
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        initUI();
        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        mIsSendMode = Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action);
        if (mIsSendMode) {
            mSendUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (mSendUris == null) {
                mSendUris = new ArrayList<>();
            }
            Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                mSendUris.add(uri);
            }
            mShowType = ShowType.SEND_MODE;
        } else {
            mShowType = ShowType.ALL;
        }
        mHandler.sendEmptyMessage(MSG_CHANGE_SHOW_TYPE);
    }

    private void initUI() {
        mRcvFiles = (RecyclerView) findViewById(R.id.rcv_files);
        mPicLayoutManager = new PicLayoutManager();
        mRcvFiles.setLayoutManager(mPicLayoutManager);
        mAdapter = new ExploreAdapter(mPicLayoutManager);
        mRcvFiles.addItemDecoration(new PicLayoutManager.PicDecoration(4));
        mRcvFiles.setAdapter(mAdapter);
    }

    private void loadData() {
        if (mShowType == ShowType.SEND_MODE) {
            String path = FileUtils.PATH_ROOT;
            if (FileUtils.PATH_ROOT.equals(path)) {
                ArrayList<File> dirs = FileUtils.getDirs(path);
                mAdapter.setDirsData(dirs);
            }
        } else {
            String path = FileUtils.PATH_ROOT;
            if (FileUtils.PATH_ROOT.equals(path)) {
                ArrayList<File> dirs = FileUtils.getDirs(path);
                mAdapter.setDirsData(dirs);
            }
        }
    }

    private void copySendItemsToPath(String path) {
        if (path == null) {
            return;
        }
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            Toast.makeText(this, "目录错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mSendUris == null) {
            Toast.makeText(this, "文件为空", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<File> files = new ArrayList<>();
        for (Uri uri : mSendUris) {
            File f = FileUtils.uriToFile(this, uri);
            if (f == null) {
                Toast.makeText(this, "文件错误: " + uri.toString(), Toast.LENGTH_SHORT).show();
            } else {
                files.add(f);
            }
        }

        for (File f : files) {
            String dstPath = new File(path, f.getName()).getPath();
            boolean suc = FileUtils.copyFile(f.getAbsolutePath(), dstPath);
            if (!suc) {
                Toast.makeText(this, "拷贝失败: " + f.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ExploreAdapter extends PicLayoutManager.PicAdapter<RecyclerView.ViewHolder> {
        private ArrayList<FileItem> mItems;

        public ExploreAdapter(PicLayoutManager plm) {
            super(plm);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh = null;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case PicLayoutManager.TYPE_PICITEM:
                    vh = new PicHolder(inflater.inflate(R.layout.item_picture, parent, false));
                    break;
                default:
                    vh = new DirHolder(inflater.inflate(R.layout.item_dir, parent, false));
                    break;
            }
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final FileItem item = mItems.get(position);

            switch (item.picItemType) {
                case PicLayoutManager.TYPE_STICKY_HEADER:
                    bindDirView((DirHolder) holder, item);
                    break;
                case PicLayoutManager.TYPE_PICITEM:
                    bindPicView((PicHolder) holder, item);
                    break;
            }
        }

        private void bindPicView(PicHolder vh, final FileItem item) {
            Glide.with(ExploreActivity.this).load(item.path).into(vh.imvPic);
        }

        private void bindDirView(DirHolder vh, final FileItem item) {
            vh.txvName.setText(item.name);

            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsSendMode) {
                        copySendItemsToPath(item.path);
                    }
                }
            });
        }

        private FileItem getItem(int pos) {
            if (mItems == null) {
                return null;
            }
            return mItems.size() > pos ? mItems.get(pos) : null;
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        @Override
        public int getOriWidth(int position) {
            FileItem item = getItem(position);
            return item == null ? 0 : item.picItemWidth;
        }

        @Override
        public int getOriHeight(int position) {
            FileItem item = getItem(position);
            return item == null ? 0 : item.picItemHeight;
        }

        @Override
        public int getPicGroupId(int position) {
            FileItem item = getItem(position);
            return item == null ? 0 : item.picItemGroupId;
        }

        @Override
        public int getHeaderIndex(int position) {
            FileItem item = getItem(position);
            return item == null ? 0 : item.picItemHeaderIndex;
        }

        @Override
        public int getItemType(int position) {
            return getItemViewType(position);
        }

        @Override
        public int getItemViewType(int position) {
            FileItem item = getItem(position);
            return item == null ? 0 : item.picItemType;
        }

        private class FileItem {
            int picItemType;
            int picItemGroupId;
            int picItemHeaderIndex;
            int picItemWidth;
            int picItemHeight;

            boolean isDir;
            String name;
            String path;
        }

        private void setDirsData(ArrayList<File> dirs) {
            if (dirs == null) {
                return;
            }

            ArrayList<FileItem> items = new ArrayList<>();
            for (File f : dirs) {
                if (f.isDirectory()) {
                    FileItem item = new FileItem();
                    item.name = f.getName();
                    item.isDir = f.isDirectory();
                    item.path = f.getAbsolutePath();
                    item.picItemType = PicLayoutManager.TYPE_STICKY_HEADER;
                    item.picItemHeaderIndex = -1;
                    items.add(item);
                    ArrayList<File> mediaFiles = FileUtils.getMediaFilesInDir(f);
                    int index = items.size() - 1;
                    if (mediaFiles != null) {
                        for (File mediaFile : mediaFiles) {
                            item = new FileItem();
                            item.name = mediaFile.getName();
                            item.isDir = mediaFile.isDirectory();
                            item.path = mediaFile.getAbsolutePath();
                            item.picItemType = PicLayoutManager.TYPE_PICITEM;
                            item.picItemHeaderIndex = index;
                            item.picItemGroupId = index;
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(item.path, options);
                            item.picItemHeight = options.outHeight;
                            item.picItemWidth = options.outWidth;
                            items.add(item);
                            Log.e("hanwei", "item.path = " + item.path);
                        }
                    }
                }
            }
            setData(items);
        }

        private void setData(ArrayList<FileItem> items) {
            mItems = (ArrayList<FileItem>) items.clone();
            relayoutPicItems();
        }
    }

    private class DirHolder extends RecyclerView.ViewHolder {
        TextView txvName;
        public DirHolder(View itemView) {
            super(itemView);
            txvName = itemView.findViewById(R.id.txv_name);
        }
    }

    private class PicHolder extends RecyclerView.ViewHolder {
        ImageView imvPic;

        public PicHolder(View itemView) {
            super(itemView);
            imvPic = itemView.findViewById(R.id.imv_pic);
        }
    }

    private static final int MSG_CHANGE_SHOW_TYPE = 1;
    private class Handler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHANGE_SHOW_TYPE:
                    loadData();
                    break;
            }
        }
    }
}
