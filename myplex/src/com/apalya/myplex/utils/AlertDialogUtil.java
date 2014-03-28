package com.apalya.myplex.utils;

import com.apalya.myplex.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertDialogUtil{
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogOption2Click();
        public void onDialogOption1Click();
    }
    
    // Use this instance of the interface to deliver action events
    static NoticeDialogListener mListener;
    
   public static void showAlert(Context mContext,String aMsg, String aOption1, String aOption2,NoticeDialogListener listener){
	   mListener=listener;
	   AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
       builder.setTitle(mContext.getString(R.string.app_name));
       builder.setMessage(aMsg);
       builder.setNegativeButton(aOption1, new DialogInterface.OnClickListener() {

           @Override
           public void onClick(DialogInterface arg0, int arg1) {
               // TODO Auto-generated method stub
        	   mListener.onDialogOption1Click();

           }
       });
       builder.setPositiveButton(aOption2, new DialogInterface.OnClickListener() {

           @Override
           public void onClick(DialogInterface arg0, int arg1) {
               // TODO Auto-generated method stub
               
        	   mListener.onDialogOption2Click();
           }
       });
       
       
       builder.show(); //To show the AlertDialog
   }
   

}