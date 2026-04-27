package com.example.travelshare.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.travelshare.model.Post;

public class NavigationUtils {
    public static void showPostMenu(Context context, View view, Post post) {
        PopupMenu popupMenu = new PopupMenu(context, view);

        popupMenu.getMenu().add("Signalez la publication");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Signalez la publication")) {
                new AlertDialog.Builder(context).setTitle("Signaler")
                        .setMessage("Voulez-vous signaler ce post ?")
                        .setPositiveButton("Signaler", (dialog, which) -> {
                            //changer pour plus tard
                            Toast.makeText(context, "Post signalé", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}
