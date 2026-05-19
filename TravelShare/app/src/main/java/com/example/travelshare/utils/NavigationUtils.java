package com.example.travelshare.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.travelshare.model.Comment;
import com.example.travelshare.model.Post;
import com.example.travelshare.viewmodel.CommentViewModel;
import com.example.travelshare.viewmodel.PostViewModel;

public class NavigationUtils {
    public static void showPostMenu(Context context, View view, Post post, String currentUserId, PostViewModel postViewModel) {
        PopupMenu popupMenu = new PopupMenu(context, view);

        if (post.getAuthorId().equals(currentUserId)) {
            popupMenu.getMenu().add("Supprimer la publication");
        }
        popupMenu.getMenu().add("Signaler la publication");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Signaler la publication")) {
                Toast.makeText(context, "Post signalé", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getTitle().equals("Supprimer la publication")) {
                new AlertDialog.Builder(context).setTitle("Supprimer")
                        .setMessage("Voulez-vous vraiment supprimer cette publication ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            if (postViewModel != null) {
                                postViewModel.deletePost(post, new com.example.travelshare.repository.FireStoreCallBack<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        Toast.makeText(context, "Post supprimé", Toast.LENGTH_SHORT).show();
                                        postViewModel.loadFeed(); // Rafraîchir le flux
                                    }

                                    @Override
                                    public void onFailure(String e) {
                                        Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    public static void showCommentMenu(Context context, View view, Comment comment, String currentUserId, CommentViewModel commentViewModel) {
        PopupMenu popupMenu = new PopupMenu(context, view);

        if (comment.getAuthorId().equals(currentUserId)) {
            popupMenu.getMenu().add("Supprimer le commentaire");
        }
        popupMenu.getMenu().add("Signaler le commentaire");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Supprimer le commentaire")) {
                new AlertDialog.Builder(context)
                        .setTitle("Supprimer")
                        .setMessage("Voulez-vous supprimer ce commentaire ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            if (commentViewModel != null) {
                                commentViewModel.deleteComment(comment.getId(), comment.getPostId());
                                Toast.makeText(context, "Commentaire supprimé", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
                return true;
            } else if (item.getTitle().equals("Signaler le commentaire")) {
                Toast.makeText(context, "Commentaire signalé", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}
