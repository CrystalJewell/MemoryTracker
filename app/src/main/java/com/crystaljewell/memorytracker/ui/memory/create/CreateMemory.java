package com.crystaljewell.memorytracker.ui.memory.create;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.crystaljewell.memorytracker.R;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class CreateMemory extends AppCompatActivity {

    private String mPicturePath;
    private List<String> encodedImages;

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_VIDEO_CAPTURE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_memory);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.add_photo_button)
    protected void addContentDialog() {
        //Builds our alert dialog
        AlertDialog.Builder chooseContent = new AlertDialog.Builder(this);
        //Sets the title of the dialog
        chooseContent.setTitle("What would you like to do?");
        //Sets content of dialog.  This one will have multiple selections so we set the content from an array of choices and set the onClickListener
        chooseContent.setItems(R.array.contentArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                switch (selection) {
                    case 0:
                        addPhoto();
                        break;
                    case 1:
                        addVideo();
                        break;
                    case 2:
                        choosePhotos();
                        break;
                    default:
                        dialog.cancel();
                        break;
                }
            }
        });
        chooseContent.create();
        chooseContent.show();
    }

    private void addPhoto() {
        //Create the intent to take a photo
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //We make a check here to make sure the device actually has a camera
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            //Give the user a message if they don't have a camera letting them know why nothing is happening
            Toast.makeText(this, getString(R.string.no_camera_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void addVideo(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }else {
            //Give the user a message if they don't have a camera letting them know why nothing is happening
            Toast.makeText(this, getString(R.string.no_camera_message), Toast.LENGTH_SHORT).show();
        }

    }

    private void choosePhotos() {
        getImage();
    }

    private void getImage() {
        //Because this intent is going to be used to open the gallery we don't need to tell it to go to another class
        Intent mImageIntent = new Intent();
        //sets the type of file we are going to use for the intent
        mImageIntent.setType("image/*");
        //Allows user to select multiple pictures from their gallery
        mImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //Because we said this intent will return an image this line will open a picker that lists all of the users apps show their gallery
        mImageIntent.setAction(Intent.ACTION_GET_CONTENT);

        //We'll start the activity for a result(getting pictures is our result) It gives our gallery the title and we give it a result code for when it comes back to onActivityResult.
        startActivityForResult(Intent.createChooser(mImageIntent,"Select Picture(s)"), RESULT_LOAD_IMAGE);
    }

    //decodes and sizes the picture appropriately
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = this.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mPicturePath = cursor.getString(columnIndex);
            cursor.close();


        }
        else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
//            mVideoView.setVideoURI(videoUri);
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);
        }
    }
}
