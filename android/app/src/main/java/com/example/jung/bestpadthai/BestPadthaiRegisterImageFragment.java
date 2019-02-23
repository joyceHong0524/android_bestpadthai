package com.example.jung.bestpadthai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.jung.bestpadthai.item.ImageItem;
import com.example.jung.bestpadthai.lib.BitmapLib;
import com.example.jung.bestpadthai.lib.FileLib;
import com.example.jung.bestpadthai.lib.GoLib;
import com.example.jung.bestpadthai.lib.MyLog;
import com.example.jung.bestpadthai.lib.MyToast;
import com.example.jung.bestpadthai.lib.RemoteLib;
import com.example.jung.bestpadthai.lib.StringLib;
import com.example.jung.bestpadthai.remote.RemoteService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

public class BestPadthaiRegisterImageFragment extends Fragment implements View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName();
    public static final String INFO_SEQ = "INFO_SEQ";

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    Activity context;
    int infoSeq;

    File imageFile;
    String imageFilename;

    EditText imageMemoEdit;
    ImageView infoImage;

    ImageItem imageItem; //instance which saves a lot of informations of the image.

    boolean isSavingImage = false; //this is for

    //returns fragment instance which saves FoodInfoItem as parameter.
    //But unlike past 2 fragments, this only uses sequence of the infoItem.
    public static BestPadthaiRegisterImageFragment newInstance(int infoSeq){
        Bundle bundle = new Bundle();
        bundle.putInt(INFO_SEQ,infoSeq);

        BestPadthaiRegisterImageFragment f = new BestPadthaiRegisterImageFragment();
        f.setArguments(bundle);

        return f;
    }

    //saves INFO_SEQ in the variable of current instance.
    //called when the new fragment instance is being made.
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(getArguments()!=null){
            infoSeq= getArguments().getInt(INFO_SEQ);
        }
    }



    //generates a fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState){
        context = this.getActivity();
        View v = inflater.inflate(R.layout.fragment_bestpadthai_register_image,container,false);
        return v;
    }

    //called after finishing onCreateView.
    //It sets basic variables and registers click events.
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        imageItem = new ImageItem();
        imageItem.infoSeq = infoSeq;

        imageFilename = infoSeq + "_" +String.valueOf(System.currentTimeMillis());
        imageFile = FileLib.getInstance().getImageFile(context, imageFilename);

        infoImage = (ImageView) view.findViewById(R.id.bestfood_image);
        imageMemoEdit = (EditText) view.findViewById(R.id.register_image_memo);

        ImageView imageRegister = (ImageView) view.findViewById(R.id.bestfood_image_register);

        imageRegister.setOnClickListener(this);

        view.findViewById(R.id.prev).setOnClickListener(this);
        view.findViewById(R.id.complete).setOnClickListener(this);
    }

    //starts an activity to get an image from camera.
    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        context.startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    //starts an activity to get an image from album.
    private void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        context.startActivityForResult(intent,PICK_FROM_ALBUM);
    }

    //handles click events
    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.bestfood_image_register :
                showImageDialog(context);
                break;
            case R.id.complete:
                saveImage();
                break;
            case R.id.prev:
                GoLib.getInstance().goBackFragment(getFragmentManager());
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if (requestCode == PICK_FROM_CAMERA) {
                Picasso.with(context).load(imageFile).into(infoImage);
            } else if (requestCode == PICK_FROM_ALBUM && data != null) {
                Uri dataUri = data.getData();

                if (dataUri != null) {
                    Picasso.with(context).load(dataUri).into(infoImage);
                    Picasso.with(context).load(dataUri).into(new Target() {
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from){
                            BitmapLib.getInstance().saveBitmapToFileThread(imageUploadHandler, imageFile,bitmap);
                            isSavingImage = true;

                        }

                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        public void onPrepareLoad(Drawable placeHolerDrawable){

                        }
                    });
                }
            }
        }
    }


    //saves selected image and memo in the ImageItem.
    private void setImageItem() {
        String imageMemo = imageMemoEdit.getText().toString();
        if (StringLib.getInstance().isBlank(imageMemo)){
            imageMemo = "";
        }

        imageItem.imageMemo = imageMemo;
        imageItem.fileName = imageFilename + ".png";

    }

    //saves image on server
    private void saveImage() {
        if(isSavingImage)
        {
            MyToast.s(context, R.string.no_image_ready);
            return;
        }
        MyLog.d(TAG, "imageFile.length() " + imageFile.length());

        if(imageFile.length()==0) {
            MyToast.s(context, R.string.no_image_selected);
            return;
        }

        setImageItem();

        RemoteLib.getInstance().uploadFoodImage(infoSeq, imageItem.imageMemo, imageFile, finishiHandler);
        isSavingImage=  false;
    }

    //shows dialog makes user decide the way select the image.
    public void showImageDialog(Context context){
        new AlertDialog.Builder(context).setTitle(R.string.title_bestfood_image_register)
                .setSingleChoiceItems(R.array.camera_album_category, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    getImageFromCamera();
                                } else {
                                    getImageFromAlbum();
                                }
                                dialog.dismiss();
                            }
                        }).show();
    }

    Handler imageUploadHandler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isSavingImage = false;
            setImageItem();
            Picasso.with(context).invalidate(RemoteService.IMAGE_URL + imageItem.fileName);
        }
    };

    Handler finishiHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            context.finish();
        }
    };

}
