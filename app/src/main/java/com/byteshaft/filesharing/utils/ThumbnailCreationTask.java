package com.byteshaft.filesharing.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.byteshaft.filesharing.fragments.VideosFragment;

public class ThumbnailCreationTask extends AsyncTask<Void, Void, Bitmap> {

    private Context mContext;
    private int mPosition;
    private VideosFragment.ViewHolder mHolder;

    public ThumbnailCreationTask(Context context, VideosFragment.ViewHolder holder, int position) {
        mContext = context;
        mPosition = position;
        mHolder = holder;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        Bitmap bitmap =  ThumbnailUtils.createVideoThumbnail
                (VideosFragment.getInstance().videoList.get(mHolder.position),
                        MediaStore.Video.Thumbnails.MINI_KIND);
        if (bitmap != null)
        return Bitmap.createScaledBitmap(bitmap, 200, 200, true);
        else
            return null;

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (mHolder.position == mPosition) {
            mHolder.videoImage.setImageBitmap(bitmap);
        }
    }
}
